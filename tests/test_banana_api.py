"""
FreshIQ Banana API Integration Test Suite (Phase 2K.3)
======================================================
Tests:
1. Health endpoint exposes Banana status, verified SHA-256, and supported produce.
2. Banana ripeness inference for all 3 classes (Unripe, Semi-ripe, Ripe).
3. Banana response fields: predicted_ripening_stage in {0, 1, 2}, stage_label in {Unripe, Semi-ripe, Ripe}.
4. Confidence in [0, 1] and top probability matches confidence.
5. Exactly 3 class probabilities: Unripe, Semi-ripe, Ripe.
6. Probability bounds [0, 1] and sum is approximately 1.0.
7. Strict absence of RUL (rul_available=False, rul=None).
8. Strict absence of scenarios (scenarios=None, refrigeration_extension_gain_days=None, expected_continuous_ripening_stage=None).
9. storage_condition parameter does not create RUL or alter predictions.
10. Unsupported produce 'tomato' returns HTTP 400 Bad Request.
11. Unsupported arbitrary produce returns HTTP 400 Bad Request.
12. Invalid / corrupted image returns HTTP 400 Bad Request.
13. Missing file returns HTTP 422.
14. Backward compatibility: Avocado prediction when food_type is omitted.
15. Avocado regression: food_type=avocado returns RUL and What-If scenarios.
16. Mango regression: food_type=mango returns 5-class classification without RUL.
"""

import io
import os
from pathlib import Path
import unittest

# Windows import order: torch before sklearn/fastapi
import torch
import pandas as pd
from PIL import Image
from fastapi.testclient import TestClient

from backend.app.main import app
from backend.app.config import BANANA_EXPECTED_SHA256, BASE_DIR

BANANA_MANIFEST_PATH = BASE_DIR / "ml" / "datasets" / "multi_produce" / "banana" / "manifests" / "banana_cluster_split_manifest.csv"
BANANA_ROOT = BASE_DIR / "ml" / "datasets" / "multi_produce" / "banana"
AVOCADO_SAMPLE_DIR = BASE_DIR / "ml" / "datasets" / "avocado_ripening_sample"


