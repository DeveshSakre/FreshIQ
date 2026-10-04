from typing import List, Optional
from pydantic import BaseModel, Field


class HealthResponse(BaseModel):
    status: str = Field(..., example="healthy")
    version: str = Field(..., example="1.0.0")
    models_loaded: bool = Field(..., example=True)
    vision_checkpoint_verified: bool = Field(..., example=True)
    rul_model_loaded: bool = Field(..., example=True)
    feature_config_loaded: bool = Field(..., example=True)
    mango_model_loaded: bool = Field(default=True, example=True)
    mango_checkpoint_verified: bool = Field(default=True, example=True)
    banana_model_loaded: bool = Field(default=True, example=True)
    banana_checkpoint_verified: bool = Field(default=True, example=True)
    supported_produce: List[str] = Field(
        default_factory=lambda: ["Avocado (Hass)", "Mango (White Chaunsa Late)", "Banana (Cavendish)"]
    )
    timestamp: str
