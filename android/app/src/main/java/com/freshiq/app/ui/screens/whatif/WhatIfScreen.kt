package com.freshiq.app.ui.screens.whatif

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.freshiq.app.data.model.ScenarioRulDto
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
fun WhatIfScreen(
    navController: NavController,
    viewModel: WhatIfViewModel = hiltViewModel()
) {
    val activePrediction by viewModel.activePrediction.collectAsState()
    val activeImageUri by viewModel.activeImageUri.collectAsState()
    val activeStorageCondition by viewModel.activeStorageCondition.collectAsState()
    val recentScans by viewModel.recentScans.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val containerFormat by viewModel.containerFormat.collectAsState()
    val ethyleneProximity by viewModel.ethyleneProximity.collectAsState()
    val reminderSet by viewModel.reminderSet.collectAsState()
    val planApplied by viewModel.planApplied.collectAsState()

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
            WhatIfEmptyState(
                navController = navController,
                recentScans = recentScans,
                onLoadRecentScan = { viewModel.loadHistoricalScan(it) }
            )
        } else if (!prediction.rulAvailable || prediction.foodType in listOf("mango", "banana")) {
            // -------------------------------------------------------------
            // UNSUPPORTED PRODUCE GUARD
            // -------------------------------------------------------------
            WhatIfUnsupportedProduceState(
                navController = navController,
                itemName = prediction.itemName
            )
        } else {
            // -------------------------------------------------------------
            // ACTIVE WHAT-IF SIMULATION LAB
            // -------------------------------------------------------------
            WhatIfContent(
                prediction = prediction,
                imageUri = activeImageUri,
                activeStorageCondition = activeStorageCondition,
                selectedPreset = selectedPreset,
                onSelectPreset = { viewModel.selectPreset(it) },
                containerFormat = containerFormat,
                onSetContainerFormat = { viewModel.setContainerFormat(it) },
                ethyleneProximity = ethyleneProximity,
                onToggleEthylene = { viewModel.toggleEthyleneProximity() },
                reminderSet = reminderSet,
                onToggleReminder = { viewModel.toggleReminder() },
                planApplied = planApplied,
                onTogglePlan = { viewModel.togglePlanApplied() },
                navController = navController
            )
        }
    }
}

// =========================================================================
// Empty State: Prompt user to scan produce first or restore from Room
// =========================================================================

@Composable
private fun WhatIfEmptyState(
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
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "What-If Storage Simulation Lab",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan an avocado first to dynamically simulate shelf-life extension and decision-support scenarios across temperature regimes and storage enclosures.",
                        fontSize = 13.5.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { navController.navigate(Screen.Scan.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Forest,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(FreshIQRadius.radiusMd),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Go to Produce Scanner",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (recentScans.isNotEmpty()) {
            item {
                Text(
                    text = "OR LOAD FROM RECENT SCANS (${recentScans.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
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
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceXl))
        }
    }
}

// =========================================================================
// Unsupported Produce Guard: When produce does not support RUL/scenarios
// =========================================================================

