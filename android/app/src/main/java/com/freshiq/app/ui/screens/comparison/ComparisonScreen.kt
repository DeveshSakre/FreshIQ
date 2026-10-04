package com.freshiq.app.ui.screens.comparison

import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.ui.components.BadgeSize
import com.freshiq.app.ui.components.ExtrapolationNoticeCard
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshCardVariant
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.FreshSecondaryButton
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
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.LimeLight
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
import com.freshiq.app.ui.theme.TextSubtle
import java.util.Locale

@Composable
fun ComparisonScreen(
    navController: NavController,
    viewModel: ComparisonViewModel = hiltViewModel()
) {
    val activePrediction by viewModel.activePrediction.collectAsState()
    val activeImageUri by viewModel.activeImageUri.collectAsState()
    val activeStorageCondition by viewModel.activeStorageCondition.collectAsState()
    val recentScans by viewModel.recentScans.collectAsState()
    val reminderSet by viewModel.reminderSet.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
    ) {
        val prediction = activePrediction

        if (prediction == null) {
            // -------------------------------------------------------------
            // EMPTY STATE: No active produce scan session
            // -------------------------------------------------------------
            ComparisonEmptyState(
                navController = navController,
                recentScans = recentScans,
                onLoadRecentScan = { viewModel.loadHistoricalScan(it) }
            )
        } else if (!prediction.rulAvailable || prediction.foodType in listOf("mango", "banana")) {
            // -------------------------------------------------------------
            // UNSUPPORTED PRODUCE GUARD
            // -------------------------------------------------------------
            ComparisonUnsupportedProduceState(
                navController = navController,
                itemName = prediction.itemName
            )
        } else {
            // -------------------------------------------------------------
            // ACTIVE COMPARISON MATRIX
            // -------------------------------------------------------------
            ComparisonContent(
                prediction = prediction,
                imageUri = activeImageUri,
                activeStorageCondition = activeStorageCondition,
                reminderSet = reminderSet,
                onToggleReminder = { viewModel.toggleReminder() },
                navController = navController
            )
        }
    }
}

// =========================================================================
// Empty State: Prompt to scan or load from history (no fake/synthetic calls)
// =========================================================================

@Composable
private fun ComparisonEmptyState(
    navController: NavController,
    recentScans: List<ScanHistoryEntity>,
    onLoadRecentScan: (ScanHistoryEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FreshIQSpacing.spaceLg),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceLg)
    ) {
        item {
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceXl))
            FreshCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FreshCardVariant.Elevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryLight)
                            .border(1.5.dp, BorderAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Active Produce Scan for Comparison",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Forest,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "To compare shelf-life longevity across temperatures (Ambient, 20°C, 10°C, and 4°C), capture or select an avocado image first.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    FreshPrimaryButton(
                        text = "Scan Produce Now",
                        icon = Icons.Default.CameraAlt,
                        onClick = { navController.navigate(Screen.Scan.route) }
                    )
                }
            }
        }

        // Recent Scans from Room history
        if (recentScans.isNotEmpty()) {
            item {
                Text(
                    text = "Or Load a Previous Scan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark,
                    letterSpacing = 0.5.sp
                )
            }

            items(recentScans.take(3).size) { index ->
                val scan = recentScans[index]
                FreshCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLoadRecentScan(scan) },
                    variant = FreshCardVariant.Subtle
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            if (scan.thumbnailPath.isNotBlank()) {
                                AsyncImage(
                                    model = scan.thumbnailPath,
                                    contentDescription = "Thumbnail",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scan ${scan.id.take(8)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = TextMain
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Condition: ${formatConditionName(scan.storageCondition)}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Load",
                            tint = PrimaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceXl))
        }
    }
}

// =========================================================================
// Unsupported Produce Guard: When produce does not support RUL/comparison
// =========================================================================

