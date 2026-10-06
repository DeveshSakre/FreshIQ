# FreshIQ

### AI-Powered Multi-Produce Ripeness & Shelf-Life Decision Support System

FreshIQ is an AI-powered produce intelligence system that uses computer vision to identify ripeness stages and, where the available data supports it, estimate remaining usable shelf life under different storage conditions.

The system combines **produce-specific computer vision models, a FastAPI inference backend, storage-condition modeling, and a native Android application** into a single decision-support platform.

> **Important:** FreshIQ is a decision-support system. AI-generated shelf-life estimates are not a food-safety guarantee.

---

## 🌱 Overview

FreshIQ is designed around a simple problem:

**"How ripe is this produce, and how should I store or use it?"**

A user can capture or select an image of supported produce through the Android application. The image is sent to the FreshIQ backend, where a produce-specific ML model determines the ripeness stage and confidence distribution.

For produce with sufficient longitudinal storage data, FreshIQ additionally estimates remaining usable shelf life and provides storage-condition scenarios.

The system intentionally does **not** provide shelf-life predictions for produce where the available dataset does not support them.

---

## ✨ Key Features

- 📷 **Image-based ripeness classification**
- 🥑 **Avocado shelf-life estimation**
- 🥭 **Mango ripeness classification**
- 🍌 **Banana ripeness classification**
- 📊 **Confidence and class-probability distribution**
- 🌡️ **Storage-condition analysis for supported produce**
- ❄️ **4°C model-based extrapolation for avocado**
- 🔬 **Produce-specific ML models**
- 📱 **Native Android application**
- ⚡ **FastAPI inference backend**
- 💾 **Local scan history using Room**
- 🔍 **What-If storage analysis**
- 📈 **Avocado storage scenario comparison**
- 🧠 **Backend as the single source of ML truth**
- 🛡️ **Responsible-AI and food-safety disclaimers**
- 🔒 **No datasets, model binaries, secrets, or APKs committed to GitHub**

---

# 🥬 Supported Produce

| Produce | Ripeness Classification | Shelf-Life / RUL | What-If Analysis |
|---|---:|---:|---:|
| Hass Avocado | ✅ | ✅ | ✅ |
| Mango | ✅ | ❌ | ❌ |
| Banana | ✅ | ❌ | ❌ |

FreshIQ uses a **capability-aware architecture** rather than pretending every produce type supports the same prediction features.

---

# 🏗️ System Architecture

