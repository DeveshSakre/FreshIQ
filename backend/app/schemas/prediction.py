from typing import Any, Dict, Optional, Union
from pydantic import BaseModel, Field


class StageProbabilities(BaseModel):
    stage_1: float = Field(..., description="Stage 1 — Underripe probability")
    stage_2: float = Field(..., description="Stage 2 — Breaking probability")
    stage_3: float = Field(..., description="Stage 3 — Ripe First Stage probability")
    stage_4: float = Field(..., description="Stage 4 — Ripe Second Stage probability")
    stage_5: float = Field(..., description="Stage 5 — Overripe probability")


class RipenessAssessment(BaseModel):
    predicted_ripening_stage: int = Field(..., ge=0, le=5, description="Predicted discrete stage (0-4 for Mango, 1-5 for Avocado, 0-2 for Banana)")
    stage_label: str = Field(..., description="Human-readable stage name")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Top class probability")
    expected_continuous_ripening_stage: Optional[float] = Field(None, ge=0.0, le=5.0, description="Continuous weighted ripening stage if applicable (null for Mango and Banana)")
    probabilities: Dict[str, float] = Field(..., description="Per-class probability distribution")


class ScenarioRUL(BaseModel):
    condition: str = Field(..., description="Storage scenario label")
    temperature_c: float = Field(..., description="Temperature in Celsius")
    estimated_rul_days: float = Field(..., ge=0.0, description="AI-estimated remaining usable shelf life in days")
    is_extrapolated: bool = Field(..., description="True if mathematically extrapolated beyond empirical dataset")
    method: str = Field(..., description="Inference method description")
    uncertainty_note: Optional[str] = Field(None, description="Uncertainty disclaimer if extrapolated")
    disclaimer: str = Field(..., description="Food safety disclaimer")
    recommendation: str = Field(..., description="Actionable management recommendation")


class PredictionResponse(BaseModel):
    food_type: str = Field(default="avocado", description="Produce identifier ('avocado', 'mango', or 'banana')")
    item_name: str = Field(default="Avocado (Hass)", description="Display produce item name and variety")
    model_id: str = Field(default="FreshIQ_MobileNetV3_Avocado", description="Model architecture/identifier")
    ripeness: RipenessAssessment
    rul_available: bool = Field(default=True, description="Whether Remaining Useful Life (RUL) modeling is supported for this produce")
    rul: Optional[Any] = Field(default=None, description="Explicit RUL object; null when rul_available is false")
    scenarios: Optional[Dict[str, ScenarioRUL]] = Field(default=None, description="Storage scenarios if RUL is supported (null for Mango and Banana)")
    refrigeration_extension_gain_days: Optional[float] = Field(default=None, description="Shelf life gained by refrigeration over ambient (null for Mango and Banana)")
    actionable_recommendation: str = Field(..., description="Primary recommendation for current state")
    legal_disclaimer: str = Field(..., description="Mandatory responsible AI disclaimer")