@Composable
private fun ComparisonUnsupportedProduceState(
    navController: NavController,
    itemName: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FreshIQSpacing.spaceLg),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceLg)
    ) {
        item {
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceXl))
            FreshCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Storage Comparison Unavailable",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Forest,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val classificationScope = if (itemName.contains("Banana", ignoreCase = true)) "3-stage optical image classification" else "5-stage optical image classification"
                    Text(
                        text = "Multi-scenario remaining usable life (RUL) comparison is currently calibrated exclusively for Hass avocados. '$itemName' is evaluated purely via $classificationScope without temperature kinetics.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FreshSecondaryButton(
                            text = "Return to Analysis",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { navController.navigate(Screen.Analysis.route) },
                            modifier = Modifier.weight(1f)
                        )
                        FreshPrimaryButton(
                            text = "Scan Produce",
                            icon = Icons.Default.CameraAlt,
                            onClick = { navController.navigate(Screen.Scan.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Active Comparison Content: Mirrors web ComparisonPage.tsx
// =========================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComparisonContent(
    prediction: PredictionResponseDto,
    imageUri: String?,
    activeStorageCondition: String,
    reminderSet: Boolean,
    onToggleReminder: () -> Unit,
    navController: NavController
) {
    val stage = prediction.ripeness.predictedRipeningStage
    val rawStageLabel = prediction.ripeness.stageLabel
    val stageLabel = if (rawStageLabel.startsWith("Stage", ignoreCase = true)) {
        rawStageLabel
    } else {
        "Stage $stage — $rawStageLabel"
    }
    val isTerminalStage5 = stage == 5

    // Real RUL values from backend response
    val scenarios = prediction.scenarios ?: emptyMap()
    val ambientDto = scenarios["ambient"]
    val room20Dto = scenarios["20C"]
    val cold10Dto = scenarios["10C"]
    val fridge4Dto = scenarios["4C_refrigerator"]

    val ambientRul = if (isTerminalStage5) 0f else (ambientDto?.estimatedRulDays ?: 0f)
    val room20Rul = if (isTerminalStage5) 0f else (room20Dto?.estimatedRulDays ?: 0f)
    val cold10Rul = if (isTerminalStage5) 0f else (cold10Dto?.estimatedRulDays ?: 0f)
    val fridge4Rul = if (isTerminalStage5) 0f else (fridge4Dto?.estimatedRulDays ?: 0f)
    val gainDays = if (isTerminalStage5) 0f else (prediction.refrigerationExtensionGainDays ?: 0f)

    val percentGain = if (ambientRul > 0f) {
        ((gainDays / ambientRul) * 100).toInt().coerceAtLeast(0)
    } else 0

    val maxBarDays = maxOf(12f, maxOf(ambientRul, room20Rul, cold10Rul, fridge4Rul) + 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FreshIQSpacing.spaceLg),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -----------------------------------------------------------------
        // 1. TOP BREADCRUMBS & SPECIMEN STATUS STRIP
        // -----------------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusMd))
                    .background(BgCard, RoundedCornerShape(FreshIQRadius.radiusMd))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clickable { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Analysis",
                        tint = Forest,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Analysis Results",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest
                    )
                }

                RipenessBadge(
                    stage = stage,
                    customLabel = stageLabel,
                    size = BadgeSize.Compact
                )
            }
        }

        // -----------------------------------------------------------------
        // 2. MAIN HEADLINE & SPECIMEN VISUAL CHIP
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = PrimaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "PRODUCE LONGEVITY MODELING • WHAT-IF SCENARIOS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Storage Temperature & Shelf-Life Comparison",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Simulated remaining usable life for your scanned Hass Avocado ($stageLabel) under calibrated ambient, cellar, and chilled thermodynamic environments.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Specimen Visual Chip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgCard, RoundedCornerShape(FreshIQRadius.radiusMd))
                        .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusMd))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BgSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Scanned Specimen",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Forest,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SCANNED SPECIMEN",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtle,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = prediction.itemName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                        Text(
                            text = "Confidence: ${String.format(Locale.US, "%.1f", prediction.ripeness.confidence * 100)}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryDark
                        )
                    }

                    // Active condition tag
                    Box(
                        modifier = Modifier
                            .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Scan: ${formatConditionName(activeStorageCondition)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 3. STAGE 5 TERMINAL ALERT BANNER (If Applicable)
        // -----------------------------------------------------------------
        if (isTerminalStage5) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(FreshIQRadius.radiusMd))
                        .border(1.5.dp, Color(0xFFFCA5A5), RoundedCornerShape(FreshIQRadius.radiusMd))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Terminal Maturity Boundary Notice (Stage 5 — Overripe)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "The model has classified this avocado in Stage 5 (Senescent / Overripe). Remaining usable shelf life is 0.0 days across all storage environments. Refrigeration cannot restore expired shelf life or reverse cellular softening. Inspect flesh before culinary use.",
                                fontSize = 12.sp,
                                color = Color(0xFF7F1D1D),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 4. STORAGE SCENARIO CARDS (4 SCENARIOS)
        // -----------------------------------------------------------------
        item {
            Text(
                text = "Storage Scenarios Evaluated",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
        }

        // Card 1: Ambient Kitchen (~20–22°C)
        item {
            ScenarioCard(
                title = "Ambient Kitchen",
                subtitle = "Warm Countertop",
                badgeText = "Accelerated Ripening",
                badgeIcon = Icons.Default.LocalFireDepartment,
                badgeBg = Color(0xFFFEE2E2),
                badgeColor = Color(0xFF991B1B),
                tempLabel = "~20–22°C / 72°F",
                rulDays = ambientRul,
                lossText = "High Loss Rate",
                lossColor = Color(0xFFDC2626),
                barColor = Color(0xFFEF4444),
                maxBarDays = maxBarDays,
                modelType = "EMPIRICAL ML MODEL",
                description = "Standard room bench. Respiration climbs rapidly. Segregate from bananas and apples to prevent autocatalytic ethylene cascade.",
                footerLabel = "Kinetic Velocity",
                footerValue = "1.8x Base",
                isActiveScanCondition = activeStorageCondition.equals("ambient", ignoreCase = true)
            )
        }

        // Card 2: Controlled 20°C
        item {
            ScenarioCard(
                title = "Controlled 20°C",
                subtitle = "Standard Pantry / Cellar",
                badgeText = "Baseline Control",
                badgeIcon = Icons.Default.Tune,
                badgeBg = BgSubtle,
                badgeColor = TextMain,
                tempLabel = "20.0°C / 68°F",
                rulDays = room20Rul,
                lossText = "Nominal Loss",
                lossColor = TextMuted,
                barColor = Color(0xFF65A30D),
                maxBarDays = maxBarDays,
                modelType = "EMPIRICAL ML MODEL",
                description = "Standard controlled temperature. Calibrated benchmark with linear cellular softening profile.",
                footerLabel = "Kinetic Velocity",
                footerValue = "1.0x Base",
                isActiveScanCondition = activeStorageCondition.equals("20C", ignoreCase = true)
            )
        }

        // Card 3: Vegetable Crisper 10°C (Optimal target)
        item {
            ScenarioCard(
                title = "Vegetable Crisper (10°C)",
                subtitle = "Dedicated Compartment",
                badgeText = "Best Quality Window",
                badgeIcon = Icons.Default.AcUnit,
                badgeBg = PrimaryLight,
                badgeColor = PrimaryDark,
                tempLabel = "10.0°C / 50°F",
                rulDays = cold10Rul,
                lossText = if (!isTerminalStage5 && gainDays > 0) "+$percentGain% Gain" else "Extended",
                lossColor = Forest,
                barColor = PrimaryGreen,
                maxBarDays = maxBarDays,
                modelType = "EMPIRICAL MODEL VERIFIED",
                description = "Ideal thermal equilibrium for Hass avocados. Maximally arrests cell wall breakdown without inducing internal mesocarp browning.",
                footerLabel = "Net Longevity Surplus",
                footerValue = if (isTerminalStage5) "0.0 Days" else "+${String.format(Locale.US, "%.1f", gainDays)} Full Days",
                isOptimalTarget = !isTerminalStage5,
                isActiveScanCondition = activeStorageCondition.equals("10C", ignoreCase = true)
            )
        }

        // Card 4: Deep Refrigeration 4°C (Model-Based Extrapolation)
        item {
            ScenarioCard4CExtrapolated(
                rulDays = fridge4Rul,
                maxBarDays = maxBarDays,
                uncertaintyNote = fridge4Dto?.uncertaintyNote,
                isActiveScanCondition = activeStorageCondition.equals("4C_refrigerator", ignoreCase = true)
            )
        }

        // -----------------------------------------------------------------
        // 5. VISUAL COMPARISON: HORIZONTAL TIMELINE BARS
        // -----------------------------------------------------------------
        item {
            TimelineBarsCard(
                ambientRul = ambientRul,
                room20Rul = room20Rul,
                cold10Rul = cold10Rul,
                fridge4Rul = fridge4Rul,
                gainDays = gainDays,
                maxBarDays = maxBarDays,
                isTerminalStage5 = isTerminalStage5
            )
        }

        // -----------------------------------------------------------------
        // 6. POSTHARVEST PHYSIOLOGY GUIDE (Educational)
        // -----------------------------------------------------------------
        item {
            PhysiologyGuideSection()
        }

        // -----------------------------------------------------------------
        // 7. ACTION BLUEPRINT & INTERVENTION BANNER
        // -----------------------------------------------------------------
        item {
            InterventionBanner(
                isTerminalStage5 = isTerminalStage5,
                gainDays = gainDays
            )
        }

        item {
            StorageBlueprintCard(
                reminderSet = reminderSet,
                onToggleReminder = onToggleReminder
            )
        }

        // -----------------------------------------------------------------
        // 8. DISCLAIMERS & POLICIES
        // -----------------------------------------------------------------
        item {
            ExtrapolationNoticeCard(
                customUncertaintyNote = fridge4Dto?.uncertaintyNote,
                isTerminalStage5 = isTerminalStage5
            )
        }

        item {
            FoodSafetyDisclaimer(
                backendDisclaimer = prediction.legalDisclaimer
            )
        }

        // -----------------------------------------------------------------
        // 9. NAVIGATION FOOTER STRIP
        // -----------------------------------------------------------------
        item {
            NavigationButtonsStrip(
                onNavigateAnalysis = { navController.popBackStack() },
                onNavigateWhatIf = { navController.navigate(Screen.WhatIf.route) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =========================================================================
// Scenario Card Component (10°C, 20°C, Ambient)
// =========================================================================

@Composable
private fun ScenarioCard(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeIcon: ImageVector,
    badgeBg: Color,
    badgeColor: Color,
    tempLabel: String,
    rulDays: Float,
    lossText: String,
    lossColor: Color,
    barColor: Color,
    maxBarDays: Float,
    modelType: String,
    description: String,
    footerLabel: String,
    footerValue: String,
    isOptimalTarget: Boolean = false,
    isActiveScanCondition: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BgCard)
            .border(
                width = if (isOptimalTarget) 2.dp else 1.dp,
                color = if (isOptimalTarget) PrimaryGreen else BorderLight,
                shape = RoundedCornerShape(FreshIQRadius.radiusLg)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optimal target header strip
            if (isOptimalTarget) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Forest)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = LimeGlow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OPTIMAL PRESERVATION TARGET",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = badgeText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    Text(
                        text = tempLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active scan condition label if matching
                if (isActiveScanCondition) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "● ACTIVE SCAN CONDITION",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Forest
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Big RUL Days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", rulDays),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextMain,
                            lineHeight = 34.sp
                        )
                        Text(
                            text = "Days",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Text(
                        text = lossText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = lossColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bar
                val barFraction = (rulDays / maxBarDays).coerceIn(0.04f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BgSubtle)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barFraction)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Model status & description
                Row(
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
                        text = modelType,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Footer row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            BorderLight,
                            RoundedCornerShape(FreshIQRadius.radiusSm)
                        )
                        .background(
                            if (isOptimalTarget) PrimaryLight else BgSubtle,
                            RoundedCornerShape(FreshIQRadius.radiusSm)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = footerLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOptimalTarget) Forest else TextMuted
                    )
                    Text(
                        text = footerValue,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOptimalTarget) PrimaryDark else TextMain
                    )
                }
            }
        }
    }
}

