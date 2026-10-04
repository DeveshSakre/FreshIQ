# FreshIQ Multi-Produce API Specification
**Phase 2K.3: Avocado, Mango & Banana Multi-Produce Backend Architecture**

---

## 1. Overview
The FreshIQ backend provides unified API routing for automated produce ripeness classification and shelf-life simulation. Produce routing is handled via the `food_type` parameter in `POST /api/predict`.

The backend acts as the **single source of truth** for all machine learning inference and biophysical simulations.

---

## 2. Produce Support Matrix

| Produce | Food Type Parameter | Visual Classifier | Output Classes | RUL Modeling Available? | Storage Simulation Available? | Extrapolation Method |
|:---|:---:|:---|:---:|:---:|:---:|:---|
| **Avocado** (*Persea americana*, Hass) | `avocado` | MobileNetV3-Small (Phase 1A) | 5 stages (Underripe to Overripe) | **YES** (HistGradientBoosting) | **YES** (10°C, 20°C, Ambient, 4°C Refrigerator) | Arrhenius / $Q_{10} = 2.38$ for 4°C |
| **Mango** (*Mangifera indica*, White Chaunsa Late) | `mango` | MobileNetV3-Large (Phase 2J.1) | 5 stages (Unripe to Perished) | **NO** (Strictly Unavailable) | **NO** (Strictly Unavailable) | None |
| **Banana** (*Musa acuminata*, Cavendish) | `banana` | MobileNetV3-Small (Phase 2K.1/2K.2) | 3 stages (Unripe, Semi-ripe, Ripe) | **NO** (Strictly Unavailable) | **NO** (Strictly Unavailable) | None |

---

## 3. Endpoints

### 3.1 GET `/api/health`
Verifies server health, produce model loading status, and cryptographic SHA-256 checkpoint verification.

**Response Schema (`HealthResponse`):**
```json
{
  "status": "healthy",
  "version": "1.0.0",
  "models_loaded": true,
  "vision_checkpoint_verified": true,
  "rul_model_loaded": true,
  "feature_config_loaded": true,
  "mango_model_loaded": true,
  "mango_checkpoint_verified": true,
  "banana_model_loaded": true,
  "banana_checkpoint_verified": true,
  "supported_produce": [
    "Avocado (Hass)",
    "Mango (White Chaunsa Late)",
    "Banana (Cavendish)"
  ],
  "timestamp": "2026-09-16T17:00:02.711485+00:00"
}
```

---

### 3.2 POST `/api/predict`
Accepts a multipart form upload containing a produce photograph and target produce specification.

**Request Parameters:**
- `file`: Image file (multipart/form-data; JPEG, PNG, or WEBP; maximum size 10 MB). **Required**.
- `food_type`: Target produce identifier string (`"avocado"`, `"mango"`, or `"banana"`). Case-insensitive. If omitted or empty, defaults to `"avocado"` for backwards compatibility.
- `storage_condition`: Optional storage context hint (`"ambient"`, `"10C"`, `"20C"`, `"4C"`). For Avocado, customizes the primary recommendation. For Mango and Banana, this hint is inert and does not alter classifier outputs or trigger shelf-life calculations.

---

## 4. Produce-Specific Behaviors

### 4.1 Avocado (`food_type=avocado`)
- **Model**: MobileNetV3-Small vision backbone + Phase 1B HistGradientBoosting RUL model.
- **Classes**:
  - `1`: Stage 1 — Underripe
  - `2`: Stage 2 — Breaking
  - `3`: Stage 3 — Ripe First Stage
  - `4`: Stage 4 — Ripe Second Stage
  - `5`: Stage 5 — Overripe
- **RUL & Storage Scenarios**:
  - `rul_available`: `true`
  - `scenarios`: Returns complete dictionary of storage conditions:
    - `10C`: Empirical ML prediction (10°C commercial storage)
    - `20C`: Empirical ML prediction (20°C distribution holding)
    - `ambient`: Empirical ML prediction (ambient room temperature)
    - `4C_refrigerator`: Biophysical Arrhenius / $Q_{10}$ kinetic extrapolation with mandatory uncertainty disclaimer.
  - `refrigeration_extension_gain_days`: Numeric shelf life extension gained by chilling over ambient storage.

---

### 4.2 Mango (`food_type=mango`)
- **Model**: MobileNetV3-Large dedicated classifier (Phase 2J.1).
- **Checkpoints**: `freshiq_mobilenetv3_mango_best.pth` (SHA-256: `4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9`).
- **Classes**:
  - `0`: Unripe
  - `1`: Semiripe
  - `2`: Fully Ripe
  - `3`: Overripe
  - `4`: Perished
- **STRICT NO-RUL ENFORCEMENT**:
  - `rul_available`: `false`
  - `rul`: `null`
  - `scenarios`: `null`
  - `refrigeration_extension_gain_days`: `null`
  - `expected_continuous_ripening_stage`: `null`
  - **Reason**: The Mango dataset contains cross-sectional photograph sets without longitudinal biological specimen tracking or temperature telemetry. Scientifically defensible RUL curves and Arrhenius simulations cannot be computed and are strictly prohibited.

---

### 4.3 Banana (`food_type=banana`)
- **Model**: MobileNetV3-Small dedicated classifier (Phase 2K.1 / Phase 2K.2).
- **Checkpoints**: `freshiq_mobilenetv3_banana_best.pth` (SHA-256: `cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462`).
- **Classes**:
  - `0`: Unripe
  - `1`: Semi-ripe
  - `2`: Ripe
- **STRICT NO-RUL ENFORCEMENT**:
  - `rul_available`: `false`
  - `rul`: `null`
  - `scenarios`: `null`
  - `refrigeration_extension_gain_days`: `null`
  - `expected_continuous_ripening_stage`: `null`
  - **Reason**: Banana dataset lacks continuous longitudinal specimen tracking and telemetry under controlled temperature regimes. Shelf-life/RUL prediction and storage simulations are strictly unavailable to prevent biological hallucinations.

---

## 5. Unsupported Produce Behavior

Requests specifying unsupported produce types (e.g., `papaya`, `tomato`, `apple`) will **never** silently fall back to Avocado, Mango, or Banana. The API immediately rejects the request with HTTP status `400 Bad Request`.

**Example Request:**
```bash
curl -X POST "http://localhost:8000/api/predict" \
  -F "file=@tomato_photo.jpg" \
  -F "food_type=tomato"
```

**Response (HTTP 400 Bad Request):**
```json
{
  "detail": "Unsupported food type 'tomato'. Supported produce types: avocado, mango, banana."
}
```

---

## 6. Checkpoint Integrity Assurances

| Model File | Checkpoint Path | Verified SHA-256 Hash | Status |
|:---|:---|:---:|:---:|
| Avocado Phase 1A Vision | `ml/saved_models/freshiq_mobilenetv3_avocado_best.pth` | `8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e` | Frozen & Untouched |
| Avocado Phase 1B RUL | `ml/saved_models/freshiq_shelflife_model.joblib` | Verified on disk | Frozen & Untouched |
| Mango Phase 2J.1 Vision | `ml/saved_models/freshiq_mobilenetv3_mango_best.pth` | `4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9` | Frozen & Untouched |
| Banana Phase 2K.1 Vision | `ml/saved_models/freshiq_mobilenetv3_banana_best.pth` | `cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462` | Frozen & Active |

