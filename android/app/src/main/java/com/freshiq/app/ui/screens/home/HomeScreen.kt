package com.freshiq.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.freshiq.app.domain.model.ProduceType
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.screens.history.HistoryViewModel
import com.freshiq.app.ui.theme.BotanicalOnPrimary
import com.freshiq.app.ui.theme.BotanicalOnPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalOnSecondaryContainer
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSecondaryContainer
import com.freshiq.app.ui.theme.BotanicalSecondaryFixed
import com.freshiq.app.ui.theme.BotanicalSurface
import com.freshiq.app.ui.theme.BotanicalSurfaceContainer
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHigh
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.BotanicalTertiaryFixed
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val historyState by historyViewModel.uiState.collectAsState()
    val mostRecentScan = historyState.rawScans.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BotanicalSurface),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceLg)
    ) {
        // =========================================================================
        // 1. Editorial Warm Header Section
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FreshIQSpacing.margin)
                    .padding(top = FreshIQSpacing.spaceLg, bottom = FreshIQSpacing.spaceSm)
            ) {
                // Category Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalSurfaceContainerHigh)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(BotanicalPrimaryContainer)
                    )
                    Text(
                        text = "AI PRODUCE INTELLIGENCE",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnSurfaceVariant,
                        letterSpacing = 0.06.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Welcome Headline
                Text(
                    text = "Know when your produce is ready.",
                    style = FreshIQTypography.headlineLarge,
                    color = BotanicalOnSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle
                Text(
                    text = "Point, scan, and discover exact ripeness and peak flavor windows in seconds.",
                    style = FreshIQTypography.bodyMedium,
                    color = BotanicalOnSurfaceVariant
                )
            }
        }

        // =========================================================================
        // 2. Interactive Tactile Scan Shutter Card
        // =========================================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FreshIQSpacing.margin)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FreshIQRadius.radius3xl))
                        .background(BotanicalPrimaryContainer)
                        .shadow(12.dp, RoundedCornerShape(FreshIQRadius.radius3xl))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(Screen.Scan.route)
                        }
                        .padding(FreshIQSpacing.spaceLg)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "INSTANT OPTICAL ANALYSIS",
                                style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = BotanicalPrimaryFixed,
                                letterSpacing = 0.08.sp
                            )
                            Text(
                                text = "Start New Scan",
                                style = FreshIQTypography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = BotanicalOnPrimary
                            )
                            Text(
                                text = "Calibrated for natural daylight",
                                style = FreshIQTypography.bodyMedium,
                                color = BotanicalOnPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        // Tactile Shutter Button
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(BotanicalSurfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Start Scan",
                                tint = BotanicalPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. Supported Botanical Field Strip / Showcase
        // =========================================================================
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FreshIQSpacing.margin),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Supported Produce",
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(BotanicalSurfaceContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "3 Active",
                                style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                                color = BotanicalOnSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Library specs",
                        style = FreshIQTypography.labelSmall,
                        color = BotanicalPrimaryContainer
                    )
                }

                // Horizontal Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = FreshIQSpacing.margin),
                    horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
                ) {
                    // Avocado Showcase Card
                    ProduceShowcaseCard(
                        produceName = "Hass Avocado",
                        capabilityTag = "Ripeness & Shelf Life",
                        featureTag = "Firmness Brix",
                        featureColor = BotanicalPrimaryContainer,
                        onClick = { navController.navigate(Screen.Scan.route) }
                    )

                    // Mango Showcase Card
                    ProduceShowcaseCard(
                        produceName = "White Chaunsa Mango",
                        capabilityTag = "5-Stage Ripeness",
                        featureTag = "Sugar Spectrum",
                        featureColor = BotanicalSecondaryContainer,
                        onClick = { navController.navigate(Screen.Scan.route) }
                    )

                    // Banana Showcase Card
                    ProduceShowcaseCard(
                        produceName = "Cavendish Banana",
                        capabilityTag = "3-Stage Ripeness",
                        featureTag = "Starch Conversion",
                        featureColor = BotanicalTertiaryFixed,
                        onClick = { navController.navigate(Screen.Scan.route) }
                    )
                }
            }
        }

        // =========================================================================
        // 4. Recent Scan Preview or Intentional Empty State
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FreshIQSpacing.margin),
                verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Analysis",
                        style = FreshIQTypography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnSurface
                    )
                    if (mostRecentScan != null) {
                        Text(
                            text = mostRecentScan.formattedDate,
                            style = FreshIQTypography.labelSmall,
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                if (mostRecentScan != null) {
                    // Real scan history preview card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                            .background(BotanicalSurfaceContainerLowest)
                            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
                            .clickable { navController.navigate(Screen.History.route) }
                            .padding(FreshIQSpacing.spaceMd)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                                        .background(BotanicalSurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Eco,
                                        contentDescription = "Produce Icon",
                                        tint = BotanicalPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = mostRecentScan.prediction.itemName,
                                            style = FreshIQTypography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BotanicalOnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(9999.dp))
                                                .background(BotanicalSurfaceContainerHigh)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = mostRecentScan.confidencePercent,
                                                style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                                                color = BotanicalOnSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(BotanicalPrimaryContainer)
                                        )
                                        Text(
                                            text = mostRecentScan.stageLabel,
                                            style = FreshIQTypography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BotanicalPrimaryContainer
                                        )
                                    }
                                }
                            }

                            if (mostRecentScan.prediction.foodType.equals("avocado", ignoreCase = true) && mostRecentScan.ambientRulDays > 0f) {
                                Text(
                                    text = "Estimated usable shelf-life: ~${String.format("%.1f", mostRecentScan.ambientRulDays)} days",
                                    style = FreshIQTypography.bodySmall,
                                    color = BotanicalOnSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Intentional clean empty state (NO fake/mock scans)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                            .background(BotanicalSurfaceContainerLow)
                            .padding(FreshIQSpacing.spaceLg),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = BotanicalOutline,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "No scans recorded yet",
                                style = FreshIQTypography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = BotanicalOnSurface
                            )
                            Text(
                                text = "Tap Start New Scan to evaluate your fresh produce.",
                                style = FreshIQTypography.bodySmall,
                                color = BotanicalOnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 5. Quick Action Navigation Pills
        // =========================================================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FreshIQSpacing.margin)
                    .padding(bottom = FreshIQSpacing.spaceXl),
                horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                // View History Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalSurfaceContainerLowest)
                        .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(9999.dp))
                        .clickable { navController.navigate(Screen.History.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = BotanicalOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "View History",
                            style = FreshIQTypography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                    }
                }

                // Browse Insights Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalSurfaceContainerLowest)
                        .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(9999.dp))
                        .clickable { navController.navigate(Screen.Insights.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Insights",
                            tint = BotanicalPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Browse Insights",
                            style = FreshIQTypography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProduceShowcaseCard(
    produceName: String,
    capabilityTag: String,
    featureTag: String,
    featureColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BotanicalSurfaceContainerLowest)
            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
            .clickable { onClick() }
            .padding(FreshIQSpacing.spaceSm)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Visual Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                    .background(BotanicalSurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = produceName,
                    tint = BotanicalPrimaryContainer,
                    modifier = Modifier.size(40.dp)
                )

                // Feature Chip in top left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BotanicalSurfaceContainerLowest.copy(alpha = 0.90f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(featureColor)
                        )
                        Text(
                            text = featureTag,
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurface
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                Text(
                    text = produceName,
                    style = FreshIQTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = capabilityTag,
                    style = FreshIQTypography.bodySmall,
                    color = BotanicalOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
