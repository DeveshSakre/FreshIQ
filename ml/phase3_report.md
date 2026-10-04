# FreshIQ Phase 3: Backend API Architecture & Integration Report
**Production-Grade FastAPI Backend & What-If Simulation Services**

---

## 1. Backend Architecture & Design

Phase 3 establishes a clean, high-performance, asynchronous REST API powered by **FastAPI**. It exposes produce ripeness assessment and multi-scenario What-If shelf-life simulation without duplicating inference logic or re-training any models.

### Modular Architecture Flow:
```
[Client Request (Multipart Image)]
            │
            ▼
┌──────────────────────────────────────┐
│ FastAPI Application (app/main.py)    │
│  - CORS Middleware                   │
│  - Global Exception Handlers         │
│  - Lifespan Singleton Context        │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│ API Routers (app/routes/)            │
│  - GET  /api/health                  │
│  - POST /api/predict                 │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│ Inference Service (app/services/)    │
│  - Image Validation & Sanitization   │
│  - In-Memory BytesIO Decoding        │
│  - Pydantic Schema Mapping           │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│ What-If Simulation Engine            │
│ (ml/simulation_engine.py)            │
│  - Frozen Phase 1A MobileNetV3-Small │
│  - Trained Phase 1B HistGradBoost    │
│  - Biophysical 4°C Extrapolation     │
└──────────────────────────────────────┘
```

### Key Engineering Principles:
1. **Single Source of Truth:** All computer vision inference and shelf-life modeling logic resides strictly inside `ml/simulation_engine.py`. The API layer acts solely as a contract handler, validator, and response serializer.
2. **Zero-Overhead Lifespan Loading:** The ML models (PyTorch checkpoint + scikit-learn regressor) are loaded **once** at server startup inside FastAPI's `@asynccontextmanager lifespan`. They remain pinned in memory as a thread-safe singleton, resulting in near-instantaneous subsequent request handling (~15–30 ms per inference).
3. **In-Memory Image Stream Processing:** Images are validated and decoded directly in RAM using `io.BytesIO`. No temporary image files are written to disk, preventing filesystem bloat, disk I/O bottlenecks, and concurrency conflicts.
4. **Strict Checkpoint Immutability:** On server startup, the engine computes the SHA-256 hash of the Phase 1A checkpoint and verifies that it exactly matches `8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e`.

---

## 2. API Endpoints Reference

### 2.1 GET `/api/health`
Checks backend operational status, model readiness, and checkpoint integrity.

- **Request:**
  ```http
  GET /api/health HTTP/1.1
  Host: localhost:8000
  ```
- **Response Schema (`HealthResponse`):**
  ```json
  {
    "status": "healthy",
    "version": "1.0.0",
    "models_loaded": true,
    "vision_checkpoint_verified": true,
    "rul_model_loaded": true,
    "feature_config_loaded": true,
    "supported_produce": [
      "Avocado (Hass)"
    ],
    "timestamp": "2026-09-04T11:33:18.588000+00:00"
  }
  ```

---

### 2.2 POST `/api/predict`
Evaluates produce ripeness from an uploaded photo and calculates remaining shelf life across storage scenarios.

- **Request:**
  - **Method:** `POST`
  - **URL:** `/api/predict`
  - **Headers:** `Content-Type: multipart/form-data`
  - **Form Parameters:**
    - `file` *(required)*: Binary image file (`image/jpeg`, `image/png`, or `image/webp`, max 10MB).
    - `storage_condition` *(optional)*: Current condition hint (`"ambient"`, `"10C"`, `"20C"`, `"4C"`).

