package com.freshiq.app.ui.screens.analysis

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.domain.model.BananaRipenessStage
import com.freshiq.app.domain.model.MangoRipenessStage
import com.freshiq.app.domain.model.ProduceType
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.OpticalReticleOverlay
import com.freshiq.app.ui.components.RipenessBadge
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BgPage
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BlueIceAccent
import com.freshiq.app.ui.theme.BlueIceBadge
import com.freshiq.app.ui.theme.BlueIceBadgeText
import com.freshiq.app.ui.theme.BlueIceBg
import com.freshiq.app.ui.theme.BlueIceBorder
import com.freshiq.app.ui.theme.BlueIceText
import com.freshiq.app.ui.theme.BorderAccent
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.LimeLight
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
import com.freshiq.app.ui.theme.TextSubtle
import java.util.Locale

// ---------------------------------------------------------------------------
// Design System Constants mirroring frontend/src/pages/AnalysisPage.tsx
// ---------------------------------------------------------------------------
private val STAGE_COLORS = mapOf(
    1 to Color(0xFF16A34A), // Underripe Green
    2 to Color(0xFF65A30D), // Breaking Olive
    3 to Color(0xFFCA8A04), // Firm Ripe Amber
    4 to Color(0xFFEA580C), // Peak Soft Ripe Orange
    5 to Color(0xFF991B1B)  // Overripe Senescent Crimson
)

private val STAGE_INTERPRETATIONS = mapOf(
    1 to "Firm botanical condition with rigid exocarp and high cellular firmness. Starches intact with slow ripening progression. Ideal for extended pantry storage or long-distance transport.",
    2 to "Breaking maturity exhibiting initial skin color transition from bright emerald to dark olive, with slight yielding under firm pressure. Ethylene synthesis actively initiating.",
    3 to "Firm-ripe botanical condition with creamy, buttery lipid texture beginning. Exocarp yields with gentle pressure; ideal structural integrity for crisp slicing and fresh preparation.",
    4 to "Soft-ripe peak eating window; rich, buttery texture and full varietal aroma. Exocarp yields easily to light thumb pressure. Consume soon for optimal flavor and culinary enjoyment.",
    5 to "Terminal senescent botanical state. Soft overripe exocarp with progressive loss of cellular pectin and lipid integrity. Must be consumed immediately if sensory checks pass."
)

private data class CulinarySuitabilityInfo(val title: String, val subtitle: String)

private val CULINARY_SUITABILITY = mapOf(
    1 to CulinarySuitabilityInfo("Storage & Holding", "Room temp ripening: 5–8d"),
    2 to CulinarySuitabilityInfo("Prep Planning", "Ready for cutting: 2–3d"),
    3 to CulinarySuitabilityInfo("Slicing & Dicing", "Salads & Sandwiches: Now"),
    4 to CulinarySuitabilityInfo("Guacamole & Spreads", "Peak flavor: Next 24–48h"),
    5 to CulinarySuitabilityInfo("Smoothies or Baking", "Check for spoilage first")
)

