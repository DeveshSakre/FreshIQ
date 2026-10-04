package com.freshiq.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.freshiq.app.ui.components.ExtrapolationNoticeCard
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.FreshAccentButton
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshCardVariant
import com.freshiq.app.ui.components.FreshSecondaryButton
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BgPage
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BlueIceBadge
import com.freshiq.app.ui.theme.BlueIceBadgeText
import com.freshiq.app.ui.theme.BlueIceText
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.Stage1Fill
import com.freshiq.app.ui.theme.Stage1Text
import com.freshiq.app.ui.theme.Stage2Fill
import com.freshiq.app.ui.theme.Stage3Fill
import com.freshiq.app.ui.theme.Stage4Fill
import com.freshiq.app.ui.theme.Stage5Fill
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
import com.freshiq.app.ui.theme.TextSubtle

// =========================================================================
// FreshIQ — ML Insights & Degradation Science (Phase 2H)
//
// PRESENTATION-ONLY SCREEN. This screen performs no inference, issues no
// network calls, and never recomputes any scientific quantity. Every number
// displayed here is a static, verified reference value transcribed from an
// evaluation artifact under ml/saved_models/ or from ml/phase1b_report.md.
// The backend remains the single source of truth for all live predictions.
//
// Provenance of every metric shown on this screen:
//   66.15% / 98.13% / 0.3576 / 1,454 of 2,198 / 2,157 of 2,198
//                                    -> ml/saved_models/test_evaluation_results.json
//                                       ml/diagnostics/diagnostic_summary.json
//   1.7485 d / 2.4568 d / 0.8420 / 1.2252 d / n = 2,112
//                                    -> ml/saved_models/shelflife_test_evaluation.json
//   Per-condition MAE / RMSE / R2    -> ml/saved_models/shelflife_test_evaluation.json
//   Ridge / GB / HistGB comparison   -> ml/saved_models/shelflife_model_comparison.json
//   Specimen + observation splits    -> ml/phase1b_report.md sections 3 and 5
//   Q10 = 2.38, 21-day chilling cap  -> ml/simulation_engine.py
// =========================================================================

private enum class CurveFilter(val label: String) {
    All("All Regimens"),
    Ambient("Ambient 20°C"),
    Cold10("Cold 10°C"),
    Chilled4("Chilled 4°C*")
}

private data class PipelinePhase(
    val phase: String,
    val title: String,
    val body: String,
    val formula: String?,
    val footer: String,
    val icon: ImageVector
)

private data class StageInfo(
    val stage: Int,
    val name: String,
    val subname: String,
    val color: Color,
    val description: String,
    val culinary: String,
    val tactile: String,
    val appearance: String,
    val rulAmbient: String,
    val rulCold: String,
    val rulChilled: String
)

private data class BenchmarkMetric(
    val label: String,
    val value: String,
    val accent: Color,
    val leftFoot: String,
    val rightFoot: String,
    val note: String
)

private data class StratifiedRow(
    val condition: String,
    val temperature: String,
    val testImages: String,
    val actualMean: String,
    val predictedMean: String,
    val mae: String,
    val rmse: String,
    val r2: String,
    val status: String,
    val isExtrapolated: Boolean
)

private data class LimitationItem(
    val title: String,
    val body: String,
    val accent: Color,
    val icon: ImageVector
)

// =========================================================================
// Local accent colors
//
// Screen-local only. These intentionally mirror the web Insights page accents
// that have no counterpart in the shared FreshIQ palette; the shared theme in
// ui/theme/Color.kt is left untouched.
//
// NOTE: these MUST be declared above the reference-content lists below.
// Kotlin initializes top-level properties in file declaration order, so a list
// that reads one of these before its initializer has run would capture
// Color(0) instead of the intended value.
// =========================================================================

private val SkyBlue = Color(0xFF0284C7)
private val Violet600 = Color(0xFF7C3AED)
private val Amber500 = Color(0xFFF59E0B)
private val Amber600 = Color(0xFFD97706)
private val Red600 = Color(0xFFDC2626)
private val RedTint = Color(0xFFFEF2F2)
private val RedTintBorder = Color(0xFFFECACA)
private val RedDeepText = Color(0xFF7F1D1D)
private val EmeraldTint = Color(0xFFECFDF5)
private val EmeraldDeep = Color(0xFF065F46)
private val GridSoft = Color(0xFFCBD5E1)
private val GridStrong = Color(0xFF94A3B8)
private val CulinaryBand = Color(0x14CA8A04)

// =========================================================================
// Reference content
// =========================================================================

