import hashlib
import json
from pathlib import Path
import unittest

import numpy as np
import pandas as pd
from PIL import Image

# Crucial for Windows: import torch before sklearn/joblib
import torch

from ml.simulation_engine import (
    WhatIfSimulationEngine,
    PHASE1A_EXPECTED_SHA256,
    DEFAULT_VISION_CKPT,
    DEFAULT_RUL_MODEL,
    DEFAULT_FEATURE_CONFIG,
)
from ml.config import TrainingConfig


class TestWhatIfSimulationEngine(unittest.TestCase):
    """
    Automated verification suite for FreshIQ Phase 2 What-If Storage Simulation Engine.
    """

    @classmethod
    def setUpClass(cls):
        cls.config = TrainingConfig()
        cls.engine = WhatIfSimulationEngine(device="cpu")
        cls.test_csv = pd.read_csv(cls.config.test_csv)
        cls.data_root = Path(cls.config.data_root)

    def test_01_phase1a_checkpoint_integrity(self):
        """Verify Phase 1A MobileNetV3-Small checkpoint is untouched and has exact SHA256."""
        self.assertTrue(DEFAULT_VISION_CKPT.exists(), "Phase 1A checkpoint does not exist.")
        with open(DEFAULT_VISION_CKPT, "rb") as f:
            actual_sha256 = hashlib.sha256(f.read()).hexdigest()
        self.assertEqual(
            actual_sha256,
            PHASE1A_EXPECTED_SHA256,
            f"Phase 1A checkpoint SHA256 mismatch! Expected {PHASE1A_EXPECTED_SHA256}, got {actual_sha256}"
        )
        self.assertTrue(self.engine.verify_vision_checkpoint_integrity())

    def test_02_phase1b_model_and_config_loading(self):
        """Verify Phase 1B HistGradientBoosting model and feature config load correctly."""
        self.assertTrue(DEFAULT_RUL_MODEL.exists(), "Phase 1B RUL model file missing.")
        self.assertTrue(DEFAULT_FEATURE_CONFIG.exists(), "Phase 1B feature config file missing.")
        self.assertIsNotNone(self.engine.rul_model)
        self.assertIsNotNone(self.engine.feature_config)

    def test_03_feature_ordering_exact_match(self):
        """Verify feature ordering strictly matches shelflife_feature_config.json."""
        with open(DEFAULT_FEATURE_CONFIG, "r") as f:
            cfg = json.load(f)
        expected_features = cfg["features"]
        self.assertEqual(
            self.engine.feature_names,
            expected_features,
            "Feature names or ordering in engine do not match shelflife_feature_config.json"
        )
        # Verify dummy feature row columns
        dummy_ripeness = {
            "probabilities": {"stage_1": 0.2, "stage_2": 0.2, "stage_3": 0.2, "stage_4": 0.2, "stage_5": 0.2},
            "predicted_stage": 3,
            "expected_stage": 3.0,
            "confidence": 0.2
        }
        feat_row = self.engine._build_feature_row(dummy_ripeness, temperature_c=20.0, is_cold_storage=0)
        self.assertEqual(list(feat_row.columns), expected_features)

    def test_04_numerical_validity_on_real_images(self):
        """Test inference on 5 sample test images across stages."""
        sample_rows = self.test_csv.sample(n=5, random_state=42)
        for _, row in sample_rows.iterrows():
            filename = str(row["filename"])
            if not filename.endswith(".jpg"):
                filename += ".jpg"
            img_path = self.data_root / filename
            self.assertTrue(img_path.exists(), f"Image {img_path} not found.")

            result = self.engine.simulate_all_scenarios(img_path)

            # Check vision outputs
            rip = result["ripeness_assessment"]
            probs = list(rip["probabilities"].values())
            self.assertEqual(len(probs), 5)
            self.assertAlmostEqual(sum(probs), 1.0, places=4)
            for p in probs:
                self.assertGreaterEqual(p, 0.0)
                self.assertLessEqual(p, 1.0)
            self.assertIn(rip["predicted_stage"], [1, 2, 3, 4, 5])
            self.assertGreaterEqual(rip["expected_stage"], 1.0)
            self.assertLessEqual(rip["expected_stage"], 5.0)
            self.assertGreaterEqual(rip["confidence"], 0.2)
            self.assertLessEqual(rip["confidence"], 1.0)

            # Check What-If scenarios
            scenarios = result["what_if_scenarios"]
            for cond in ["10C", "20C", "ambient", "4C_refrigerator"]:
                self.assertIn(cond, scenarios)
                sc = scenarios[cond]
                self.assertGreaterEqual(sc["estimated_rul_days"], 0.0, f"Negative RUL in {cond}!")
                self.assertIsInstance(sc["estimated_rul_days"], float)
                self.assertTrue(len(sc["recommendation"]) > 10)

    def test_05_rul_non_negativity_constraint(self):
        """Verify RUL cannot become negative under any synthetic input boundary."""
        for stage in range(1, 6):
            synth_ripeness = {
                "probabilities": {f"stage_{i}": 1.0 if i == stage else 0.0 for i in range(1, 6)},
                "predicted_stage": stage,
                "expected_stage": float(stage),
                "confidence": 1.0
            }
            for cond in ["10C", "20C", "ambient", "4C_refrigerator"]:
                res = self.engine.simulate_condition(synth_ripeness, condition=cond)
                self.assertGreaterEqual(res["estimated_rul_days"], 0.0)

    def test_06_stage5_terminal_boundary_behavior(self):
        """Verify Stage 5 produce yields exactly 0.0 days remaining usable life."""
        stage5_ripeness = {
            "probabilities": {"stage_1": 0.0, "stage_2": 0.0, "stage_3": 0.0, "stage_4": 0.05, "stage_5": 0.95},
            "predicted_stage": 5,
            "expected_stage": 4.95,
            "confidence": 0.95
        }
        for cond in ["10C", "20C", "ambient", "4C_refrigerator"]:
            res = self.engine.simulate_condition(stage5_ripeness, condition=cond)
            self.assertEqual(
                res["estimated_rul_days"],
                0.0,
                f"Stage 5 fruit had non-zero RUL in condition {cond}: {res['estimated_rul_days']}"
            )
            self.assertIn("Overripe", res["recommendation"])

    def test_07_separation_of_empirical_and_extrapolated_conditions(self):
        """Verify strict separation between empirical conditions (10C, 20C, ambient) and 4C extrapolation."""
        dummy_ripeness = {
            "probabilities": {"stage_1": 0.8, "stage_2": 0.15, "stage_3": 0.05, "stage_4": 0.0, "stage_5": 0.0},
            "predicted_stage": 1,
            "expected_stage": 1.25,
            "confidence": 0.8
        }

        # Empirical conditions
        for cond in ["10C", "20C", "ambient"]:
            res = self.engine.simulate_condition(dummy_ripeness, condition=cond)
            self.assertFalse(res["is_extrapolated"], f"Empirical condition {cond} marked as extrapolated!")
            self.assertIn("HistGradientBoosting", res["method"])
            self.assertIsNone(res["uncertainty_note"])

        # Extrapolated condition (4C)
        res_4c = self.engine.simulate_condition(dummy_ripeness, condition="4C_refrigerator")
        self.assertTrue(res_4c["is_extrapolated"], "4C condition not marked as extrapolated!")
        self.assertIn("Arrhenius", res_4c["method"])
        self.assertIsNotNone(res_4c["uncertainty_note"])
        self.assertIn("Q10", res_4c["uncertainty_note"])

    def test_08_temperature_shelf_life_ordering(self):
        """Verify that for underripe/early-stage fruit, colder temperatures extend shelf life."""
        underripe = {
            "probabilities": {"stage_1": 0.9, "stage_2": 0.1, "stage_3": 0.0, "stage_4": 0.0, "stage_5": 0.0},
            "predicted_stage": 1,
            "expected_stage": 1.1,
            "confidence": 0.9
        }
        res_10c = self.engine.simulate_condition(underripe, condition="10C")
        res_20c = self.engine.simulate_condition(underripe, condition="20C")
        res_4c = self.engine.simulate_condition(underripe, condition="4C_refrigerator")

        # 4C refrigerator >= 10C cold storage > 20C room temperature
        self.assertGreater(res_10c["estimated_rul_days"], res_20c["estimated_rul_days"])
        self.assertGreaterEqual(res_4c["estimated_rul_days"], res_10c["estimated_rul_days"])


if __name__ == "__main__":
    unittest.main()