- **Response Schema (`PredictionResponse`):**
  ```json
  {
    "item_name": "Avocado (Hass)",
    "ripeness": {
      "predicted_ripening_stage": 1,
      "stage_label": "Stage 1 — Underripe",
      "confidence": 0.9861,
      "expected_continuous_ripening_stage": 1.014,
      "probabilities": {
        "stage_1": 0.9861,
        "stage_2": 0.0135,
        "stage_3": 0.0003,
        "stage_4": 0.0001,
        "stage_5": 0.0000
      }
    },
    "scenarios": {
      "10C": {
        "condition": "10C",
        "temperature_c": 10.0,
        "estimated_rul_days": 20.6,
        "is_extrapolated": false,
        "method": "Empirical ML Model (HistGradientBoosting)",
        "uncertainty_note": null,
        "disclaimer": "AI-estimated remaining usable shelf life. Not a food safety guarantee.",
        "recommendation": "Underripe / firm. Estimated remaining usable shelf life: ~20.6 days under 10C conditions. Store at room temperature away from direct sunlight."
      },
      "20C": {
        "condition": "20C",
        "temperature_c": 20.0,
        "estimated_rul_days": 8.6,
        "is_extrapolated": false,
        "method": "Empirical ML Model (HistGradientBoosting)",
        "uncertainty_note": null,
        "disclaimer": "AI-estimated remaining usable shelf life. Not a food safety guarantee.",
        "recommendation": "Underripe / firm. Estimated remaining usable shelf life: ~8.6 days under 20C conditions. Store at room temperature away from direct sunlight."
      },
      "ambient": {
        "condition": "ambient",
        "temperature_c": 20.0,
        "estimated_rul_days": 8.6,
        "is_extrapolated": false,
        "method": "Empirical ML Model (HistGradientBoosting)",
        "uncertainty_note": null,
        "disclaimer": "AI-estimated remaining usable shelf life. Not a food safety guarantee.",
        "recommendation": "Underripe / firm. Estimated remaining usable shelf life: ~8.6 days under ambient conditions. Store at room temperature away from direct sunlight."
      },
      "4C_refrigerator": {
        "condition": "4C_refrigerator",
        "temperature_c": 4.0,
        "estimated_rul_days": 21.0,
        "is_extrapolated": true,
        "method": "Biophysical Arrhenius / Q10 Extrapolation",
        "uncertainty_note": "4°C is not an empirically observed training condition in the dataset. This prediction is an AI biophysical kinetic simulation based on avocado respiration slowing (Q10 = 2.38). Shelf life is physiologically bounded by chilling sensitivity.",
        "disclaimer": "AI-estimated remaining usable shelf life. Not a food safety guarantee.",
        "recommendation": "Underripe / firm. Estimated remaining usable shelf life: ~21.0 days under 4C_refrigerator conditions. Store at room temperature away from direct sunlight."
      }
    },
    "refrigeration_extension_gain_days": 12.4,
    "actionable_recommendation": "Underripe / firm. Estimated remaining usable shelf life: ~8.6 days under ambient conditions. Store at room temperature away from direct sunlight.",
    "legal_disclaimer": "FreshIQ outputs are AI-estimated remaining usable shelf life indicators based on computer vision and biophysical kinetics. FreshIQ does not provide microbiological food-safety guarantees. Consumers must visually inspect produce for mold, rot, or off-odors before consumption."
  }
  ```

---

## 3. Validation and Error Handling Architecture

The backend implements defensive programming for all user inputs:

| Error Condition | HTTP Code | Returned Error Message / Payload |
| :--- | :---: | :--- |
| **Missing Image File** | `422 Unprocessable` | FastAPI default schema validation error indicating `file` field is missing. |
| **Unsupported File Type** | `400 Bad Request` | `"Invalid file type '...'. Supported image formats: JPEG, PNG, WEBP."` |
| **Corrupted Image Bytes** | `400 Bad Request` | `"Uploaded file is corrupted or not a readable image: ..."` |
| **Empty File (0 bytes)** | `400 Bad Request` | `"Uploaded file is empty."` |
| **File Exceeds Max Size** | `413 Payload Too Large` | `"File size exceeds maximum permitted limit of 10.0 MB."` |
| **Model Unavailable** | `503 Unavailable` | `"FreshIQ simulation engine is not initialized."` |

---

## 4. Automated Testing Results

The comprehensive test suite covers unit and integration tests across both the simulation engine and the API endpoints:

- **Full Suite Execution Command:**
  ```powershell
  python -m unittest discover tests -v
  ```

### Test Results Breakdown (17/17 Passed):