private val PIPELINE_PHASES = listOf(
    PipelinePhase(
        phase = "PHASE 01",
        title = "Image Capture",
        body = "RGB photograph of the produce exocarp. Resized to 224×224 and normalized with standard ImageNet statistics (mean [0.485, 0.456, 0.406], std [0.229, 0.224, 0.225]).",
        formula = null,
        footer = "Input: [3, 224, 224] Tensor",
        icon = Icons.Default.CameraAlt
    ),
    PipelinePhase(
        phase = "PHASE 02",
        title = "Neural Backbone",
        body = "MobileNetV3-Small vision backbone. Depthwise separable convolutions, squeeze-and-excitation attention, and hard-swish activation keep inference light enough for edge deployment.",
        formula = null,
        footer = "Backbone: MobileNetV3-Small",
        icon = Icons.Default.Memory
    ),
    PipelinePhase(
        phase = "PHASE 03",
        title = "Stage Regressor",
        body = "Produces a discrete 5-class Softmax distribution [p₁ … p₅] and the continuous probability-weighted expected stage:",
        formula = "E = Σ k · p_k ∈ [1.00, 5.00]",
        footer = "Continuous Expected Index",
        icon = Icons.Default.Layers
    ),
    PipelinePhase(
        phase = "PHASE 04",
        title = "Arrhenius Kinetics",
        body = "Quantifies temperature-dependent reaction rates through the calibrated temperature quotient, derived from the empirical 20°C and 10°C lifespan ratio:",
        formula = "Q₁₀ = (k₂₀ / k₁₀)^(10/10) ≈ 2.38",
        footer = "Kinetic Scaling Factor",
        icon = Icons.Default.DeviceThermostat
    ),
    PipelinePhase(
        phase = "PHASE 05",
        title = "What-If Simulation",
        body = "Compares shelf-life scenarios across ambient, 20°C, and 10°C — all directly predicted by the regressor — while 4°C is produced by biophysical extrapolation rather than direct prediction.",
        formula = null,
        footer = "Scenario Shelf-Life & Gain",
        icon = Icons.Default.Tune
    )
)

// Stage descriptors are qualitative. Instrumented firmness (N), hue angle, and
// lipid fraction are deliberately NOT shown: FreshIQ has no firmness, colorimetry,
// or lipid model, so publishing such values would imply measurements the system
// never makes. Numeric RUL for a real specimen comes from the backend at scan time.
private val STAGE_DATA = listOf(
    StageInfo(
        stage = 1,
        name = "Hard / Underripe",
        subname = "Pre-Climacteric Basal Phase",
        color = Stage1Fill,
        description = "Firm, taut, granular exocarp with high chlorophyll saturation. Respiration sits at its basal pre-climacteric rate and dense parenchymal cell walls prevent any tactile yield.",
        culinary = "Not suited to immediate consumption. Hold at ambient room temperature to allow natural ethylene emission and the climacteric rise to begin.",
        tactile = "No yield to firm palm pressure",
        appearance = "Vibrant green, granular skin",
        rulAmbient = "8–10 Days",
        rulCold = "22–26 Days",
        rulChilled = "Up to 21 Days (Chilling Limit)*"
    ),
    StageInfo(
        stage = 2,
        name = "Breaking",
        subname = "Olive-Green Developmental Shift",
        color = Stage2Fill,
        description = "Chlorophyll degradation begins as anthocyanin synthesis drives an olive hue shift. The ethylene rise activates pectin methylesterase and endopolygalacturonase, starting cell-wall softening.",
        culinary = "Firm cutting, high-heat grilling, or pickling. Moves toward the optimal eating window over the following days.",
        tactile = "First perceptible give under pressure",
        appearance = "Dusky olive transition",
        rulAmbient = "5–7 Days",
        rulCold = "14–18 Days",
        rulChilled = "Up to 21 Days (Chilling Limit)*"
    ),
    StageInfo(
        stage = 3,
        name = "Firm Ripe (Ripe 1)",
        subname = "Prime Culinary Window Opens",
        color = Stage3Fill,
        description = "The prime culinary eating window begins. Dark pebbled exocarp with a visible purple undertone. The fruit yields gently to palm pressure without bruising.",
        culinary = "Optimal for firm slicing — salads, sushi, poke bowls, and avocado toast, where intact slices with a tender bite are wanted.",
        tactile = "Yields gently, springs back",
        appearance = "Dark pebbled, purple undertone",
        rulAmbient = "3–4 Days",
        rulCold = "8–12 Days",
        rulChilled = "Up to 21 Days (Chilling Limit)*"
    ),
    StageInfo(
        stage = 4,
        name = "Ripe Second Stage",
        subname = "Climacteric Peak / Urgent Window",
        color = Stage4Fill,
        description = "Climacteric respiration crests. Mesocarp cell walls have undergone extensive enzymatic dissolution, producing a silky, buttery texture at peak palatability.",
        culinary = "Optimal for guacamole, spreads, dressings, and smoothies. High urgency — consume soon or refrigerate to slow senescence.",
        tactile = "Soft, velvet yield",
        appearance = "Deep purple-black",
        rulAmbient = "1–2 Days",
        rulCold = "3–5 Days",
        rulChilled = "Up to 21 Days (Chilling Limit)*"
    ),
    StageInfo(
        stage = 5,
        name = "Overripe",
        subname = "Post-Climacteric Senescence",
        color = Stage5Fill,
        description = "Post-climacteric terminal event. Membrane lipid peroxidation and polyphenol oxidase activity produce stringy brown vascular bundles, tissue liquefaction, and off-flavour volatiles.",
        culinary = "Discard if internal browning, sour or rancid odour, or fungal decay is present. Refrigeration cannot restore expired shelf life.",
        tactile = "Structural collapse under light pressure",
        appearance = "Matte black, often blemished",
        rulAmbient = "0.0 Days",
        rulCold = "0.0 Days",
        rulChilled = "0.0 Days"
    )
)

