package com.freshiq.app.ui.screens.comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.freshiq.app.data.local.ScanHistoryEntity
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.ui.components.ExtrapolationNoticeCard
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshCardVariant
import com.freshiq.app.ui.components.FreshIQTopBar
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.FreshSecondaryButton
import com.freshiq.app.ui.components.RipenessBadge
import com.freshiq.app.ui.components.StorageScenarioCard
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BlueIceBadge
import com.freshiq.app.ui.theme.BlueIceBadgeText
import com.freshiq.app.ui.theme.BlueIceBg
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSurface
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.StatusWarningBg
import com.freshiq.app.ui.theme.StatusWarningBorder
import com.freshiq.app.ui.theme.StatusWarningIcon
import com.freshiq.app.ui.theme.StatusWarningText
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BotanicalSurface)
    ) {
        FreshIQTopBar(
            title = "Storage Comparison",
            canNavigateBack = true,
            onNavigateBack = { navController.popBackStack() }
        )

        val prediction = activePrediction

        if (prediction == null) {
            // Empty State: No active produce scan session
            ComparisonEmptyState(
                navController = navController,
                recentScans = recentScans,
                onLoadRecentScan = { viewModel.loadHistoricalScan(it) }
            )
        } else if (!prediction.rulAvailable || prediction.foodType.lowercase(Locale.US) in listOf("mango", "banana")) {
            // Guard: Unsupported Produce (Mango / Banana classification-only)
            ComparisonUnsupportedProduceState(
                navController = navController,
                itemName = prediction.itemName
            )
        } else if (prediction.scenarios.isNullOrEmpty()) {
            // Fallback: Missing scenario data
            ComparisonNoScenariosState(
                navController = navController
            )
        } else {
            // Active Comparison Matrix: Presentation-Only, Strictly Backend Data-Driven
            ComparisonActiveContent(
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
// Active Comparison Content: Easy to Scan, Compact, Backend-Data-Driven
// =========================================================================

@Composable
private fun ComparisonActiveContent(
    prediction: PredictionResponseDto,
    imageUri: String?,
    activeStorageCondition: String,
    reminderSet: Boolean,
    onToggleReminder: () -> Unit,
    navController: NavController
) {
    val stage = prediction.ripeness.predictedRipeningStage
    val rawStageLabel = prediction.ripeness.stageLabel
    val displayStageLabel = if (rawStageLabel.startsWith("Stage", ignoreCase = true)) {
        rawStageLabel
    } else {
        "Stage $stage — $rawStageLabel"
    }
    val isTerminalStage5 = stage == 5
    val scenarios = prediction.scenarios ?: emptyMap()
    val extrapolatedScenario = scenarios.values.firstOrNull { it.isExtrapolated || it.temperatureC <= 4.0f }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FreshIQSpacing.margin),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
    ) {
        item {
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceSm))
        }

        // 1. Current Produce / Result Summary Header
        item {
            FreshCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FreshCardVariant.Elevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FreshIQSpacing.spaceMd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceMd)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                            .background(BotanicalSurfaceContainerLow),
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
                                tint = BotanicalPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prediction.itemName,
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalOnSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RipenessBadge(stage = stage, customLabel = displayStageLabel)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                    .background(BotanicalSurfaceContainerLow)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = formatConditionName(activeStorageCondition),
                                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                                    color = BotanicalOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Terminal Maturity Boundary Banner (Stage 5 Overripe)
        if (isTerminalStage5) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                        .background(StatusWarningBg)
                        .border(1.5.dp, StatusWarningBorder, RoundedCornerShape(FreshIQRadius.radiusMd))
                        .padding(FreshIQSpacing.spaceMd)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = StatusWarningIcon,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Terminal Maturity Boundary Notice (Stage 5 — Overripe)",
                                style = FreshIQTypography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarningText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "The model has classified this produce in Stage 5 (Senescent / Overripe). Remaining usable shelf life is 0.0 days across all storage environments. Refrigeration cannot restore expired shelf life or reverse cellular softening.",
                                style = FreshIQTypography.bodySmall,
                                color = StatusWarningText,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Scenario Comparison Overview & Context
        item {
            FreshCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FreshCardVariant.Elevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FreshIQSpacing.spaceMd)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = BotanicalPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Storage Temperature Comparison",
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalOnSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Side-by-side comparison of calibrated remaining usable life across storage environments. Values are directly supplied by validated backend models.",
                        style = FreshIQTypography.bodyMedium,
                        color = BotanicalOnSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    // Backend-provided refrigeration gain (if available from backend)
                    val backendGain = prediction.refrigerationExtensionGainDays
                    if (backendGain != null && backendGain > 0f && !isTerminalStage5) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                                .background(BotanicalSurfaceContainerLow)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Refrigeration Extension Gain:",
                                    style = FreshIQTypography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BotanicalOnSurface
                                )
                                Text(
                                    text = "+${String.format(Locale.US, "%.1f", backendGain)} days",
                                    fontFamily = FontFamily.Monospace,
                                    style = FreshIQTypography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BotanicalPrimary
                                )
                                Text(
                                    text = "(backend model estimate)",
                                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                                    color = BotanicalOutline
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Scenario Cards / Comparison Rows (Reusing Phase 2 StorageScenarioCard)
        item {
            Text(
                text = "SCENARIOS COMPARED",
                style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                fontWeight = FontWeight.Bold,
                color = BotanicalOutline,
                letterSpacing = 0.08.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        items(scenarios.values.toList()) { scenario ->
            StorageScenarioCard(
                scenario = scenario,
                isBaseline = matchesStorageCondition(activeStorageCondition, scenario.condition)
            )
        }

        // 5. Extrapolation Distinction Notice (When 4°C or model-based extrapolation is present)
        if (extrapolatedScenario != null) {
            item {
                ExtrapolationNoticeCard(
                    customUncertaintyNote = extrapolatedScenario.uncertaintyNote,
                    customDisclaimer = extrapolatedScenario.disclaimer,
                    isTerminalStage5 = isTerminalStage5
                )
            }
        }

        // 6. Actionable Recommendation from Backend
        if (prediction.actionableRecommendation.isNotBlank()) {
            item {
                FreshCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = FreshCardVariant.Elevated
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(FreshIQSpacing.spaceMd)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = BotanicalPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Actionable Storage Recommendation",
                                style = FreshIQTypography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BotanicalOnSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = prediction.actionableRecommendation,
                            style = FreshIQTypography.bodyMedium,
                            color = BotanicalOnSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // 7. Storage Handling Blueprint & Mid-Week Reminder (Advisory-Only)
        item {
            FreshCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FreshCardVariant.Subtle
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FreshIQSpacing.spaceMd)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Storage Handling Procedures",
                            style = FreshIQTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalOnSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                                .background(BotanicalSurfaceContainerLow)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Advisory Guidance",
                                style = FreshIQTypography.labelSmall.copy(fontSize = 9.sp),
                                color = BotanicalOutline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1. Crisper placement: Store in the vegetable crisper compartment to moderate moisture loss.\n2. Climacteric isolation: Maintain separation from ethylene-emitting produce (apples, bananas).\n3. Re-inspection: Check stem node yield gently before culinary use.",
                        style = FreshIQTypography.bodySmall,
                        color = BotanicalOnSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Storage reminder toggle
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(if (reminderSet) PrimaryLight else BotanicalSurfaceContainerLow)
                            .clickable { onToggleReminder() }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (reminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = if (reminderSet) PrimaryDark else BotanicalOutline,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (reminderSet) "Re-inspection Reminder Set" else "Set Re-inspection Reminder",
                                style = FreshIQTypography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (reminderSet) PrimaryDark else BotanicalOnSurface
                            )
                        }
                    }
                }
            }
        }

        // 8. Responsible AI Food Safety Disclaimer
        item {
            FoodSafetyDisclaimer(
                modifier = Modifier.fillMaxWidth(),
                backendDisclaimer = prediction.legalDisclaimer
            )
        }

        // 9. Navigation Actions
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FreshIQSpacing.spaceSm),
                verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                FreshPrimaryButton(
                    text = "Open Interactive What-If Lab",
                    icon = Icons.Default.Tune,
                    onClick = { navController.navigate(Screen.WhatIf.route) },
                    modifier = Modifier.fillMaxWidth()
                )

                FreshSecondaryButton(
                    text = "Return to Analysis Result",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceLg))
        }
    }
}

