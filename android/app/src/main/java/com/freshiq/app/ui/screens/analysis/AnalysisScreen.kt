package com.freshiq.app.ui.screens.analysis

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
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
import com.freshiq.app.ui.components.BadgeSize
import com.freshiq.app.ui.components.ExpandableOpticalDetails
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.ProduceRipenessSpectrums
import com.freshiq.app.ui.components.RipenessBadge
import com.freshiq.app.ui.components.SegmentedRipenessSpectrum
import com.freshiq.app.ui.components.StorageScenarioCard
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BotanicalOnPrimary
import com.freshiq.app.ui.theme.BotanicalOnPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalOutlineVariant
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSecondaryContainer
import com.freshiq.app.ui.theme.BotanicalSurface
import com.freshiq.app.ui.theme.BotanicalSurfaceContainer
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHigh
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography
import java.util.Locale

/**
 * Stitch Botanical Intelligence Produce Analysis Screen.
 *
 * Visual & Biophysical Hierarchy:
 * IMAGE
 *  ↓
 * PRODUCE IDENTITY
 *  ↓
 * PREDICTED RIPENESS STAGE (RipenessBadge)
 *  ↓
 * CONFIDENCE (Classification confidence only)
 *  ↓
 * RIPENESS PROGRESSION (SegmentedRipenessSpectrum)
 *  ↓
 * ACTIONABLE RECOMMENDATION (Backend-supplied advice)
 *  ↓
 * PRODUCE-SPECIFIC CAPABILITIES (Avocado: RUL + Scenarios + 4°C Extrapolation; Mango/Banana: Optical classification only)
 *  ↓
 * OPTIONAL TECHNICAL DETAILS (ExpandableOpticalDetails, collapsed by default)
 *  ↓
 * FOOD-SAFETY DISCLAIMER (FoodSafetyDisclaimer)
 *
 * CRITICAL DATA INTEGRITY:
 * Purely presentation-based. Zero client calculation of ripeness, confidence,
 * probabilities, continuous score, RUL, refrigeration gain, Q10, or Arrhenius extrapolation.
 */
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BotanicalSurface)
    ) {
        val prediction = activePrediction

        if (prediction == null) {
            AnalysisEmptyState(
                navController = navController,
                isGeneratingDemo = isGeneratingDemo,
                demoError = demoError,
                recentScans = recentScans,
                onRunDemoScan = { viewModel.runDemoScan(3) },
                onLoadRecentScan = { viewModel.loadHistoricalScan(it) }
            )
        } else {
            AnalysisContent(
                prediction = prediction,
                imageUri = activeImageUri,
                produceType = activeProduceType,
                isSavedToHistory = isSavedToHistory,
                onSaveToHistory = { viewModel.saveToHistory() },
                onShare = { shareDiagnostic(context, prediction) },
                navController = navController
            )
        }
    }
}