private val BENCHMARK_METRICS = listOf(
    BenchmarkMetric(
        label = "Phase 1A Exact Stage Match",
        value = "66.15%",
        accent = PrimaryGreen,
        leftFoot = "1,454 of 2,198 samples",
        rightFoot = "Strict Argmax",
        note = "Exact match of the discrete ripening stage with no adjacent-class tolerance on held-out test instances."
    ),
    BenchmarkMetric(
        label = "Within-One-Stage Accuracy",
        value = "98.13%",
        accent = Stage1Fill,
        leftFoot = "2,157 of 2,198 samples",
        rightFoot = "±1 Stage Tolerance",
        note = "98.13% of predictions fall on the true stage or exactly one adjoining transitional stage."
    ),
    BenchmarkMetric(
        label = "Ordinal Error (MAE)",
        value = "0.3576",
        accent = SkyBlue,
        leftFoot = "Stage Deviation",
        rightFoot = "Mean Absolute Error",
        note = "Average prediction divergence is just over one-third of a single developmental ripening stage."
    ),
    BenchmarkMetric(
        label = "Phase 1B RUL MAE",
        value = "1.7485 d",
        accent = PrimaryDark,
        leftFoot = "≈41.9 hours deviation",
        rightFoot = "Median: 1.2252 d",
        note = "Mean absolute error in forecasting the number of days remaining until Stage 5, on untouched test specimens."
    ),
    BenchmarkMetric(
        label = "RUL Test RMSE",
        value = "2.4568 d",
        accent = Stage3Fill,
        leftFoot = "Root Mean Square Error",
        rightFoot = "Variance Dispersion",
        note = "Penalises outlier deviations, confirming errors stay tightly bounded across both cold and room regimens."
    ),
    BenchmarkMetric(
        label = "Model Determination (R²)",
        value = "0.8420",
        accent = Forest,
        leftFoot = "Holdout Correlation",
        rightFoot = "84.2% Variance",
        note = "84.2% of post-harvest shelf-life variance is captured by coupling the vision logits with the storage condition."
    )
)

private val STRATIFIED_ROWS = listOf(
    StratifiedRow(
        condition = "Ambient Room Storage",
        temperature = "~20–22°C",
        testImages = "374",
        actualMean = "3.48 d",
        predictedMean = "3.71 d",
        mae = "0.93 d",
        rmse = "1.16 d",
        r2 = "0.8399",
        status = "Empirical",
        isExtrapolated = false
    ),
    StratifiedRow(
        condition = "Controlled Room Storage",
        temperature = "20.0°C",
        testImages = "460",
        actualMean = "4.00 d",
        predictedMean = "3.52 d",
        mae = "1.02 d",
        rmse = "1.30 d",
        r2 = "0.8131",
        status = "Empirical",
        isExtrapolated = false
    ),
    StratifiedRow(
        condition = "Crisper Cold Storage",
        temperature = "10.0°C",
        testImages = "1,278",
        actualMean = "8.49 d",
        predictedMean = "9.00 d",
        mae = "2.25 d",
        rmse = "2.99 d",
        r2 = "0.8151",
        status = "Empirical",
        isExtrapolated = false
    ),
    StratifiedRow(
        condition = "Domestic Refrigerator",
        temperature = "4.0°C",
        testImages = "N/A (Simulation)",
        actualMean = "N/A",
        predictedMean = "Simulated",
        mae = "Kinetic Q₁₀ = 2.38",
        rmse = "N/A",
        r2 = "N/A",
        status = "Extrapolated*",
        isExtrapolated = true
    )
)

