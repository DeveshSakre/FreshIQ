import io
import logging
from typing import Optional
from PIL import Image, UnidentifiedImageError
from fastapi import HTTPException, UploadFile, status

from ml.simulation_engine import WhatIfSimulationEngine
from ml.mango_classifier import MangoClassifierEngine
from ml.banana_classifier import BananaClassifierEngine
from backend.app.config import (
    VISION_CHECKPOINT_PATH,
    RUL_MODEL_PATH,
    FEATURE_CONFIG_PATH,
    MANGO_CHECKPOINT_PATH,
    MANGO_CONFIG_PATH,
    BANANA_CHECKPOINT_PATH,
    BANANA_CONFIG_PATH,
    MAX_FILE_SIZE_BYTES,
    ALLOWED_MIME_TYPES
)
from backend.app.schemas.prediction import (
    PredictionResponse,
    RipenessAssessment,
    ScenarioRUL
)

logger = logging.getLogger("FreshIQ.InferenceService")


class InferenceService:
    """
    Singleton multi-produce inference service.
    Encapsulates:
      1. Avocado What-If Simulation Engine (MobileNetV3-Small + RUL + Arrhenius extrapolation)
      2. Mango Ripeness Classifier Engine (MobileNetV3-Large 5-class classification only)
      3. Banana Ripeness Classifier Engine (MobileNetV3-Small 3-class classification only)
    """
    _instance: Optional["InferenceService"] = None

    def __init__(self):
        self.engine: Optional[WhatIfSimulationEngine] = None  # Avocado engine
        self.mango_engine: Optional[MangoClassifierEngine] = None  # Mango engine
        self.banana_engine: Optional[BananaClassifierEngine] = None  # Banana engine
        self.is_initialized: bool = False

    @classmethod
    def get_instance(cls) -> "InferenceService":
        if cls._instance is None:
            cls._instance = InferenceService()
        return cls._instance

    def initialize(self):
        """Loads Avocado, Mango, and Banana production models once at server startup."""
        if self.is_initialized:
            return

        try:
            logger.info("Loading Avocado Phase 1A/1B simulation engine...")
            self.engine = WhatIfSimulationEngine(
                vision_ckpt_path=VISION_CHECKPOINT_PATH,
                rul_model_path=RUL_MODEL_PATH,
                feature_config_path=FEATURE_CONFIG_PATH
            )

            logger.info("Loading Mango Phase 2J.1 dedicated classifier engine...")
            self.mango_engine = MangoClassifierEngine(
                checkpoint_path=MANGO_CHECKPOINT_PATH,
                config_path=MANGO_CONFIG_PATH
            )

            logger.info("Loading Banana Phase 2K.1 dedicated classifier engine...")
            self.banana_engine = BananaClassifierEngine(
                checkpoint_path=BANANA_CHECKPOINT_PATH,
                config_path=BANANA_CONFIG_PATH
            )

            self.is_initialized = True
            logger.info("All FreshIQ produce models loaded and verified successfully.")
        except Exception as e:
            self.is_initialized = False
            logger.error(f"Failed to initialize produce models: {str(e)}", exc_info=True)
            raise RuntimeError(f"Failed to initialize FreshIQ models: {str(e)}") from e

    async def validate_and_load_image(self, file: UploadFile) -> Image.Image:
        """
        Validates content-type, size, and image integrity.
        Reads file into memory and returns PIL Image RGB object.
        """
        # 1. Content-Type Header check
        content_type = file.content_type.lower() if file.content_type else ""
        filename = file.filename.lower() if file.filename else ""

        is_valid_ext = any(filename.endswith(ext) for ext in [".jpg", ".jpeg", ".png", ".webp"])
        if content_type not in ALLOWED_MIME_TYPES and not is_valid_ext:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Invalid file type '{content_type}'. Supported image formats: JPEG, PNG, WEBP."
            )

        # 2. Read bytes into memory
        contents = await file.read()
        if not contents or len(contents) == 0:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Uploaded file is empty."
            )

        # 3. File size check
        if len(contents) > MAX_FILE_SIZE_BYTES:
            max_mb = MAX_FILE_SIZE_BYTES / (1024 * 1024)
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail=f"File size exceeds maximum permitted limit of {max_mb:.1f} MB."
            )

        # 4. Image decoding & format verification
        try:
            byte_stream = io.BytesIO(contents)
            image = Image.open(byte_stream)
            image.verify()  # Verifies file integrity
            # Re-open after verify (PIL requirement) and convert to RGB
            byte_stream.seek(0)
            image = Image.open(byte_stream).convert("RGB")
        except (UnidentifiedImageError, OSError, ValueError) as e:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Uploaded file is corrupted or not a readable image: {str(e)}"
            )

        return image

    def run_avocado_prediction(self, image: Image.Image) -> PredictionResponse:
        """
        Runs complete Avocado ripeness assessment and multi-scenario What-If simulation.
        Returns validated Pydantic PredictionResponse.
        """
        if not self.is_initialized or self.engine is None:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="FreshIQ Avocado simulation engine is not initialized."
            )

        raw_result = self.engine.simulate_all_scenarios(image)

        rip = raw_result["ripeness_assessment"]
        probs = rip["probabilities"]

        probabilities = {
            "stage_1": float(probs["stage_1"]),
            "stage_2": float(probs["stage_2"]),
            "stage_3": float(probs["stage_3"]),
            "stage_4": float(probs["stage_4"]),
            "stage_5": float(probs["stage_5"])
        }

        ripeness = RipenessAssessment(
            predicted_ripening_stage=rip["predicted_stage"],
            stage_label=rip["predicted_stage_label"],
            confidence=rip["confidence"],
            expected_continuous_ripening_stage=rip["expected_stage"],
            probabilities=probabilities
        )

        scenarios_dict = {}
        for cond_key, sc in raw_result["what_if_scenarios"].items():
            scenarios_dict[cond_key] = ScenarioRUL(
                condition=sc["condition"],
                temperature_c=sc["temperature_c"],
                estimated_rul_days=sc["estimated_rul_days"],
                is_extrapolated=sc["is_extrapolated"],
                method=sc["method"],
                uncertainty_note=sc["uncertainty_note"],
                disclaimer=sc["disclaimer"],
                recommendation=sc["recommendation"]
            )

        return PredictionResponse(
            food_type="avocado",
            item_name=raw_result["item_name"],
            model_id="FreshIQ_MobileNetV3_Avocado",
            ripeness=ripeness,
            rul_available=True,
            rul=None,
            scenarios=scenarios_dict,
            refrigeration_extension_gain_days=raw_result["refrigeration_extension_gain_days"],
            actionable_recommendation=raw_result["primary_recommendation"],
            legal_disclaimer=raw_result["legal_disclaimer"]
        )

    def run_mango_prediction(self, image: Image.Image) -> PredictionResponse:
        """
        Runs dedicated Mango 5-class ripeness classification ONLY.
        NO RUL, NO days remaining, NO storage-condition simulations.
        """
        if not self.is_initialized or self.mango_engine is None:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="FreshIQ Mango classifier engine is not initialized."
            )

        raw_result = self.mango_engine.predict(image)

        ripeness = RipenessAssessment(
            predicted_ripening_stage=raw_result["predicted_stage"],
            stage_label=raw_result["stage_label"],
            confidence=raw_result["confidence"],
            expected_continuous_ripening_stage=None,
            probabilities=raw_result["probabilities"]
        )

        return PredictionResponse(
            food_type="mango",
            item_name=raw_result["item_name"],
            model_id=raw_result["model_id"],
            ripeness=ripeness,
            rul_available=False,
            rul=None,
            scenarios=None,
            refrigeration_extension_gain_days=None,
            actionable_recommendation=raw_result["actionable_recommendation"],
            legal_disclaimer=raw_result["legal_disclaimer"]
        )

    def run_banana_prediction(self, image: Image.Image) -> PredictionResponse:
        """
        Runs dedicated Banana 3-class ripeness classification ONLY.
        NO RUL, NO days remaining, NO storage-condition simulations.
        """
        if not self.is_initialized or self.banana_engine is None:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="FreshIQ Banana classifier engine is not initialized."
            )

        raw_result = self.banana_engine.predict(image)

        ripeness = RipenessAssessment(
            predicted_ripening_stage=raw_result["predicted_stage"],
            stage_label=raw_result["stage_label"],
            confidence=raw_result["confidence"],
            expected_continuous_ripening_stage=None,
            probabilities=raw_result["probabilities"]
        )

        return PredictionResponse(
            food_type="banana",
            item_name=raw_result["item_name"],
            model_id=raw_result["model_id"],
            ripeness=ripeness,
            rul_available=False,
            rul=None,
            scenarios=None,
            refrigeration_extension_gain_days=None,
            actionable_recommendation=raw_result["actionable_recommendation"],
            legal_disclaimer=raw_result["legal_disclaimer"]
        )

    def run_prediction(self, image: Image.Image) -> PredictionResponse:
        """Backwards compatibility alias for Avocado prediction."""
        return self.run_avocado_prediction(image)


def get_inference_service() -> InferenceService:
    return InferenceService.get_instance()
