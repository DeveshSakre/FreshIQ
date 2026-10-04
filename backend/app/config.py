from pathlib import Path
from typing import List

# Root project directory
BASE_DIR = Path(__file__).resolve().parent.parent.parent

# ML Model Paths — Avocado (Phase 1A / Phase 1B)
SAVED_MODELS_DIR = BASE_DIR / "ml" / "saved_models"
VISION_CHECKPOINT_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_avocado_best.pth"
RUL_MODEL_PATH = SAVED_MODELS_DIR / "freshiq_shelflife_model.joblib"
FEATURE_CONFIG_PATH = SAVED_MODELS_DIR / "shelflife_feature_config.json"

# ML Model Paths — Mango (Phase 2J.1)
MANGO_CHECKPOINT_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_best.pth"
MANGO_CONFIG_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_mango_config.json"
MANGO_EXPECTED_SHA256 = "4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9"

# ML Model Paths — Banana (Phase 2K.1 / Phase 2K.2)
BANANA_CHECKPOINT_PATH = SAVED_MODELS_DIR / "freshiq_mobilenetv3_banana_best.pth"
BANANA_CONFIG_PATH = SAVED_MODELS_DIR / "banana_training_config.json"
BANANA_EXPECTED_SHA256 = "cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462"

# Produce Routing Configuration
SUPPORTED_FOOD_TYPES = {"avocado", "mango", "banana"}

# API Configuration
PROJECT_NAME = "FreshIQ AI Engine API"
VERSION = "1.0.0"
DESCRIPTION = "Production-grade API for produce ripeness assessment and What-If shelf-life simulation."

# Upload Constraints
MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024  # 10 MB
ALLOWED_MIME_TYPES = {
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/webp"
}

# CORS Settings
CORS_ORIGINS: List[str] = [
    "http://localhost:3000",
    "http://localhost:5173",
    "http://127.0.0.1:3000",
    "http://127.0.0.1:5173",
    "*"
]