private val LIMITATIONS = listOf(
    LimitationItem(
        title = "1. Relative Humidity (RH) Not Measured",
        body = "The pipeline assumes standard ambient or crisper humidity. Low humidity accelerates moisture desiccation, causing skin shrivelling before physiological ripening; high humidity fosters fungal proliferation. Neither can be estimated from temperature alone.",
        accent = Amber500,
        icon = Icons.Default.WarningAmber
    ),
    LimitationItem(
        title = "2. Packaging / MAP Not Modeled",
        body = "Modified atmosphere packaging, sealed wraps, and edible surface coatings alter oxygen and carbon dioxide permeation, shifting respiration kinetics. FreshIQ assumes standard atmospheric air storage with no artificial gas buffering.",
        accent = Amber500,
        icon = Icons.Default.Inventory2
    ),
    LimitationItem(
        title = "3. 4°C is Extrapolated",
        body = "The camera monitoring experiment recorded sequences exclusively at 10°C, 20°C, and ambient temperature. Predictions for 4°C domestic refrigeration are derived mathematically through Arrhenius Q₁₀ modeling, not from direct observation.",
        accent = SkyBlue,
        icon = Icons.Default.DeviceThermostat
    ),
    LimitationItem(
        title = "4. Dataset is Avocado-Specific",
        body = "Both the neural backbone and the gradient boosting model are trained exclusively on Persea americana (Hass cultivar). Predictions do not generalise to other climacteric fruit or to non-climacteric produce without dedicated data collection and retraining.",
        accent = Violet600,
        icon = Icons.Default.Storage
    ),
    LimitationItem(
        title = "5. 4°C is Not Direct Ground Truth",
        body = "Prolonged chilling below 5°C introduces chilling injury: polyphenol oxidase drives internal vascular browning and bitter off-flavours even while the exterior still looks fresh. The 4°C projection is capped at 21 days to reflect this physiological ceiling.",
        accent = Red600,
        icon = Icons.Default.Shield
    )
)

// =========================================================================
// Screen
// =========================================================================

@Composable
fun InsightsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedStage by remember { mutableIntStateOf(3) }
    var activeCurve by remember { mutableStateOf(CurveFilter.All) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPage),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.space2xl)
    ) {
        item { InsightsHeader() }

        item { PipelineSection() }

        item {
            RipeningStagesSection(
                selectedStage = selectedStage,
                onSelectStage = { selectedStage = it }
            )
        }

        item { RulModelingSection() }

        item {
            DegradationTrajectoriesSection(
                activeCurve = activeCurve,
                onSelectCurve = { activeCurve = it }
            )
        }

        item { BenchmarksSection() }

        item { LimitationsSection() }

        item { ResponsibleAiSection() }

        item { CtaBanner(navController = navController) }

        item { Spacer(modifier = Modifier.height(FreshIQSpacing.spaceXl)) }
    }
}

// =========================================================================
// Header
// =========================================================================

@Composable
private fun InsightsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusFull))
                .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "MODEL TRANSPARENCY & SCIENTIFIC ARCHITECTURE",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark,
                letterSpacing = 0.4.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Academic & Benchmark Report v1.2",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = EmeraldDeep,
            modifier = Modifier
                .background(EmeraldTint, RoundedCornerShape(FreshIQRadius.radiusFull))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ML Insights & Degradation Science",
            style = FreshIQTypography.displayLarge.copy(
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "A transparent look at how FreshIQ combines a lightweight convolutional neural network with post-harvest respiration kinetics to estimate discrete developmental stages, a continuous ripening index, and multi-scenario Remaining Usable Life.",
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryPill("MobileNetV3-Small backbone", Icons.Default.Memory, PrimaryDark)
            TelemetryPill("Exact 66.15% | ±1 Stage 98.13%", Icons.Default.EmojiEvents, PrimaryGreen)
            TelemetryPill("HistGB RUL MAE 1.7485 d", Icons.Default.Timeline, SkyBlue)
            TelemetryPill("Arrhenius Q₁₀ = 2.38", Icons.Default.DeviceThermostat, Amber600)
            TelemetryPill("478 specimens / 14,710 observations", Icons.Default.Storage, Violet600)
        }
    }
}

@Composable
private fun TelemetryPill(
    text: String,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .background(BgCard, RoundedCornerShape(FreshIQRadius.radiusFull))
            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Forest,
            maxLines = 1,
            softWrap = false
        )
    }
}

// =========================================================================
// Shared section scaffolding
// =========================================================================

