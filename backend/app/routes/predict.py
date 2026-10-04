from typing import Optional
import logging
from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status

from backend.app.config import SUPPORTED_FOOD_TYPES
from backend.app.schemas.prediction import PredictionResponse
from backend.app.services.inference_service import InferenceService, get_inference_service

logger = logging.getLogger("FreshIQ.Route.Predict")

router = APIRouter(tags=["Prediction"])


@router.post(
    "/predict",
    response_model=PredictionResponse,
    status_code=status.HTTP_200_OK,
    summary="Predict produce ripeness stage and remaining shelf life (if supported)",
    response_description="Detailed assessment of ripeness, class probabilities, and shelf life / storage recommendations."
)
async def predict_produce(
    file: UploadFile = File(..., description="Produce image file (JPEG, PNG, or WEBP, max 10MB)"),
    food_type: Optional[str] = Form(
        None,
        description="Target produce type: 'avocado', 'mango', or 'banana'. Defaults to 'avocado' for backwards compatibility."
    ),
    storage_condition: Optional[str] = Form(
        None,
        description="Optional current storage condition hint ('ambient', '10C', '20C', '4C')"
    ),
    service: InferenceService = Depends(get_inference_service)
) -> PredictionResponse:
    """
    Accepts an uploaded produce photograph and routes to the dedicated ML model:
      - Avocado: 5-stage ripeness classification + RUL simulation (10°C, 20°C, Ambient, 4°C extrapolation)
      - Mango: 5-stage ripeness classification ONLY (Unripe, Semiripe, Fully Ripe, Overripe, Perished; NO RUL)
      - Banana: 3-stage ripeness classification ONLY (Unripe, Semi-ripe, Ripe; NO RUL)
    
    Unsupported produce types (e.g. 'tomato') return a controlled 400 Bad Request error.
    """
    # 1. Produce type validation and normalization
    if food_type is None or food_type.strip() == "":
        norm_food_type = "avocado"
    else:
        norm_food_type = food_type.strip().lower()

    if norm_food_type not in SUPPORTED_FOOD_TYPES:
        supported_str = ", ".join(sorted(SUPPORTED_FOOD_TYPES))
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Unsupported food type '{food_type}'. Supported produce types: {supported_str}."
        )

    # 2. Validate, decode and load image into memory
    pil_image = await service.validate_and_load_image(file)

    # 3. Route inference by produce type
    if norm_food_type == "avocado":
        prediction = service.run_avocado_prediction(pil_image)

        # Storage condition hint customization for Avocado What-If scenarios
        if storage_condition and prediction.scenarios:
            norm_cond = storage_condition.strip().lower().replace("°", "")
            if "10" in norm_cond and "10C" in prediction.scenarios:
                prediction.actionable_recommendation = prediction.scenarios["10C"].recommendation
            elif "4" in norm_cond and "4C_refrigerator" in prediction.scenarios:
                prediction.actionable_recommendation = prediction.scenarios["4C_refrigerator"].recommendation
            elif "20" in norm_cond and "20C" in prediction.scenarios:
                prediction.actionable_recommendation = prediction.scenarios["20C"].recommendation

    elif norm_food_type == "mango":
        prediction = service.run_mango_prediction(pil_image)
        # Note: For Mango, image-only classification is performed.
        # storage_condition does NOT alter stage, confidence, probabilities, or fabricate RUL.

    elif norm_food_type == "banana":
        prediction = service.run_banana_prediction(pil_image)
        # Note: For Banana, image-only classification is performed.
        # storage_condition does NOT alter stage, confidence, probabilities, or fabricate RUL.

    else:
        # Safeguard fallback (unreachable due to SUPPORTED_FOOD_TYPES check)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Produce type '{norm_food_type}' is not currently supported."
        )

    return prediction