/**
 * Intentional empty state when no scan session exists.
 */
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
            .padding(horizontal = FreshIQSpacing.margin, vertical = FreshIQSpacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusXl))
                    .background(BotanicalSurfaceContainerLowest)
                    .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusXl))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = BotanicalPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Active Produce Scan",
                        style = FreshIQTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan fresh produce or select an image to inspect AI ripening analysis, confidence distributions, and actionable handling guidance.",
                        style = FreshIQTypography.bodyMedium,
                        color = BotanicalOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Action: Open Scanner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(BotanicalPrimary)
                            .clickable { navController.navigate(Screen.Scan.route) }
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = BotanicalOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Start New Scan",
                            style = FreshIQTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = BotanicalOnPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Action: Demo Scan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(BotanicalSurfaceContainerLow)
                            .border(1.dp, BotanicalOutlineVariant, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .clickable(enabled = !isGeneratingDemo) { onRunDemoScan() }
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isGeneratingDemo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = BotanicalPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Generating Demo...",
                                style = FreshIQTypography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = BotanicalOnSurface
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = BotanicalPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Run Live Demo Scan (Avocado Stage 3)",
                                style = FreshIQTypography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = BotanicalOnSurface
                            )
                        }
                    }

                    if (demoError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(FreshIQRadius.radiusMd))
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
                                style = FreshIQTypography.bodySmall,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }

                    // Recent Scans list
                    if (recentScans.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(28.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE6E6DF))
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECENT SCANS (${recentScans.size})",
                                style = FreshIQTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BotanicalOutline
                            )
                            Text(
                                text = "View All →",
                                style = FreshIQTypography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BotanicalPrimary,
                                modifier = Modifier.clickable { navController.navigate(Screen.History.route) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        recentScans.take(3).forEach { scan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                                    .background(BotanicalSurfaceContainerLow)
                                    .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusMd))
                                    .clickable { onLoadRecentScan(scan) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BotanicalSurfaceContainer),
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
                                            tint = BotanicalOutline,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Scan ${scan.id.take(12)}",
                                        style = FreshIQTypography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BotanicalOnSurface
                                    )
                                    Text(
                                        text = "Storage: ${scan.storageCondition}",
                                        style = FreshIQTypography.bodySmall,
                                        color = BotanicalOnSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = BotanicalOutline,
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

/**
 * Main Analysis Content displaying the active prediction result.
 */
@Composable
private fun AnalysisContent(
    prediction: PredictionResponseDto,
    imageUri: String?,
    produceType: ProduceType,
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onShare: () -> Unit,
    navController: NavController
) {
    val effectiveProduce = when {
        prediction.foodType.lowercase().trim() == "banana" -> ProduceType.BANANA
        prediction.foodType.lowercase().trim() == "mango" -> ProduceType.MANGO
        produceType != ProduceType.AVOCADO -> produceType
        else -> ProduceType.fromBackendValue(prediction.foodType)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FreshIQSpacing.margin),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // 1. SPECIMEN HERO IMAGE & IDENTITY
        item {
            SpecimenHeroCard(
                imageUri = imageUri,
                produceType = effectiveProduce,
                itemName = prediction.itemName,
                modelId = prediction.modelId
            )
        }

        // 2. PRIMARY RIPENESS ASSESSMENT & CONFIDENCE
        item {
            PrimaryRipenessAssessmentCard(
                prediction = prediction,
                produceType = effectiveProduce
            )
        }

        // 3. SEGMENTED RIPENESS SPECTRUM (Phase 2 Reusable Component)
        item {
            SegmentedRipenessSpectrumSection(
                prediction = prediction,
                produceType = effectiveProduce
            )
        }

        // 4. ACTIONABLE RECOMMENDATION (Backend-supplied advice)
        item {
            ActionableRecommendationCard(
                recommendation = prediction.actionableRecommendation
            )
        }

        // 5. PRODUCE-SPECIFIC CAPABILITIES
        if (effectiveProduce == ProduceType.AVOCADO && prediction.rulAvailable) {
            // Avocado Full Capability: RUL Hero + Storage Scenarios + Extrapolation + Shortcuts
            item {
                AvocadoCapabilitiesSection(
                    prediction = prediction,
                    navController = navController
                )
            }
        } else {
            // Mango & Banana: Classification-only scope notice (NO RUL, NO What-If, NO Comparison)
            item {
                ClassificationOnlyNoticeCard(
                    produceType = effectiveProduce
                )
            }
        }

        // 6. EXPANDABLE OPTICAL MODEL PROBABILITIES (Phase 2 Reusable Component)
        item {
            ExpandableOpticalDetails(
                probabilities = prediction.ripeness.probabilities.getProbabilitiesList(effectiveProduce),
                modelId = prediction.modelId,
                title = "Classification Probability Spread",
                subtitle = "Classification softmax class distribution",
                initiallyExpanded = false
            )
        }

        // 7. FOOD SAFETY DISCLAIMER (Phase 2 Reusable Component)
        item {
            FoodSafetyDisclaimer(
                backendDisclaimer = prediction.legalDisclaimer
            )
        }

        // 8. ACTION BAR: Save, Share, Start New Scan
        item {
            AnalysisActionBar(
                isSavedToHistory = isSavedToHistory,
                onSaveToHistory = onSaveToHistory,
                onShare = onShare,
                onNewScan = { navController.navigate(Screen.Scan.route) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 1. Specimen hero image with taxonomy badges and model identifier.
 */
@Composable
private fun SpecimenHeroCard(
    imageUri: String?,
    produceType: ProduceType,
    itemName: String,
    modelId: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusXl))
            .background(BotanicalSurfaceContainerLowest)
            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusXl))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Specimen Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .background(BotanicalSurfaceContainer)
            ) {
                if (!imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Analyzed Specimen",
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
                            tint = BotanicalOutline,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Optical daylight calibration badge overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = BotanicalSecondaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Natural Daylight Optical Scan",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // AI Complete badge overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(BotanicalPrimary)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BotanicalOnPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "AI Verified",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnPrimary
                    )
                }
            }

            // Produce Identity Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FreshIQSpacing.spaceMd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = itemName.ifBlank { produceType.displayName },
                        style = FreshIQTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOnSurface
                    )
                    Text(
                        text = produceType.scientificName,
                        style = FreshIQTypography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = BotanicalOnSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(BotanicalSurfaceContainerHigh)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = modelId.take(18),
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = BotanicalOutline
                    )
                }
            }
        }
    }
}

/**
 * 2. Primary Ripeness Assessment Card: Visual focal point of predicted stage and model confidence.
 */
