import { HealthResponse, PredictionResponse } from '../types/prediction';

// Configurable base URL: falls back to relative '/api' which works via Vite dev proxy or production reverse proxy
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export class ApiError extends Error {
  status: number;
  detail?: string;

  constructor(message: string, status: number, detail?: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.detail = detail;
  }
}

/**
 * Checks API server health, model loading status, and Phase 1A SHA-256 verification.
 */
export async function checkHealth(): Promise<HealthResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/health`);
    if (!response.ok) {
      throw new ApiError(`Health check failed with HTTP ${response.status}`, response.status);
    }
    return await response.json();
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError('Unable to connect to FreshIQ backend API server.', 0);
  }
}

/**
 * Uploads an avocado photograph to the FreshIQ AI pipeline for ripeness assessment
 * and multi-scenario What-If shelf life simulation.
 * 
 * @param file The image File or Blob object to analyze.
 * @param storageCondition Optional storage condition hint ('ambient', '10C', '20C', '4C').
 */
export async function predictProduce(
  file: File | Blob,
  storageCondition?: string
): Promise<PredictionResponse> {
  const formData = new FormData();
  formData.append('file', file, (file as File).name || 'produce_scan.jpg');

  if (storageCondition) {
    formData.append('storage_condition', storageCondition);
  }

  try {
    const response = await fetch(`${API_BASE_URL}/predict`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      let detail = 'Prediction request failed';
      try {
        const errorJson = await response.json();
        detail = errorJson.detail || detail;
      } catch {
        // Response wasn't JSON
      }
      throw new ApiError(detail, response.status, detail);
    }

    return await response.json();
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new ApiError('Network error occurred during produce prediction.', 0);
  }
}
