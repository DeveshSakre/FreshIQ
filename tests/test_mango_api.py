import io
from pathlib import Path
import unittest
from unittest.mock import patch

# Important for Windows: import torch before sklearn/fastapi
import torch
import pandas as pd
from PIL import Image
from fastapi.testclient import TestClient

from backend.app.main import app
from backend.app.config import MANGO_EXPECTED_SHA256, BASE_DIR

SPLIT_MANIFEST_PATH = BASE_DIR / "ml" / "datasets" / "multi_produce" / "Mango" / "manifests" / "mango_train_val_test_split.csv"
RAW_MANGO_DIR = BASE_DIR / "ml" / "datasets" / "multi_produce" / "Mango Shelf-Life Dataset"
PREPROCESSED_MANGO_DIR = BASE_DIR / "ml" / "datasets" / "multi_produce" / "Mango_preprocessed_256"


class TestFreshIQMangoAPI(unittest.TestCase):
    """
    Integration test suite for FreshIQ Mango 5-Class Ripeness API.
    Tests Mango classification inference, RUL exclusion, produce routing,
    error handling, and cross-produce isolation.
    """

    @classmethod
    def setUpClass(cls):
        # Trigger application lifespan (loads Avocado and Mango models)
        cls.client_cm = TestClient(app)
        cls.client = cls.client_cm.__enter__()

        # Read split manifest and find 1 test image per class
        cls.split_df = pd.read_csv(SPLIT_MANIFEST_PATH)
        test_df = cls.split_df[cls.split_df["split"] == "test"]

        cls.sample_images = {}
        for cls_name in ["Unripe", "Semiripe", "Fully Ripe", "Overripe", "Perished"]:
            row = test_df[test_df["class"] == cls_name].iloc[0]
            rel_path = row["relative_path"]
            img_p = PREPROCESSED_MANGO_DIR / rel_path
            if not img_p.exists():
                img_p = RAW_MANGO_DIR / rel_path
            assert img_p.exists(), f"Test image not found: {img_p}"
            cls.sample_images[cls_name] = img_p

    @classmethod
    def tearDownClass(cls):
        cls.client_cm.__exit__(None, None, None)

    def _predict(self, image_path: Path, food_type: str = "mango", storage_condition: str = None):
        with open(image_path, "rb") as f:
            image_bytes = f.read()

        data = {"food_type": food_type}
        if storage_condition:
            data["storage_condition"] = storage_condition

        return self.client.post(
            "/api/predict",
            files={"file": (image_path.name, image_bytes, "image/jpeg")},
            data=data
        )

    def test_01_health_reports_mango_and_avocado(self):
        """Verify /api/health verifies both models and reports both supported produce types."""
        response = self.client.get("/api/health")
        self.assertEqual(response.status_code, 200)
        data = response.json()

        self.assertEqual(data["status"], "healthy")
        self.assertTrue(data["models_loaded"])
        self.assertTrue(data["vision_checkpoint_verified"])
        self.assertTrue(data["rul_model_loaded"])
        self.assertTrue(data["feature_config_loaded"])
        self.assertTrue(data["mango_model_loaded"])
        self.assertTrue(data["mango_checkpoint_verified"])
        self.assertIn("Avocado (Hass)", data["supported_produce"])
        self.assertIn("Mango (White Chaunsa Late)", data["supported_produce"])

    def test_02_mango_classes_inference(self):
        """
        Test POST /api/predict with food_type=mango for each of the 5 classes.
        Verifies valid stages, probabilities summing to ~1.0, and strict absence of RUL.
        """
        expected_classes = ["Unripe", "Semiripe", "Fully Ripe", "Overripe", "Perished"]

        for expected_class, img_path in self.sample_images.items():
            with self.subTest(class_name=expected_class):
                response = self._predict(img_path, food_type="mango")
                self.assertEqual(response.status_code, 200)
                data = response.json()

                # Produce identity
                self.assertEqual(data["food_type"], "mango")
                self.assertEqual(data["item_name"], "Mango (White Chaunsa Late)")
                self.assertEqual(data["model_id"], "FreshIQ_MobileNetV3_Mango")

                # Ripeness assessment
                rip = data["ripeness"]
                self.assertIn(rip["predicted_ripening_stage"], [0, 1, 2, 3, 4])
                self.assertIn(rip["stage_label"], expected_classes)
                self.assertGreaterEqual(rip["confidence"], 0.20)
                self.assertLessEqual(rip["confidence"], 1.0)
                self.assertIsNone(rip["expected_continuous_ripening_stage"])

                # Class probabilities
                probs = rip["probabilities"]
                self.assertEqual(len(probs), 5)
                for c in expected_classes:
                    self.assertIn(c, probs)
                    self.assertGreaterEqual(probs[c], 0.0)
                    self.assertLessEqual(probs[c], 1.0)
                self.assertAlmostEqual(sum(probs.values()), 1.0, places=4)

                # STRICT NO-RUL ENFORCEMENT
                self.assertFalse(data["rul_available"])
                self.assertIsNone(data["rul"])
                self.assertIsNone(data["scenarios"])
                self.assertIsNone(data["refrigeration_extension_gain_days"])

                # Recommendation and disclaimer
                self.assertTrue(len(data["actionable_recommendation"]) > 10)
                self.assertTrue(len(data["legal_disclaimer"]) > 10)
                self.assertIn("Mango", data["legal_disclaimer"])

    def test_03_unsupported_produce_papaya(self):
        """Verify unsupported produce (e.g. food_type=papaya) returns a controlled 400 Bad Request error."""
        img_path = self.sample_images["Fully Ripe"]
        response = self._predict(img_path, food_type="papaya")
        self.assertEqual(response.status_code, 400)
        data = response.json()
        self.assertIn("Unsupported food type 'papaya'", data["detail"])
        self.assertIn("avocado", data["detail"])
        self.assertIn("mango", data["detail"])
        self.assertIn("banana", data["detail"])

    def test_04_unsupported_produce_tomato(self):
        """Verify food_type=tomato returns a controlled 400 Bad Request error."""
        img_path = self.sample_images["Fully Ripe"]
        response = self._predict(img_path, food_type="tomato")
        self.assertEqual(response.status_code, 400)
        data = response.json()
        self.assertIn("Unsupported food type 'tomato'", data["detail"])

    def test_05_unsupported_produce_arbitrary(self):
        """Verify food_type=invalid_food returns a controlled 400 Bad Request error."""
        img_path = self.sample_images["Unripe"]
        response = self._predict(img_path, food_type="dragonfruit")
        self.assertEqual(response.status_code, 400)
        self.assertIn("Unsupported food type 'dragonfruit'", response.json()["detail"])

    def test_06_storage_condition_invariance_for_mango(self):
        """
        Verify that passing storage_condition for Mango does NOT alter
        predicted stage, confidence, or fabricate shelf-life days.
        """
        img_path = self.sample_images["Fully Ripe"]

        resp_none = self._predict(img_path, food_type="mango", storage_condition=None)
        resp_4c = self._predict(img_path, food_type="mango", storage_condition="4C")
        resp_ambient = self._predict(img_path, food_type="mango", storage_condition="ambient")

        self.assertEqual(resp_none.status_code, 200)
        self.assertEqual(resp_4c.status_code, 200)
        self.assertEqual(resp_ambient.status_code, 200)

        data_none = resp_none.json()
        data_4c = resp_4c.json()
        data_ambient = resp_ambient.json()

        # Predictions must be strictly identical regardless of storage_condition
        self.assertEqual(data_none["ripeness"]["predicted_ripening_stage"], data_4c["ripeness"]["predicted_ripening_stage"])
        self.assertEqual(data_none["ripeness"]["predicted_ripening_stage"], data_ambient["ripeness"]["predicted_ripening_stage"])
        self.assertAlmostEqual(data_none["ripeness"]["confidence"], data_4c["ripeness"]["confidence"], places=5)
        self.assertAlmostEqual(data_none["ripeness"]["confidence"], data_ambient["ripeness"]["confidence"], places=5)

        # RUL must remain None
        self.assertIsNone(data_4c["scenarios"])
        self.assertIsNone(data_4c["refrigeration_extension_gain_days"])
        self.assertFalse(data_4c["rul_available"])

    def test_07_no_cross_produce_contamination(self):
        """
        Verify that a Mango request NEVER invokes the Avocado simulation engine,
        and an Avocado request NEVER invokes the Mango classifier.
        """
        img_path = self.sample_images["Fully Ripe"]

        # When predicting Mango, avocado engine must NOT be called
        with patch("ml.simulation_engine.WhatIfSimulationEngine.simulate_all_scenarios") as mock_avocado_sim:
            resp = self._predict(img_path, food_type="mango")
            self.assertEqual(resp.status_code, 200)
            mock_avocado_sim.assert_not_called()

        # When predicting Avocado, mango engine must NOT be called
        with patch("ml.mango_classifier.MangoClassifierEngine.predict") as mock_mango_pred:
            resp = self._predict(img_path, food_type="avocado")
            self.assertEqual(resp.status_code, 200)
            mock_mango_pred.assert_not_called()

    def test_08_corrupted_image_for_mango(self):
        """Verify corrupted image returns controlled 400 Bad Request."""
        corrupted_bytes = b"\xFF\xD8\xFF\xE0notarealimagegarbage"
        response = self.client.post(
            "/api/predict",
            files={"file": ("corrupt.jpg", corrupted_bytes, "image/jpeg")},
            data={"food_type": "mango"}
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn("corrupted or not a readable image", response.json()["detail"])

    def test_09_empty_image_for_mango(self):
        """Verify empty 0-byte file returns controlled 400 Bad Request."""
        response = self.client.post(
            "/api/predict",
            files={"file": ("empty.jpg", b"", "image/jpeg")},
            data={"food_type": "mango"}
        )
        self.assertEqual(response.status_code, 400)
        self.assertIn("empty", response.json()["detail"])


if __name__ == "__main__":
    unittest.main()
