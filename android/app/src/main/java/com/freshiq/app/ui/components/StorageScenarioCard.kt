package com.freshiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.data.model.ScenarioRulDto
import com.freshiq.app.ui.theme.BlueIceAccent
import com.freshiq.app.ui.theme.BlueIceBadge
import com.freshiq.app.ui.theme.BlueIceBadgeText
import com.freshiq.app.ui.theme.BlueIceBg
import com.freshiq.app.ui.theme.BlueIceBorder
import com.freshiq.app.ui.theme.BlueIceText
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSecondaryContainer
import com.freshiq.app.ui.theme.BotanicalSecondaryFixed
import com.freshiq.app.ui.theme.BotanicalSurfaceContainer
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHigh
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography

/**
 * Reusable storage scenario card presenting backend-calculated shelf-life scenarios.
 *
 * GUARDRAILS:
 * - Strictly displays scenario data returned by the backend (ScenarioRulDto).
 * - NEVER calculates delta days, gain, Q10, Arrhenius, or thermal interpolation on client.
 * - 4°C domestic refrigeration carries the MANDATORY DISTINCT BLUE styling and
 *   "Model-based extrapolation" badge when marked as extrapolated by the backend.
 */
@Composable
fun StorageScenarioCard(
    scenario: ScenarioRulDto,
    modifier: Modifier = Modifier,
    isBaseline: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val isExtrapolated = scenario.isExtrapolated || scenario.temperatureC <= 4.0f

    val cardBg = if (isExtrapolated) BlueIceBg else BotanicalSurfaceContainerLowest
    val cardBorder = if (isExtrapolated) BlueIceBorder else Color(0xFFE6E6DF)
    val accentColor = if (isExtrapolated) BlueIceAccent else BotanicalPrimaryContainer

    val iconVector = when {
        isExtrapolated -> Icons.Default.AcUnit
        scenario.temperatureC <= 12.0f -> Icons.Default.DeviceThermostat
        else -> Icons.Default.WbSunny
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(FreshIQRadius.radiusLg))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(FreshIQSpacing.spaceMd)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
        ) {
            // Header Row: Icon + Title + Temperature Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isExtrapolated) BlueIceBadge else BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = scenario.condition,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = scenario.condition,
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                        Text(
                            text = "${scenario.temperatureC.toInt()}°C Regime",
                            style = FreshIQTypography.labelSmall,
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                if (isBaseline) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(BotanicalSurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Baseline",
                            style = FreshIQTypography.labelSmall,
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }
            }

            // Extrapolation Badge (Mandatory distinct label for 4°C / extrapolated)
            if (isExtrapolated) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(9999.dp))
                        .background(BlueIceBadge)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Extrapolation",
                        tint = BlueIceBadgeText,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Model-based extrapolation",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = BlueIceBadgeText
                    )
                }
            }

            // RUL Value Display (Directly from backend ScenarioRulDto)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "PREDICTED USABLE LIFE",
                        style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                        color = BotanicalOnSurfaceVariant,
                        letterSpacing = 0.05.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", scenario.estimatedRulDays),
                            style = FreshIQTypography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = " days",
                            style = FreshIQTypography.bodyMedium,
                            color = BotanicalOnSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Backend Recommendation or Uncertainty Note
            if (scenario.recommendation.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                        .background(if (isExtrapolated) Color.White.copy(alpha = 0.7f) else BotanicalSurfaceContainerLow)
                        .padding(FreshIQSpacing.spaceSm)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Note",
                            tint = accentColor,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = scenario.recommendation,
                            style = FreshIQTypography.bodySmall,
                            color = BotanicalOnSurface
                        )
                    }
                }
            }

            if (!scenario.uncertaintyNote.isNullOrBlank()) {
                Text(
                    text = scenario.uncertaintyNote,
                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isExtrapolated) BlueIceText else BotanicalOutline
                )
            }
        }
    }
}
