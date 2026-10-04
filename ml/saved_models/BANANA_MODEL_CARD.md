# FreshIQ Banana Ripeness Classifier — Model Card

## Model Overview
- **Model Name:** `FreshIQ_MobileNetV3_Banana`
- **Task:** 3-Class Ripeness Classification
- **Target Produce:** Banana (*Musa acuminata*)
- **Status:** `FROZEN_FOR_INTEGRATION` (Validated Experimental Ripeness Classifier)
- **Architecture:** `MobileNetV3-Small` (ImageNet Pretrained Initialization)
- **Parameter Count:** 1,520,931 parameters
- **Input Geometry:** $3 \times 224 \times 224$ RGB, normalized via standard ImageNet parameters

## Checkpoint & Cryptographic Fingerprint
- **Checkpoint Path:** `ml/saved_models/freshiq_mobilenetv3_banana_best.pth`
- **Checkpoint SHA-256:** `cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462`
- **Checkpoint Size:** 6,211,079 bytes

## Target Classes
1. **Class 0 (Unripe):** Days 0–2 (Peel green and firm; pre-climacteric)
2. **Class 1 (Semi-ripe):** Days 3–5 (Peel yellow with green tips/shoulders; climacteric transition)
3. **Class 2 (Ripe):** Days 6–7 (Peel full yellow with incipient brown spots; peak consumption ripeness)

*Ground Truth Note:* Class labels derive directly from the Mendeley Data dataset authors' published temporal guidelines. They are not independently verified biochemical titrations (Brix/acid).

## Performance Summary

### Held-Out Test Evaluation (7 Clusters, 56 Images)
- **Accuracy:** **73.21%** (41 / 56 correct)
- **Macro-F1:** **69.22%**
- **Macro Precision:** **80.51%**
- **Macro Recall:** **69.05%**
- **Balanced Accuracy:** **69.05%**
- **Cohen's Kappa:** **0.5789**
- **Ordinal MAE:** **0.2679 stages**

### Confusion Matrix & Error Profile
```
                     Predicted Unripe   Predicted Semi-ripe   Predicted Ripe
Actual Unripe (21)         20                    1                  0
Actual Semi-ripe (21)       5                   16                  0
Actual Ripe (14)            0                    9                  5
```
- **Adjacent 1-Stage Errors:** **15 / 15** (**100.00%** of all errors)
- **Non-Adjacent 2-Stage Errors:** **0 / 15** (**0.00%**)
- Zero catastrophic failures occurred between Unripe and Ripe.

### 5-Fold GroupKFold Cross-Validation (34 Train Clusters)
- **Mean Macro-F1:** **76.89%** $\pm$ **4.43%**
- **Mean Accuracy:** **80.53%** $\pm$ **3.59%**

## Production Capabilities

| Capability | Status | Notes |
| :--- | :---: | :--- |
| **Image Ripeness Classification** | **SUPPORTED** | Discrete 3-class visual prediction |
| **Softmax Class Probabilities** | **SUPPORTED** | Confidence vector summing to 1.0 |
| **Remaining Useful Life (RUL)** | **NOT SUPPORTED** | Strictly unapproved & disabled |
| **Shelf-Life Prediction (Days)** | **NOT SUPPORTED** | Strictly unapproved & disabled |
| **Storage Temperature What-If** | **NOT SUPPORTED** | No kinetic telemetry |
| **Food Safety Determination** | **NOT SUPPORTED** | Not a food-safety guarantee |

## Documented Scientific Limitations
1. **Biological Sampling:** Exactly 48 biological sequence clusters are available from the source dataset.
2. **Substantial Duplicate Redundancy:** The source dataset contained 50.0% exact duplicates that required biological sequence clustering to resolve.
3. **Inferred Trajectories:** Specimen identity is inferred from prepared biological sequence clusters and cannot be independently proven beyond audit evidence.
4. **Author-Derived Labels:** Ripeness categories represent author observation day bins rather than laboratory chemical endpoints.
5. **No Temperature/RH Telemetry:** Zero environmental logs exist in the source dataset.
6. **No Banana RUL Support:** The model does NOT predict shelf life or remaining days.
7. **Not a Food-Safety Tool:** The model cannot detect bacterial spoilage, internal fungal rot, or chemical toxins.
8. **Cultivar Specificity:** Dataset photos represent Cavendish/Robusta bananas in a single Indian laboratory.
9. **No Universal Generalization:** High test accuracy does not guarantee performance across all banana varieties, consumer smartphones, or grocery retail lighting.
10. **Pre-Production Experimental Model:** Banana is an experimental classifier frozen for backend integration testing; it is NOT certified for commercial production.