```text
                    ┌─────────────────────────┐
                    │      Android App        │
                    │                         │
                    │  Camera / Gallery       │
                    │  Produce Selection      │
                    │  Analysis               │
                    │  What-If                │
                    │  Comparison             │
                    │  History                │
                    └────────────┬────────────┘
                                 │
                                 │ HTTP / REST
                                 ▼
                    ┌─────────────────────────┐
                    │     FastAPI Backend     │
                    │                         │
                    │  Request Validation     │
                    │  Produce Routing        │
                    │  Model Loading          │
                    │  Prediction             │
                    │  Storage Scenarios      │
                    └────────────┬────────────┘
                                 │
                ┌────────────────┼────────────────┐
                │                │                │
                ▼                ▼                ▼
        ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
        │   Avocado    │ │    Mango     │ │    Banana    │
        │ Vision Model │ │ Vision Model │ │ Vision Model │
        └──────┬───────┘ └──────────────┘ └──────────────┘
               │
               ▼
        ┌──────────────────────┐
        │ Avocado RUL / Storage│
        │ Modeling             │
        └──────────────────────┘


        🤖 Machine Learning
FreshIQ uses separate models for each produce type because the available datasets, ripening stages, and prediction capabilities differ between produce.
🥑 Hass Avocado
Ripeness Classification
The avocado classifier recognizes five ripening stages:
1. Underripe
2. Breaking
3. Ripe First Phase
4. Ripe Second Phase
5. Overripe
Model:
- Architecture: MobileNetV3-Small
- Transfer learning with ImageNet-pretrained weights
- Produce-specific classification pipeline
- Specimen-level grouping used during dataset preparation
- Non-decreasing ripening trajectories verified for longitudinal sequences
Classification Results
Metric	Test Result
Accuracy	66.15%
Macro Precision	68.12%
Macro Recall	65.76%
Macro F1	65.27%
Ordinal MAE	0.3576 stages
Accuracy within ±1 stage	98.13%


The ordinal evaluation is particularly relevant because ripeness stages have a natural ordering. A prediction one stage away is materially different from a prediction several stages away.
⏳ Avocado Remaining Useful Life
For avocado, FreshIQ also estimates remaining usable shelf life where the dataset supports longitudinal storage modeling.
The current RUL model uses a HistGradientBoosting model.
Test Results
Metric	Result
MAE	1.7485 days
RMSE	2.4568 days
R²	0.8420
Median Absolute Error	1.2252 days
Bias	+0.2439 days


Approximately:
1.75 days MAE ≈ 41.9 hours
The model's error varies by storage condition, which is expected because the available data is not equally distributed across all conditions.
Stage 5 Behavior
Once an avocado reaches the terminal Overripe stage:
Remaining Useful Life = 0 days

🥭 Mango
Mango is currently supported for ripeness classification only.
The dataset does not provide the longitudinal specimen tracking and numerical storage-condition data required for a defensible remaining shelf-life model.
Ripeness Stages
1. Unripe
2. Semiripe
3. Fully Ripe
4. Overripe
5. Perished
Model
- Architecture: MobileNetV3-Large
- ImageNet-pretrained weights
- Produce-specific transfer-learning pipeline
- 224 × 224 RGB input
Test Results
Metric	Result
Accuracy	95.64%
Macro Precision	95.47%
Macro Recall	95.54%
Macro F1	95.42%
Weighted F1	95.66%
Balanced Accuracy	95.54%
Cohen's Kappa	0.9426
Ordinal MAE	0.0436 stages
Accuracy within ±1 stage	100%


FreshIQ intentionally does not generate RUL, storage scenarios, or What-If shelf-life predictions for mango.
🍌 Banana
Banana is also supported for ripeness classification only.
The dataset contains day-based observations but does not provide the numerical storage-condition information and specimen-level structure required for a defensible shelf-life model.
FreshIQ Banana Taxonomy
The original observations are mapped into three actionable stages:
1. Unripe
2. Semi-ripe
3. Ripe
Dataset Preparation
The banana dataset required additional preprocessing because it contained duplicate and closely related images.
The preparation pipeline included:
- SHA-256 duplicate detection
- Connected-component grouping
- Clone-group analysis
- Group-aware train/validation/test splitting
- Prevention of related image leakage between splits
Model
- Architecture: MobileNetV3-Small
- ImageNet-pretrained weights
- Two-stage transfer learning
- 224 × 224 input
Held-Out Test Results
Metric	Result
Accuracy	73.21%
Macro Precision	80.51%
Macro Recall	69.05%
Macro F1	69.22%
Weighted F1	71.30%
Balanced Accuracy	69.05%
Cohen's Kappa	0.5789
Ordinal MAE	0.2679 stages
Non-adjacent errors	0


The test errors were all adjacent-stage errors, which is important for an ordered ripeness problem.
FreshIQ does not generate RUL or storage-condition What-If predictions for banana.
🌡️ Storage & What-If Modeling
Storage analysis is available only where the underlying data and modeling methodology support it.
For avocado, FreshIQ provides scenario information for:
- 20°C
- 10°C
- 4°C
The 10°C and 20°C scenarios are based on observed storage conditions.
4°C Scenario
The available avocado training data does not directly provide observed 4°C longitudinal measurements.
Therefore, the 4°C result is explicitly labeled:
Model-based extrapolation

FreshIQ keeps this distinction visible in the UI instead of presenting extrapolated values as directly observed experimental results.
🔬 Backend as the Single Source of Truth
A major architectural principle of FreshIQ is:
The Android client never performs ML or biophysical calculations.

The backend is responsible for:
- Ripeness stage
- Confidence
- Class probabilities
- Continuous ripening representation
- Remaining useful life
- Storage scenarios
- Storage extrapolation
- Recommendations
- Model metadata
The Android application is responsible for:
- Capturing/selecting images
- Sending requests
- Rendering backend results
- Local scan history
- UI state and navigation
This prevents the mobile client from accidentally producing predictions that are inconsistent with the trained models.
📱 Android Application
FreshIQ is implemented as a native Android application using modern Android development practices.
Technology Stack
- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Hilt
- Retrofit
- OkHttp
- Room
- Coil
- Navigation Compose
Android Architecture
UI
│
├── Home
├── Scan
├── Analysis
├── What-If
├── Comparison
├── History
└── Insights
       │
       ▼
ViewModels
       │
       ▼
Repositories
       │
       ├── Retrofit API
       │
       └── Room Database

📸 Scan Flow
The primary user flow is:
Home
  ↓
Select Produce
  ↓
Take Photo / Choose from Gallery
  ↓
Preview
  ↓
Analyze
  ↓
FastAPI Prediction
  ↓
Analysis Result

The application uses a guarded prediction flow so that a single analysis action does not unintentionally trigger multiple backend prediction requests.
📊 Analysis Screen
The Analysis screen adapts to the capabilities of the selected produce.
Avocado
Displays:
- Input image
- Predicted ripeness stage
- Confidence
- Class probability distribution
- Ripening progression
- Remaining useful life
- Storage scenarios
- 4°C extrapolation indicator
- Recommendation
- What-If analysis
- Comparison
- Food-safety disclaimer
Mango / Banana
Displays:
- Input image
- Predicted ripeness stage
- Confidence
- Class probability distribution
- Ripeness progression
- Recommendation
- Food-safety disclaimer
Unsupported shelf-life functionality is intentionally hidden rather than simulated.
💾 Local Scan History
FreshIQ uses Room for local scan history.
History supports:
- Saving analyzed scans
- Searching
- Filtering by produce
- Opening previous results
- Deleting individual scans
- Clearing history
Opening an existing history item does not trigger a new backend prediction request.
This keeps historical results local and avoids unnecessary inference calls.
🔍 What-If Analysis
What-If analysis allows supported avocado predictions to be explored under available storage scenarios.
The Android client does not calculate:
- Shelf-life deltas
- Percentage gains
- Q10 values
- Arrhenius calculations
- RUL interpolation
- 4°C estimates
These values come from the backend.
The UI only presents the backend-provided scenario information.
🆚 Storage Comparison
For avocado, FreshIQ can compare supported storage scenarios using backend-provided data.
The comparison layer does not independently recompute shelf-life improvements.
This keeps the prediction and visualization layers separated.
🛡️ Responsible AI
FreshIQ intentionally follows several principles to avoid overstating model capabilities.
1. Produce-specific capabilities
Different datasets support different predictions.
Avocado → Classification + RUL + Storage What-If
Mango   → Classification
Banana  → Classification

2. No unsupported predictions
If a dataset does not support shelf-life modeling, FreshIQ does not invent a shelf-life value.
3. Explicit extrapolation
The 4°C avocado scenario is clearly identified as model-based extrapolation.
4. Backend authority
ML predictions are generated by the backend rather than recreated on-device.
5. Food-safety disclaimer
FreshIQ provides decision support and does not guarantee that food is safe to consume.
Visual ripeness and predicted usability should not be treated as a substitute for appropriate food-safety practices.
🧪 Validation
The finalized system was tested across the ML/backend/Android stack.
Validation included:
- Android unit tests
- Backend API tests
- Model loading verification
- Model hash verification
- End-to-end gallery predictions
- Camera-flow validation
- Produce selector validation
- History validation
- What-If validation
- Comparison validation
- Navigation validation
- Error-state validation
- Network-request behavior validation
Final Android Validation
- 26 Android tests passed
- Compile successful
- Debug APK successfully assembled
- Backend integration tests passed
- All supported produce models verified as loaded
- GitHub repository hygiene verified
The finalized QA also verified that:
- Fresh analysis produces exactly one prediction request
- What-If does not create an additional prediction request
- Comparison does not create an additional prediction request
- History does not trigger inference
- Insights does not trigger inference
- Mango and Banana do not expose unsupported RUL functionality
🧰 Technology Stack
Machine Learning
- Python 3
- PyTorch
- MobileNetV3
- scikit-learn
- NumPy
- pandas
- Computer Vision
- Transfer Learning
- Classification
- Regression
- Model Evaluation
Backend
- Python
- FastAPI
- REST API
- Pydantic
- Model inference
- Produce-specific routing
Android
- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Hilt
- Retrofit
- OkHttp
- Room
- Coil
- Navigation Compose
Development
- Android Studio
- Git
- GitHub
- ADB
- Physical Android device testing
📁 Project Structure
FreshIQ/
│
├── android/
│   └── FreshIQ Android application
│
├── backend/
│   └── FastAPI inference backend
│
├── ml/
│   ├── Dataset preparation
│   ├── Model training
│   ├── Evaluation
│   ├── Storage modeling
│   └── Simulation / scenario engine
│
├── README.md
├── .gitignore
└── requirements / configuration files

Datasets, trained model binaries, secrets, local configuration, APKs, and generated build artifacts are intentionally excluded from the repository.
🚀 Getting Started
Prerequisites
Backend
- Python 3
- pip
- FastAPI environment dependencies
Android
- Android Studio
- Android SDK
- JDK 17
- Android device or emulator
- USB debugging for physical-device testing
1. Clone the Repository
git clone https://github.com/DeveshSakre/FreshScan.git
cd FreshScan

2. Backend Setup
Create a Python virtual environment:
python -m venv .venv

Activate it on Windows:
.venv\Scripts\Activate.ps1

Install the required dependencies:
pip install -r requirements.txt

Start the FastAPI backend using the project's backend entry point.
The Android application should point to the running backend according to the development environment being used.
3. Android Setup
Open:
FreshIQ/android

in Android Studio.
Allow Gradle to synchronize and build the project.
For a physical Android device, USB debugging can be used.
During local development, ADB reverse can expose the backend running on the development machine:
adb reverse tcp:8000 tcp:8000

The Android client can then communicate with:
http://127.0.0.1:8000/

🔐 Repository & Security
The repository intentionally excludes:
- Dataset files
- Trained model binaries
- .env files
- API keys
- google-services.json
- Android local.properties
- APK/AAB files
- Build outputs
- Temporary screenshots
- Debug dumps
- Python virtual environments
This keeps the public repository lightweight and prevents accidental exposure of sensitive or generated artifacts.
📌 Current Limitations
FreshIQ is a research and decision-support prototype and has several important limitations.
Dataset limitations
Model performance depends on the datasets used for training and evaluation.
Different produce types have different levels of:
- Sample size
- Longitudinal tracking
- Storage-condition coverage
- Image diversity
- Specimen metadata
Shelf-life limitations
Remaining useful life is currently supported only for avocado.
Mango and banana are intentionally classification-only.
Storage extrapolation
The avocado 4°C scenario is model-based extrapolation rather than direct 4°C longitudinal observation.
Real-world variability
Real produce can vary because of:
- Cultivar
- Growing conditions
- Harvest maturity
- Handling
- Transportation
- Temperature fluctuations
- Humidity
- Physical damage
- Microbial activity
- Storage environment
Therefore, model predictions should not be interpreted as guaranteed outcomes.
🔮 Future Scope
Potential future improvements include:
- Larger multi-produce datasets
- More geographically diverse produce samples
- More storage temperatures and humidity conditions
- Additional longitudinal experiments
- Model calibration
- On-device inference
- More produce-specific shelf-life models
- Improved uncertainty estimation
- Cloud-based user accounts and synchronization
- Expanded food-quality intelligence
- Real-world deployment studies
Future features will only be introduced where the underlying data and validation methodology support them.
📊 Project Highlights
Area	Implementation
Computer Vision	Produce-specific ripeness classification
Deep Learning	MobileNetV3 transfer learning
Shelf-Life Modeling	Avocado RUL
Storage Modeling	Avocado scenario analysis
Backend	FastAPI
Mobile	Native Android + Jetpack Compose
Architecture	MVVM + Repository
Local Storage	Room
Dependency Injection	Hilt
Networking	Retrofit + OkHttp
Testing	Unit + integration + physical-device QA
Repository	GitHub


👨‍💻 Author
Devesh Sakre
B.Tech Computer Science & Engineering
VIT Bhopal University
GitHub:
https://github.com/DeveshSakre
⚠️ Disclaimer
FreshIQ is an AI-based research and decision-support system.
Predictions represent model outputs based on the available training data and input image. Shelf-life estimates are not guarantees of food safety, quality, or suitability for consumption.
Always use appropriate food-safety practices and human judgment before consuming stored produce.
⭐ If you find FreshIQ interesting
Feel free to explore the repository, review the ML pipelines, experiment with the Android application, or build upon the project.

**One correction from the earlier version:** I removed the unsupported claim about **TensorFlow** from the ML stack. The README now sticks to the technologies actually established for the finalized FreshIQ system.