@Composable
private fun SectionIntro(
    eyebrow: String,
    title: String,
    body: String,
    eyebrowColor: Color = PrimaryDark
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = eyebrow.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = eyebrowColor,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = FreshIQTypography.headlineMedium.copy(fontSize = 21.sp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun MonoFooterChip(
    text: String,
    background: Color = BgSubtle,
    textColor: Color = PrimaryDark
) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier
            .background(background, RoundedCornerShape(FreshIQRadius.radiusSm))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun FormulaBlock(
    formula: String,
    accent: Color = PrimaryGreen
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
            .padding(start = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .background(accent, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = formula,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Forest,
                modifier = Modifier.padding(vertical = 10.dp)
            )
        }
    }
}

// =========================================================================
// Section 1 — Inference & Degradation Pipeline
// =========================================================================

@Composable
private fun PipelineSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionIntro(
            eyebrow = "System Architecture",
            title = "1. Inference & Degradation Pipeline",
            body = "FreshIQ couples edge-deployable deep learning with post-harvest biological kinetics across five modular phases."
        )

        PIPELINE_PHASES.forEach { phase ->
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = phase.phase,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(BgSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = phase.icon,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = phase.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Forest
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = phase.body,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = TextMuted
                    )

                    if (phase.formula != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FormulaBlock(formula = phase.formula)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MonoFooterChip(text = phase.footer)
                }
            }
        }
    }
}

// =========================================================================
// Section 2 — Five Ripening Stages
// =========================================================================

@Composable
private fun RipeningStagesSection(
    selectedStage: Int,
    onSelectStage: (Int) -> Unit
) {
    val stage = STAGE_DATA[selectedStage - 1]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionIntro(
            eyebrow = "Taxonomic Reference",
            title = "2. Five Ripening Stages of Hass Avocado",
            body = "The five-stage ordinal scale the classifier is trained against. Select a stage to inspect its physiological profile."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            STAGE_DATA.forEach { item ->
                val isSelected = item.stage == selectedStage
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(if (isSelected) item.color else BgCard)
                        .border(
                            width = if (isSelected) 0.dp else 1.dp,
                            color = if (isSelected) Color.Transparent else BorderLight,
                            shape = RoundedCornerShape(FreshIQRadius.radiusFull)
                        )
                        .clickable { onSelectStage(item.stage) }
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else item.color)
                    )
                    Text(
                        text = "Stage ${item.stage}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Forest,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        FreshCard(
            modifier = Modifier.fillMaxWidth(),
            variant = FreshCardVariant.Subtle
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(30.dp)
                            .background(stage.color, RoundedCornerShape(2.dp))
                    )
                    Column {
                        Text(
                            text = stage.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Forest
                        )
                        Text(
                            text = stage.subname,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = stage.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stage.description,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                StageAttributeRow(label = "Tactile Response", value = stage.tactile)
                Spacer(modifier = Modifier.height(6.dp))
                StageAttributeRow(label = "Visual Appearance", value = stage.appearance)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "CULINARY DIRECTIVE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stage.culinary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = TextMain
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "EXPECTED SHELF LIFESPAN (REFERENCE)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                StageAttributeRow(label = "Ambient (~20°C)", value = stage.rulAmbient)
                Spacer(modifier = Modifier.height(4.dp))
                StageAttributeRow(label = "Crisper Cold (10°C)", value = stage.rulCold)
                Spacer(modifier = Modifier.height(4.dp))
                StageAttributeRow(label = "Chilled (4°C*)", value = stage.rulChilled)

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Stage descriptors are qualitative reference material. FreshIQ does not measure firmness, hue angle, or lipid content — a numeric shelf-life estimate for a real specimen is produced by the backend when you scan it.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextMuted,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun StageAttributeRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Forest,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

// =========================================================================
// Section 3 — RUL Modeling
// =========================================================================

@Composable
private fun RulModelingSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionIntro(
            eyebrow = "Predictive Shelf-Life Formulation",
            title = "3. Remaining Usable Life (RUL) Modeling",
            body = "RUL is formulated not as an arbitrary countdown but as the duration in days until a specimen reaches the irreversible terminal event of Stage 5."
        )

        // Card A — RUL concept
        FreshCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CardHeading(
                    title = "The RUL Concept & Terminal Event",
                    icon = Icons.Default.Schedule,
                    tint = PrimaryGreen
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "For any observation of specimen i imaged at observation day d:",
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormulaBlock(formula = "RUL_{i,d} = max(0, D_{stage5,i} − d)")

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "D_{stage5,i} is the recorded day on which specimen i first entered Stage 5. Stage 4 is treated strictly as an active, edible culinary phase and is never conflated with the terminal event.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                CheckFooter(text = "Strictly non-negative target; Stage 5 terminal ground truth = 0.0 days.")
            }
        }

        // Card B — degradation velocity
        FreshCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CardHeading(
                    title = "Visual Stage vs. Degradation Velocity",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    tint = Stage3Fill
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Degradation is non-linear. The relationship between visual ripening stage and remaining usable life follows an asymptotic deceleration:",
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                VelocityBullet(
                    lead = "Stage 1 → 2:",
                    body = "Extended pre-climacteric latency. The exocarp stays green with minimal softening."
                )
                VelocityBullet(
                    lead = "Stage 2 → 4:",
                    body = "Climacteric surge. Respiration rises sharply and softening accelerates."
                )
                VelocityBullet(
                    lead = "Stage 4 → 5:",
                    body = "Terminal cliff. Pectin dissolves and sensory shelf life falls away quickly."
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Non-linear response modeled via HistGradientBoosting decision trees.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    color = TextMuted
                )
            }
        }

        // Card C — leakage-safe partitioning (CORRECTED COUNTS)
        FreshCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CardHeading(
                    title = "Leakage-Safe Specimen Partitioning",
                    icon = Icons.Default.Storage,
                    tint = SkyBlue
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Longitudinal tracking produces repeated photographs of the same specimen on consecutive days, so a random split would leak the same fruit across partitions. Splitting is therefore done at specimen level:",
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SplitRow(label = "Train", specimens = "334 specimens", rows = "9,370 uncensored rows")
                    SplitRow(label = "Validation", specimens = "71 specimens", rows = "2,122 uncensored rows")
                    SplitRow(label = "Holdout Test", specimens = "73 specimens", rows = "2,112 uncensored rows")

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "478 specimens / 14,710 observations in total. Regression is fitted only on uncensored specimens — those with an observed Stage 5 transition — so the row counts above are smaller than the full image counts. The classifier's held-out test set is 2,198 images.",
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                CheckFooter(text = "Zero specimen overlap across all three splits.")
            }
        }
    }
}