@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activePrediction by viewModel.activePrediction.collectAsState()
    val activeImageUri by viewModel.activeImageUri.collectAsState()
    val activeProduceType by viewModel.activeProduceType.collectAsState()
    val isSavedToHistory by viewModel.isSavedToHistory.collectAsState()
    val isGeneratingDemo by viewModel.isGeneratingDemo.collectAsState()
    val demoError by viewModel.demoError.collectAsState()
    val recentScans by viewModel.recentScans.collectAsState()

    var showReticleOverlay by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        val prediction = activePrediction

        if (prediction == null) {
            // -------------------------------------------------------------
            // EMPTY STATE: No active scan session
            // -------------------------------------------------------------
            AnalysisEmptyState(
                navController = navController,
                isGeneratingDemo = isGeneratingDemo,
                demoError = demoError,
                recentScans = recentScans,
                onRunDemoScan = { viewModel.runDemoScan(3) },
                onLoadRecentScan = { viewModel.loadHistoricalScan(it) }
            )
        } else {
            // -------------------------------------------------------------
            // ACTIVE DIAGNOSTIC RESULTS
            // -------------------------------------------------------------
            AnalysisContent(
                prediction = prediction,
                imageUri = activeImageUri,
                showReticleOverlay = showReticleOverlay,
                onToggleReticle = { showReticleOverlay = !showReticleOverlay },
                isSavedToHistory = isSavedToHistory,
                onSaveToHistory = { viewModel.saveToHistory() },
                onShare = { shareDiagnostic(context, prediction) },
                navController = navController,
                produceType = activeProduceType
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisEmptyState(
    navController: NavController,
    isGeneratingDemo: Boolean,
    demoError: String?,
    recentScans: List<ScanHistoryEntity>,
    onRunDemoScan: () -> Unit,
    onLoadRecentScan: (ScanHistoryEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            FreshCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PrimaryDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "No Active Produce Scan Result",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "To inspect MobileNetV3 ripening stage classification, confidence distributions, and What-If shelf-life projections, please upload or photograph an avocado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Primary CTA: Scan Produce
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(PrimaryGreen)
                                .clickable { navController.navigate(Screen.Scan.route) }
                                .padding(horizontal = 22.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Go to Produce Scanner",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Secondary CTA: Demo Scan
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(Color.White)
                                .clickable(enabled = !isGeneratingDemo) { onRunDemoScan() }
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isGeneratingDemo) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryGreen
                                )
                                Text(
                                    text = "Analyzing Demo...",
                                    color = TextMain,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = PrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Run Live Demo Scan (Stage 3)",
                                    color = TextMain,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (demoError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFF991B1B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = demoError,
                                color = Color(0xFF991B1B),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Recent scans quick load
                    if (recentScans.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(28.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderLight)
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OR LOAD FROM RECENT SCANS (${recentScans.size})",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = "View All History →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark,
                                modifier = Modifier.clickable { navController.navigate(Screen.History.route) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        recentScans.take(3).forEach { scan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                                    .background(BgSubtle)
                                    .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusSm))
                                    .clickable { onLoadRecentScan(scan) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(BorderLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (scan.thumbnailPath.isNotBlank()) {
                                        AsyncImage(
                                            model = scan.thumbnailPath,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Scan ${scan.id.take(8)}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = TextMain
                                    )
                                    Text(
                                        text = "Condition: ${scan.storageCondition}",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisContent(
    prediction: PredictionResponseDto,
    imageUri: String?,
    showReticleOverlay: Boolean,
    onToggleReticle: () -> Unit,
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onShare: () -> Unit,
    navController: NavController,
    produceType: ProduceType = ProduceType.AVOCADO
) {
    val effectiveProduce = when {
        prediction.foodType.lowercase().trim() == "banana" -> ProduceType.BANANA
        prediction.foodType.lowercase().trim() == "mango" -> ProduceType.MANGO
        produceType != ProduceType.AVOCADO -> produceType
        else -> ProduceType.fromBackendValue(prediction.foodType)
    }

    when (effectiveProduce) {
        ProduceType.BANANA -> {
            BananaAnalysisContent(
                prediction = prediction,
                imageUri = imageUri,
                showReticleOverlay = showReticleOverlay,
                onToggleReticle = onToggleReticle,
                isSavedToHistory = isSavedToHistory,
                onSaveToHistory = onSaveToHistory,
                onShare = onShare,
                navController = navController
            )
            return
        }
        ProduceType.MANGO -> {
            MangoAnalysisContent(
                prediction = prediction,
                imageUri = imageUri,
                showReticleOverlay = showReticleOverlay,
                onToggleReticle = onToggleReticle,
                isSavedToHistory = isSavedToHistory,
                onSaveToHistory = onSaveToHistory,
                onShare = onShare,
                navController = navController
            )
            return
        }
        ProduceType.AVOCADO -> {
            if (!prediction.rulAvailable) {
                MangoAnalysisContent(
                    prediction = prediction,
                    imageUri = imageUri,
                    showReticleOverlay = showReticleOverlay,
                    onToggleReticle = onToggleReticle,
                    isSavedToHistory = isSavedToHistory,
                    onSaveToHistory = onSaveToHistory,
                    onShare = onShare,
                    navController = navController
                )
                return
            }
        }
    }

    val stage = prediction.ripeness.predictedRipeningStage
    val stageLabel = prediction.ripeness.stageLabel
    val confidence = prediction.ripeness.confidence
    val expectedContinuous = prediction.ripeness.expectedContinuousRipeningStage ?: (stage.toFloat())
    val probs = prediction.ripeness.probabilities
    val scenarios = prediction.scenarios ?: emptyMap()

    val isTerminalStage5 = stage == 5
    val interpretation = STAGE_INTERPRETATIONS[stage] ?: STAGE_INTERPRETATIONS[3]!!
    val culinary = CULINARY_SUITABILITY[stage] ?: CULINARY_SUITABILITY[3]!!

    val ambientRul = if (isTerminalStage5) 0.0f else (scenarios["ambient"]?.estimatedRulDays ?: 0.0f)
    val room20Rul = if (isTerminalStage5) 0.0f else (scenarios["20C"]?.estimatedRulDays ?: 0.0f)
    val cold10Rul = if (isTerminalStage5) 0.0f else (scenarios["10C"]?.estimatedRulDays ?: 0.0f)
    val fridge4Scenario = scenarios["4C_refrigerator"]
    val fridge4Rul = if (isTerminalStage5) 0.0f else (fridge4Scenario?.estimatedRulDays ?: 0.0f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -------------------------------------------------------------
        // 1. TOP BREADCRUMB & METADATA BAR
        // -------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(12.dp))
            TopBreadcrumbBar(
                itemName = prediction.itemName,
                onBackToScan = { navController.navigate(Screen.Scan.route) }
            )
        }

        // -------------------------------------------------------------
        // 2. HEADER CALLOUT TITLE & ACTION BUTTONS
        // -------------------------------------------------------------
        item {
            HeaderCalloutSection(
                onShare = onShare,
                isSavedToHistory = isSavedToHistory,
                onSaveToHistory = onSaveToHistory,
                onNavigateWhatIf = { navController.navigate(Screen.WhatIf.route) },
                onNavigateComparison = { navController.navigate(Screen.Comparison.route) }
            )
        }

        // -------------------------------------------------------------
        // 3. SPATIAL BIO-SCAN FEED & SPECIMEN IMAGE
        // -------------------------------------------------------------
        item {
            SpatialBioScanFeedCard(
                imageUri = imageUri,
                stage = stage,
                stageLabel = stageLabel,
                confidence = confidence,
                expectedContinuous = expectedContinuous,
                showReticleOverlay = showReticleOverlay,
                onToggleReticle = onToggleReticle,
                isTerminalStage5 = isTerminalStage5
            )
        }

        // -------------------------------------------------------------
        // 4. CURRENT AMBIENT BASELINE STRIP
        // -------------------------------------------------------------
        item {
            AmbientBaselineStrip()
        }

        // -------------------------------------------------------------
        // 5. PRIMARY RIPENESS DIAGNOSTIC & RUL HERO BANNER
        // -------------------------------------------------------------
        item {
            PrimaryRipenessHeroCard(
                stage = stage,
                stageLabel = stageLabel,
                confidence = confidence,
                expectedContinuous = expectedContinuous,
                interpretation = interpretation,
                ambientRul = ambientRul,
                cold10Rul = cold10Rul,
                refrigerationGain = prediction.refrigerationExtensionGainDays ?: 0f,
                isTerminalStage5 = isTerminalStage5
            )
        }

        // -------------------------------------------------------------
        // 6. 5-STAGE RIPENING SPECTRUM TRAJECTORY
        // -------------------------------------------------------------
        item {
            RipeningSpectrumTrajectoryCard(stage = stage)
        }

        // -------------------------------------------------------------
        // 7. CLASSIFICATION PROBABILITIES (Softmax Spread N=5)
        // -------------------------------------------------------------
        item {
            ClassificationProbabilitiesCard(
                stage = stage,
                probs = probs,
                expectedContinuous = expectedContinuous
            )
        }

        // -------------------------------------------------------------
        // 8. KINETIC DEGRADATION TRAJECTORY (API Data Curve)
        // -------------------------------------------------------------
        item {
            KineticDegradationTrajectoryCard(
                ambientRul = ambientRul,
                cold10Rul = cold10Rul,
                isTerminalStage5 = isTerminalStage5
            )
        }

        // -------------------------------------------------------------
        // 9. REMAINING USABLE LIFE (RUL) EMPIRICAL SCENARIOS
        // -------------------------------------------------------------
        item {
            EmpiricalScenariosCard(
                ambientRul = ambientRul,
                room20Rul = room20Rul,
                cold10Rul = cold10Rul
            )
        }

        // -------------------------------------------------------------
        // 10. VISUALLY SEPARATED 4°C DOMESTIC REFRIGERATOR EXTRAPOLATION
        // -------------------------------------------------------------
        item {
            Fridge4CExtrapolationCard(
                fridge4Rul = fridge4Rul,
                fridge4Scenario = fridge4Scenario,
                isTerminalStage5 = isTerminalStage5
            )
        }

        // -------------------------------------------------------------
        // 11. ACTION PROTOCOL / CHEF & PANTRY ADVISORY
        // -------------------------------------------------------------
        item {
            ChefAdvisoryCard(
                stage = stage,
                recommendation = prediction.actionableRecommendation,
                culinary = culinary,
                isTerminalStage5 = isTerminalStage5,
                isSavedToHistory = isSavedToHistory,
                onSaveToHistory = onSaveToHistory,
                onNavigateComparison = { navController.navigate(Screen.Comparison.route) },
                onNavigateWhatIf = { navController.navigate(Screen.WhatIf.route) },
                onNavigateScan = { navController.navigate(Screen.Scan.route) }
            )
        }

        // -------------------------------------------------------------
        // 12. MANDATORY RESPONSIBLE FOOD SAFETY DISCLAIMER
        // -------------------------------------------------------------
        item {
            FoodSafetyDisclaimer(
                backendDisclaimer = prediction.legalDisclaimer
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// SUBCOMPONENTS
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopBreadcrumbBar(
    itemName: String,
    onBackToScan: () -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.clickable { onBackToScan() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = TextMain,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Scan Produce",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
            }
            Text(text = "/", color = BorderLight, fontSize = 13.sp)
            Text(text = "Analysis Results", fontSize = 13.sp, color = TextMain)
            Text(text = "/", color = BorderLight, fontSize = 13.sp)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(BgSubtle)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = itemName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(PrimaryLight)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryDark,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "AI Assessment Complete",
                    color = PrimaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "MobileNetV3-Small",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderCalloutSection(
    onShare: () -> Unit,
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onNavigateWhatIf: () -> Unit,
    onNavigateComparison: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(LimeLight)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "PERSEA AMERICANA • HASS AVOCADO",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF365314)
                )
            }
            Text(
                text = "SHA256 #8aaff3e3",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextMuted
            )
        }

        Text(
            text = "Produce Ripeness Diagnostic",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Forest,
            lineHeight = 34.sp
        )

        // Action Controls Row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Share Diagnostic
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(Color.White)
                    .clickable { onShare() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = TextMain,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Share Diagnostic",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain
                )
            }

            // Save to History
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .border(
                        1.dp,
                        if (isSavedToHistory) PrimaryGreen else BorderLight,
                        RoundedCornerShape(FreshIQRadius.radiusFull)
                    )
                    .background(if (isSavedToHistory) PrimaryLight else Color.White)
                    .clickable { onSaveToHistory() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isSavedToHistory) Icons.Default.BookmarkAdded else Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = if (isSavedToHistory) PrimaryDark else TextMain,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (isSavedToHistory) "Saved to History" else "Save to History",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSavedToHistory) PrimaryDark else TextMain
                )
            }

            // What-If Lab
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(Color.White)
                    .clickable { onNavigateWhatIf() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = TextMain,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "What-If Lab",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain
                )
            }

            // Compare Storage
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(PrimaryGreen)
                    .clickable { onNavigateComparison() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Compare Storage",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun SpatialBioScanFeedCard(
    imageUri: String?,
    stage: Int,
    stageLabel: String,
    confidence: Float,
    expectedContinuous: Float,
    showReticleOverlay: Boolean,
    onToggleReticle: () -> Unit,
    isTerminalStage5: Boolean
) {
    val stageColor = STAGE_COLORS[stage] ?: Color(0xFF16A34A)

    FreshCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSubtle)
                    .border(width = 1.dp, color = BorderLight)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                    )
                    Text(
                        text = "SPECIMEN VISUAL OBSERVATION",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "Visual Inspection Feed",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Image Container with Optical Reticle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(Color(0xFF0F172A))
            ) {
                if (!imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Scanned Specimen",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Optical Reticle Overlay
                if (showReticleOverlay) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val cLen = 24.dp.toPx()
                        val sWidth = 2.dp.toPx()

                        // Dashed bounding box
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        drawRect(
                            color = stageColor.copy(alpha = 0.45f),
                            style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
                        )

                        // 4 Corners
                        // TL
                        drawLine(stageColor, Offset(0f, 0f), Offset(cLen, 0f), sWidth)
                        drawLine(stageColor, Offset(0f, 0f), Offset(0f, cLen), sWidth)
                        // TR
                        drawLine(stageColor, Offset(w, 0f), Offset(w - cLen, 0f), sWidth)
                        drawLine(stageColor, Offset(w, 0f), Offset(w, cLen), sWidth)
                        // BL
                        drawLine(stageColor, Offset(0f, h), Offset(cLen, h), sWidth)
                        drawLine(stageColor, Offset(0f, h), Offset(0f, h - cLen), sWidth)
                        // BR
                        drawLine(stageColor, Offset(w, h), Offset(w - cLen, h), sWidth)
                        drawLine(stageColor, Offset(w, h), Offset(w, h - cLen), sWidth)

                        // Center reticle ring
                        val cx = w / 2f
                        val cy = h / 2f
                        val radius = 54.dp.toPx()
                        drawCircle(
                            color = stageColor.copy(alpha = 0.75f),
                            radius = radius,
                            style = Stroke(width = 1.5.dp.toPx(), pathEffect = dashEffect)
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(cx - radius * 1.3f, cy),
                            end = Offset(cx + radius * 1.3f, cy),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(cx, cy - radius * 1.3f),
                            end = Offset(cx, cy + radius * 1.3f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Top Badges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(Color(0xD90F291E))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(stageColor)
                                    .alpha(pulseAlpha)
                            )
                            Text(
                                text = "VISUAL FOCUS: ACTIVE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(Color.White.copy(alpha = 0.9f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Optical Alignment",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                        }
                    }

                    // Center ROI Label
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 80.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Forest)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SPECIMEN ROI",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.5.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Bottom Overlay Strip: Visual Reference & Layer Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xE00F291E))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "VISUAL MORPHOLOGY REFERENCE",
                                color = LimeGlow,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stageLabel,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Illustrative visual reference guide",
                                color = LimeLight,
                                fontSize = 9.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f))
                                .clickable { onToggleReticle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Toggle Reticle",
                                tint = Forest,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Real Verified Backend Metrics Strip Below Produce
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SensorMetricBox(
                        label = "MODEL CONFIDENCE",
                        value = String.format(Locale.US, "%.1f%%", confidence * 100),
                        color = PrimaryDark,
                        modifier = Modifier.weight(1f)
                    )
                    SensorMetricBox(
                        label = "RIPENING INDEX",
                        value = String.format(Locale.US, "%.2f / 5.0", expectedContinuous),
                        color = Forest,
                        modifier = Modifier.weight(1f)
                    )
                    SensorMetricBox(
                        label = "PREDICTED STAGE",
                        value = "Stage $stage",
                        color = stageColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Visual specimen review corresponding to predicted $stageLabel. Ripeness and shelf-life predictions are computed from deep visual features via MobileNetV3; chemical and penetrometer metrics (such as dry matter % or firmness in N) are not measured.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun SensorMetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
            .background(BgSubtle)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun AmbientBaselineStrip() {
    FreshCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BgSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Current Ambient Baseline",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    Text(
                        text = "Room Storage • ~20°C (68°F) • Nominal Pantry",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                    .background(PrimaryLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "NOMINAL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrimaryRipenessHeroCard(
    stage: Int,
    stageLabel: String,
    confidence: Float,
    expectedContinuous: Float,
    interpretation: String,
    ambientRul: Float,
    cold10Rul: Float,
    refrigerationGain: Float,
    isTerminalStage5: Boolean
) {
    val bgBrush = if (isTerminalStage5) {
        Brush.linearGradient(listOf(Color(0xFFFEF2F2), Color.White))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF0FDF4), Color.White))
    }
    val borderColor = if (isTerminalStage5) Color(0xFFFCA5A5) else BorderAccent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(bgBrush)
            .border(1.5.dp, borderColor, RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Status & Confidence Badges in clean 2-row layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RipenessBadge(stage = stage, size = com.freshiq.app.ui.components.BadgeSize.Compact)
                    if (isTerminalStage5) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFF991B1B),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Terminal Boundary",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(PrimaryLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryDark,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (stage == 4) "Peak Consumption" else "Active Shelf Life",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(Color.White)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", confidence * 100)}% AI Confidence",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(BgSubtle)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Index: ${String.format(Locale.US, "%.2f", expectedContinuous)} / 5.0",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Stage Headline & Interpretation
            Text(
                text = if (stageLabel.startsWith("Stage")) stageLabel else "Stage $stage — $stageLabel",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isTerminalStage5) Color(0xFF991B1B) else Forest
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = interpretation,
                fontSize = 13.5.sp,
                color = TextMain,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // KPI Box: Remaining Usable Life Hero
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                    .background(if (isTerminalStage5) Color(0xFFFFF5F5) else Color.White.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        if (isTerminalStage5) Color(0xFFFED7D7) else BorderLight,
                        RoundedCornerShape(FreshIQRadius.radiusMd)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "ESTIMATED REMAINING USABLE LIFE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTerminalStage5) Color(0xFF991B1B) else TextMuted,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", ambientRul),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isTerminalStage5) Color(0xFF991B1B) else Forest,
                        lineHeight = 44.sp
                    )
                    Text(
                        text = "Days",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "(at Ambient ~20°C Room Temp)",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isTerminalStage5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                            .background(Color(0xFFFEE2E2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(FreshIQRadius.radiusSm))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Terminal State Notice: Produce has reached Stage 5 (Overripe). Refrigeration cannot restore expired shelf life.",
                            color = Color(0xFF991B1B),
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (refrigerationGain > 0f) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                            .background(Color.White)
                            .border(1.dp, BorderAccent, RoundedCornerShape(FreshIQRadius.radiusSm))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = PrimaryDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Refrigerate at 10°C / 4°C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Forest
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("Extends to ~")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = PrimaryDark)) {
                                        append("${String.format(Locale.US, "%.1f", cold10Rul)} Days")
                                    }
                                    append(" (+${String.format(Locale.US, "%.1f", refrigerationGain)}d gain)")
                                },
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RipeningSpectrumTrajectoryCard(stage: Int) {
    val stageColor = STAGE_COLORS[stage] ?: PrimaryGreen

    FreshCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ripening Spectrum Trajectory",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    Text(
                        text = "Dynamic stage progression benchmarked across standard Hass ripening scales.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                        .background(stageColor.copy(alpha = 0.12f))
                        .border(1.dp, stageColor.copy(alpha = 0.3f), RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "STAGE $stage ACTIVE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = stageColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5-Segment Visual Progression Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(BgSubtle)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                (1..5).forEach { s ->
                    val isCurrent = s == stage
                    val isPassed = s < stage
                    val segColor = STAGE_COLORS[s] ?: PrimaryGreen

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(
                                when {
                                    isCurrent -> segColor
                                    isPassed -> segColor.copy(alpha = 0.5f)
                                    else -> BorderLight
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Descriptive Column Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Triple(1, "S1 • Hard", "8–10d"),
                    Triple(2, "S2 • Breaking", "5–7d"),
                    Triple(3, "S3 • Firm-Ripe", "3–4d"),
                    Triple(4, "S4 • Soft Ripe", "1–2d"),
                    Triple(5, "S5 • Senescent", "0d")
                ).forEach { (s, name, rulEst) ->
                    val isCurrent = s == stage
                    val itemColor = STAGE_COLORS[s] ?: PrimaryGreen

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrent) BgSubtle else Color.Transparent)
                            .border(1.dp, if (isCurrent) BorderAccent else Color.Transparent, RoundedCornerShape(6.dp))
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Forest else TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$rulEst RUL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) itemColor else TextSubtle,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassificationProbabilitiesCard(
    stage: Int,
    probs: com.freshiq.app.data.model.StageProbabilitiesDto,
    expectedContinuous: Float
) {
    FreshCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Classification Probabilities",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
                Text(
                    text = "Softmax Spread (N=5)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Full probability distribution across convolutional feature embeddings. Highest certainty concentrated in predicted class.",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            listOf(
                Triple(1, "Stage 1 — Underripe (Hard)", probs.stage1),
                Triple(2, "Stage 2 — Breaking (Olive)", probs.stage2),
                Triple(3, "Stage 3 — Ripe First Stage", probs.stage3),
                Triple(4, "Stage 4 — Ripe Second Stage (Soft Ripe)", probs.stage4),
                Triple(5, "Stage 5 — Overripe (Senescent)", probs.stage5)
            ).forEach { (num, label, probVal) ->
                val isDominant = num == stage
                val barColor = STAGE_COLORS[num] ?: PrimaryGreen
                val percentString = String.format(Locale.US, "%.1f", probVal * 100)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                        .background(if (isDominant) BgSubtle else Color.Transparent)
                        .border(
                            1.dp,
                            if (isDominant) BorderAccent else Color.Transparent,
                            RoundedCornerShape(FreshIQRadius.radiusSm)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isDominant) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(barColor)
                                )
                            }
                            Text(
                                text = label,
                                fontSize = 12.5.sp,
                                fontWeight = if (isDominant) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDominant) Forest else TextMain
                            )
                        }

                        Text(
                            text = "$percentString%",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = if (isDominant) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isDominant) barColor else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isDominant) 8.dp else 6.dp)
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(if (isDominant) BgCard else BgSubtle)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (probVal.coerceIn(0.01f, 1f)))
                                .height(if (isDominant) 8.dp else 6.dp)
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(barColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderLight)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Expected Continuous Ripeness Value:",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text(
                    text = "${String.format(Locale.US, "%.3f", expectedContinuous)} / 5.000",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
            }
        }
    }
}