@Composable
private fun PrimaryRipenessAssessmentCard(
    prediction: PredictionResponseDto,
    produceType: ProduceType
) {
    val stageInt = prediction.ripeness.predictedRipeningStage
    val stageLabel = prediction.ripeness.stageLabel
    val confidence = prediction.ripeness.confidence

    // Produce-specific badge stage indexing
    val badgeStage = when (produceType) {
        ProduceType.AVOCADO -> stageInt.coerceIn(1, 5)
        ProduceType.MANGO -> (MangoRipenessStage.fromLabel(stageLabel).stageNumber + 1).coerceIn(1, 5)
        ProduceType.BANANA -> (BananaRipenessStage.fromLabel(stageLabel).stageNumber + 1).coerceIn(1, 3)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BotanicalSurfaceContainerLowest)
            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceMd)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ASSESSED MATURITY",
                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOutline,
                    letterSpacing = 0.08.sp
                )

                // Model confidence pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(BotanicalPrimaryFixed.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Model confidence",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        color = BotanicalOnSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", confidence * 100),
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = BotanicalPrimary
                    )
                }
            }

            // Visual Focal Point: Predicted Ripeness Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RipenessBadge(
                    stage = badgeStage,
                    customLabel = stageLabel,
                    size = BadgeSize.Large,
                    pulseDot = true
                )
            }

            // Explanatory Subtitle
            Text(
                text = "Classification derived non-destructively from visible surface colorimetry and biological texture profiles.",
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * 3. Segmented Ripeness Spectrum section using Phase 2 component.
 */
@Composable
private fun SegmentedRipenessSpectrumSection(
    prediction: PredictionResponseDto,
    produceType: ProduceType
) {
    val stageLabel = prediction.ripeness.stageLabel
    val activeIndex = when (produceType) {
        ProduceType.AVOCADO -> (prediction.ripeness.predictedRipeningStage - 1).coerceIn(0, 4)
        ProduceType.MANGO -> MangoRipenessStage.fromLabel(stageLabel).stageNumber.coerceIn(0, 4)
        ProduceType.BANANA -> BananaRipenessStage.fromLabel(stageLabel).stageNumber.coerceIn(0, 2)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BotanicalSurfaceContainerLowest)
            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        SegmentedRipenessSpectrum(
            activeStageIndex = activeIndex,
            stageLabels = ProduceRipenessSpectrums.getLabelsFor(produceType),
            stageColors = ProduceRipenessSpectrums.getColorsFor(produceType),
            spectrumTitle = "${produceType.displayName} Ripeness Spectrum",
            activeColor = BotanicalPrimaryContainer
        )
    }
}

/**
 * 4. Actionable Recommendation Card displaying backend-supplied recommendation.
 */
@Composable
private fun ActionableRecommendationCard(
    recommendation: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BotanicalSurfaceContainerLow)
            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "Actionable Guidance",
                    style = FreshIQTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
            }

            Text(
                text = recommendation.ifBlank {
                    "Hold at ambient room temperature away from direct sunlight for balanced ripening."
                },
                style = FreshIQTypography.bodyMedium,
                color = BotanicalOnSurface,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * 5A. Avocado Full Capabilities: RUL hero, storage scenario projections, and shortcuts.
 */
@Composable
private fun AvocadoCapabilitiesSection(
    prediction: PredictionResponseDto,
    navController: NavController
) {
    val stage = prediction.ripeness.predictedRipeningStage
    val scenarios = prediction.scenarios ?: emptyMap()
    val ambientScenario = scenarios["ambient"]
    val ambientRul = if (stage == 5) 0.0f else (ambientScenario?.estimatedRulDays ?: 0.0f)

    Column(
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
    ) {
        // RUL Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                .background(BotanicalSurfaceContainerLowest)
                .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
                .padding(FreshIQSpacing.spaceLg)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PREDICTED USABLE LIFE",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = BotanicalOutline,
                        letterSpacing = 0.08.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(BotanicalSurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Ambient ~20°C",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", ambientRul),
                        style = FreshIQTypography.displayLarge.copy(fontSize = 44.sp),
                        fontWeight = FontWeight.Bold,
                        color = BotanicalPrimary
                    )
                    Text(
                        text = if (ambientRul == 1.0f) "day remaining" else "days remaining",
                        style = FreshIQTypography.titleMedium,
                        color = BotanicalOnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = if (stage == 5) {
                        "Produce has reached senescent maturity (RUL = 0); chilling cannot reverse overripeness."
                    } else {
                        "Biophysical respiration kinetic estimate based on Hass avocado ripening dynamics. Shelf life varies with temperature."
                    },
                    style = FreshIQTypography.bodySmall,
                    color = BotanicalOnSurfaceVariant
                )
            }
        }

        // Storage Scenarios Section
        if (scenarios.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                Text(
                    text = "STORAGE REGIMES & PROJECTIONS",
                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOutline,
                    letterSpacing = 0.08.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                scenarios.values.forEach { scenario ->
                    StorageScenarioCard(
                        scenario = scenario,
                        isBaseline = scenario.condition.lowercase().contains("ambient")
                    )
                }
            }
        }

        // Navigation Action Buttons: What-If Lab & Comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
        ) {
            // What-If Lab Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(BotanicalSurfaceContainerLowest)
                    .border(1.dp, BotanicalPrimary, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .clickable { navController.navigate(Screen.WhatIf.route) }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = BotanicalPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "What-If Lab",
                    style = FreshIQTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalPrimary
                )
            }

            // Comparison Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(BotanicalSurfaceContainerLowest)
                    .border(1.dp, BotanicalOutlineVariant, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .clickable { navController.navigate(Screen.Comparison.route) }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = null,
                    tint = BotanicalOnSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Compare Regimes",
                    style = FreshIQTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
            }
        }
    }
}