// =========================================================================
// Scenario Card 4: 4°C Domestic Refrigerator (Model-Based Extrapolation)
// =========================================================================

@Composable
private fun ScenarioCard4CExtrapolated(
    rulDays: Float,
    maxBarDays: Float,
    uncertaintyNote: String?,
    isActiveScanCondition: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BlueIceBg)
            .border(1.5.dp, BlueIceBorder, RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(Color(0xFFDBEAFE), RoundedCornerShape(FreshIQRadius.radiusFull))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = Color(0xFF1E40AF),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Maximum Chilling",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                }

                Text(
                    text = "4.0°C / 39°F",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E40AF)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isActiveScanCondition) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "● ACTIVE SCAN CONDITION",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Main Fridge Shelf",
                fontSize = 11.sp,
                color = Color(0xFF1E40AF)
            )
            Text(
                text = "Deep Refrigeration (4°C)",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Big RUL
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", rulDays),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 34.sp
                    )
                    Text(
                        text = "Days",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E40AF),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFDBEAFE), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Model-based extrapolation",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E40AF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hatched / Blue Progress Bar
            val barFraction = (rulDays / maxBarDays).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFDBEAFE))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barFraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF3B82F6))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Extrapolation header & note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = Color(0xFF1E40AF),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "MODEL-BASED EXTRAPOLATION (Q10 = 2.38)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = uncertaintyNote ?: "4°C is not an empirically observed training condition. Modeled via Arrhenius biophysical respiration scaling (Q10 = 2.38); never represented as experimental ground truth.",
                fontSize = 12.sp,
                color = Color(0xFF1E3A8A),
                lineHeight = 16.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chilling injury alert box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Chilling Injury Notice: Prolonged domestic refrigeration below 5°C risks internal flesh browning upon warming.",
                    fontSize = 11.sp,
                    color = Color(0xFF991B1B),
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(FreshIQRadius.radiusSm))
                    .background(Color(0xFFDBEAFE).copy(alpha = 0.5f), RoundedCornerShape(FreshIQRadius.radiusSm))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sensory Browning Risk",
                    fontSize = 11.5.sp,
                    color = Color(0xFF1E40AF)
                )
                Text(
                    text = "Moderate (Chilling Risk)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB91C1C)
                )
            }
        }
    }
}

