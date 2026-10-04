import hashlib
import json
from pathlib import Path
from typing import Any, Dict, Optional, Union
from PIL import Image

# Important for Windows: import torch before sklearn/scipy
import torch
import torch.nn as nn
import torch.nn.functional as F
from torchvision import models, transforms

# Verified Phase 2J.1 SHA-256 hash for Mango model checkpoint integrity assurance
MANGO_EXPECTED_SHA256 = "4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9"

DEFAULT_MODEL_DIR = Path(__file__).parent / "saved_models"
DEFAULT_MANGO_CKPT = DEFAULT_MODEL_DIR / "freshiq_mobilenetv3_mango_best.pth"
DEFAULT_MANGO_CONFIG = DEFAULT_MODEL_DIR / "freshiq_mobilenetv3_mango_config.json"

MANGO_CLASSES = ["Unripe", "Semiripe", "Fully Ripe", "Overripe", "Perished"]
MANGO_CLASS_TO_IDX = {c: i for i, c in enumerate(MANGO_CLASSES)}
MANGO_IDX_TO_CLASS = {i: c for i, c in enumerate(MANGO_CLASSES)}

STAGE_RECOMMENDATIONS = {
    0: "Fruit is firm green and unripe. Store at room temperature away from direct sunlight to allow natural ripening.",
    1: "Fruit is transitioning to semiripe with initial softening. Maintain at ambient room temperature to continue ripening.",
    2: "Fruit is at peak consumption ripeness with optimal sweetness and aroma. Best consumed fresh or processed immediately.",
    3: "Fruit is overripe with advanced softening. Consume immediately or utilize for culinary blending/purees.",
    4: "Fruit has reached terminal decay or senescence and is no longer fit for consumption. Discard responsibly."
}

LEGAL_DISCLAIMER = (
    "FreshIQ Mango Ripeness Classifier. Stage predictions are derived strictly from surface visual characteristics. "
    "Remaining Useful Life (RUL) modeling, shelf-life day counts, and storage-condition simulations are intentionally "
    "unavailable for Mango because longitudinal specimen tracking data does not exist for this produce type."
)


class MangoClassifierEngine:
    """
    FreshIQ Dedicated Mango 5-Class Ripeness Classifier Engine.
    
    Architecture: MobileNetV3-Large (5 classes)
    Trained: Phase 2J.1 (held-out test accuracy: 95.64%, within +-1 stage: 100.0%)
    
    IMPORTANT: This engine supports image classification ONLY.
    No RUL, days remaining, or temperature-conditioned simulations are calculated.
    """

    def __init__(
        self,
        checkpoint_path: Optional[Union[str, Path]] = None,
        config_path: Optional[Union[str, Path]] = None,
        device: Optional[str] = None
    ):
        self.checkpoint_path = Path(checkpoint_path or DEFAULT_MANGO_CKPT)
        self.config_path = Path(config_path or DEFAULT_MANGO_CONFIG)

        if device is None:
            self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        else:
            self.device = torch.device(device)

        self._load_config()
        self._load_model()
        self._setup_transforms()

    def verify_checkpoint_integrity(self) -> bool:
        """Verifies that the Mango model checkpoint matches the validated SHA-256 hash."""
        if not self.checkpoint_path.exists():
            return False
        with open(self.checkpoint_path, "rb") as f:
            file_hash = hashlib.sha256(f.read()).hexdigest()
        return file_hash == MANGO_EXPECTED_SHA256

    def _load_config(self):
        if self.config_path.exists():
            with open(self.config_path, "r") as f:
                self.config = json.load(f)
        else:
            self.config = {
                "model_name": "FreshIQ_MobileNetV3_Mango",
                "produce": "Mango",
                "variety": "White Chaunsa Late",
                "num_classes": 5
            }

    def _load_model(self):
        if not self.checkpoint_path.exists():
            raise FileNotFoundError(f"Mango checkpoint not found at {self.checkpoint_path}")

        # Build MobileNetV3-Large with 5-class linear output head
        self.model = models.mobilenet_v3_large(weights=None)
        in_features = self.model.classifier[3].in_features
        self.model.classifier[3] = nn.Linear(in_features, len(MANGO_CLASSES))

        state_dict = torch.load(self.checkpoint_path, map_location=self.device)
        self.model.load_state_dict(state_dict)
        self.model.to(self.device)
        self.model.eval()

    def _setup_transforms(self):
        """Evaluation transforms: Resize 256 -> CenterCrop 224 -> ImageNet Normalization."""
        self.transforms = transforms.Compose([
            transforms.Resize(256),
            transforms.CenterCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
        ])

    def preprocess_image(self, image_input: Union[str, Path, Image.Image]) -> torch.Tensor:
        """Loads and pre-processes an image into a normalized tensor [1, 3, 224, 224]."""
        if isinstance(image_input, (str, Path)):
            img = Image.open(image_input).convert("RGB")
        elif isinstance(image_input, Image.Image):
            img = image_input.convert("RGB")
        else:
            raise TypeError(f"Unsupported image input type: {type(image_input)}")

        tensor = self.transforms(img).unsqueeze(0)
        return tensor.to(self.device)

    def predict(self, image_input: Union[str, Path, Image.Image]) -> Dict[str, Any]:
        """
        Runs dedicated Mango 5-class classification on the input image.
        Returns predicted discrete stage (0-4), stage label, confidence, and probabilities.
        """
        tensor = self.preprocess_image(image_input)

        with torch.no_grad():
            logits = self.model(tensor)
            probs_tensor = F.softmax(logits, dim=1).squeeze(0)

        probs_np = probs_tensor.cpu().numpy()
        pred_idx = int(probs_np.argmax())
        confidence = float(probs_np[pred_idx])
        stage_label = MANGO_IDX_TO_CLASS[pred_idx]

        probabilities = {
            MANGO_CLASSES[i]: float(probs_np[i])
            for i in range(len(MANGO_CLASSES))
        }

        recommendation = STAGE_RECOMMENDATIONS.get(
            pred_idx,
            "Consume or process according to visible ripeness state."
        )

        return {
            "food_type": "mango",
            "item_name": "Mango (White Chaunsa Late)",
            "model_id": "FreshIQ_MobileNetV3_Mango",
            "predicted_stage": pred_idx,
            "stage_label": stage_label,
            "confidence": confidence,
            "probabilities": probabilities,
            "actionable_recommendation": recommendation,
            "legal_disclaimer": LEGAL_DISCLAIMER
        }
