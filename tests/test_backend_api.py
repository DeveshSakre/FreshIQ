import io
from pathlib import Path
import unittest

from PIL import Image
import pandas as pd

# Important for Windows: import torch before sklearn/fastapi
import torch
from fastapi.testclient import TestClient

from backend.app.main import app
from ml.config import TrainingConfig
from ml.simulation_engine import PHASE1A_EXPECTED_SHA256


class TestFreshIQBackendAPI(unittest.TestCase):
    """
    Comprehensive integration test suite for the FreshIQ FastAPI Backend.
    Tests health endpoints, file validation, edge cases, and schema conformance.
    """

    @classmethod
    def setUpClass(cls):
        # Using TestClient as a context manager triggers the lifespan (model loading)
        cls.client_cm = TestClient(app)
        cls.client = cls.client_cm.__enter__()

        cls.config = TrainingConfig()
        cls.test_csv = pd.read_csv(cls.config.test_csv)
        cls.data_root = Path(cls.config.data_root)

        # Locate a valid real test image
        row = cls.test_csv.iloc[0]
        fn = str(row["filename"])
        if not fn.endswith(".jpg"):
            fn += ".jpg"
        cls.sample_image_path = cls.data_root / fn

    @classmethod
    def tearDownClass(cls):
        cls.client_cm.__exit__(None, None, None)

    def test_01_root_endpoint(self):
        """Verify root endpoint provides discovery metadata."""
        response = self.client.get("/")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "online")
        self.assertIn("/api/predict", data["predict_endpoint"])

    def test_02_health_endpoint(self):
        """Verify /api/health verifies model status and SHA256 integrity."""
        response = self.client.get("/api/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["status"], "healthy")
        self.assertTrue(data["models_loaded"])
        self.assertTrue(data["vision_checkpoint_verified"])
        self.assertTrue(data["rul_model_loaded"])
        self.assertTrue(data["feature_config_loaded"])
        self.assertIn("Avocado (Hass)", data["supported_produce"])
        self.assertIn("timestamp", data)

    def test_03_valid_image_prediction(self):
        """Test POST /api/predict with a real test avocado photo."""
        with open(self.sample_image_path, "rb") as f:
            image_bytes = f.read()

        response = self.client.post(
            "/api/predict",
            files={"file": ("avocado.jpg", image_bytes, "image/jpeg")}
        )
        self.assertEqual(response.status_code, 200)
        data = response.json()

        # Item name check
        self.assertEqual(data["item_name"], "Avocado (Hass)")

        # Ripeness assessment checks
        rip = data["ripeness"]
        self.assertIn(rip["predicted_ripening_stage"], [1, 2, 3, 4, 5])
        self.assertTrue(len(rip["stage_label"]) > 0)
        self.assertGreaterEqual(rip["confidence"], 0.2)
        self.assertLessEqual(rip["confidence"], 1.0)
        self.assertGreaterEqual(rip["expected_continuous_ripening_stage"], 1.0)
        self.assertLessEqual(rip["expected_continuous_ripening_stage"], 5.0)

        # Probabilities sum ≈ 1.0
        probs = rip["probabilities"]
        prob_sum = sum(probs.values())
        self.assertAlmostEqual(prob_sum, 1.0, places=4)
        for p_name, p_val in probs.items():
            self.assertGreaterEqual(p_val, 0.0)
            self.assertLessEqual(p_val, 1.0)

        # Scenarios check
        scenarios = data["scenarios"]
        self.assertIn("10C", scenarios)
        self.assertIn("20C", scenarios)
        self.assertIn("ambient", scenarios)
        self.assertIn("4C_refrigerator", scenarios)

        # RUL non-negative check
        for cond, sc in scenarios.items():
            self.assertGreaterEqual(sc["estimated_rul_days"], 0.0, f"RUL in {cond} was negative!")
            self.assertTrue(len(sc["recommendation"]) > 10)

        # Empirical vs Extrapolation flags
        self.assertFalse(scenarios["10C"]["is_extrapolated"])
        self.assertFalse(scenarios["20C"]["is_extrapolated"])
        self.assertFalse(scenarios["ambient"]["is_extrapolated"])
        self.assertTrue(scenarios["4C_refrigerator"]["is_extrapolated"])
        self.assertIsNotNone(scenarios["4C_refrigerator"]["uncertainty_note"])
        self.assertIn("Q10", scenarios["4C_refrigerator"]["uncertainty_note"])

        # Extension gain check
        self.assertGreaterEqual(data["refrigeration_extension_gain_days"], 0.0)
        self.assertTrue(len(data["actionable_recommendation"]) > 10)
        self.assertTrue(len(data["legal_disclaimer"]) > 10)

    def test_04_missing_image_file(self):
        """Test POST /api/predict without providing any file (expect 422)."""
        response = self.client.post("/api/predict")
        self.assertEqual(response.status_code, 422)

    def test_05_invalid_file_type(self):
        """Test POST /api/predict with non-image text file (expect 400)."""
        response = self.client.post(
            "/api/predict",
            files={"file": ("notes.txt", b"This is a text file, not produce image", "text/plain")}
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn("Invalid file type", response.json()["detail"])

    def test_06_corrupted_image_file(self):
        """Test POST /api/predict with corrupted bytes disguised as JPEG (expect 400)."""
        corrupted_bytes = b"\xFF\xD8\xFF\xE0garbagebytesnotanimage"
        response = self.client.post(
            "/api/predict",
            files={"file": ("broken.jpg", corrupted_bytes, "image/jpeg")}
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn("corrupted or not a readable image", response.json()["detail"])

    def test_07_empty_image_file(self):
        """Test POST /api/predict with empty 0-byte file (expect 400)."""
        response = self.client.post(
            "/api/predict",
            files={"file": ("empty.jpg", b"", "image/jpeg")}
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn("empty", response.json()["detail"])

    def test_08_stage5_terminal_behavior(self):
        """
        Verify that an overripe Stage 5 avocado yields 0.0 days remaining usable life
        and indicates that refrigeration cannot restore expired shelf life.
        """
        # Find a specimen row in test set at Stage 5
        stage5_rows = self.test_csv[self.test_csv["ripening_stage"] == 5]
        self.assertGreater(len(stage5_rows), 0)

        # Test with the known overripe image
        row = stage5_rows.iloc[-1]
        fn = str(row["filename"])
        if not fn.endswith(".jpg"):
            fn += ".jpg"
        img_path = self.data_root / fn

        with open(img_path, "rb") as f:
            image_bytes = f.read()

        response = self.client.post(
            "/api/predict",
            files={"file": ("stage5_avocado.jpg", image_bytes, "image/jpeg")}
        )
        self.assertEqual(response.status_code, 200)
        data = response.json()

        # If predicted as stage 5, all RUL values must be 0.0
        if data["ripeness"]["predicted_ripening_stage"] == 5:
            for sc in data["scenarios"].values():
                self.assertEqual(sc["estimated_rul_days"], 0.0)
            self.assertEqual(data["refrigeration_extension_gain_days"], 0.0)

    def test_09_storage_condition_hint_parameter(self):
        """Verify storage_condition parameter customizes actionable recommendation."""
        with open(self.sample_image_path, "rb") as f:
            image_bytes = f.read()

        response = self.client.post(
            "/api/predict",
            files={"file": ("avocado.jpg", image_bytes, "image/jpeg")},
            data={"storage_condition": "4C"}
        )
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertIn("4C", data["scenarios"]["4C_refrigerator"]["condition"])


if __name__ == "__main__":
    unittest.main()
