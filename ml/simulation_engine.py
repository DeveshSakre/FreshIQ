import hashlib
import json
from pathlib import Path
from typing import Any, Dict, List, Optional, Union

from PIL import Image
import numpy as np
import pandas as pd

# Important for Windows: import torch before sklearn/scipy to prevent c10.dll crash
import torch
import torch.nn.functional as F
import joblib

from ml.config import TrainingConfig
from ml.model import build_mobilenet_v3_small
from ml.dataset import get_eval_transforms


# Verified Phase 1A SHA-256 hash for integrity assurance
PHASE1A_EXPECTED_SHA256 = "8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e"

DEFAULT_MODEL_DIR = Path(__file__).parent / "saved_models"
DEFAULT_VISION_CKPT = DEFAULT_MODEL_DIR / "freshiq_mobilenetv3_avocado_best.pth"
DEFAULT_RUL_MODEL = DEFAULT_MODEL_DIR / "freshiq_shelflife_model.joblib"
DEFAULT_FEATURE_CONFIG = DEFAULT_MODEL_DIR / "shelflife_feature_config.json"


class WhatIfSimulationEngine:
    """
    FreshIQ Phase 2: What-If Storage Simulation Engine.
    
    Combines:
      1. Frozen Phase 1A MobileNetV3-Small vision classifier (5 ripening stages)
      2. Trained Phase 1B HistGradientBoosting RUL model (predicting days until Stage 5)
      3. Biophysical Arrhenius / Q10 kinetic extrapolation engine for 4C domestic refrigeration.
    """

    def __init__(
        self,
        vision_ckpt_path: Optional[Union[str, Path]] = None,
        rul_model_path: Optional[Union[str, Path]] = None,
        feature_config_path: Optional[Union[str, Path]] = None,
        device: Optional[str] = None
    ):
        self.vision_ckpt_path = Path(vision_ckpt_path or DEFAULT_VISION_CKPT)
        self.rul_model_path = Path(rul_model_path or DEFAULT_RUL_MODEL)
        self.feature_config_path = Path(feature_config_path or DEFAULT_FEATURE_CONFIG)

        if device is None:
            self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        else:
            self.device = torch.device(device)

        self.config = TrainingConfig()
        self._load_feature_config()
        self._load_vision_model()
        self._load_rul_model()
        self.transforms = get_eval_transforms(image_size=self.config.image_size)

    def verify_vision_checkpoint_integrity(self) -> bool:
        """Verifies that the Phase 1A model checkpoint has not been altered."""
        if not self.vision_ckpt_path.exists():
            return False
        with open(self.vision_ckpt_path, "rb") as f:
            file_hash = hashlib.sha256(f.read()).hexdigest()
        return file_hash == PHASE1A_EXPECTED_SHA256

    def _load_feature_config(self):
        if not self.feature_config_path.exists():
            raise FileNotFoundError(f"Feature config not found at {self.feature_config_path}")
        with open(self.feature_config_path, "r") as f:
            self.feature_config = json.load(f)
        self.feature_names = self.feature_config["features"]

    def _load_vision_model(self):
        if not self.vision_ckpt_path.exists():
            raise FileNotFoundError(f"Vision checkpoint not found at {self.vision_ckpt_path}")
        
        self.vision_model = build_mobilenet_v3_small(
            num_classes=self.config.num_classes,
            pretrained=False,
            freeze_backbone=False
        )
        checkpoint = torch.load(self.vision_ckpt_path, map_location=self.device)
        state_dict = checkpoint["model_state_dict"] if "model_state_dict" in checkpoint else checkpoint
        self.vision_model.load_state_dict(state_dict)
        self.vision_model.to(self.device)
        self.vision_model.eval()

    def _load_rul_model(self):
        if not self.rul_model_path.exists():
            raise FileNotFoundError(f"RUL model not found at {self.rul_model_path}")
        self.rul_model = joblib.load(self.rul_model_path)

    def preprocess_image(self, image_input: Union[str, Path, Image.Image, torch.Tensor]) -> torch.Tensor:
        """Converts image input into a normalized batch tensor [1, 3, 224, 224]."""
        if isinstance(image_input, (str, Path)):
            img = Image.open(image_input).convert("RGB")
            tensor = self.transforms(img).unsqueeze(0)
        elif isinstance(image_input, Image.Image):
            img = image_input.convert("RGB")
            tensor = self.transforms(img).unsqueeze(0)
        elif isinstance(image_input, torch.Tensor):
            tensor = image_input
            if tensor.ndim == 3:
                tensor = tensor.unsqueeze(0)
        else:
            raise TypeError(f"Unsupported image input type: {type(image_input)}")

        return tensor.to(self.device)

    @torch.no_grad()
    def predict_ripeness(self, image_input: Union[str, Path, Image.Image, torch.Tensor]) -> Dict[str, Any]:
        """
        Runs Phase 1A MobileNetV3-Small classifier on produce photo.
        Returns probabilities, predicted stage, expected stage value, and confidence.
        """
        tensor = self.preprocess_image(image_input)
        logits = self.vision_model(tensor)
        probs = F.softmax(logits, dim=1).cpu().numpy()[0]

        pred_stage_idx = int(np.argmax(probs))  # 0..4
        pred_stage = pred_stage_idx + 1         # 1..5
        confidence = float(probs[pred_stage_idx])

        # Continuous probability-weighted expected ripening stage
        expected_stage = float(sum((k + 1) * probs[k] for k in range(5)))

        stage_labels = self.config.stage_labels

        return {
            "probabilities": {
                "stage_1": float(probs[0]),
                "stage_2": float(probs[1]),
                "stage_3": float(probs[2]),
                "stage_4": float(probs[3]),
                "stage_5": float(probs[4]),
            },
            "predicted_stage": pred_stage,
            "predicted_stage_label": stage_labels.get(pred_stage, f"Stage {pred_stage}"),
            "expected_stage": round(expected_stage, 3),
            "confidence": round(confidence, 4)
        }

    def _build_feature_row(self, ripeness_result: Dict[str, Any], temperature_c: float, is_cold_storage: int) -> pd.DataFrame:
        """
        Constructs the exact 9-feature row required by the Phase 1B HistGradientBoosting model.
        Features: p1, p2, p3, p4, p5, expected_stage, confidence, temperature_c, is_cold_storage.
        """
        probs = ripeness_result["probabilities"]
        row_dict = {
            "p1": probs["stage_1"],
            "p2": probs["stage_2"],
            "p3": probs["stage_3"],
            "p4": probs["stage_4"],
            "p5": probs["stage_5"],
            "expected_stage": ripeness_result["expected_stage"],
            "confidence": ripeness_result["confidence"],
            "temperature_c": float(temperature_c),
            "is_cold_storage": int(is_cold_storage)
        }
        # Ensure exact column ordering as trained
        return pd.DataFrame([[row_dict[col] for col in self.feature_names]], columns=self.feature_names)

    def _generate_recommendation(self, predicted_stage: int, rul_days: float, storage_condition: str) -> str:
        """Provides actionable, consumer-friendly food management guidance."""
        if predicted_stage == 5 or rul_days <= 0.2:
            return (
                "Terminal maturity (Overripe). Use immediately for smoothies, dips, or baking if sensory "
                "qualities are acceptable. Always inspect for off-odors, mold, or discoloration before consuming."
            )
        elif predicted_stage == 4:
            return (
                f"Peak edible ripeness (Consume Soon). Optimal texture and flavor. Consume within "
                f"{max(1, round(rul_days))} day(s). Refrigerate immediately if you need to hold it for an extra day."
            )
        elif predicted_stage == 3:
            return (
                f"Firm ripe. Yields slightly to gentle pressure; ideal for slicing. Estimated remaining shelf "
                f"life: ~{rul_days:.1f} days. Store at room temperature to finish ripening, or chill to slow progress."
            )
        elif predicted_stage == 2:
            return (
                f"Breaking ripeness. Transitioning from green to ripe. Estimated remaining shelf life: "
                f"~{rul_days:.1f} days under {storage_condition} conditions. Keep on counter until dark and yielding."
            )
        else:  # Stage 1
            return (
                f"Underripe / firm. Estimated remaining usable shelf life: ~{rul_days:.1f} days under "
                f"{storage_condition} conditions. Store at room temperature away from direct sunlight."
            )

    def simulate_condition(
        self,
        ripeness_result: Dict[str, Any],
        condition: str = "ambient"
    ) -> Dict[str, Any]:
        """
        Runs remaining usable shelf-life (RUL) estimation for a specific storage scenario.
        
        Supported conditions:
          - '10C' (Cold Storage, 10°C) -> Direct empirical model
          - '20C' (Controlled Room, 20°C) -> Direct empirical model
          - 'ambient' (Ambient Room Temp ~20-22°C) -> Direct empirical model (20°C calibration)
          - '4C_refrigerator' (Domestic Refrigerator, 4°C) -> Biophysical Arrhenius / Q10 Extrapolation
        """
        cond_normalized = condition.strip().lower().replace("°", "").replace(" ", "_")

        pred_stage = ripeness_result["predicted_stage"]

        # Terminal state handling: Stage 5 avocados have reached the terminal boundary
        if pred_stage == 5:
            # Overripe fruit has 0 remaining usable shelf life
            rul_days = 0.0
            is_extrapolated = (cond_normalized in ["4c", "4c_refrigerator", "refrigerator"])
            method = "Empirical ML Model (HistGradientBoosting)" if not is_extrapolated else "Biophysical Arrhenius / Q10 Extrapolation"
            return {
                "condition": condition,
                "temperature_c": 4.0 if is_extrapolated else (10.0 if "10" in cond_normalized else 20.0),
                "estimated_rul_days": 0.0,
                "is_extrapolated": is_extrapolated,
                "method": method,
                "uncertainty_note": (
                    "Fruit has already reached Stage 5 (Overripe). Refrigeration cannot restore expired shelf life."
                    if is_extrapolated else None
                ),
                "disclaimer": "AI-estimated remaining usable shelf life. Not a food safety guarantee.",
                "recommendation": self._generate_recommendation(pred_stage, 0.0, condition)
            }

        if cond_normalized in ["10c", "10_c", "t10", "cold_storage"]:
            temp = 10.0
            is_cold = 1
            is_extrapolated = False
            method = "Empirical ML Model (HistGradientBoosting)"
            uncertainty_note = None

            feat_df = self._build_feature_row(ripeness_result, temperature_c=temp, is_cold_storage=is_cold)
            raw_rul = float(self.rul_model.predict(feat_df.values)[0])
            rul_days = max(0.0, raw_rul)

        elif cond_normalized in ["20c", "20_c", "t20"]:
            temp = 20.0
            is_cold = 0
            is_extrapolated = False
            method = "Empirical ML Model (HistGradientBoosting)"
            uncertainty_note = None

            feat_df = self._build_feature_row(ripeness_result, temperature_c=temp, is_cold_storage=is_cold)
            raw_rul = float(self.rul_model.predict(feat_df.values)[0])
            rul_days = max(0.0, raw_rul)

        elif cond_normalized in ["ambient", "tam", "room", "room_temperature"]:
            temp = 20.0  # Dataset ambient is ~20-22°C, modeled via 20°C calibrated baseline
            is_cold = 0
            is_extrapolated = False
            method = "Empirical ML Model (HistGradientBoosting)"
            uncertainty_note = None

            feat_df = self._build_feature_row(ripeness_result, temperature_c=temp, is_cold_storage=is_cold)
            raw_rul = float(self.rul_model.predict(feat_df.values)[0])
            rul_days = max(0.0, raw_rul)

        elif cond_normalized in ["4c", "4c_refrigerator", "refrigerator", "fridge"]:
            temp = 4.0
            is_extrapolated = True
            method = "Biophysical Arrhenius / Q10 Extrapolation"
            uncertainty_note = (
                "4°C is not an empirically observed training condition in the dataset. This prediction is an "
                "AI biophysical kinetic simulation based on avocado respiration slowing (Q10 = 2.38). "
                "Shelf life is physiologically bounded by chilling sensitivity."
            )

            # First get baseline ambient RUL (at 20°C)
            feat_ambient = self._build_feature_row(ripeness_result, temperature_c=20.0, is_cold_storage=0)
            baseline_rul = max(0.0, float(self.rul_model.predict(feat_ambient.values)[0]))

            if baseline_rul <= 0.2:
                rul_days = 0.0
            else:
                # Q10 kinetic scaling: Factor = (Q10) ^ ((20 - 4) / 10) = (2.38) ^ 1.6 = ~3.996
                # Capped at biological ceiling (~21 days under domestic refrigeration due to post-climacteric chilling breakdown)
                q10 = 2.38
                scale_factor = q10 ** ((20.0 - 4.0) / 10.0)
                extrapolated_days = baseline_rul * scale_factor

                # Biological capping: chilling injury and loss of texture after 21 days
                rul_days = min(21.0, max(0.0, extrapolated_days))

        else:
            raise ValueError(f"Unsupported storage condition: '{condition}'. Supported: '10C', '20C', 'ambient', '4C_refrigerator'")

        return {
            "condition": condition,
            "temperature_c": temp,
            "estimated_rul_days": round(rul_days, 1),
            "is_extrapolated": is_extrapolated,
            "method": method,
            "uncertainty_note": uncertainty_note,
            "disclaimer": "AI-estimated remaining usable shelf life. Not a food safety guarantee.",
            "recommendation": self._generate_recommendation(pred_stage, rul_days, condition)
        }

    def simulate_all_scenarios(
        self,
        image_input: Union[str, Path, Image.Image, torch.Tensor]
    ) -> Dict[str, Any]:
        """
        Runs comprehensive FreshIQ produce analysis:
          1. Vision classification (MobileNetV3-Small)
          2. Multi-scenario What-If simulation:
             - 10°C (Cold Storage)
             - 20°C (Controlled Room)
             - Ambient (~20–22°C)
             - 4°C Refrigerator (Biophysical Extrapolation)
        """
        ripeness = self.predict_ripeness(image_input)

        scenarios = {
            "10C": self.simulate_condition(ripeness, condition="10C"),
            "20C": self.simulate_condition(ripeness, condition="20C"),
            "ambient": self.simulate_condition(ripeness, condition="ambient"),
            "4C_refrigerator": self.simulate_condition(ripeness, condition="4C_refrigerator")
        }

        # Calculate shelf-life extension gain from refrigeration (4°C vs ambient)
        amb_days = scenarios["ambient"]["estimated_rul_days"]
        fridge_days = scenarios["4C_refrigerator"]["estimated_rul_days"]
        extension_gain_days = max(0.0, round(fridge_days - amb_days, 1))

        return {
            "item_name": "Avocado (Hass)",
            "ripeness_assessment": ripeness,
            "what_if_scenarios": scenarios,
            "refrigeration_extension_gain_days": extension_gain_days,
            "primary_recommendation": scenarios["ambient"]["recommendation"],
            "legal_disclaimer": (
                "FreshIQ outputs are AI-estimated remaining usable shelf life indicators based on computer "
                "vision and biophysical kinetics. FreshIQ does not provide microbiological food-safety guarantees. "
                "Consumers must visually inspect produce for mold, rot, or off-odors before consumption."
            )
        }
