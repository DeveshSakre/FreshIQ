"""
FreshIQ Banana Ripeness Classifier Engine (Phase 2K.1)
======================================================
Dedicated lightweight inference engine for Banana ripeness stage classification.

Architecture: MobileNetV3-Small (3 classes: Unripe, Semi-ripe, Ripe)
Trained: Phase 2K.1
Input: 3 x 224 x 224 RGB image

IMPORTANT LEGAL & SCIENTIFIC MANDATE:
This engine supports surface visual ripeness classification ONLY.
No Remaining Useful Life (RUL), shelf-life days remaining, or temperature/humidity
simulations are supported for Banana because longitudinal ground truth and environmental
kinetics are not recorded in the source dataset.
"""

import hashlib
import json
from pathlib import Path
from typing import Any, Dict, Optional, Union
from PIL import Image

import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import models, transforms

# Verified Phase 2K.1 SHA-256 hash for Banana model checkpoint integrity
BANANA_EXPECTED_SHA256 = "cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462"

DEFAULT_MODEL_DIR = Path(__file__).parent / "saved_models"
DEFAULT_BANANA_CKPT = DEFAULT_MODEL_DIR / "freshiq_mobilenetv3_banana_best.pth"
DEFAULT_BANANA_CONFIG = DEFAULT_MODEL_DIR / "banana_training_config.json"

BANANA_CLASSES = ["Unripe", "Semi-ripe", "Ripe"]
BANANA_CLASS_TO_IDX = {c: i for i, c in enumerate(BANANA_CLASSES)}
BANANA_IDX_TO_CLASS = {i: c for i, c in enumerate(BANANA_CLASSES)}

STAGE_RECOMMENDATIONS = {
    0: "Fruit is firm green and unripe (Days 0-2). Store at ambient room temperature away from direct sunlight to allow natural ripening.",
    1: "Fruit is transitioning to semi-ripe with yellowing peel and green tips (Days 3-5). Maintain at ambient room temperature to continue ripening.",
    2: "Fruit is at peak consumption ripeness with full yellow peel and incipient spots (Days 6-7). Best consumed fresh or refrigerated to decelerate senescence."
}

LEGAL_DISCLAIMER = (
    "FreshIQ Banana Ripeness Classifier. Stage predictions are derived strictly from surface visual characteristics. "
    "Remaining Useful Life (RUL) modeling, shelf-life day counts, and storage-condition simulations are intentionally "
    "unavailable for Banana because longitudinal specimen tracking ground truth and environmental telemetry do not exist "
    "for this produce type."
)


class BananaClassifierEngine:
    """
    FreshIQ Dedicated Banana 3-Class Ripeness Classifier Engine.
    
    Architecture: MobileNetV3-Small (3 classes)
    Trained: Phase 2K.1
    
    IMPORTANT: This engine supports image classification ONLY.
    No RUL, days remaining, or temperature-conditioned simulations are calculated.
    """

    def __init__(
        self,
        checkpoint_path: Optional[Union[str, Path]] = None,
        config_path: Optional[Union[str, Path]] = None,
        device: Optional[str] = None
    ):
        self.checkpoint_path = Path(checkpoint_path or DEFAULT_BANANA_CKPT)
        self.config_path = Path(config_path or DEFAULT_BANANA_CONFIG)

        if device is None:
            self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        else:
            self.device = torch.device(device)

        self._load_config()
        self._load_model()
        self._setup_transforms()

    def verify_checkpoint_integrity(self) -> bool:
        """Verifies that the Banana model checkpoint matches the validated SHA-256 hash."""
        if not self.checkpoint_path.exists():
            return False
        with open(self.checkpoint_path, "rb") as f:
            file_hash = hashlib.sha256(f.read()).hexdigest()
        return file_hash == BANANA_EXPECTED_SHA256

    def _load_config(self):
        if self.config_path.exists():
            with open(self.config_path, "r") as f:
                self.config = json.load(f)
        else:
            self.config = {
                "model_name": "FreshIQ_MobileNetV3_Banana",
                "produce": "Banana",
                "scientific_name": "Musa acuminata",
                "num_classes": 3
            }

    def _load_model(self):
        if not self.checkpoint_path.exists():
            raise FileNotFoundError(f"Banana checkpoint not found at {self.checkpoint_path}")

        # Build MobileNetV3-Small with 3-class linear output head
        self.model = models.mobilenet_v3_small(weights=None)
        in_features = self.model.classifier[3].in_features
        self.model.classifier[3] = nn.Linear(in_features, len(BANANA_CLASSES))

        state_dict = torch.load(self.checkpoint_path, map_location=self.device)
        self.model.load_state_dict(state_dict)
        self.model.to(self.device)
        self.model.eval()

    def _setup_transforms(self):
        self.eval_transforms = transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(
                mean=[0.485, 0.456, 0.406],
                std=[0.229, 0.224, 0.225]
            )
        ])

    def predict(self, image_input: Union[str, Path, Image.Image]) -> Dict[str, Any]:
        """
        Runs ripeness stage classification inference on a single banana image.

        Args:
            image_input: File path (str/Path) or PIL Image instance.

        Returns:
            Dictionary containing predicted stage, confidence, and class probabilities.
        """
        if isinstance(image_input, (str, Path)):
            with Image.open(image_input) as img:
                image = img.convert("RGB")
        elif isinstance(image_input, Image.Image):
            image = image_input.convert("RGB")
        else:
            raise TypeError(f"Unsupported image input type: {type(image_input)}")

        tensor = self.eval_transforms(image).unsqueeze(0).to(self.device)

        with torch.no_grad():
            logits = self.model(tensor)
            probabilities = F.softmax(logits, dim=1).cpu().numpy()[0]

        pred_idx = int(torch.argmax(logits, dim=1).item())
        pred_class = BANANA_CLASSES[pred_idx]
        confidence = float(probabilities[pred_idx])

        return {
            "produce": "Banana",
            "scientific_name": "Musa acuminata",
            "item_name": "Banana (Cavendish)",
            "model_id": "FreshIQ_MobileNetV3_Banana",
            "predicted_class_id": pred_idx,
            "predicted_stage": pred_idx,
            "predicted_class_name": pred_class,
            "stage_label": pred_class,
            "confidence": confidence,
            "probabilities": {
                cls_name: float(prob)
                for cls_name, prob in zip(BANANA_CLASSES, probabilities)
            },
            "recommendation": STAGE_RECOMMENDATIONS[pred_idx],
            "actionable_recommendation": STAGE_RECOMMENDATIONS[pred_idx],
            "disclaimer": LEGAL_DISCLAIMER,
            "legal_disclaimer": LEGAL_DISCLAIMER
        }