@Composable
private fun SplitRow(label: String, specimens: String, rows: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
        )
        Text(
            text = "$specimens · $rows",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Forest
        )
    }
}

@Composable
private fun VelocityBullet(lead: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(PrimaryGreen)
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Forest)) {
                    append("$lead ")
                }
                append(body)
            },
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun CardHeading(
    title: String,
    icon: ImageVector,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Forest
        )
    }
}

@Composable
private fun CheckFooter(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(13.dp)
        )
        Text(
            text = text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryDark,
            lineHeight = 16.sp
        )
    }
}

// =========================================================================
// Section 4 — Degradation Trajectories
// =========================================================================

@Composable
private fun DegradationTrajectoriesSection(
    activeCurve: CurveFilter,
    onSelectCurve: (CurveFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionIntro(
            eyebrow = "Biophysical Comparison",
            title = "4. Degradation Trajectories by Storage Condition",
            body = "Storage temperature governs respiration velocity. In the source experiment, produce held at ambient 20°C progressed from Stage 1 to Stage 5 in roughly 8–10 days, while 10°C cold storage extended that to roughly 22–26 days."
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurveFilter.entries.forEach { mode ->
                val isSelected = mode == activeCurve
                Text(
                    text = mode.label,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else TextMain,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(if (isSelected) Forest else BgSubtle)
                        .clickable { onSelectCurve(mode) }
                        .padding(horizontal = 13.dp, vertical = 7.dp)
                )
            }
        }

        FreshCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "KINETIC RIPENING CURVES",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ripening Progression by Thermal Regimen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Forest
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Schematic representation of relative progression rates — illustrative shapes, not plotted measurements.",
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp,
                    color = TextMuted,
                    fontStyle = FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(12.dp))

                RipeningCurvesChart(activeCurve = activeCurve)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendSwatch(color = Forest, label = "20°C")
                    LegendSwatch(color = PrimaryDark, label = "10°C")
                    LegendSwatch(color = SkyBlue, label = "4°C*")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "R(T) = R₀ × Q₁₀^((T − T₀)/10)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Q₁₀ = 2.38",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                    }
                }
            }
        }

        ConditionCard(
            title = "Ambient (~20–22°C) & 20°C Controlled",
            badge = "Empirical Ground Truth",
            badgeBg = PrimaryLight,
            badgeText = Stage1Text,
            body = "Directly represented in the training data. Specimens progressed from Stage 1 to Stage 5 in roughly 8–10 days, with the optimal eating window occupying a short span within that.",
            isExtrapolated = false
        )

        ConditionCard(
            title = "10°C Crisper / Cold Storage",
            badge = "Empirical Ground Truth",
            badgeBg = PrimaryLight,
            badgeText = Stage1Text,
            body = "Also directly observed. Cold storage extended total usable lifespan to roughly 22–26 days, and the model reaches 2.25 d MAE on this condition in the holdout test set.",
            isExtrapolated = false
        )

        ConditionCard(
            title = "4°C Domestic Refrigerator",
            badge = "Extrapolated Q₁₀ Model",
            badgeBg = BlueIceBadge,
            badgeText = BlueIceBadgeText,
            body = "4°C was never observed in the camera-monitored dataset. It is modeled through biophysical Arrhenius Q₁₀ = 2.38 extrapolation and capped at 21 days to reflect sub-5°C chilling injury risk.",
            isExtrapolated = true
        )

        ExtrapolationNoticeCard()
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(3.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun ConditionCard(
    title: String,
    badge: String,
    badgeBg: Color,
    badgeText: Color,
    body: String,
    isExtrapolated: Boolean
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = if (isExtrapolated) FreshCardVariant.Extrapolated4C else FreshCardVariant.Default
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isExtrapolated) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = badgeText,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isExtrapolated) BlueIceText else Forest,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = badgeText,
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(FreshIQRadius.radiusSm))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = body,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = if (isExtrapolated) BlueIceText else TextMuted
            )
        }
    }
}

