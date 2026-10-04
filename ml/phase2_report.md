# FreshIQ Phase 2: What-If Storage Simulation Engine Report
**Multi-Scenario Shelf-Life Simulation & Biophysical Extrapolation**

---

## 1. Executive Summary & Architecture Overview

Phase 2 builds the core decision-support engine of FreshIQ by integrating the frozen computer-vision classifier (Phase 1A MobileNetV3-Small) with the champion remaining usable life (RUL) regression model (Phase 1B HistGradientBoosting).

The resulting **What-If Storage Simulation Engine** (`ml/simulation_engine.py`) takes an avocado photograph as input and simultaneously simulates four distinct storage conditions:
1. **10°C (Cold Storage chamber)** — *Direct Empirical ML Model*
2. **20°C (Controlled Room Temperature)** — *Direct Empirical ML Model*
3. **Ambient (~20–22°C Room Conditions)** — *Direct Empirical ML Model (20°C Baseline)*
4. **4°C (Domestic Refrigerator)** — *Biophysical Arrhenius / $Q_{10}$ Kinetic Extrapolation*

```
                  +-----------------------------------+
                  |      Input Avocado Photograph     |
                  +-----------------------------------+
                                    |
                                    v
                  +-----------------------------------+
                  |    MobileNetV3-Small (Frozen)     |
                  |    Phase 1A: SHA-256 Verified     |
                  +-----------------------------------+
                                    |
                 p1..p5, expected_stage, confidence
                                    |
                    +---------------+---------------+
                    |                               |
                    v                               v
        [Empirical Scenarios]             [Extrapolated Scenario]
         HistGradientBoosting               Biophysical Kinetics
         (T10, T20, Ambient)                   (4°C Domestic)
                    |                               |
                    +---------------+---------------+
                                    |
                                    v
                  +-----------------------------------+
                  |   What-If Simulation Engine       |
                  |   - RUL for 10°C, 20°C, Amb, 4°C  |
                  |   - Refrigeration Extension Gain  |
                  |   - Actionable Guidance           |
                  |   - Responsible AI Disclaimers    |
                  +-----------------------------------+
```

---

## 2. Integrity of Prior Phases

To guarantee absolute scientific integrity and avoid data leakage or accidental model regression:
- **Phase 1A Checkpoint Integrity:**
  - File: `D:\FreshIQ\ml\saved_models\freshiq_mobilenetv3_avocado_best.pth`
  - Expected & Verified SHA-256: `8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e`
  - Status: **100% Unmodified / Frozen**.
- **Phase 1B Regressor Integrity:**
  - File: `D:\FreshIQ\ml\saved_models\freshiq_shelflife_model.joblib`
  - Model Type: `HistGradientBoostingRegressor`
  - Status: Loaded directly with feature configuration from `shelflife_feature_config.json`.
- **Feature Schema & Ordering:**
  Strictly 9 features in order:
  `['p1', 'p2', 'p3', 'p4', 'p5', 'expected_stage', 'confidence', 'temperature_c', 'is_cold_storage']`

---

## 3. Mathematical Equations & Modeling Methodology

### 3.1 Computer Vision Ripening Classification
For an input image $X$, the network outputs logits $z \in \mathbb{R}^5$. Class probabilities are computed via Softmax:
$$p_k = \frac{e^{z_k}}{\sum_{j=1}^5 e^{z_j}}, \quad k \in \{1, 2, 3, 4, 5\}$$

- **Predicted Stage:**
  $$\hat{y} = \arg\max_{k \in \{1..5\}} (p_k)$$
- **Model Confidence:**
  $$C = \max_{k \in \{1..5\}} (p_k)$$
- **Expected Ripening Stage (Continuous Velocity):**
  $$\hat{S} = \sum_{k=1}^5 k \cdot p_k, \quad \hat{S} \in [1.0, 5.0]$$

---

### 3.2 Empirical Remaining Usable Life (10°C, 20°C, Ambient)
For experimental storage conditions ($T \in \{10^\circ\text{C}, 20^\circ\text{C}\}$):
$$\text{RUL}_{\text{raw}} = f_{\text{HGB}}\left(p_1, p_2, p_3, p_4, p_5, \hat{S}, C, T, \mathbb{I}(T \le 12)\right)$$
$$\text{RUL}_{\text{empirical}} = \max\left(0.0, \text{RUL}_{\text{raw}}\right)$$

Where:
- For **10°C (Cold Storage)**: $T = 10.0$, $\mathbb{I}(T \le 12) = 1$.
- For **20°C (Controlled Room)**: $T = 20.0$, $\mathbb{I}(T \le 12) = 0$.
- For **Ambient (~20–22°C)**: $T = 20.0$, $\mathbb{I}(T \le 12) = 0$.