| Test File | Test Method | Purpose | Result |
| :--- | :--- | :--- | :---: |
| `test_backend_api` | `test_01_root_endpoint` | Root service discovery metadata | **PASSED** |
| `test_backend_api` | `test_02_health_endpoint` | Health check & SHA-256 verification | **PASSED** |
| `test_backend_api` | `test_03_valid_image_prediction` | Valid photo inference across all scenarios | **PASSED** |
| `test_backend_api` | `test_04_missing_image_file` | Missing file raises HTTP 422 | **PASSED** |
| `test_backend_api` | `test_05_invalid_file_type` | Non-image text file raises HTTP 400 | **PASSED** |
| `test_backend_api` | `test_06_corrupted_image_file` | Broken bytes raise HTTP 400 | **PASSED** |
| `test_backend_api` | `test_07_empty_image_file` | 0-byte file raises HTTP 400 | **PASSED** |
| `test_backend_api` | `test_08_stage5_terminal_behavior` | Stage 5 overripe produce strictly yields RUL = 0.0d | **PASSED** |
| `test_backend_api` | `test_09_storage_condition_hint` | Custom condition hint parameter | **PASSED** |
| `test_simulation_engine` | `test_01_phase1a_checkpoint_integrity` | Checkpoint hash verification (`8aaff3e...`) | **PASSED** |
| `test_simulation_engine` | `test_02_phase1b_model_and_config` | Joblib and JSON loading | **PASSED** |
| `test_simulation_engine` | `test_03_feature_ordering_exact_match` | Schema column order verification | **PASSED** |
| `test_simulation_engine` | `test_04_numerical_validity_real_images`| Real image probability sum $\approx 1$, $C \in [0,1]$ | **PASSED** |
| `test_simulation_engine` | `test_05_rul_non_negativity` | Non-negativity constraint verification | **PASSED** |
| `test_simulation_engine` | `test_06_stage5_terminal_boundary` | Terminal event RUL clamping | **PASSED** |
| `test_simulation_engine` | `test_07_separation_empirical_extrapolated` | Strict metadata separation for 4°C | **PASSED** |
| `test_simulation_engine` | `test_08_temperature_shelf_life_ordering` | $\text{RUL}_{4^\circ\text{C}} \ge \text{RUL}_{10^\circ\text{C}} \ge \text{RUL}_{20^\circ\text{C}}$ | **PASSED** |

**Total Execution Time:** 3.052 seconds.

---

## 5. How to Run the Server

To launch the FreshIQ backend server locally:

```powershell
# From the project root D:\FreshIQ:
uvicorn backend.app.main:app --host 0.0.0.0 --port 8000 --reload
```

Once running:
- **Interactive Swagger UI:** [http://localhost:8000/docs](http://localhost:8000/docs)
- **ReDoc Documentation:** [http://localhost:8000/redoc](http://localhost:8000/redoc)
- **Health Check:** [http://localhost:8000/api/health](http://localhost:8000/api/health)
- **Prediction Endpoint:** `POST http://localhost:8000/api/predict`

---

## 6. TypeScript / React Frontend Integration Example

The integration contract has been drafted in [`backend/client_example.ts`](file:///D:/FreshIQ/backend/client_example.ts). 

### Usage in a React Component:
```typescript
import React, { useState } from 'react';
import { predictProduceRipeness, PredictionResponse } from './client_example';

export const ProduceScanner: React.FC = () => {
  const [result, setResult] = useState<PredictionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    setLoading(true);
    setError(null);
    try {
      const data = await predictProduceRipeness(file);
      setResult(data);
    } catch (err: any) {
      setError(err.message || 'Failed to analyze produce.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-4">
      <input type="file" accept="image/*" onChange={handleUpload} />
      {loading && <p>Analyzing produce ripeness and shelf life...</p>}
      {error && <p className="text-red-500">{error}</p>}
      {result && (
        <div className="mt-4 border rounded p-4">
          <h3>{result.item_name}: {result.ripeness.stage_label}</h3>
          <p>Confidence: {(result.ripeness.confidence * 100).toFixed(1)}%</p>
          <div className="grid grid-cols-3 gap-2 mt-2">
            <div>Ambient RUL: {result.scenarios.ambient.estimated_rul_days} days</div>
            <div>10°C RUL: {result.scenarios['10C'].estimated_rul_days} days</div>
            <div>4°C Refrigerator RUL: {result.scenarios['4C_refrigerator'].estimated_rul_days} days</div>
          </div>
          <p className="mt-2 font-semibold">
            Refrigeration Gain: +{result.refrigeration_extension_gain_days} days
          </p>
          <p className="text-sm text-gray-600 mt-2">{result.actionable_recommendation}</p>
        </div>
      )}
    </div>
  );
};
```

---

## 7. Limitations & Production Notes

1. **GPU Acceleration:** By default, the engine auto-detects CUDA and falls back to CPU. On CPU, inference completes in ~25 ms per image. For high-concurrency production deployments, hosting with a GPU-enabled instance reduces latency further.
2. **Discrete Produce Type:** The current pipeline is trained for Hass Avocados. Future phases will introduce multi-produce routing (e.g. bananas, tomatoes, mangos) by prepending a general produce classification head.
3. **CORS Configuration:** Default CORS allows local frontend ports (`3000`, `5173`) and wildcard for development. Production deployments should restrict origins to designated domain names.
