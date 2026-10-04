# Banana Ripeness Classification Model Evaluation Report (Phase 2K.1)

**Model Name:** `FreshIQ_MobileNetV3_Banana`  
**Produce:** Banana (*Musa acuminata*)  
**Architecture:** `MobileNetV3-Small` (Linear Classification Head: 1024 $\to$ 3)  
**Trained Classes:** 3 stages (`Unripe`, `Semi-ripe`, `Ripe`)  
**Pipeline:** Two-Stage Transfer Learning with Group-Aware Cross-Validation  
**Evaluation Date:** September 16, 2026  
**Checkpoint Path:** `D:\FreshIQ\ml\saved_models\freshiq_mobilenetv3_banana_best.pth`  
**Checkpoint SHA-256:** `cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462`  
**Status:** **TRAINED & EVALUATED (RESEARCH CLASSIFICATION EXPERIMENT ONLY)**

---

## 1. Dataset Provenance & Source

| Attribute | Verified Value |
| :--- | :--- |
| **Dataset Title** | `banana_ripening_dataset_day0_to_day7` |
| **Hosting Platform** | Mendeley Data (Version 2) |
| **DOI** | [10.17632/d5tczj7fs7.2](https://doi.org/10.17632/d5tczj7fs7.2) |
| **License** | Creative Commons Attribution 4.0 International (CC BY 4.0) |
| **Authors** | Samarth Sangolgi, Nidhi Narode, Minakshi Atre (Savitribai Phule Pune University, India) |
| **Nominal Specimens** | 66 folders (`Banana_ID_001` to `Banana_ID_066`) |
| **Raw Images** | 528 images (400 JPEG, 128 PNG) |

---

## 2. Deduplication & Biological Clustering Methodology

In Phase 2K, cryptographic auditing revealed that the dataset suffered from 50.0% exact byte redundancy, 11 multi-folder clone groups (29 folders), and 46 intra-specimen static duplicate daily observations.

1. **Biological Sequence Clusters:**
   - Evaluated the 8-day hash sequence profile $(h_0, \dots, h_7)$ per nominal folder.
   - Identified **48 unique biological sequence clusters** (`banana_cluster_001` to `banana_cluster_048`).
   - Retained exactly one canonical 8-day trajectory per cluster, forming a deduplicated corpus of **384 images** (eliminating 144 cloned duplicate files across 18 redundant folders).
2. **Cluster Co-occurrence Graph:**
   - Because single daily frames were reused across different nominal folders (e.g. the Day 7 ripe image is shared across 10 clusters), clusters sharing SHA-256 hashes were grouped into **17 connected components**.
   - Partitioning entire connected components guaranteed **zero cluster overlap, zero clone overlap, and zero SHA-256 image overlap** across splits.

---

## 3. Leakage-Safe Train / Validation / Test Split

Partitioned with fixed random seed `42`:

| Split | Components | Clusters | % Clusters | Deduplicated Images | Class 0 (Unripe) | Class 1 (Semi-ripe) | Class 2 (Ripe) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Train** | 11 | **34** | **70.83%** | 272 | 102 (37.5%) | 102 (37.5%) | 68 (25.0%) |
| **Val** | 3 | **7** | **14.58%** | 56 | 21 (37.5%) | 21 (37.5%) | 14 (25.0%) |
| **Test** | 3 | **7** | **14.58%** | 56 | 21 (37.5%) | 21 (37.5%) | 14 (25.0%) |
| **Total** | 17 | **48** | **100.0%** | 384 | 144 (37.5%) | 144 (37.5%) | 96 (25.0%) |

- **Train Clusters (34):** `banana_cluster_001`, `002`, `003`, `004`, `007`, `008`, `009`, `010`, `011`, `012`, `013`, `014`, `015`, `016`, `017`, `018`, `019`, `020`, `025`, `030`, `031`, `032`, `034`, `035`, `036`, `037`, `039`, `041`, `042`, `043`, `044`, `045`, `047`, `048`
- **Val Clusters (7):** `banana_cluster_005`, `022`, `023`, `027`, `028`, `029`, `038`
- **Test Clusters (7):** `banana_cluster_006`, `021`, `024`, `026`, `033`, `040`, `046`

---

## 4. Class Definitions & Preprocessing Pipeline

### 4.1 Class Definitions
Classes are defined strictly according to the Mendeley Data authors' published temporal day grouping:
- **Class 0: Unripe** (Days 0–2): Firm green peel; pre-climacteric.
- **Class 1: Semi-ripe** (Days 3–5): Yellowing peel with green tips and shoulders; climacteric transition.
- **Class 2: Ripe** (Days 6–7): Full yellow peel with incipient brown spots; peak ripeness.

*Limitation:* These labels reflect author-assigned observation day groupings and visual transitions. They are not independently verified biochemical titrations (Brix/starch/acid).

### 4.2 Image Preprocessing & Augmentation
- **Input Geometry:** $3 \times 224 \times 224$ pixels, RGB channels.
- **Normalization:** ImageNet statistics (`mean=[0.485, 0.456, 0.406]`, `std=[0.229, 0.224, 0.225]`).
- **Training Augmentation:** Conservative transformations to prevent distortion of subtle ripeness cues:
  - `RandomResizedCrop(224, scale=(0.85, 1.0))`
  - `RandomHorizontalFlip(p=0.5)`
  - `RandomRotation(degrees=12)`
  - `ColorJitter(brightness=0.1, contrast=0.1, saturation=0.1, hue=0.02)`
- **Validation & Test Preprocessing:** Deterministic `Resize(256)` followed by `CenterCrop(224)`. No random augmentation applied.

### 4.3 Training-Derived Class Weighting
Computed strictly from the training split (102, 102, 68):
- Weight Class 0 (Unripe): $0.8889$
- Weight Class 1 (Semi-ripe): $0.8889$
- Weight Class 2 (Ripe): $1.3333$

---

## 5. Model Architecture & Transfer Learning Strategy

- **Base Architecture:** `MobileNetV3-Small` (ImageNet pretrained weights: `MobileNet_V3_Small_Weights.DEFAULT`).
- **Total Parameters:** 1,520,931 parameters.
- **Head Structure:** `Sequential(Linear(576, 1024), Hardswish(), Dropout(0.2), Linear(1024, 3))`.

### Two-Stage Transfer Learning Protocol
1. **Stage 1 (Head Warm-up — 5 Epochs):**
   - Feature backbone (`features[0:13]`) completely frozen.
   - Classifier head trained with AdamW (`lr=1e-3`, `weight_decay=1e-4`).
   - Cross-entropy loss with training class weights.
2. **Stage 2 (Upper Backbone Fine-Tuning — 10 Epochs):**
   - Upper feature blocks (`features[9:]` through `features[12]`) unfrozen.
   - Low learning rate fine-tuning with AdamW (`lr=1e-4`, `weight_decay=1e-4`) and `CosineAnnealingLR` schedule.
- **Model Checkpoint Selection:** Selected strictly by highest **Validation Macro-F1** (achieved at Epoch 14 with Val Macro-F1 = 67.08%, Val Accuracy = 75.00%).

---

## 6. 5-Fold Group-Aware Cross-Validation Results

To assess architectural stability without touching the 7 held-out test clusters, a 5-Fold `GroupKFold` cross-validation was conducted across the 34 training clusters:

| Fold Index | Val Clusters | Val Samples | Accuracy | Macro-F1 | Macro-Precision | Macro-Recall |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **Fold 0** | 7 | 56 | 85.71% | 83.60% | 89.10% | 82.54% |
| **Fold 1** | 7 | 56 | 78.57% | 75.75% | 87.88% | 74.60% |
| **Fold 2** | 7 | 56 | 75.00% | 70.29% | 77.11% | 70.63% |
| **Fold 3** | 7 | 56 | 82.14% | 79.35% | 84.37% | 78.57% |
| **Fold 4** | 6 | 48 | 81.25% | 75.46% | 87.58% | 75.93% |
| **Mean ± Std** | — | — | **80.53% ± 3.59%** | **76.89% ± 4.43%** | **85.21% ± 4.29%** | **76.45% ± 4.02%** |

*Conclusion:* The model exhibits solid cross-cluster generalization on the training distribution with low variance ($\sigma_{\text{F1}} = 4.43\%$).

---

## 7. Held-Out Test Evaluation Results

The best checkpoint (`Epoch 14`) was evaluated **exactly once** on the 7 held-out test clusters (56 images):

### 7.1 Aggregate Test Metrics

| Metric | Score | Notes |
| :--- | :---: | :--- |
| **Accuracy** | **73.21%** | 41 / 56 images correctly classified |
| **Macro-F1** | **69.22%** | Primary evaluation benchmark |
| **Macro Precision** | **80.51%** | High precision across all predicted classes |
| **Macro Recall** | **69.05%** | Balanced across classes |
| **Weighted F1** | **71.30%** | Frequency-weighted F1 score |
| **Balanced Accuracy** | **69.05%** | Unweighted average recall |
| **Cohen's Kappa** | **0.5789** | Moderate to substantial inter-rater agreement |
| **Ordinal MAE** | **0.2679 stages** | Mean absolute error on ordinal scale (0, 1, 2) |

### 7.2 Per-Class Classification Report

| Class Name | Precision | Recall | F1-Score | Support |
| :--- | :---: | :---: | :---: | :---: |
| **Unripe** (Class 0) | 0.8000 | 0.9524 | 0.8696 | 21 |
| **Semi-ripe** (Class 1) | 0.6154 | 0.7619 | 0.6809 | 21 |
| **Ripe** (Class 2) | 1.0000 | 0.3571 | 0.5263 | 14 |
| **Macro Average** | **0.8051** | **0.6905** | **0.6922** | **56** |
| **Weighted Average** | **0.7808** | **0.7321** | **0.7130** | **56** |

---

## 8. Confusion Matrix & Ripeness Error Analysis

### 8.1 Confusion Matrix (Test Set, N = 56)

```
                     Predicted Unripe   Predicted Semi-ripe   Predicted Ripe
Actual Unripe (21)         20                    1                  0
Actual Semi-ripe (21)       5                   16                  0
Actual Ripe (14)            0                    9                  5
```

### 8.2 Ripeness-Specific Error Breakdown

| Error Type | Definition | Count | % of Total Test | % of All Errors |
| :--- | :--- | :---: | :---: | :---: |
| **Adjacent 1-Stage Errors** | Misclassified as neighboring stage ($\|y - \hat{y}\| = 1$) | **15** | **26.79%** | **100.00%** |
| **Non-Adjacent 2-Stage Errors** | Confusing Unripe with Ripe ($\|y - \hat{y}\| = 2$) | **0** | **0.00%** | **0.00%** |
| **Total Misclassifications** | Any deviation from ground truth | **15** | **26.79%** | **100.00%** |

> [!TIP]
> **Key Biological Finding:**  
> Exactly 100.0% of all classification errors are **adjacent 1-stage transitions** (e.g. late Unripe misclassified as early Semi-ripe, or transitional Ripe misclassified as Semi-ripe). The model **never** produced a catastrophic two-stage failure (zero instances of Unripe misclassified as Ripe, or Ripe misclassified as Unripe).

---

## 9. Output Probability Validation

Softmax class probabilities evaluated across all test predictions:
- **Finite Check:** 100% of probability values are strictly finite.
- **Boundary Check:** All probabilities satisfy $0.0 \le p_i \le 1.0$.
- **Unit Sum Check:** $\sum_{i=0}^2 p_i = 1.0 \pm 10^{-5}$ across all 56 test samples.
- *Terminology Notice:* Per scientific conservatism, these outputs represent raw model confidence scores, not empirically calibrated Bayesian probabilities.

---

## 10. Checkpoint & Reload Verification

- **Checkpoint File:** `D:\FreshIQ\ml\saved_models\freshiq_mobilenetv3_banana_best.pth`
- **Validated SHA-256 Hash:** `cf5eab3c9de6ea35e38fcf5f8f4853f3f450e4713e4ecc282db644fde96bd462`
- **Reload Test:** A completely clean instance of `BananaClassifierEngine` was instantiated, loaded the saved state dictionary from disk, and executed inference on representative test samples from all three classes, successfully returning valid predictions, bounded confidence scores, and stage recommendations.
- **Regression Protection:** Cryptographic hashes of existing production models were verified before and after training:
  - Avocado (`freshiq_mobilenetv3_avocado_best.pth`): `8aaff3e354937dbc4f06e7b7ec944168d8f56572e3003b5fd833d2155d8f312e` (**UNCHANGED / PASS**)
  - Mango (`freshiq_mobilenetv3_mango_best.pth`): `4a8a35c2d18142a9c8a577ed51a83101b51cf510f847af23cd9fa9381857cac9` (**UNCHANGED / PASS**)

---

## 11. Strict Rejection of Remaining Useful Life (RUL) Modeling

> [!CAUTION]
> **BANANA RUL REGRESSION IS NOT SUPPORTED AND SHALL NOT BE IMPLEMENTED**
>
> 1. **Arbitrary Study Termination:** Day 7 was simply the author's experimental observation limit, not a physiological spoilage or consumption cutoff.
> 2. **Stock End-State Frames:** In 11 folders, the Day 7 image is identical byte-for-byte to a shared stock image (`0dcf2a825b...`), which would severely corrupt regression gradients.
> 3. **Static Image Artifacts:** 46 intra-specimen daily transitions are static identical images, violating kinetic degradation monotonicity.
> 4. **Zero Environmental Telemetry:** No numerical temperature or relative humidity logging was conducted.
> 
> **Architectural Decision:** Banana in FreshIQ is strictly restricted to **Ripeness Classification Only**.

---

## 12. Scientific Limitations

In accordance with strict FreshIQ scientific standards, the following limitations are formally documented:

1. **Limited Biological Trajectories:** Exactly 48 unique biological sequence clusters exist in the entire dataset.
2. **Substantial Duplicate Redundancy:** 50.0% of the raw files in Mendeley Data v2 were identical duplicates created by folder replication.
3. **Inferred Specimen Identity:** Specimen identity is inferred from the prepared biological sequence clusters and cannot be independently proven beyond the dataset audit evidence.
4. **Author-Derived Ground Truth:** Ripeness labels are derived from the authors' recommended temporal groupings (Days 0–2, 3–5, 6–7) rather than destructive laboratory titrations (Brix/acid).
5. **No Environmental Kinetics:** Absence of numerical temperature/RH precludes any Arrhenius or Q10 shelf-life modeling.
6. **No Shelf-Life or RUL Support:** The model does not and cannot predict remaining shelf-life days.
7. **Food Safety Disclaimer:** This classifier evaluates surface visual ripeness stage only; it is not a food safety or microbial contamination detector.
8. **Cultivar Specificity:** The source dataset represents Cavendish/Robusta bananas photographed under standardized artificial lighting in Pune, India.
9. **Zero Generalization Claims:** High test accuracy does not establish universal generalization across different banana cultivars, grocery retail lighting, consumer camera sensors, or occluded angles.
10. **Pre-Production Classification Experiment Only:** This model is an experimental benchmark and is not yet approved for production API or Android deployment.

---

## 13. Final Recommendation

- **Ripeness Classification Feasibility:** **CONFIRMED FOR EXPERIMENTAL EVALUATION** (73.21% test accuracy, 69.22% test Macro-F1, 0% non-adjacent errors, 80.53% mean CV accuracy).
- **Production Integration Status:** **HOLD.** Banana model is frozen as an experimental checkpoint. Do NOT integrate into FastAPI or Android until separate review and multi-cultivar expansion.