@Composable
private fun WhatIfUnsupportedProduceState(
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
                        text = "What-If Simulation Unavailable",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Forest,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val classificationScope = if (itemName.contains("Banana", ignoreCase = true)) "3-stage optical image classification" else "5-stage optical image classification"
                    Text(
                        text = "Interactive What-If temperature simulations and kinetic shelf-life decay curves are currently calibrated exclusively for Hass avocados. '$itemName' is evaluated purely via $classificationScope without remaining usable days (RUL) modeling.",
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
// Active What-If Content
// =========================================================================

@Composable
private fun WhatIfContent(
    prediction: PredictionResponseDto,
    imageUri: String?,
    activeStorageCondition: String,
    selectedPreset: StoragePresetKey,
    onSelectPreset: (StoragePresetKey) -> Unit,
    containerFormat: ContainerFormat,
    onSetContainerFormat: (ContainerFormat) -> Unit,
    ethyleneProximity: Boolean,
    onToggleEthylene: () -> Unit,
    reminderSet: Boolean,
    onToggleReminder: () -> Unit,
    planApplied: Boolean,
    onTogglePlan: () -> Unit,
    navController: NavController
) {
    val stage = prediction.ripeness.predictedRipeningStage
    val rawStageLabel = prediction.ripeness.stageLabel
    val confidence = prediction.ripeness.confidence
    val expectedContinuousStage = prediction.ripeness.expectedContinuousRipeningStage
    val displayStageLabel = if (rawStageLabel.startsWith("Stage ", ignoreCase = true)) {
        rawStageLabel
    } else {
        "Stage $stage — $rawStageLabel"
    }
    val isTerminalStage5 = stage == 5

    // Real scenario values from backend response (single source of truth)
    val scenarios = prediction.scenarios ?: emptyMap()
    val ambientScenario = scenarios["ambient"]
    val activeScenario = scenarios[selectedPreset.key] ?: scenarios["10C"]

    val ambientRul = if (isTerminalStage5) 0f else (ambientScenario?.estimatedRulDays ?: 0f)
    val activeRul = if (isTerminalStage5) 0f else (activeScenario?.estimatedRulDays ?: 0f)
    val isExtrapolated = activeScenario?.isExtrapolated == true || selectedPreset == StoragePresetKey.FRIDGE_4C

    val maxHorizon = maxOf(14f, maxOf(ambientRul, activeRul) + 2f)

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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(BgSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Hass",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMain
                        )
                    }
                    RipenessBadge(stage = stage, customLabel = displayStageLabel)
                }
            }
        }

        // -----------------------------------------------------------------
        // 2. MAIN HEADLINE & SPECIMEN CONTEXT CHIP
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "INTERACTIVE RIPENING SIMULATION",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "What-If Storage Lab",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Forest,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Simulate how changing temperatures, humidity, and containment alters your avocado's remaining usable life in real time.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Specimen Context Chip
                FreshCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = FreshCardVariant.Elevated
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
                                .size(50.dp)
                                .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
                                .background(BgSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Specimen",
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = prediction.itemName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Forest
                                )
                                Box(
                                    modifier = Modifier
                                        .background(PrimaryLight, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Stage $stage",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryDark
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Baseline: ~${String.format(Locale.US, "%.1f", ambientRul)}d at ~20°C Ambient (Scan: ${formatConditionName(activeStorageCondition)})",
                                fontSize = 11.5.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 3. TERMINAL MATURITY BANNER (Stage 5 Overripe)
        // -----------------------------------------------------------------
        if (isTerminalStage5) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFFFCA5A5), RoundedCornerShape(FreshIQRadius.radiusMd))
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(FreshIQRadius.radiusMd))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Terminal Maturity Boundary Notice (Stage 5 — Overripe)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF991B1B)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "This produce has reached terminal senescent maturity (Stage 5). Remaining usable life is 0.0 days across all simulation scenarios. Refrigeration cannot restore expired shelf life or reverse cellular breakdown.",
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
        // 4. THERMAL REGIME SELECTOR (4 Scenarios)
        // -----------------------------------------------------------------
        item {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = Forest,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Thermal Regime Selector",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Forest
                            )
                        }
                        Text(
                            text = "Arrhenius Q10 Deck",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Select target thermal zone to view the real ML and biophysical RUL predictions.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4 Preset Cards in Column format for mobile responsiveness
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StoragePresetKey.values().forEach { preset ->
                            val scenarioDto = prediction.scenarios?.get(preset.key)
                            val isSelected = selectedPreset == preset
                            val isPresetExtrapolated = scenarioDto?.isExtrapolated == true || preset == StoragePresetKey.FRIDGE_4C
                            val presetRul = if (isTerminalStage5) 0f else (scenarioDto?.estimatedRulDays ?: 0f)
                            val isActiveScanCondition = matchesStorageCondition(activeStorageCondition, preset.key)

                            PresetSelectorCard(
                                preset = preset,
                                isSelected = isSelected,
                                isExtrapolated = isPresetExtrapolated,
                                rulDays = presetRul,
                                isActiveScan = isActiveScanCondition,
                                onClick = { onSelectPreset(preset) }
                            )
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 5. MICROCLIMATE MODIFIERS (Container & Ethylene)
        // -----------------------------------------------------------------
        item {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = Forest,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Microclimate Modifiers",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Forest
                            )
                        }
                        Text(
                            text = "Vapor & Ethylene",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Storage Enclosure Format",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3 Segments: Open air, Crisper drawer, Paper bag
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ContainerFormat.values().forEach { fmt ->
                            val isFmtSelected = containerFormat == fmt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isFmtSelected) Color.White else Color.Transparent)
                                    .border(
                                        width = if (isFmtSelected) 1.dp else 0.dp,
                                        color = if (isFmtSelected) BorderAccent else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onSetContainerFormat(fmt) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fmt.label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isFmtSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isFmtSelected) Forest else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ethylene Proximity Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Proximity to High-Ethylene Fruits",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMain
                            )
                            Text(
                                text = "Apples, Bananas, Kiwis within 30cm radius",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Switch(
                            checked = ethyleneProximity,
                            onCheckedChange = { onToggleEthylene() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Forest,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFCBD5E1)
                            )
                        )
                    }

                    if (ethyleneProximity) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Ethylene Warning: Co-locating avocados with climacteric fruits accelerates softening via autocatalytic gas exposure. Maintain 15cm+ separation to retain full shelf life.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF92400E),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Backend Model Boundary: Numerical RUL days are strictly computed by the validated backend models for Ambient, 20°C, 10°C, and 4°C. The frontend never synthesizes speculative mathematical formulas.",
                        fontSize = 11.sp,
                        color = TextSubtle,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // 6. MAIN DYNAMIC RESULTS HERO CARD (Simulated Projection)
        // -----------------------------------------------------------------
        item {
            FreshCard(
                modifier = Modifier.fillMaxWidth(),
                variant = FreshCardVariant.Elevated
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "SIMULATED MODEL PROJECTION • ${selectedPreset.label.uppercase(Locale.US)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", activeRul),
                                fontSize = 46.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isTerminalStage5) Color(0xFF991B1B) else Forest,
                                lineHeight = 48.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                                Text(
                                    text = "Days",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                                Text(
                                    text = "Remaining Usable Life",
                                    fontSize = 10.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Extension Badge vs Baseline
                        Column(horizontalAlignment = Alignment.End) {
                            val isFridge4CWithBackendGain = selectedPreset == StoragePresetKey.FRIDGE_4C &&
                                !isTerminalStage5 &&
                                (prediction.refrigerationExtensionGainDays ?: 0f) > 0f

                            val (badgeBg, badgeText, badgeTextColor) = when {
                                isTerminalStage5 -> Triple(Color(0xFFFEE2E2), "Terminal Ripeness", Color(0xFF991B1B))
                                isFridge4CWithBackendGain -> Triple(
                                    BlueIceBg,
                                    "+${String.format(Locale.US, "%.1f", prediction.refrigerationExtensionGainDays ?: 0f)}d Refrig Gain",
                                    BlueIceText
                                )
                                selectedPreset == StoragePresetKey.COLD_10C -> Triple(PrimaryLight, "Cold Preservation Tier", PrimaryDark)
                                selectedPreset == StoragePresetKey.ROOM_20C -> Triple(BgSubtle, "Standard Reference", TextMuted)
                                else -> Triple(BgSubtle, "Baseline Reference", TextMuted)
                            }

                            Box(
                                modifier = Modifier
                                    .background(badgeBg, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeTextColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isExtrapolated) "Model-Based Extrapolation (Q10 = 2.38)" else "Validated Empirical Model",
                                fontSize = 10.sp,
                                fontWeight = if (isExtrapolated) FontWeight.Bold else FontWeight.Medium,
                                color = if (isExtrapolated) Color(0xFF1E40AF) else TextMuted
                            )
                        }
                    }

                    // 4°C Extrapolation Disclaimer Notice inside Hero
                    if (selectedPreset == StoragePresetKey.FRIDGE_4C) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(FreshIQRadius.radiusMd))
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(FreshIQRadius.radiusMd))
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "Info",
                                    tint = Color(0xFF1E40AF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "MODEL-BASED EXTRAPOLATION DISCLAIMER (4°C DOMESTIC REFRIGERATOR)",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = activeScenario?.uncertaintyNote ?: "4°C is not an empirically observed training condition in the dataset. This prediction is an AI biophysical kinetic simulation based on avocado respiration slowing (Q10 = 2.38). Shelf life is physiologically bounded by chilling sensitivity. Never presented as direct ground-truth experimental data.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF1E3A8A),
                                        lineHeight = 16.sp
                                    )
                                    if (isTerminalStage5) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Note: Produce is already in Stage 5 (Overripe); refrigeration cannot reverse decay.",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Comparative Timeline Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusMd))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SHELF-LIFE DIFFERENTIAL",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMain
                                )
                                Text(
                                    text = "Day Horizon (0 – ${maxHorizon.toInt()})",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            // Baseline Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Current Baseline (20°C Ambient)",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", ambientRul)}d",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMain
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                val baseFraction = (ambientRul / maxHorizon).coerceIn(0.04f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(baseFraction)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF94A3B8))
                                    )
                                }
                            }

                            // Simulated Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Simulated: ${selectedPreset.label} (${selectedPreset.temp})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExtrapolated) Color(0xFF1E40AF) else PrimaryDark
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", activeRul)}d",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExtrapolated) Color(0xFF1E40AF) else Forest
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                val simFraction = (activeRul / maxHorizon).coerceIn(0.04f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(simFraction)
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (isExtrapolated) Color(0xFF3B82F6) else PrimaryGreen)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Degradation Kinetics Trajectory Curve
                    Text(
                        text = "ESTIMATED FIRMNESS TRAJECTORY (N) VS. TIME",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    DegradationKineticsCanvas(
                        ambientRul = ambientRul,
                        activeRul = activeRul,
                        maxHorizon = maxHorizon,
                        isExtrapolated = isExtrapolated,
                        isTerminalStage5 = isTerminalStage5
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optimal Culinary Readiness Window Breakdown
                    CulinaryReadinessSection(
                        isTerminalStage5 = isTerminalStage5
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // 7. ACTION ADVISORY GUIDANCE CARD (What Should I Do?)
        // -----------------------------------------------------------------
        item {
            ActionAdvisoryCard(
                preset = selectedPreset,
                recommendation = activeScenario?.recommendation ?: prediction.actionableRecommendation,
                reminderSet = reminderSet,
                onToggleReminder = onToggleReminder,
                planApplied = planApplied,
                onTogglePlan = onTogglePlan
            )
        }

        // -----------------------------------------------------------------
        // 8. SCIENTIFIC METHODOLOGY NOTE & BADGES
        // -----------------------------------------------------------------
        item {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Scientific Methodology",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = Forest
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Estimates for 10°C and 20°C conditions are directly validated against controlled laboratory respiration and firmness degradation cohorts. The 4°C estimate is a mathematical extrapolation derived from the Arrhenius temperature-dependence equation (Q10 = 2.38), as the primary ground-truth training dataset monitored specimens at 10°C and 20°C. While deep chilling delays soft-rot, domestic refrigerator temperatures below 5°C carry physiological risks of vascular browning.",
                        fontSize = 12.sp,
                        color = TextMuted,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Empirical: 10°C & 20°C ML Models",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryDark
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFF1E40AF),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Model: Arrhenius / Q10 = 2.38 Extrapolation",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Chilling Injury: <5°C Vascular Browning Risk",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 9. FOOD SAFETY DISCLAIMER
        // -----------------------------------------------------------------
        item {
            FoodSafetyDisclaimer(
                modifier = Modifier.fillMaxWidth()
            )
        }

        // -----------------------------------------------------------------
        // 10. NAVIGATION ACTIONS FOOTER
        // -----------------------------------------------------------------
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.Comparison.route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Forest,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(FreshIQRadius.radiusMd),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Open Shelf-Life Comparison Screen →",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Forest
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Forest),
                    shape = RoundedCornerShape(FreshIQRadius.radiusMd),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Return to Analysis Result",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(FreshIQSpacing.spaceXl))
        }
    }
}