@Composable
private fun KineticDegradationTrajectoryCard(
    ambientRul: Float,
    cold10Rul: Float,
    isTerminalStage5: Boolean
) {
    FreshCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kinetic Degradation Trajectory",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp, 3.dp).background(Forest, RoundedCornerShape(1.dp)))
                        Text(text = "Ambient (${String.format(Locale.US, "%.1f", ambientRul)}d)", fontSize = 10.5.sp, color = Forest)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp, 3.dp).background(PrimaryGreen, RoundedCornerShape(1.dp)))
                        Text(text = "Chilled (${String.format(Locale.US, "%.1f", cold10Rul)}d)", fontSize = 10.5.sp, color = PrimaryDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Forward-looking shelf-life decay derived directly from AI regression estimates. (No historical points invented).",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Plot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                    .background(BgSubtle)
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val startX = 40.dp.toPx()
                    val endX = w - 24.dp.toPx()
                    val topY = 20.dp.toPx()
                    val bottomY = h - 28.dp.toPx()
                    val plotWidth = endX - startX

                    val maxPlottedDays = maxOf(7f, cold10Rul + 1f)

                    // Grid Lines
                    val gridPaint = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(startX, topY),
                        end = Offset(endX, topY),
                        pathEffect = gridPaint
                    )
                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(startX, (topY + bottomY) / 2),
                        end = Offset(endX, (topY + bottomY) / 2),
                        pathEffect = gridPaint
                    )
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(startX, bottomY),
                        end = Offset(endX, bottomY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Y labels using nativeCanvas
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 24f
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    drawContext.canvas.nativeCanvas.drawText("100%", 8f, topY + 8f, textPaint)
                    drawContext.canvas.nativeCanvas.drawText("50%", 14f, (topY + bottomY) / 2 + 8f, textPaint)
                    drawContext.canvas.nativeCanvas.drawText("0%", 20f, bottomY + 8f, textPaint)

                    if (isTerminalStage5) {
                        // Flat crimson line at bottom
                        drawLine(
                            color = Color(0xFF991B1B),
                            start = Offset(startX, bottomY),
                            end = Offset(endX, bottomY),
                            strokeWidth = 3.dp.toPx()
                        )
                        drawCircle(color = Color(0xFF991B1B), radius = 5.dp.toPx(), center = Offset(startX, bottomY))
                        val labelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#991B1B")
                            textSize = 26f
                            isFakeBoldText = true
                            typeface = android.graphics.Typeface.MONOSPACE
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            "Terminal Maturity (0.0d RUL)",
                            startX + 16f,
                            bottomY - 14f,
                            labelPaint
                        )
                    } else {
                        val ambientFraction = (ambientRul / maxPlottedDays).coerceIn(0f, 1f)
                        val cold10Fraction = (cold10Rul / maxPlottedDays).coerceIn(0f, 1f)

                        val ambientEndX = startX + ambientFraction * plotWidth
                        val cold10EndX = startX + cold10Fraction * plotWidth

                        // Chilled 10C Curve (Dashed PrimaryGreen)
                        val coldPath = Path().apply {
                            moveTo(startX, topY + 15f)
                            quadraticTo(
                                (startX + cold10EndX) / 2,
                                topY + 30f,
                                cold10EndX,
                                bottomY
                            )
                        }
                        drawPath(
                            path = coldPath,
                            color = PrimaryGreen,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                            )
                        )

                        // Ambient 20C Curve (Solid Forest)
                        val ambientPath = Path().apply {
                            moveTo(startX, topY + 15f)
                            quadraticTo(
                                (startX + ambientEndX) / 2,
                                topY + 45f,
                                ambientEndX,
                                bottomY
                            )
                        }
                        drawPath(
                            path = ambientPath,
                            color = Forest,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Scan point marker (Today)
                        drawCircle(color = Forest, radius = 5.dp.toPx(), center = Offset(startX, topY + 15f))
                        drawCircle(
                            color = PrimaryGreen.copy(alpha = 0.6f),
                            radius = 9.dp.toPx(),
                            center = Offset(startX, topY + 15f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Terminal dots & day labels
                        drawCircle(color = Forest, radius = 4.dp.toPx(), center = Offset(ambientEndX, bottomY))
                        val ambientLabelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#0F291E")
                            textSize = 24f
                            isFakeBoldText = true
                            typeface = android.graphics.Typeface.MONOSPACE
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            "${String.format(Locale.US, "%.1f", ambientRul)}d",
                            ambientEndX - 20f,
                            bottomY + 22f,
                            ambientLabelPaint
                        )

                        drawCircle(color = PrimaryGreen, radius = 4.dp.toPx(), center = Offset(cold10EndX, bottomY))
                        val coldLabelPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#15803D")
                            textSize = 24f
                            isFakeBoldText = true
                            typeface = android.graphics.Typeface.MONOSPACE
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            "${String.format(Locale.US, "%.1f", cold10Rul)}d",
                            cold10EndX - 20f,
                            bottomY + 22f,
                            coldLabelPaint
                        )
                    }

                    // Day 0 label
                    drawContext.canvas.nativeCanvas.drawText(
                        "Day 0 (Now)",
                        startX - 10f,
                        bottomY + 22f,
                        textPaint
                    )
                }
            }
        }
    }
}

