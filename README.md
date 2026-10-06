# FreshIQ

### AI-Powered Multi-Produce Ripeness & Shelf-Life Decision Support

FreshIQ is an AI-powered system that uses computer vision to classify produce ripeness from images and, where the available data supports it, estimate remaining usable shelf life under different storage conditions.

The system combines a **native Android application**, **FastAPI inference backend**, and **produce-specific machine learning pipelines**. FreshIQ deliberately uses different capabilities for different produce types instead of applying unsupported shelf-life predictions universally.

> **Important:** FreshIQ provides AI-based decision support. Shelf-life estimates are not a food-safety guarantee.

---

## 🌱 Supported Produce

| Produce | Ripeness Classification | RUL / Shelf Life | Storage What-If |
|:---|:---:|:---:|:---:|
| 🥑 Hass Avocado | ✅ | ✅ | ✅ |
| 🥭 Mango | ✅ | — | — |
| 🍌 Banana | ✅ | — | — |

### Why are the capabilities different?

FreshIQ's capabilities are determined by the data available for each produce.

**Hass Avocado** has longitudinal storage observations that support both ripeness classification and remaining useful life (RUL) modeling.

**Mango and Banana** datasets support ripeness classification but do not provide the longitudinal temperature-controlled observations required for a defensible RUL model.

Therefore, FreshIQ intentionally does **not** fabricate shelf-life predictions for Mango or Banana.

---

## ✨ Features

- 📷 **Image-Based Ripeness Classification**
  - Analyze produce images using computer vision models.
  - Produce-specific ripeness taxonomies.

- 🧠 **Produce-Specific AI Models**
  - Separate models and capabilities for Avocado, Mango, and Banana.
  - Capability boundaries are enforced by the backend and Android client.

- 📊 **Confidence & Class Distribution**
  - Displays model confidence and classification probability distribution.

- 🥑 **Avocado Remaining Useful Life (RUL)**
  - Estimates remaining usable life for Hass Avocado.
  - Uses storage-aware modeling based on the available longitudinal dataset.

- 🌡️ **Storage Scenario Analysis**
  - Backend-provided storage scenarios for supported Avocado predictions.
  - Compare estimated RUL across supported conditions.

- ❄️ **4°C Model-Based Extrapolation**
  - 4°C results are explicitly identified as model-based extrapolation.
  - They are not presented as direct experimental ground truth.

- 🔄 **What-If Analysis**
  - Explore supported Avocado storage scenarios without triggering additional prediction requests.

- 📱 **Native Android Application**
  - Kotlin + Jetpack Compose.
  - Scan, analyze, compare, save, and revisit predictions.

- 🗂️ **Local Scan History**
  - Scan results are persisted locally using Room.
  - Reopening a saved result does not require another ML prediction.

- ⚡ **FastAPI Inference Backend**
  - Centralized prediction and storage-model logic.
  - Backend acts as the single source of truth for ML outputs.

- 🛡️ **Responsible AI Guardrails**
  - Unsupported RUL predictions are not generated.
  - Android does not independently calculate ML or biophysical outputs.
  - Food-safety limitations are clearly communicated.

---

## 🏗️ System Architecture

```text
                         FreshIQ
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           │
     ┌──────────────────┐                 │
     │   Android App    │                 │
     │                  │                 │
     │ Kotlin           │                 │
     │ Jetpack Compose  │                 │
     │ MVVM             │                 │
     │ Hilt             │                 │
     │ Retrofit         │                 │
     │ Room             │                 │
     └────────┬─────────┘                 │
              │                           │
              │ HTTP                      │
              ▼                           │
     ┌──────────────────┐                 │
     │ FastAPI Backend  │                 │
     │                  │                 │
     │ Prediction API   │                 │
     │ Produce Routing  │                 │
     │ Scenario Logic   │                 │
     └────────┬─────────┘                 │
              │                           │
        ┌─────┼──────────────┐            │
        │     │              │            │
        ▼     ▼              ▼            │
   ┌────────┐ ┌────────┐ ┌────────┐      │
   │Avocado │ │ Mango  │ │ Banana │      │
   │ Vision │ │ Vision │ │ Vision │      │
   │  + RUL │ │        │ │        │      │
   └────┬───┘ └────────┘ └────────┘      │
        │                                  │
        ▼                                  │
   ┌──────────────────────────┐            │
   │ Avocado Storage Modeling │            │
   │                          │            │
   │ 10°C / 20°C empirical   │            │
   │ 4°C model extrapolation  │            │
   └──────────────────────────┘            │
