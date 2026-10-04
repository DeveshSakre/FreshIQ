export interface StageProbabilities {
  stage_1: number;
  stage_2: number;
  stage_3: number;
  stage_4: number;
  stage_5: number;
}

export interface RipenessAssessment {
  predicted_ripening_stage: 1 | 2 | 3 | 4 | 5;
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
    '10C': ScenarioRUL;
    '20C': ScenarioRUL;
    ambient: ScenarioRUL;
    '4C_refrigerator': ScenarioRUL;
    [key: string]: ScenarioRUL;
  };
  refrigeration_extension_gain_days: number;
  actionable_recommendation: string;
  legal_disclaimer: string;
}

export interface HealthResponse {
  status: 'healthy' | 'degraded' | 'offline';
  version: string;
  models_loaded: boolean;
  vision_checkpoint_verified: boolean;
  rul_model_loaded: boolean;
  feature_config_loaded: boolean;
  supported_produce: string[];
  timestamp: string;
}

export interface ScanHistoryItem {
  id: string;
  timestamp: string;
  thumbnailUrl: string;
  result: PredictionResponse;
  storageCondition?: string;
}
