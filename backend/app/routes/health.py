from datetime import datetime, timezone
from fastapi import APIRouter, Depends

from backend.app.config import VERSION
from backend.app.schemas.health import HealthResponse
from backend.app.services.inference_service import InferenceService, get_inference_service

router = APIRouter(tags=["Health"])


@router.get("/health", response_model=HealthResponse, summary="Check API health and produce model loading status")
async def health_check(
    service: InferenceService = Depends(get_inference_service)
) -> HealthResponse:
    """
    Returns service health status, produce model loading verification, and SHA-256 integrity status
    for Avocado (Phase 1A/1B), Mango (Phase 2J.1), and Banana (Phase 2K.1).
    """
    avocado_ready = service.is_initialized and service.engine is not None
    avocado_verified = service.engine.verify_vision_checkpoint_integrity() if avocado_ready else False
    rul_loaded = service.engine.rul_model is not None if avocado_ready else False
    feat_loaded = service.engine.feature_config is not None if avocado_ready else False

    mango_ready = service.is_initialized and service.mango_engine is not None
    mango_verified = service.mango_engine.verify_checkpoint_integrity() if mango_ready else False

    banana_ready = service.is_initialized and service.banana_engine is not None
    banana_verified = service.banana_engine.verify_checkpoint_integrity() if banana_ready else False

    all_healthy = (
        avocado_ready and avocado_verified and rul_loaded and
        mango_ready and mango_verified and
        banana_ready and banana_verified
    )

    return HealthResponse(
        status="healthy" if all_healthy else "degraded",
        version=VERSION,
        models_loaded=service.is_initialized,
        vision_checkpoint_verified=avocado_verified,
        rul_model_loaded=rul_loaded,
        feature_config_loaded=feat_loaded,
        mango_model_loaded=mango_ready,
        mango_checkpoint_verified=mango_verified,
        banana_model_loaded=banana_ready,
        banana_checkpoint_verified=banana_verified,
        supported_produce=["Avocado (Hass)", "Mango (White Chaunsa Late)", "Banana (Cavendish)"],
        timestamp=datetime.now(timezone.utc).isoformat()
    )