// =========================================================================
// Subcomponent: PresetSelectorCard
// =========================================================================

@Composable
private fun PresetSelectorCard(
    preset: StoragePresetKey,
    isSelected: Boolean,
    isExtrapolated: Boolean,
    rulDays: Float,
    isActiveScan: Boolean,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (preset) {
        StoragePresetKey.AMBIENT -> Icons.Default.WbSunny
        StoragePresetKey.ROOM_20C -> Icons.Default.Airplay
        StoragePresetKey.COLD_10C -> Icons.Default.Inbox
        StoragePresetKey.FRIDGE_4C -> Icons.Default.AcUnit
    }

    val cardBg = if (isSelected) {
        if (isExtrapolated) Color(0xFFEFF6FF) else PrimaryLight
    } else BgSubtle

    val cardBorderColor = if (isSelected) {
        if (isExtrapolated) Color(0xFF3B82F6) else PrimaryGreen
    } else BorderLight

    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
            .background(cardBg)
            .border(borderWidth, cardBorderColor, RoundedCornerShape(FreshIQRadius.radiusMd))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                if (isExtrapolated) Color(0xFFDBEAFE) else Color.White
                            } else Color(0xFFE2E8F0)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) {
                            if (isExtrapolated) Color(0xFF1E40AF) else Forest
                        } else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = preset.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) Forest else TextMain
                        )
                        if (isActiveScan) {
                            Box(
                                modifier = Modifier
                                    .background(Forest, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SCAN",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Text(
                        text = preset.sub,
                        fontSize = 11.sp,
                        color = if (isSelected) PrimaryDark else TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (isExtrapolated) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDBEAFE), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "EXTRAPOLATED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(if (isSelected) Color(0xFFDCFCE7) else Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "EMPIRICAL",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Forest else TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "~${String.format(Locale.US, "%.1f", rulDays)} Days",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isSelected) {
                        if (isExtrapolated) Color(0xFF1E40AF) else Forest
                    } else TextMain
                )
            }
        }
    }
}

