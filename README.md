# FreshIQ

FreshIQ is an AI-powered multi-produce freshness, ripeness, and shelf-life decision-support system. It combines deep learning computer vision with postharvest biophysical modeling to analyze produce images, classify ripeness stages, project remaining useful life (RUL) under different storage temperatures, and provide actionable storage recommendations.

## Overview

FreshIQ delivers image-based produce analysis tailored to the biological characteristics and available data for each produce type. Rather than applying a generic heuristic, capabilities are strictly model- and dataset-dependent:

- **Hass Avocado**: Supports multi-stage ripeness classification, regression-based Remaining Useful Life (RUL) estimation, and empirical storage scenario simulations (10°C and 20°C) alongside model-based refrigerated extrapolation (4°C).
- **Mango**: Supports multi-stage visual ripeness classification with confidence scores and storage recommendations.
- **Banana**: Supports 3-stage visual ripeness classification with confidence scores and consumption guidance.

> **Food Safety Notice**: AI shelf-life estimates are decision-support outputs and are not a food-safety guarantee. Always inspect produce for physical signs of spoilage (mold, off-odors, excessive softening) before consumption.

---

## Supported Produce

| Produce | Ripeness Classification | Taxonomy Stages | Remaining Useful Life (RUL) | Storage Scenarios / What-If |
|---|---|---|---|---|
| **Hass Avocado** | Yes | 5 Stages (Stage 1 to Stage 5) | Yes | Yes (10°C, 20°C empirical; 4°C extrapolation) |
| **Mango** | Yes | 5 Stages (Unripe, Early Ripe, Partially Ripe, Ripe, Overripe) | No | No |
| **Banana** | Yes | 3 Stages (Unripe, Semi-ripe, Ripe) | No | No |

*Note: Mango and Banana do not support RUL or storage scenario simulation due to the absence of longitudinal temperature-controlled storage datasets for those varieties.*

---

## Key Features

- **Multi-Produce Viewfinder & Gallery**: Capture photos or select gallery images with real-time produce taxonomy switching.
- **AI Ripeness Classification**: Deep learning inference predicting stage classification and full softmax probability distributions.
- **Avocado Remaining Useful Life (RUL)**: Predicts remaining days of edible shelf life under ambient and cool storage.
- **Storage Scenario Projections**: Compares postharvest longevity at ambient room temperature (20°C) versus cool storage (10°C).
- **Model-Based 4°C Extrapolation**: Evaluates potential refrigerated holding life via biophysical respiration modeling with explicit extrapolation labels.
- **What-If Simulation**: Interactive temperature selection for avocado shelf-life planning.
- **Side-by-Side Comparison**: Direct visual comparison between baseline ambient conditions and target storage temperatures.
- **Room Persistence & History**: Local scan history storage with search, stage-based filtering, and offline record review without redundant network calls.
- **Educational Insights**: Postharvest handling intelligence, temperature management guidance, and chilling injury advisories.
- **Modern Botanical Android UI**: Built with Jetpack Compose following Material 3 design guidelines.

---

## Architecture

The project is structured with a strict separation between presentation and machine learning inference:

### Android Client (`android/`)
- **Language & Runtime**: Kotlin, Java 17
- **UI Framework**: Jetpack Compose, Material 3
- **Architecture**: MVVM with unidirectional data flow (StateFlow)
- **Dependency Injection**: Hilt
- **Networking**: Retrofit, OkHttp
- **Local Persistence**: Room SQLite Database
- **Image Loading**: Coil
- **Navigation**: Navigation Compose

### Backend API (`backend/`)
- **Framework**: FastAPI (Python)
- **Server**: Uvicorn ASGI
- **Endpoints**:
  - `GET /api/health`: System health and loaded model verification
  - `POST /api/predict`: Multipart image inference with produce-type routing
- **Validation**: Pydantic schemas with strict validation for supported produce types