@Composable
private fun RipeningCurvesChart(activeCurve: CurveFilter) {
    val stageLabels = listOf("S5", "S4", "S3", "S2", "S1")

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .width(26.dp)
                .height(180.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            stageLabels.forEach { label ->
                Text(
                    text = label,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TextSubtle
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(180.dp)
                .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
                .padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val dashed = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)

                // Horizontal grid lines (one per stage band)
                for (i in 0..4) {
                    val y = h * (i / 4f)
                    drawLine(
                        color = if (i == 4) GridStrong else GridSoft,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(w, y),
                        strokeWidth = if (i == 4) 1.5f else 1f,
                        pathEffect = if (i == 4) null else dashed
                    )
                }

                // Optimal culinary band (Stage 3 -> Stage 4)
                drawRect(
                    color = CulinaryBand,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, h * 0.25f),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.25f)
                )

                // Curves are expressed in the same normalized space as the web SVG:
                // x in [0,1] left-to-right over the 20-day horizon,
                // y in [0,1] where 0 = Stage 5 (top) and 1 = Stage 1 (bottom).
                fun px(nx: Float) = nx * w
                fun py(ny: Float) = ny * h

                fun curve(
                    p0: Pair<Float, Float>,
                    c1: Pair<Float, Float>,
                    c2: Pair<Float, Float>,
                    p1: Pair<Float, Float>,
                    c3: Pair<Float, Float>,
                    c4: Pair<Float, Float>,
                    p2: Pair<Float, Float>
                ): Path = Path().apply {
                    moveTo(px(p0.first), py(p0.second))
                    cubicTo(
                        px(c1.first), py(c1.second),
                        px(c2.first), py(c2.second),
                        px(p1.first), py(p1.second)
                    )
                    cubicTo(
                        px(c3.first), py(c3.second),
                        px(c4.first), py(c4.second),
                        px(p2.first), py(p2.second)
                    )
                }

                val showAmbient = activeCurve == CurveFilter.All || activeCurve == CurveFilter.Ambient
                val show10 = activeCurve == CurveFilter.All || activeCurve == CurveFilter.Cold10
                val show4 = activeCurve == CurveFilter.All || activeCurve == CurveFilter.Chilled4

                if (showAmbient) {
                    drawPath(
                        path = curve(
                            0f to 1f, 0.111f to 0.972f, 0.178f to 0.722f, 0.267f to 0.5f,
                            0.333f to 0.25f, 0.389f to 0.028f, 0.422f to 0f
                        ),
                        color = Forest,
                        style = Stroke(width = 3f)
                    )
                }

                if (show10) {
                    drawPath(
                        path = curve(
                            0f to 1f, 0.178f to 0.989f, 0.356f to 0.833f, 0.511f to 0.5f,
                            0.644f to 0.25f, 0.733f to 0.083f, 0.822f to 0f
                        ),
                        color = PrimaryDark,
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    )
                }

                if (show4) {
                    drawPath(
                        path = curve(
                            0f to 1f, 0.244f to 1f, 0.578f to 0.917f, 0.778f to 0.611f,
                            0.889f to 0.389f, 0.944f to 0.194f, 1f to 0.028f
                        ),
                        color = SkyBlue,
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp, top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("Day 0", "Day 4", "Day 8", "Day 12", "Day 16", "Day 20").forEach { label ->
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = TextSubtle
            )
        }
    }
}

// =========================================================================
// Section 5 — Model Performance Benchmarks
// =========================================================================

