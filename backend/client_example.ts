/**
 * FreshIQ API Client Integration Example (TypeScript / React)
 * 
 * Demonstrates type-safe API communication with POST /api/predict
 * and GET /api/health.
 */

export interface StageProbabilities {
  stage_1: number;
  stage_2: number;
  stage_3: number;
  stage_4: number;
  stage_5: number;
}

export interface RipenessAssessment {
  predicted_ripening_stage: number;
  stage_label: string;
  confidence: number;
  expected_continuous_ripening_stage: number;
  probabilities: StageProbabilities;
}

export interface ScenarioRUL {
  condition: string;
  temperature_c: number;
  estimated_rul_days: number;
  is_extrapolated: boolean;
  method: string;
  uncertainty_note: string | null;
  disclaimer: string;
  recommendation: string;
}

export interface PredictionResponse {
  item_name: string;
  ripeness: RipenessAssessment;
  scenarios: {
    "10C": ScenarioRUL;
    "20C": ScenarioRUL;
    "ambient": ScenarioRUL;
    "4C_refrigerator": ScenarioRUL;
  };
  refrigeration_extension_gain_days: number;
  actionable_recommendation: string;
  legal_disclaimer: string;
}

export interface HealthResponse {
  status: string;
  version: string;
  models_loaded: boolean;
  vision_checkpoint_verified: boolean;
  rul_model_loaded: boolean;
  feature_config_loaded: boolean;
  supported_produce: string[];
  timestamp: string;
}

const API_BASE_URL = "http://localhost:8000/api";

/**
 * Checks API server health and model verification status.
 */
export async function checkFreshIQHealth(): Promise<HealthResponse> {
  const response = await fetch(`${API_BASE_URL}/health`);
  if (!response.ok) {
    throw new Error(`Health check failed with status: ${response.status}`);
  }
  return response.json();
}

/**
 * Uploads a produce photo to FreshIQ for ripeness assessment and shelf-life simulation.
 * 
 * @param imageFile The File object obtained from an <input type="file"> or camera capture.
 * @param storageCondition Optional storage hint ('ambient', '10C', '20C', '4C')
 */
export async function predictProduceRipeness(
  imageFile: File,
  storageCondition?: string
): Promise<PredictionResponse> {
  const formData = new FormData();
  formData.append("file", imageFile);

  if (storageCondition) {
    formData.append("storage_condition", storageCondition);
  }

  const response = await fetch(`${API_BASE_URL}/predict`, {
    method: "POST",
    body: formData,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ detail: "Unknown server error" }));
    throw new Error(errorData.detail || `Prediction failed with status: ${response.status}`);
  }

  return response.json();
}