// =========================================================================
// Subcomponent: DegradationKineticsCanvas
// =========================================================================

@Composable
private fun DegradationKineticsCanvas(
    ambientRul: Float,
    activeRul: Float,
    maxHorizon: Float,
    isExtrapolated: Boolean,
    isTerminalStage5: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val startX = 10f
            val plotWidth = width - 20f
            val topY = 15f
            val bottomY = height - 15f

            // Grid lines
            val gridY1 = topY + (bottomY - topY) * 0.33f
            val gridY2 = topY + (bottomY - topY) * 0.66f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(startX, gridY1),
                end = Offset(width - 10f, gridY1),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(startX, gridY2),
                end = Offset(width - 10f, gridY2),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
            // Bottom Axis
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(startX, bottomY),
                end = Offset(width - 10f, bottomY),
                strokeWidth = 1.5f
            )

            if (isTerminalStage5) {
                // Flat line at 0
                drawLine(
                    color = Color(0xFF991B1B),
                    start = Offset(startX, bottomY),
                    end = Offset(width - 10f, bottomY),
                    strokeWidth = 3f
                )
            } else {
                val baselineEndX = startX + minOf(plotWidth, (ambientRul / maxHorizon) * plotWidth)
                val simulatedEndX = startX + minOf(plotWidth, (activeRul / maxHorizon) * plotWidth)

                // Baseline curve (dashed gray)
                val basePath = Path().apply {
                    moveTo(startX, topY)
                    quadraticTo(
                        (startX + baselineEndX) / 2f,
                        topY + (bottomY - topY) * 0.7f,
                        baselineEndX,
                        bottomY
                    )
                }
                drawPath(
                    path = basePath,
                    color = Color(0xFF94A3B8),
                    style = Stroke(width = 2.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
                )
                drawCircle(
                    color = Color(0xFF94A3B8),
                    radius = 4.5f,
                    center = Offset(baselineEndX, bottomY)
                )

                // Simulated curve (solid green or blue)
                val simColor = if (isExtrapolated) Color(0xFF3B82F6) else PrimaryGreen
                val simPath = Path().apply {
                    moveTo(startX, topY)
                    quadraticTo(
                        (startX + simulatedEndX) / 2f,
                        topY + (bottomY - topY) * 0.55f,
                        simulatedEndX,
                        bottomY
                    )
                }
                drawPath(
                    path = simPath,
                    color = simColor,
                    style = Stroke(width = 3.2f)
                )
                drawCircle(
                    color = simColor,
                    radius = 5.5f,
                    center = Offset(simulatedEndX, bottomY)
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Day 0", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextMuted)
        Text(text = "Base: ${String.format(Locale.US, "%.1f", ambientRul)}d", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextMuted)
        Text(text = "Sim: ${String.format(Locale.US, "%.1f", activeRul)}d", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = if (isExtrapolated) Color(0xFF1E40AF) else PrimaryDark, fontWeight = FontWeight.Bold)
        Text(text = "${maxHorizon.toInt()}d Max", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextMuted)
    }
}

// =========================================================================
// Subcomponent: CulinaryReadinessSection
// =========================================================================

@Composable
private fun CulinaryReadinessSection(
    isTerminalStage5: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Forest,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Culinary Readiness Stages",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Forest
                )
            }
            Text(
                text = "Illustrative Guidance (Non-ML)",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isTerminalStage5) {
            Text(
                text = "Terminal Maturity: Ready for immediate puree, smoothie, or baking use if sensory inspection passes.",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF991B1B)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Window 1: Firm Slicing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Firm Slicing Window",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextMain
                            )
                            Text(
                                text = "Early Phase",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PrimaryDark
                            )
                        }
                        Text(
                            text = "Clean slicing for salads, bowls, and toast with zero tissue collapse.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Window 2: Creamy Guacamole
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Creamy Guacamole",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextMain
                            )
                            Text(
                                text = "Peak Ripeness",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Text(
                            text = "Peak lipid richness, buttery mouthfeel, and maximum aromatic bouquet.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Window 3: Final Usable Margin
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Final Usable Margin",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextMain
                            )
                            Text(
                                text = "Late Stage",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFDC2626)
                            )
                        }
                        Text(
                            text = "Soft pulp state; consume promptly before cellular softening depression.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Subcomponent: ActionAdvisoryCard
// =========================================================================

@Composable
private fun ActionAdvisoryCard(
    preset: StoragePresetKey,
    recommendation: String,
    reminderSet: Boolean,
    onToggleReminder: () -> Unit,
    planApplied: Boolean,
    onTogglePlan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0F291E), Color(0xFF1A3826))
                )
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "WHAT SHOULD I DO?",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LimeGlow,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Action Advisory: ${preset.label}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = recommendation,
                fontSize = 12.5.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle Reminder Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (reminderSet) PrimaryLight else Color.White.copy(alpha = 0.15f)
                        )
                        .clickable { onToggleReminder() }
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (reminderSet) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (reminderSet) Forest else Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (reminderSet) "Reminder Active" else "Set Reminder",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminderSet) Forest else Color.White
                        )
                    }
                }

                // Apply Plan Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (planApplied) PrimaryGreen else Color.White
                        )
                        .clickable { onTogglePlan() }
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdded,
                            contentDescription = null,
                            tint = if (planApplied) Color.White else Forest,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (planApplied) "Plan Logged" else "Apply Plan",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (planApplied) Color.White else Forest
                        )
                    }
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

private fun matchesStorageCondition(sessionCondition: String, presetKey: String): Boolean {
    val normSession = sessionCondition.lowercase(Locale.US).replace("°", "").replace(" ", "").replace("_", "")
    val normPreset = presetKey.lowercase(Locale.US).replace("°", "").replace(" ", "").replace("_", "")
    return when {
        normPreset.contains("ambient") && normSession.contains("ambient") -> true
        normPreset.contains("20c") && normSession.contains("20c") -> true
        normPreset.contains("10c") && normSession.contains("10c") -> true
        (normPreset.contains("4c") || normPreset.contains("fridge")) && (normSession.contains("4c") || normSession.contains("fridge") || normSession.contains("refrigerator")) -> true
        else -> false
    }
}
