# FreshIQ Banana Model Freeze Report (Phase 2K.2)

**Produce:** Banana (*Musa acuminata*)  
**Model Status:** **BANANA_MODEL_STATUS = FROZEN_FOR_INTEGRATION**  
**Freeze Date:** September 16, 2026  
**Freeze Authority:** Antigravity AI Engine (FreshIQ ML Subsystem)  

---

## 1. Freeze Summary
The trained FreshIQ Banana 3-class ripeness classifier has successfully undergone independent cryptographic verification, configuration review, manifest immutability checks, and exact test set reproduction. The model artifact is officially frozen for backend integration.

## 2. Checkpoint Fingerprint & Verification
- **Checkpoint File:** `ml/saved_models/freshiq_mobilenetv3_banana_best.pth`
- **Validated SHA-256 Hash:** `cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462`
- **File Size:** 6,211,079 bytes
- **Verification Status:** **CONFIRMED & MATCHED**

## 3. Dataset & Split Provenance
- **Dataset Source:** Mendeley Data (Version 2, DOI: `10.17632/d5tczj7fs7.2`)
- **Authoritative Manifest:** `ml/datasets/multi_produce/banana/manifests/banana_cluster_split_manifest.csv`
- **Train Split:** 34 clusters (272 images)
- **Validation Split:** 7 clusters (56 images)
- **Held-Out Test Split:** 7 clusters (56 images)
- **Leakage Status:** **0 clusters, 0 clones, and 0 hashes overlap across splits**

## 4. Performance Reproduction Summary

| Benchmark Metric | Phase 2K.1 Value | Reproduced Phase 2K.2 Value | Status |
| :--- | :---: | :---: | :---: |
| **Accuracy** | 73.21% (41/56) | 73.21% (41/56) | **EXACT MATCH** |
| **Macro-F1** | 69.22% | 69.22% | **EXACT MATCH** |
| **Macro Precision** | 80.51% | 80.51% | **EXACT MATCH** |
| **Macro Recall** | 69.05% | 69.05% | **EXACT MATCH** |
| **Balanced Accuracy** | 69.05% | 69.05% | **EXACT MATCH** |
| **Cohen's Kappa** | 0.5789 | 0.5789 | **EXACT MATCH** |
| **Ordinal MAE** | 0.2679 stages | 0.2679 stages | **EXACT MATCH** |
| **Adjacent 1-Stage Errors** | 15 (100.0%) | 15 (100.0%) | **EXACT MATCH** |
| **Non-Adjacent 2-Stage Errors** | 0 (0.0%) | 0 (0.0%) | **EXACT MATCH** |

## 5. Cross-Validation Stability
- **Cross-Validation Scheme:** 5-Fold `GroupKFold` grouped by `cluster_id` on the 34 training clusters.
- **Mean Macro-F1:** 76.89% $\pm$ 4.43%
- **Mean Accuracy:** 80.53% $\pm$ 3.59%
- **Held-Out Test Isolation:** 7 test clusters strictly excluded from CV.

## 6. Output Probability Validity
- Evaluated on test set: Softmax class probabilities are finite, bounded in $[0, 1]$, and sum to $1.0 \pm 10^{-5}$.
- Terminology standard: Documented strictly as *softmax class probability outputs* (uncalibrated).

## 7. Model Selection Independence
- Checkpoint selection utilized validation split Macro-F1 exclusively (Best at Epoch 14: 67.08%).
- Test set was evaluated strictly post-checkpoint freeze.

## 8. Regression Protection Verification
- **Avocado Checkpoint (`freshiq_mobilenetv3_avocado_best.pth`):**
  - Expected: `8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e`
  - Actual:   `8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e`
  - Status:   **UNCHANGED / PASS**
- **Mango Checkpoint (`freshiq_mobilenetv3_mango_best.pth`):**
  - Expected: `4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9`
  - Actual:   `4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9`
  - Status:   **UNCHANGED / PASS**

## 9. Capability Matrix for Production Integration

| Feature / Output | Supported | Integration Policy |
| :--- | :---: | :--- |
| **Ripeness Classification** | **YES** | Return discrete stage: Unripe (0), Semi-ripe (1), Ripe (2) |
| **Class Confidence Probabilities** | **YES** | Return 3-element probability vector |
| **Stage Care Recommendation** | **YES** | Return textual guidance based on predicted stage |
| **Remaining Useful Life (RUL)** | **NO** | Return `null` / explicitly omitted |
| **Shelf-Life Days Prediction** | **NO** | Return `null` / explicitly omitted |
| **Storage Condition Simulation** | **NO** | Disabled |
| **Food Safety Guarantee** | **NO** | Surface visual ripeness classification only |

## 10. Freeze Certification & Next Phase Mandate
- **Freeze Status:** **FROZEN_FOR_INTEGRATION**
- **Next Phase:** Phase 2K.3 (Backend FastAPI Multi-Produce Router Integration)
- **Constraint:** The checkpoint `freshiq_mobilenetv3_banana_best.pth` is frozen and shall not be modified or retrained.