### Machine Learning (`ml/`)
- **Classification Models**: PyTorch MobileNetV3-Small vision classifiers trained on produce datasets
- **RUL Regression Model**: scikit-learn Random Forest regressor extracting deep feature embeddings from vision checkpoints
- **Simulation Engine**: Respiration rate biophysical modeling for scenario generation

---

## Backend Source of Truth

To maintain scientific integrity and prevent client-side inconsistencies:
- The **backend** is the sole authority for ML predictions, confidence scores, class probability distributions, RUL estimates, scenario projections, and storage advice.
- The **Android client** is strictly a presentation and persistence client. The mobile application never calculates ripeness stages, RUL days, Q10 kinetic factors, or simulated temperatures.

---

## 4°C Refrigeration Extrapolation

Empirical storage datasets for avocado were collected at 10°C and 20°C. Storage projections at 4°C are calculated via an Arrhenius-derived respiration model ($Q_{10} = 2.38$):

- The 4°C projection is explicitly designated as a **model-based extrapolation** rather than measured ground truth.
- Avocados stored below 5°C are susceptible to **chilling injury** (internal mesocarp browning, vascular browning, failure to ripen normally). The application prominently displays this caveat alongside 4°C projections.

---

## Project Structure

```
FreshIQ/
├── android/                  # Native Android application
│   ├── app/
│   │   ├── src/main/java/com/freshiq/app/
│   │   │   ├── data/         # Room database, DTOs, Retrofit API client
│   │   │   ├── di/           # Hilt dependency injection modules
│   │   │   ├── model/        # Domain entities (ProduceType, RipenessStage)
│   │   │   ├── ui/           # Jetpack Compose screens, components, theme
│   │   │   └── viewmodel/    # MVVM ViewModels & ScanSessionManager
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── backend/                  # FastAPI backend service
│   ├── app/
│   │   ├── routes/           # API routes (/health, /predict)
│   │   ├── schemas/          # Pydantic request/response schemas
│   │   ├── services/         # Inference and model loading service
│   │   └── main.py           # Application entry point
│   └── requirements.txt
├── ml/                       # ML training, evaluation, and simulation
│   ├── banana_classifier.py  # Banana MobileNetV3 training and architecture
│   ├── mango_classifier.py   # Mango MobileNetV3 training and architecture
│   ├── model.py              # Avocado MobileNetV3 architecture
│   ├── shelflife_data.py     # Feature extraction for RUL modeling
│   ├── simulation_engine.py  # Temperature scenario simulation engine
│   └── train_shelflife.py    # Random Forest RUL regressor training
├── freshiq_manifests/        # Dataset splits and manifest CSVs
├── frontend/                 # Web client
└── README.md
```

---

## Setup & Running Locally

### Backend Setup

1. **Prerequisites**: Python 3.10+ (tested up to 3.12)
2. **Install Dependencies**:
   ```bash
   pip install -r backend/requirements.txt
   ```
3. **Start the API Server**:
   ```bash
   uvicorn backend.app.main:app --host 127.0.0.1 --port 8000
   ```
4. **Verify Health**:
   Navigate to `http://127.0.0.1:8000/api/health` in a browser or curl to confirm models are loaded.

### Android Setup

1. **Prerequisites**: Android Studio Ladybug or newer, Android SDK 34+
2. **Reverse Port Forwarding** (for physical device testing over USB):
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```
3. **Build and Run**:
   Open the `android/` directory in Android Studio, sync Gradle, and run on a connected device or emulator.
   To build via command line:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```

---

## Limitations

- **Produce Scope**: Currently supports Hass Avocado, Mango, and Banana. Unrecognized produce returns a clean validation error.
- **Longitudinal Storage Data**: Only Hass Avocado includes experimental shelf-life storage data; Mango and Banana provide classification only.
- **Visual Surface Limitation**: Visual analysis reflects exterior skin condition; internal disorders without surface manifestation cannot be detected optically.
- **Model Extrapolation**: 4°C storage estimates are mathematical projections based on respiration kinetics and should not be treated as empirical shelf-life trials.