/**
 * 5B. Mango & Banana Notice Card: Transparently explains classification-only biophysical scope.
 */
@Composable
private fun ClassificationOnlyNoticeCard(
    produceType: ProduceType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BotanicalSurfaceContainerLowest)
            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BotanicalSurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BotanicalOutline,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${produceType.displayName} Biophysical Scope",
                    style = FreshIQTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BotanicalOnSurface
                )
                Text(
                    text = "Optical classification model only. Remaining usable life (RUL) and storage kinetic simulations are currently supported for Hass avocado only.",
                    style = FreshIQTypography.bodySmall,
                    color = BotanicalOnSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * 8. Analysis Action Bar: Save to History, Share Diagnostic, and Start New Scan.
 */
@Composable
private fun AnalysisActionBar(
    isSavedToHistory: Boolean,
    onSaveToHistory: () -> Unit,
    onShare: () -> Unit,
    onNewScan: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
        ) {
            // Save to History Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(if (isSavedToHistory) BotanicalPrimaryFixed.copy(alpha = 0.35f) else BotanicalSurfaceContainerLow)
                    .border(
                        1.dp,
                        if (isSavedToHistory) BotanicalPrimary else BotanicalOutlineVariant,
                        RoundedCornerShape(FreshIQRadius.radiusFull)
                    )
                    .clickable { onSaveToHistory() }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSavedToHistory) Icons.Default.BookmarkAdded else Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = if (isSavedToHistory) BotanicalPrimary else BotanicalOnSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSavedToHistory) "Saved" else "Save Scan",
                    style = FreshIQTypography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSavedToHistory) BotanicalPrimary else BotanicalOnSurface
                )
            }

            // Share Diagnostic Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                    .background(BotanicalSurfaceContainerLow)
                    .border(1.dp, BotanicalOutlineVariant, RoundedCornerShape(FreshIQRadius.radiusFull))
                    .clickable { onShare() }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = BotanicalOnSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share",
                    style = FreshIQTypography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BotanicalOnSurface
                )
            }
        }

        // Scan Another Produce Primary Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                .background(BotanicalPrimary)
                .clickable { onNewScan() }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                tint = BotanicalOnPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Scan Another Produce",
                style = FreshIQTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = BotanicalOnPrimary
            )
        }
    }
}

/**
 * Standard Android text share intent for analysis diagnostics.
 * Presentation-only: Zero network requests.
 */
private fun shareDiagnostic(context: Context, prediction: PredictionResponseDto) {
    val stageLabel = prediction.ripeness.stageLabel
    val confidence = prediction.ripeness.confidence
    val isRulSupported = prediction.rulAvailable && prediction.foodType.lowercase() == "avocado"

    val text = buildString {
        appendLine("FreshIQ Produce Ripeness Diagnostic:")
        appendLine("Produce: ${prediction.itemName}")
        appendLine("Assessed Stage: $stageLabel")
        appendLine("Model Confidence: ${String.format(Locale.US, "%.1f", confidence * 100)}%")
        if (isRulSupported) {
            val ambientRul = prediction.scenarios?.get("ambient")?.estimatedRulDays ?: 0f
            appendLine("Estimated Usable Life: ${String.format(Locale.US, "%.1f", ambientRul)} days (Ambient ~20°C)")
        } else {
            appendLine("Remaining Usable Life: Not modeled for this produce (Optical classification only)")
        }
        appendLine("Actionable Guidance: ${prediction.actionableRecommendation}")
        appendLine("\nGenerated via FreshIQ Botanical Intelligence")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Diagnostic"))
}