---

### 3.3 4°C Domestic Refrigerator Biophysical Extrapolation

Because 4°C was **not** present in the empirical imaging study, FreshIQ **does not claim direct learning** for 4°C. Instead, it computes an Arrhenius-based respiration kinetic deceleration:

#### The $Q_{10}$ Temperature Quotient:
$$Q_{10} = \left(\frac{k_{2}}{k_{1}}\right)^{\frac{10}{T_2 - T_1}}$$

Using the empirically observed ripening durations between 20°C (mean transition ~8.5 days) and 10°C (mean transition ~20.2 days):
$$\frac{k_{20}}{k_{10}} \approx \frac{20.2}{8.5} \approx 2.38 \implies Q_{10} = 2.38$$

#### Scaling from Ambient (20°C) to Domestic Refrigerator (4°C):
The deceleration multiplier for $\Delta T = 20^\circ\text{C} - 4^\circ\text{C} = 16^\circ\text{C}$:
$$\text{Multiplier}_{20 \to 4} = (Q_{10})^{\frac{20 - 4}{10}} = (2.38)^{1.6} \approx 3.996 \approx 4.0$$

#### Boundary Clamping & Physiological Limits:
1. **Terminal State Invariance:** If the avocado is already Stage 5 (Overripe), placing it into the refrigerator does **not** restore shelf life:
   $$\text{RUL}_{4^\circ\text{C}} = 0.0 \quad \text{if } \hat{y} = 5$$
2. **Post-Climacteric Chilling Injury Ceiling:** In standard non-controlled atmosphere domestic refrigerators, Hass avocados suffer vascular browning, pulp pitting, and textural breakdown after prolonged cold storage. Therefore, projections are capped at a physiological threshold:
   $$\text{RUL}_{4^\circ\text{C}} = \min\left(21.0, \, \text{RUL}_{20^\circ\text{C}} \times 4.0\right)$$

---

## 4. Strict Separation of Empirical vs Extrapolated Results

Every scenario output in the simulation engine payload includes explicit metadata denoting whether the value was directly learned or mathematically extrapolated:

```json
{
  "10C": {
    "temperature_c": 10.0,
    "estimated_rul_days": 16.2,
    "is_extrapolated": false,
    "method": "Empirical ML Model (HistGradientBoosting)",
    "uncertainty_note": null
  },
  "4C_refrigerator": {
    "temperature_c": 4.0,
    "estimated_rul_days": 21.0,
    "is_extrapolated": true,
    "method": "Biophysical Arrhenius / Q10 Extrapolation",
    "uncertainty_note": "4°C is not an empirically observed training condition in the dataset. This prediction is an AI biophysical kinetic simulation based on avocado respiration slowing (Q10 = 2.38). Shelf life is physiologically bounded by chilling sensitivity."
  }
}
```

---

## 5. Automated Verification & Test Results

An automated test suite was constructed and executed (`tests/test_simulation_engine.py`). All 8 test cases passed completely with zero failures and zero warnings.

| Test Case | Description | Result |
| :--- | :--- | :---: |
| `test_01_phase1a_checkpoint_integrity` | Confirms SHA-256 hash matches `8aaff3e...` exactly | **PASSED** |
| `test_02_phase1b_model_and_config_loading` | Confirms HistGradientBoosting joblib and JSON configs load | **PASSED** |
| `test_03_feature_ordering_exact_match` | Confirms strict 9-feature schema matching `shelflife_feature_config.json` | **PASSED** |
| `test_04_numerical_validity_on_real_images`| Tests real test-set images; asserts $\sum p = 1$, $C \in [0,1]$, $\hat{S} \in [1,5]$ | **PASSED** |
| `test_05_rul_non_negativity_constraint` | Tests all stage boundaries; verifies RUL never drops below 0.0 | **PASSED** |
| `test_06_stage5_terminal_boundary_behavior`| Asserts Stage 5 produce strictly yields $\text{RUL} = 0.0$ days | **PASSED** |
| `test_07_separation_of_empirical_and_extrapolated` | Verifies `is_extrapolated` flag logic across all 4 conditions | **PASSED** |
| `test_08_temperature_shelf_life_ordering` | Verifies $\text{RUL}_{4^\circ\text{C}} \ge \text{RUL}_{10^\circ\text{C}} \ge \text{RUL}_{20^\circ\text{C}}$ on underripe fruit | **PASSED** |

**Test Execution Time:** 2.617 seconds.

---

## 6. End-to-End Prediction Demonstrations on Test Set

The following demonstrations show the exact outputs produced by the engine on real test-split images across all 5 ripening stages:

### Example 1: Stage 1 (Underripe) — Specimen `T10_014` (Day 1)
- **Vision Inference:**
  - Predicted Stage: **Stage 1 — Underripe** (Confidence: 98.6%)
  - Expected Stage: 1.014