@Composable
private fun EmpiricalScenariosCard(
    ambientRul: Float,
    room20Rul: Float,
    cold10Rul: Float
) {
    FreshCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Remaining Usable Life Across Storage Scenarios",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Empirical predictions derived from HistGradientBoosting model; 4°C refrigerator simulated via biophysical kinetics.",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EMPIRICAL OBSERVED CONDITIONS (DIRECT MODEL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "EMPIRICAL DATASET",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Empirical Condition Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EmpiricalItemRow(
                    title = "AMBIENT (~20–22°C)",
                    days = ambientRul
                )
                EmpiricalItemRow(
                    title = "CONTROLLED 20°C",
                    days = room20Rul
                )
                EmpiricalItemRow(
                    title = "10°C COLD CRISPER",
                    days = cold10Rul
                )
            }
        }
    }
}

@Composable
private fun EmpiricalItemRow(title: String, days: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
            .background(BgSubtle)
            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = String.format(Locale.US, "%.1f", days),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Forest
                )
                Text(
                    text = "days",
                    fontSize = 13.sp,
                    color = TextMain,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(PrimaryLight)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Empirical ML",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
        }
    }
}

@Composable
private fun Fridge4CExtrapolationCard(
    fridge4Rul: Float,
    fridge4Scenario: com.freshiq.app.data.model.ScenarioRulDto?,
    isTerminalStage5: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
            .background(BlueIceBg)
            .border(1.5.dp, BlueIceBorder, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Title & Extrapolation Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = BlueIceAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "4°C REFRIGERATOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueIceText,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(BlueIceBadge)
                        .border(1.dp, BlueIceBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Model-based extrapolation",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueIceBadgeText
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "is_extrapolated: ${fridge4Scenario?.isExtrapolated ?: true} • Biophysical respiration kinetics",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color(0xFF3B82F6)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Main KPI: Full width unconstrained display
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "EXTRAPOLATED USABLE LIFE (4°C)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BlueIceBadgeText,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = String.format(Locale.US, "%.1f", fridge4Rul),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlueIceText
                    )
                    Text(
                        text = "days",
                        fontSize = 14.sp,
                        color = BlueIceText,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Method: ${fridge4Scenario?.method ?: "Biophysical Arrhenius / Q10 Extrapolation"}",
                    fontSize = 11.sp,
                    color = Color(0xFF3B82F6)
                )
            }

            // Scenario Recommendation Box: Full width underneath KPI
            if (!fridge4Scenario?.recommendation.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Scenario Recommendation:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BlueIceBadgeText
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = fridge4Scenario?.recommendation ?: "",
                            fontSize = 12.sp,
                            color = BlueIceText,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Uncertainty Note Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(FreshIQRadius.radiusSm))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BlueIceText)) {
                                append("Model-Based Extrapolation Notice: ")
                            }
                            if (!fridge4Scenario?.uncertaintyNote.isNullOrBlank()) {
                                append(fridge4Scenario?.uncertaintyNote)
                            } else {
                                append("4°C Domestic Refrigerator shelf-life is mathematically extrapolated via biochemical respiration kinetics (Q10 = 2.38). 4°C is not direct ground-truth training data.")
                            }
                            if (isTerminalStage5) {
                                append(" Produce has already reached senescent maturity (RUL = 0); chilling cannot reverse overripeness.")
                            }
                        },
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    if (!fridge4Scenario?.disclaimer.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Disclaimer: ${fridge4Scenario?.disclaimer}",
                            fontSize = 10.5.sp,
                            color = Color(0xFF64748B),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChefAdvisoryCard(
    stage: Int,
    recommendation: String,
    culinary: CulinarySuitabilityInfo,
    isTerminalStage5: Boolean,
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onNavigateComparison: () -> Unit,
    onNavigateWhatIf: () -> Unit,
    onNavigateScan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(
                Brush.linearGradient(
                    listOf(Forest, Color(0xFF1A3826))
                )
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = PrimaryDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHEF & PANTRY ADVISORY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGlow,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Action Protocol: " + when {
                            isTerminalStage5 -> "Immediate Consumption or Discard"
                            stage == 4 -> "Peak Eating Window"
                            else -> "Controlled Maturation"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recommendation,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Culinary Suitability Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CULINARY SUITABILITY",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = LimeGlow
                        )
                        Text(
                            text = culinary.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = culinary.subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(PrimaryGreen)
                        .clickable { onNavigateComparison() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Explore Storage Comparison",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { onNavigateWhatIf() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Open What-If Lab",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(if (isSavedToHistory) PrimaryGreen else Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(FreshIQRadius.radiusFull))
                        .clickable { onSaveToHistory() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isSavedToHistory) Icons.Default.BookmarkAdded else Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isSavedToHistory) "Saved to History!" else "Save to Scan History",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(FreshIQRadius.radiusFull))
                        .clickable { onNavigateScan() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Scan Another Produce",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun shareDiagnostic(context: Context, prediction: PredictionResponseDto) {
    val stageLabel = prediction.ripeness.stageLabel
    val confidence = prediction.ripeness.confidence
    val continuousIndex = prediction.ripeness.expectedContinuousRipeningStage
    val isRulSupported = prediction.rulAvailable && prediction.foodType == "avocado"

    val text = buildString {
        appendLine("FreshIQ Produce Ripeness Diagnostic:")
        appendLine("Produce: ${prediction.itemName}")
        appendLine("Stage: $stageLabel")
        appendLine("Confidence: ${String.format(Locale.US, "%.1f", confidence * 100)}%")
        if (continuousIndex != null) {
            appendLine("Continuous Index: ${String.format(Locale.US, "%.2f", continuousIndex)} / 5.0")
        }
        if (isRulSupported) {
            val ambientRul = prediction.scenarios?.get("ambient")?.estimatedRulDays ?: 0f
            appendLine("Estimated RUL: ${String.format(Locale.US, "%.1f", ambientRul)} days (Ambient ~20°C)")
        } else {
            val stageDesc = if (prediction.foodType == "banana") "3-stage optical classification only" else "5-stage optical classification only"
            appendLine("Remaining Shelf-Life: Not supported for this produce ($stageDesc)")
        }
        appendLine("Chef Advisory: ${prediction.actionableRecommendation}")
        appendLine("\nGenerated via FreshIQ MobileNetV3 AI Engine")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Diagnostic"))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MangoAnalysisContent(
    prediction: PredictionResponseDto,
    imageUri: String?,
    showReticleOverlay: Boolean,
    onToggleReticle: () -> Unit,
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onShare: () -> Unit,
    navController: NavController
) {
    val stageNumber = prediction.ripeness.predictedRipeningStage
    val mangoStage = MangoRipenessStage.fromNumber(stageNumber)
    val confidence = prediction.ripeness.confidence
    val probsList = prediction.ripeness.probabilities.getProbabilitiesList(isMango = true)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Breadcrumb Bar
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { navController.navigate(Screen.Scan.route) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextMain,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Scan Produce",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
                Text(text = "/", color = BorderLight, fontSize = 13.sp)
                Text(text = "Mango Analysis", fontSize = 13.sp, color = TextMain)
                Text(text = "/", color = BorderLight, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Mangifera indica",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }

        // 2. Header Callout Section (Share & Save - NO What-If / Comparison)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MANGIFERA INDICA • MANGO",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }

                    Text(
                        text = "#FIQ-MANGO-5CLASS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = "Mango Ripeness Diagnostic",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )

                Text(
                    text = "Dedicated 5-class neural vision classifier trained on optical ripening dynamics. Evaluates developmental phase from Unripe to Perished.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    lineHeight = 20.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(Color.White)
                            .clickable { onShare() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextMain, modifier = Modifier.size(15.dp))
                        Text(text = "Share Diagnostic", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                    }

                    // Save to History
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(
                                1.dp,
                                if (isSavedToHistory) PrimaryGreen else BorderLight,
                                RoundedCornerShape(FreshIQRadius.radiusFull)
                            )
                            .background(if (isSavedToHistory) PrimaryLight else Color.White)
                            .clickable { onSaveToHistory() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSavedToHistory) Icons.Default.BookmarkAdded else Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (isSavedToHistory) PrimaryDark else TextMain,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSavedToHistory) "Saved to History" else "Save to History",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSavedToHistory) PrimaryDark else TextMain
                        )
                    }
                }
            }
        }

        // 3. Specimen Bio-Scan ROI Feed Card
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Ribbon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle)
                            .border(width = 1.dp, color = BorderLight)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(mangoStage.color)
                            )
                            Text(
                                text = "SPECIMEN ROI INTAKE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(mangoStage.color.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "STAGE ${mangoStage.stageNumber}: ${mangoStage.label.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = mangoStage.color
                            )
                        }
                    }

                    // Specimen image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        if (showReticleOverlay) {
                            OpticalReticleOverlay(
                                modifier = Modifier.fillMaxSize(),
                                roiLabel = "ROI: Epidermal Chroma",
                                telemetryTopRight = "Mangifera indica",
                                telemetryBottomLeft = "Model: MobileNetV3-Large",
                                telemetryBottomRight = "Stage: ${mangoStage.label}"
                            ) {
                                if (!imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "Mango Specimen",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(BgSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "Specimen Preview Unavailable", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            if (!imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Mango Specimen",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(BgSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Specimen Preview Unavailable", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Reticle toggle bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showReticleOverlay) "Optical reticle overlay active" else "Clean photo display",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = if (showReticleOverlay) "Hide Reticle" else "Show Reticle",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            modifier = Modifier.clickable { onToggleReticle() }
                        )
                    }
                }
            }
        }

        // 4. Primary Ripeness Hero Card (Classification ONLY - NO RUL)
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREDICTED RIPENING STAGE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f%% Confidence", confidence * 100f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier
                                .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(mangoStage.color)
                        )
                        Text(
                            text = mangoStage.label,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = mangoStage.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Educational Disclaimer Box: No RUL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(FreshIQRadius.radiusSm))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Shelf-Life (RUL) Prediction Unavailable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Remaining usable days and multi-temperature storage curves are currently uncalibrated for mango. Only 5-stage optical image classification is performed.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        // 5. 5-Stage Mango Ripening Spectrum
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "MANGO RIPENING SPECTRUM",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MangoRipenessStage.entries.forEach { entry ->
                            val isActive = entry.stageNumber == mangoStage.stageNumber
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 24.dp else 16.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) entry.color else entry.color.copy(alpha = 0.3f))
                                        .border(
                                            width = if (isActive) 2.dp else 0.dp,
                                            color = if (isActive) Forest else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                                Text(
                                    text = entry.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) Forest else TextMuted,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Classification Probabilities (Softmax Spread N=5)
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOFTMAX PROBABILITY SPREAD",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "N=5 Classes",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    probsList.forEachIndexed { index, pair ->
                        val stageColor = MangoRipenessStage.fromNumber(index).color
                        val isPredicted = index == mangoStage.stageNumber

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pair.first,
                                    fontSize = 12.sp,
                                    fontWeight = if (isPredicted) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPredicted) Forest else TextMain
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", pair.second * 100f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isPredicted) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPredicted) Forest else TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(BgSubtle)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pair.second.coerceIn(0f, 1f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isPredicted) stageColor else stageColor.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Chef Advisory & Handling Guidance Card
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "MANGO HANDLING & ADVISORY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = mangoStage.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest,
                        lineHeight = 19.sp
                    )

                    if (prediction.actionableRecommendation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = prediction.actionableRecommendation,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FreshPrimaryButton(
                            text = "Scan Another Produce",
                            icon = Icons.Default.CameraAlt,
                            onClick = { navController.navigate(Screen.Scan.route) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 8. Food Safety Disclaimer
        item {
            FoodSafetyDisclaimer(
                backendDisclaimer = prediction.legalDisclaimer
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BananaAnalysisContent(
    prediction: PredictionResponseDto,
    imageUri: String?,
    showReticleOverlay: Boolean,
    onToggleReticle: () -> Unit,
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onShare: () -> Unit,
    navController: NavController
) {
    val stageNumber = prediction.ripeness.predictedRipeningStage
    val bananaStage = BananaRipenessStage.fromNumber(stageNumber)
    val confidence = prediction.ripeness.confidence
    val probsList = prediction.ripeness.probabilities.getProbabilitiesList(ProduceType.BANANA)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Breadcrumb Bar
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { navController.navigate(Screen.Scan.route) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = TextMain,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Scan Produce",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
                Text(text = "/", color = BorderLight, fontSize = 13.sp)
                Text(text = "Banana Analysis", fontSize = 13.sp, color = TextMain)
                Text(text = "/", color = BorderLight, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Musa acuminata",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }

        // 2. Header Callout Section (Share & Save - NO What-If / Comparison)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "MUSA ACUMINATA • BANANA",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }

                    Text(
                        text = "#FIQ-BANANA-3CLASS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = "Banana Ripeness Diagnostic",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )

                Text(
                    text = "Dedicated 3-class neural vision classifier trained on optical ripening dynamics. Evaluates developmental phase: Unripe, Semi-ripe, or Ripe.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    lineHeight = 20.sp
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Share
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(Color.White)
                            .clickable { onShare() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = TextMain, modifier = Modifier.size(15.dp))
                        Text(text = "Share Diagnostic", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                    }

                    // Save to History
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .border(
                                1.dp,
                                if (isSavedToHistory) PrimaryGreen else BorderLight,
                                RoundedCornerShape(FreshIQRadius.radiusFull)
                            )
                            .background(if (isSavedToHistory) PrimaryLight else Color.White)
                            .clickable { onSaveToHistory() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSavedToHistory) Icons.Default.BookmarkAdded else Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (isSavedToHistory) PrimaryDark else TextMain,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSavedToHistory) "Saved to History" else "Save to History",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSavedToHistory) PrimaryDark else TextMain
                        )
                    }
                }
            }
        }

        // 3. Specimen Bio-Scan ROI Feed Card
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Ribbon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle)
                            .border(width = 1.dp, color = BorderLight)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(bananaStage.color)
                            )
                            Text(
                                text = "SPECIMEN ROI INTAKE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(bananaStage.color.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "STAGE ${bananaStage.stageNumber}: ${bananaStage.label.uppercase()}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = bananaStage.color
                            )
                        }
                    }

                    // Specimen image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        if (showReticleOverlay) {
                            OpticalReticleOverlay(
                                modifier = Modifier.fillMaxSize(),
                                roiLabel = "ROI: Epidermal Chroma",
                                telemetryTopRight = "Musa acuminata",
                                telemetryBottomLeft = "Model: MobileNetV3-Small",
                                telemetryBottomRight = "Stage: ${bananaStage.label}"
                            ) {
                                if (!imageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "Banana Specimen",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(BgSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "Specimen Preview Unavailable", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            if (!imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Banana Specimen",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(BgSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Specimen Preview Unavailable", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Reticle toggle bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showReticleOverlay) "Optical reticle overlay active" else "Clean photo display",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = if (showReticleOverlay) "Hide Reticle" else "Show Reticle",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            modifier = Modifier.clickable { onToggleReticle() }
                        )
                    }
                }
            }
        }

        // 4. Primary Ripeness Hero Card (Classification ONLY - NO RUL)
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREDICTED RIPENING STAGE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f%% Confidence", confidence * 100f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark,
                            modifier = Modifier
                                .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(bananaStage.color)
                        )
                        Text(
                            text = bananaStage.label,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = bananaStage.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Educational Disclaimer Box: No RUL
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(FreshIQRadius.radiusSm))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Shelf-Life (RUL) Prediction Unavailable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Remaining usable days and multi-temperature storage curves are uncalibrated for banana. Only 3-stage optical image classification is performed.",
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        // 5. 3-Stage Banana Ripening Spectrum
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "BANANA RIPENING SPECTRUM",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BananaRipenessStage.entries.forEach { entry ->
                            val isActive = entry.stageNumber == bananaStage.stageNumber
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 24.dp else 16.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) entry.color else entry.color.copy(alpha = 0.3f))
                                        .border(
                                            width = if (isActive) 2.dp else 0.dp,
                                            color = if (isActive) Forest else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                                Text(
                                    text = entry.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) Forest else TextMuted,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Classification Probabilities (Softmax Spread N=3)
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOFTMAX PROBABILITY SPREAD",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "N=3 Classes",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    probsList.forEachIndexed { index, pair ->
                        val stageColor = BananaRipenessStage.fromNumber(index).color
                        val isPredicted = index == bananaStage.stageNumber

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pair.first,
                                    fontSize = 12.sp,
                                    fontWeight = if (isPredicted) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPredicted) Forest else TextMain
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", pair.second * 100f),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isPredicted) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPredicted) Forest else TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(BgSubtle)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(pair.second.coerceIn(0f, 1f))
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isPredicted) stageColor else stageColor.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Chef Advisory & Handling Guidance Card
        item {
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "BANANA HANDLING & ADVISORY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = bananaStage.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest,
                        lineHeight = 19.sp
                    )

                    if (prediction.actionableRecommendation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = prediction.actionableRecommendation,
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FreshPrimaryButton(
                            text = "Scan Another Produce",
                            icon = Icons.Default.CameraAlt,
                            onClick = { navController.navigate(Screen.Scan.route) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 8. Food Safety Disclaimer
        item {
            FoodSafetyDisclaimer(
                backendDisclaimer = prediction.legalDisclaimer
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