@Composable
private fun BenchmarksSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionIntro(
            eyebrow = "Holdout Evaluation",
            title = "5. Model Performance Benchmarks",
            body = "Verified test-set metrics on 73 untouched test specimens — 2,198 images for the classifier and 2,112 uncensored rows for the shelf-life regressor."
        )

        BENCHMARK_METRICS.forEach { metric ->
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(metric.accent, RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = metric.label.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = metric.value,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = metric.accent,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = metric.leftFoot,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = metric.rightFoot,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = metric.accent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = metric.note,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Text(
            text = "RUL Performance Stratified by Storage Condition",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Forest,
            modifier = Modifier.padding(top = 4.dp)
        )

        STRATIFIED_ROWS.forEach { row ->
            FreshCard(
                modifier = Modifier.fillMaxWidth(),
                variant = if (row.isExtrapolated) FreshCardVariant.Extrapolated4C else FreshCardVariant.Default
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = row.condition,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (row.isExtrapolated) BlueIceText else Forest
                            )
                            Text(
                                text = row.temperature,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = row.status,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (row.isExtrapolated) BlueIceBadgeText else Stage1Text,
                            modifier = Modifier
                                .background(
                                    if (row.isExtrapolated) BlueIceBadge else PrimaryLight,
                                    RoundedCornerShape(FreshIQRadius.radiusSm)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    StatLine(label = "Test Images", value = row.testImages)
                    StatLine(label = "Actual RUL Mean", value = row.actualMean)
                    StatLine(label = "Predicted Mean", value = row.predictedMean)
                    StatLine(
                        label = "MAE",
                        value = row.mae,
                        emphasise = true,
                        emphasisColor = if (row.isExtrapolated) BlueIceBadgeText else PrimaryDark
                    )
                    StatLine(label = "RMSE", value = row.rmse)
                    StatLine(label = "R² Score", value = row.r2)
                }
            }
        }

        Text(
            text = "* The 4°C row carries no empirical error metrics because no 4°C data was ever collected. Its value is produced by kinetic extrapolation, not by measurement.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = TextMuted,
            fontStyle = FontStyle.Italic
        )
    }
}

@Composable
private fun StatLine(
    label: String,
    value: String,
    emphasise: Boolean = false,
    emphasisColor: Color = PrimaryDark
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = if (emphasise) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasise) emphasisColor else Forest
        )
    }
}

// =========================================================================
// Section 6 — Scientific Limitations
// =========================================================================

@Composable
private fun LimitationsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SectionIntro(
            eyebrow = "Academic Transparency",
            title = "6. Scientific Limitations",
            body = "To maintain research transparency, the following experimental boundaries and modeling constraints should be understood.",
            eyebrowColor = Red600
        )

        LIMITATIONS.forEach { limitation ->
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(46.dp)
                            .background(limitation.accent, RoundedCornerShape(2.dp))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(
                                imageVector = limitation.icon,
                                contentDescription = null,
                                tint = limitation.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = limitation.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Forest
                            )
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Text(
                            text = limitation.body,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Section 7 — Responsible AI & Food-Safety Protocol
// =========================================================================

@Composable
private fun ResponsibleAiSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Red600,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "7. RESPONSIBLE AI & FOOD-SAFETY PROTOCOL",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Forest,
                letterSpacing = 0.5.sp
            )
        }

        FreshCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = RedTint,
            borderColor = RedTintBorder
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(58.dp)
                        .background(Red600, RoundedCornerShape(2.dp))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sensory Inspection Precedence & Non-Medical Disclaimer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        lineHeight = 20.sp,
                        color = Stage5Fill
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("FreshIQ communicates every estimate strictly as ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("“estimated remaining usable shelf life”")
                            }
                            append(". The platform never claims produce is guaranteed fresh, safe to eat for a given number of days, or free from foodborne pathogens.")
                        },
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp,
                        color = RedDeepText
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("Computer vision of fruit skin cannot detect internal bacterial contamination, fermentation, fungal rot, or chemical spoilage. ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Always apply physical sensory inspection: discard produce showing surface mould, off-odours, skin breakage, or uncharacteristic softness, regardless of any numeric estimate.")
                            }
                        },
                        fontSize = 12.5.sp,
                        lineHeight = 19.sp,
                        color = RedDeepText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xBFFFFFFF), RoundedCornerShape(FreshIQRadius.radiusSm))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Protocol: an academic decision-support tool for household food-waste reduction, designed in line with responsible AI transparency guidelines.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            lineHeight = 15.sp,
                            color = Stage5Fill
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        FoodSafetyDisclaimer()
    }
}

// =========================================================================
// CTA Banner
// =========================================================================

@Composable
private fun CtaBanner(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(FreshIQRadius.radiusXl))
            .background(Forest)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PIPELINE IN ACTION",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LimeGlow,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Experience the Pipeline in Action",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 25.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Run live inference on your own produce photo, or simulate a change of storage conditions in the What-If Lab.",
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = Color(0xD9FFFFFF)
            )

            Spacer(modifier = Modifier.height(18.dp))

            FreshAccentButton(
                text = "Scan Produce",
                icon = Icons.Default.CameraAlt,
                onClick = { navController.navigate(Screen.Scan.route) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            FreshSecondaryButton(
                text = "What-If Storage Lab",
                icon = Icons.Default.Tune,
                onClick = { navController.navigate(Screen.WhatIf.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =========================================================================
// Preview
// =========================================================================

@Preview(showBackground = true)
@Composable
fun InsightsScreenPreview() {
    FreshIQTheme {
        InsightsScreen(navController = rememberNavController())
    }
}