// =========================================================================
// Visual Comparison: Horizontal Timeline Longevity Bars
// =========================================================================

@Composable
private fun TimelineBarsCard(
    ambientRul: Float,
    room20Rul: Float,
    cold10Rul: Float,
    fridge4Rul: Float,
    gainDays: Float,
    maxBarDays: Float,
    isTerminalStage5: Boolean
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "COMPARATIVE SHELF-LIFE ANALYSIS",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Usable Longevity Windows by Thermal Environment",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Forest
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                    )
                    Text(
                        text = "Empirical ML Data",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6))
                    )
                    Text(
                        text = "Model-based extrapolation (4°C)",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Axis scale labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.dp, Color.Transparent)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("0d", "2d", "4d", "6d", "8d", "10d", "12d+").forEach { tick ->
                    Text(
                        text = tick,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        color = TextSubtle
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderLight)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Ambient Kitchen
            TimelineRow(
                label = "Ambient Kitchen (~20–22°C)",
                daysText = "${String.format(Locale.US, "%.1f", ambientRul)} Days",
                rulDays = ambientRul,
                maxBarDays = maxBarDays,
                barColor = Color(0xFFEF4444)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Controlled Room 20C
            TimelineRow(
                label = "Controlled Room (20°C Baseline)",
                daysText = "${String.format(Locale.US, "%.1f", room20Rul)} Days",
                rulDays = room20Rul,
                maxBarDays = maxBarDays,
                barColor = Color(0xFF65A30D)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 3: 10C Crisper (Optimal Target)
            TimelineRow(
                label = "Crisper (10°C)",
                daysText = "${String.format(Locale.US, "%.1f", cold10Rul)} Days",
                rulDays = cold10Rul,
                maxBarDays = maxBarDays,
                barColor = PrimaryGreen,
                highlightTag = if (!isTerminalStage5) "Recommended" else null,
                gainSurplusText = if (!isTerminalStage5 && gainDays > 0) "+${String.format(Locale.US, "%.1f", gainDays)}d net gain" else null
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 4: 4C Refrigerator (Extrapolated)
            TimelineRow(
                label = "Domestic Fridge (4°C)",
                daysText = "${String.format(Locale.US, "%.1f", fridge4Rul)} Days",
                rulDays = fridge4Rul,
                maxBarDays = maxBarDays,
                barColor = Color(0xFF3B82F6),
                isExtrapolated = true,
                gainSurplusText = "Browning Risk"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footnote
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.dp, Color.Transparent)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryDark,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Predictions calibrated with multi-temperature dataset. 4°C extrapolation applies Q10 = 2.38.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(
    label: String,
    daysText: String,
    rulDays: Float,
    maxBarDays: Float,
    barColor: Color,
    highlightTag: String? = null,
    isExtrapolated: Boolean = false,
    gainSurplusText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (highlightTag != null) {
                    Box(
                        modifier = Modifier
                            .background(Forest, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = highlightTag,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                if (isExtrapolated) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "EXTRAPOLATED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = daysText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isExtrapolated) Color(0xFF1E40AF) else TextMain,
                maxLines = 1,
                softWrap = false
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val barFraction = (rulDays / maxBarDays).coerceIn(0.04f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BgSubtle)
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barFraction)
                    .height(20.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (gainSurplusText != null && barFraction > 0.35f) Arrangement.SpaceBetween else Arrangement.End
                ) {
                    if (gainSurplusText != null && barFraction > 0.35f) {
                        Text(
                            text = gainSurplusText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    if (rulDays > 0) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", rulDays)}d",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Postharvest Physiology Guide Section (3 Principles)
// =========================================================================

@Composable
private fun PhysiologyGuideSection() {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "POSTHARVEST PHYSIOLOGY GUIDE",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Why Temperature Governs Shelf Life",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Avocado is a climacteric fruit whose ripening is driven by an irreversible burst in respiration and autocatalytic ethylene synthesis. Storage temperature governs these reaction rates.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Scientific Cards
            PhysiologyPrincipleCard(
                icon = Icons.Default.Thermostat,
                title = "1. Respiration Kinetics & Q10 Coefficient",
                description = "Respiration rate roughly doubles for every 10°C increase (Q10 ≈ 2.0–2.5). Dropping from 20°C to 10°C cuts metabolic consumption in half, conserving carbohydrates and lipids.",
                cardBg = BgSubtle,
                iconTint = PrimaryDark,
                iconBg = PrimaryLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            PhysiologyPrincipleCard(
                icon = Icons.Default.Eco,
                title = "2. Ethylene & Pectin Hydrolysis",
                description = "Warm temperatures activate polygalacturonase (PG) enzymes that hydrolyze middle lamella pectin gluing plant cells together. Chilling to 10°C delays ethylene binding, preserving firm texture.",
                cardBg = BgSubtle,
                iconTint = PrimaryDark,
                iconBg = PrimaryLight
            )

            Spacer(modifier = Modifier.height(10.dp))

            PhysiologyPrincipleCard(
                icon = Icons.Default.AcUnit,
                title = "3. The 10°C vs 4°C Chilling Threshold",
                description = "While 4°C extends nominal duration, temperatures below 5°C induce membrane lipid phase transitions. This leaks polyphenol oxidase (PPO), leading to internal vascular browning upon warming.",
                cardBg = Color(0xFFEFF6FF),
                iconTint = Color(0xFF1E40AF),
                iconBg = Color(0xFFDBEAFE),
                isChillingRisk = true
            )
        }
    }
}

@Composable
private fun PhysiologyPrincipleCard(
    icon: ImageVector,
    title: String,
    description: String,
    cardBg: Color,
    iconTint: Color,
    iconBg: Color,
    isChillingRisk: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
            .background(cardBg)
            .border(
                1.dp,
                if (isChillingRisk) Color(0xFFBFDBFE) else BorderLight,
                RoundedCornerShape(FreshIQRadius.radiusMd)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChillingRisk) Color(0xFF1E3A8A) else Forest
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.5.sp,
                    color = if (isChillingRisk) Color(0xFF1E40AF) else TextMuted,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// =========================================================================
// Intervention Banner & Waste Savings Bento
// =========================================================================

@Composable
private fun InterventionBanner(
    isTerminalStage5: Boolean,
    gainDays: Float
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
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(FreshIQRadius.radiusFull)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "OPTIMAL INTERVENTION",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGlow
                    )
                }
                Text(
                    text = "• Actionable Guidance",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isTerminalStage5) {
                    "Produce has reached terminal overripe maturity."
                } else {
                    "Chilling to 10°C buys you an extra +${String.format(Locale.US, "%.1f", gainDays)} days of peak culinary quality."
                },
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isTerminalStage5) {
                    "Stage 5 produce has completed senescence. Refrigeration cannot restore expired shelf life. Use immediately or discard if spoiled."
                } else {
                    "By transferring this avocado to a vegetable crisper now, you suppress polygalacturonase enzyme activity, stalling soft-rot without triggering the cold shock browning caused by domestic 4°C shelves."
                },
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bento metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = LimeGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "WASTE SAVINGS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$3.20 value conserved",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = LimeGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "EMISSION AVOIDANCE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.5.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "0.48 kg CO₂eq saved",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// Storage Blueprint Card (3 Handling Procedures + Reminder)
// =========================================================================

@Composable
private fun StorageBlueprintCard(
    reminderSet: Boolean,
    onToggleReminder: () -> Unit
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "STORAGE BLUEPRINT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "How to Store This Avocado",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Follow these three targeted handling procedures to attain full preservation potential:",
                fontSize = 12.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            BlueprintStep(
                stepNumber = "1",
                headline = "Transfer to middle crisper drawer",
                subtext = "Set humidity slider to ~85% RH (High Humidity)."
            )

            Spacer(modifier = Modifier.height(8.dp))

            BlueprintStep(
                stepNumber = "2",
                headline = "Isolate from climacteric neighbors",
                subtext = "Keep at least 15cm separation from ripe bananas, apples, or tomatoes."
            )

            Spacer(modifier = Modifier.height(8.dp))

            BlueprintStep(
                stepNumber = "3",
                headline = "Perform re-inspection before use",
                subtext = "Check stem node yield for gentle softening before slicing or mashing."
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Reminder Toggle Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(if (reminderSet) PrimaryLight else BgSubtle)
                    .border(
                        1.dp,
                        if (reminderSet) PrimaryGreen else BorderLight,
                        RoundedCornerShape(FreshIQRadius.radiusFull)
                    )
                    .clickable { onToggleReminder() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (reminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (reminderSet) PrimaryDark else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (reminderSet) "Reminder Set for Mid-Week Inspection!" else "Set Mid-Week Re-Inspection Reminder",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (reminderSet) PrimaryDark else TextMain
                    )
                }
            }
        }
    }
}

@Composable
private fun BlueprintStep(
    stepNumber: String,
    headline: String,
    subtext: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSubtle, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = headline,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMain
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

// =========================================================================
// Navigation Buttons Strip
// =========================================================================

@Composable
private fun NavigationButtonsStrip(
    onNavigateAnalysis: () -> Unit,
    onNavigateWhatIf: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FreshSecondaryButton(
            text = "Return to Analysis Result",
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            onClick = onNavigateAnalysis,
            modifier = Modifier.fillMaxWidth()
        )

        FreshPrimaryButton(
            text = "Open Interactive What-If Lab",
            icon = Icons.Default.Tune,
            onClick = onNavigateWhatIf,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatConditionName(conditionKey: String): String {
    return when (conditionKey.lowercase()) {
        "ambient" -> "Ambient"
        "20c" -> "20°C"
        "10c" -> "10°C"
        "4c_refrigerator", "4c" -> "4°C"
        else -> conditionKey
    }
}