- **What-If Simulation:**
  - **Ambient (20°C):** **8.6 days**
  - **10°C Cold Storage:** **20.6 days**
  - **4°C Refrigerator (Extrapolated):** **21.0 days** *(Capped by chilling boundary)*
  - **Extension Gain from Refrigeration:** **+12.4 days**
- **Actionable Guidance:**
  *"Underripe / firm. Estimated remaining usable shelf life: ~8.6 days under ambient conditions. Store at room temperature away from direct sunlight."*

---

### Example 2: Stage 2 (Breaking) — Specimen `T10_014` (Day 8)
- **Vision Inference:**
  - Predicted Stage: **Stage 2 — Breaking** (Confidence: 58.6%)
  - Expected Stage: 1.593
- **What-If Simulation:**
  - **Ambient (20°C):** **6.8 days**
  - **10°C Cold Storage:** **16.2 days**
  - **4°C Refrigerator (Extrapolated):** **21.0 days**
  - **Extension Gain from Refrigeration:** **+14.2 days**
- **Actionable Guidance:**
  *"Breaking ripeness. Transitioning from green to ripe. Estimated remaining shelf life: ~6.8 days under ambient conditions. Keep on counter until dark and yielding."*

---

### Example 3: Stage 4 (Ripe Second Stage — Optimal Eat) — Specimen `T10_014` (Day 19)
- **Vision Inference:**
  - Predicted Stage: **Stage 4 — Ripe Second Stage** (Confidence: 79.0%)
  - Expected Stage: 3.973
- **What-If Simulation:**
  - **Ambient (20°C):** **1.6 days**
  - **10°C Cold Storage:** **3.9 days**
  - **4°C Refrigerator (Extrapolated):** **6.6 days**
  - **Extension Gain from Refrigeration:** **+5.0 days**
- **Actionable Guidance:**
  *"Peak edible ripeness (Consume Soon). Optimal texture and flavor. Consume within 2 day(s). Refrigerate immediately if you need to hold it for an extra day."*

---

### Example 4: Stage 5 (Overripe — Terminal) — Specimen `T10_034` (Day 25)
- **Vision Inference:**
  - Predicted Stage: **Stage 5 — Overripe** (Confidence: 56.4%)
  - Expected Stage: 4.512
- **What-If Simulation:**
  - **Ambient (20°C):** **0.0 days**
  - **10°C Cold Storage:** **0.0 days**
  - **4°C Refrigerator (Extrapolated):** **0.0 days**
  - **Extension Gain from Refrigeration:** **0.0 days**
- **Actionable Guidance:**
  *"Terminal maturity (Overripe). Use immediately for smoothies, dips, or baking if sensory qualities are acceptable. Always inspect for off-odors, mold, or discoloration before consuming."*

---

## 7. Assumptions & Known Limitations

1. **Biophysical $Q_{10}$ Constancy:**
   The Arrhenius / $Q_{10}$ model assumes a constant temperature sensitivity quotient ($Q_{10} \approx 2.38$) between 20°C and 4°C. While standard for climacteric fruit respiration, actual metabolic activity may decelerate more sharply as enzymatic activity approaches 0°C.
2. **Chilling Injury Ceiling:**
   Prolonged storage below 5°C causes internal browning and loss of ripening competence in un-ripened avocados. The 21-day hard limit acts as a protective boundary to prevent misleading claims of indefinite storage.
3. **Humidity & Atmosphere Non-Modeling:**
   Domestic refrigerators vary in relative humidity (RH 40%–80%). Dessication, shriveling, or ethylene accumulation from co-stored produce (e.g., apples, bananas) are not modeled from static images.
4. **Responsible AI Policy:**
   FreshIQ models predict **usable physical shelf life until overripeness**, not food safety or pathogen contamination. Outputs are accompanied by disclaimers instructing users to visually and olfactorily inspect produce.

---

## 8. Saved Artifacts Summary

| Artifact | Location |
| :--- | :--- |
| **Simulation Engine Code** | `D:\FreshIQ\ml\simulation_engine.py` |
| **Automated Verification Suite** | `D:\FreshIQ\tests\test_simulation_engine.py` |
| **Phase 1A Frozen Checkpoint** | `D:\FreshIQ\ml\saved_models\freshiq_mobilenetv3_avocado_best.pth` |
| **Phase 1B Trained Model** | `D:\FreshIQ\ml\saved_models\freshiq_shelflife_model.joblib` |
| **Phase 1B Feature Config** | `D:\FreshIQ\ml\saved_models\shelflife_feature_config.json` |
| **Comprehensive Phase 2 Report** | `D:\FreshIQ\ml\phase2_report.md` |