// =========================================================================
// Empty State: Prompt to scan or load from Room
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
            .padding(horizontal = FreshIQSpacing.margin),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
    ) {
        item {
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceLg))
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
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            tint = BotanicalPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Active Produce Scan for Comparison",
                        style = FreshIQTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "To compare shelf-life longevity across storage environments, capture or select an avocado image first.",
                        style = FreshIQTypography.bodyMedium,
                        color = BotanicalOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FreshPrimaryButton(
                        text = "Scan Produce Now",
                        icon = Icons.Default.CameraAlt,
                        onClick = { navController.navigate(Screen.Scan.route) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (recentScans.isNotEmpty()) {
            item {
                Text(
                    text = "OR LOAD A PREVIOUS SCAN (${recentScans.size})",
                    style = FreshIQTypography.labelSmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOutline,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(recentScans.take(3)) { scan ->
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
                                .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
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
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceLg))
        }
    }
}

// =========================================================================
// Unsupported Produce Guard: Mango / Banana
// =========================================================================

@Composable
private fun ComparisonUnsupportedProduceState(
    navController: NavController,
    itemName: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FreshIQSpacing.margin),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
    ) {
        item {
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceLg))
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
                        style = FreshIQTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val classificationScope = if (itemName.contains("Banana", ignoreCase = true)) {
                        "3-stage optical image classification"
                    } else {
                        "5-stage optical image classification"
                    }

                    Text(
                        text = "Multi-scenario remaining usable life (RUL) comparison is currently calibrated exclusively for Hass avocados. '$itemName' is evaluated purely via $classificationScope without temperature kinetics.",
                        style = FreshIQTypography.bodyMedium,
                        color = BotanicalOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
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
// Fallback: No Scenarios Available
// =========================================================================

@Composable
private fun ComparisonNoScenariosState(
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FreshIQSpacing.margin),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
    ) {
        item {
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceLg))
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
                            .background(BotanicalSurfaceContainerLow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BotanicalPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Storage Scenarios Unavailable",
                        style = FreshIQTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Storage scenarios aren't available for this scan.",
                        style = FreshIQTypography.bodyMedium,
                        color = BotanicalOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FreshSecondaryButton(
                        text = "Return to Analysis",
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// =========================================================================
// Helpers
// =========================================================================

private fun formatConditionName(condition: String): String {
    return when (condition.lowercase(Locale.US)) {
        "ambient", "ambient counter" -> "Ambient (~20–22°C)"
        "20c", "controlled room (20°c)", "20°c" -> "20°C Controlled"
        "10c", "cold storage (10°c)", "10°c" -> "10°C Cold Storage"
        "4c", "domestic refrigerator (4°c)", "4°c", "4c_refrigerator" -> "4°C Refrigerator"
        else -> condition
    }
}

private fun matchesStorageCondition(sessionCondition: String, scenarioCondition: String): Boolean {
    val normSession = sessionCondition.lowercase(Locale.US).replace("°", "").replace(" ", "").replace("_", "")
    val normScenario = scenarioCondition.lowercase(Locale.US).replace("°", "").replace(" ", "").replace("_", "")
    return when {
        normScenario.contains("ambient") && normSession.contains("ambient") -> true
        normScenario.contains("20c") && normSession.contains("20c") -> true
        normScenario.contains("10c") && normSession.contains("10c") -> true
        (normScenario.contains("4c") || normScenario.contains("fridge")) &&
            (normSession.contains("4c") || normSession.contains("fridge") || normSession.contains("refrigerator")) -> true
        else -> normScenario == normSession
    }
}