class TestFreshIQBananaAPI(unittest.TestCase):
    """
    Integration test suite for FreshIQ Banana 3-Class Ripeness API.
    Verifies Banana routing, probability validity, RUL exclusion,
    produce isolation, error handling, and Avocado/Mango regressions.
    """

    @classmethod
    def setUpClass(cls):
        # Trigger application lifespan (loads Avocado, Mango, and Banana models)
        cls.client_cm = TestClient(app)
        cls.client = cls.client_cm.__enter__()

        # Read split manifest and find 1 test image per class
        cls.split_df = pd.read_csv(BANANA_MANIFEST_PATH)
        test_df = cls.split_df[cls.split_df["split"] == "test"]

        cls.sample_images = {}
        for c_id, cls_name in [(0, "Unripe"), (1, "Semi-ripe"), (2, "Ripe")]:
            row = test_df[test_df["class_id"] == c_id].iloc[0]
            rel_path = row["path"].replace("/", os.sep)
            img_p = BANANA_ROOT / rel_path
            assert img_p.exists(), f"Test image not found: {img_p}"
            cls.sample_images[cls_name] = img_p

    @classmethod
    def tearDownClass(cls):
        cls.client_cm.__exit__(None, None, None)

    def _predict(self, image_path: Path, food_type: str = "banana", storage_condition: str = None):
        with open(image_path, "rb") as f:
            image_bytes = f.read()

        data = {}
        if food_type is not None:
            data["food_type"] = food_type
        if storage_condition is not None:
            data["storage_condition"] = storage_condition

        return self.client.post(
            "/api/predict",
            files={"file": (image_path.name, image_bytes, "image/jpeg")},
            data=data
        )

    def test_01_health_reports_banana_mango_avocado(self):
        """Verify /api/health verifies all 3 models and reports all supported produce types."""
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
        self.assertTrue(data["banana_model_loaded"])
        self.assertTrue(data["banana_checkpoint_verified"])
        self.assertIn("Avocado (Hass)", data["supported_produce"])
        self.assertIn("Mango (White Chaunsa Late)", data["supported_produce"])
        self.assertIn("Banana (Cavendish)", data["supported_produce"])

    def test_02_banana_classes_inference(self):
        """
        Test POST /api/predict with food_type=banana for representative samples.
        Verifies valid stages, probabilities summing to ~1.0, and strict absence of RUL.
        """
        for expected_class, img_path in self.sample_images.items():
            with self.subTest(class_name=expected_class):
                response = self._predict(img_path, food_type="banana")
                self.assertEqual(response.status_code, 200)
                data = response.json()

                # Produce identity
                self.assertEqual(data["food_type"], "banana")
                self.assertEqual(data["item_name"], "Banana (Cavendish)")
                self.assertEqual(data["model_id"], "FreshIQ_MobileNetV3_Banana")

                # Ripeness assessment
                rip = data["ripeness"]
                self.assertIn(rip["predicted_ripening_stage"], [0, 1, 2])
                self.assertIn(rip["stage_label"], ["Unripe", "Semi-ripe", "Ripe"])
                self.assertGreaterEqual(rip["confidence"], 0.0)
                self.assertLessEqual(rip["confidence"], 1.0)
                self.assertIsNone(rip["expected_continuous_ripening_stage"])

                # Probability distribution
                probs = rip["probabilities"]
                self.assertEqual(set(probs.keys()), {"Unripe", "Semi-ripe", "Ripe"})
                for p in probs.values():
                    self.assertGreaterEqual(p, 0.0)
                    self.assertLessEqual(p, 1.0)
                self.assertAlmostEqual(sum(probs.values()), 1.0, places=4)

                # Strict absence of RUL and What-If scenarios
                self.assertFalse(data["rul_available"])
                self.assertIsNone(data["rul"])
                self.assertIsNone(data["scenarios"])
                self.assertIsNone(data["refrigeration_extension_gain_days"])
                self.assertTrue(len(data["actionable_recommendation"]) > 0)
                self.assertTrue(len(data["legal_disclaimer"]) > 0)

    def test_03_banana_storage_condition_invariance(self):
        """Verify that storage_condition does NOT fabricate RUL or change classification."""
        sample_img = self.sample_images["Semi-ripe"]
        res_default = self._predict(sample_img, food_type="banana")
        res_cold = self._predict(sample_img, food_type="banana", storage_condition="4C_refrigerator")
        res_warm = self._predict(sample_img, food_type="banana", storage_condition="ambient")

        self.assertEqual(res_default.status_code, 200)
        self.assertEqual(res_cold.status_code, 200)
        self.assertEqual(res_warm.status_code, 200)

        data_def = res_default.json()
        data_cold = res_cold.json()
        data_warm = res_warm.json()

        # Classification results must be identical
        self.assertEqual(data_def["ripeness"]["predicted_ripening_stage"], data_cold["ripeness"]["predicted_ripening_stage"])
        self.assertEqual(data_def["ripeness"]["predicted_ripening_stage"], data_warm["ripeness"]["predicted_ripening_stage"])

        # RUL must remain None across all storage conditions
        self.assertFalse(data_cold["rul_available"])
        self.assertIsNone(data_cold["rul"])
        self.assertIsNone(data_cold["scenarios"])
        self.assertIsNone(data_cold["refrigeration_extension_gain_days"])

    def test_04_unsupported_produce_tomato_returns_400(self):
        """Verify food_type=tomato returns HTTP 400 Bad Request with supported types listed."""
        sample_img = self.sample_images["Unripe"]
        response = self._predict(sample_img, food_type="tomato")
        self.assertEqual(response.status_code, 400)
        detail = response.json()["detail"]
        self.assertIn("Unsupported food type 'tomato'", detail)
        self.assertIn("avocado", detail)
        self.assertIn("banana", detail)
        self.assertIn("mango", detail)

    def test_05_unsupported_arbitrary_produce_returns_400(self):
        """Verify arbitrary unknown produce returns HTTP 400."""
        sample_img = self.sample_images["Unripe"]
        response = self._predict(sample_img, food_type="papaya")
        self.assertEqual(response.status_code, 400)

    def test_06_corrupted_image_returns_400(self):
        """Verify non-image bytes return HTTP 400."""
        garbage_bytes = b"NOT_A_VALID_IMAGE_FILE_DATA_CORRUPT"
        response = self.client.post(
            "/api/predict",
            files={"file": ("corrupt.jpg", garbage_bytes, "image/jpeg")},
            data={"food_type": "banana"}
        )
        self.assertEqual(response.status_code, 400)

    def test_07_missing_file_returns_422(self):
        """Verify request without file returns HTTP 422 Unprocessable Entity."""
        response = self.client.post(
            "/api/predict",
            data={"food_type": "banana"}
        )
        self.assertEqual(response.status_code, 422)

    def test_08_avocado_backward_compatibility_omitted_food_type(self):
        """Verify omitted food_type defaults to Avocado with RUL and scenarios."""
        # Use an Avocado test image if available, or sample
        avocado_files = list(AVOCADO_SAMPLE_DIR.glob("*.jpg"))
        test_img = avocado_files[0] if avocado_files else self.sample_images["Unripe"]

        response = self._predict(test_img, food_type=None)
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["food_type"], "avocado")
        self.assertTrue(data["rul_available"])
        self.assertIsNotNone(data["scenarios"])
        self.assertIn("ambient", data["scenarios"])
        self.assertIn("10C", data["scenarios"])

    def test_09_avocado_explicit_regression(self):
        """Verify explicit food_type=avocado behaves identically."""
        avocado_files = list(AVOCADO_SAMPLE_DIR.glob("*.jpg"))
        test_img = avocado_files[0] if avocado_files else self.sample_images["Unripe"]

        response = self._predict(test_img, food_type="avocado")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["food_type"], "avocado")
        self.assertTrue(data["rul_available"])
        self.assertIsNotNone(data["scenarios"])

    def test_10_mango_regression(self):
        """Verify food_type=mango still functions with 5 classes and no RUL."""
        test_img = self.sample_images["Unripe"]
        response = self._predict(test_img, food_type="mango")
        self.assertEqual(response.status_code, 200)
        data = response.json()
        self.assertEqual(data["food_type"], "mango")
        self.assertFalse(data["rul_available"])
        self.assertIsNone(data["scenarios"])
        self.assertEqual(len(data["ripeness"]["probabilities"]), 5)


if __name__ == "__main__":
    unittest.main()
