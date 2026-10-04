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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BlueIceAccent
import com.freshiq.app.ui.theme.BlueIceBadge
import com.freshiq.app.ui.theme.BlueIceBadgeText
import com.freshiq.app.ui.theme.BlueIceBorder
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.BorderSubtle
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.ForestDark
import com.freshiq.app.ui.theme.FreshIQElevation
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.MonoDataMetric
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.Stage1Fill
import com.freshiq.app.ui.theme.Stage2Fill
import com.freshiq.app.ui.theme.Stage3Fill
import com.freshiq.app.ui.theme.Stage4Fill
import com.freshiq.app.ui.theme.Stage5Fill
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
import com.freshiq.app.ui.theme.TextSubtle

// =========================================================================
// Section Header
// =========================================================================

@Composable
fun FreshSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FreshIQTypography.titleLarge,
                color = Forest
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = FreshIQTypography.bodyMedium,
                    color = TextMuted
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

// =========================================================================
// Metric Card
// =========================================================================

@Composable
fun FreshMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    subValue: String? = null,
    icon: ImageVector? = null,
    iconColor: Color = PrimaryGreen
) {
    FreshCard(
        modifier = modifier,
        variant = FreshCardVariant.Default
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    letterSpacing = 0.5.sp
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MonoDataMetric
            )

            if (subValue != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subValue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =========================================================================
// Buttons (Primary, Secondary, Accent)
// =========================================================================

@Composable
fun FreshPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
            .background(if (enabled) Forest else Forest.copy(alpha = 0.5f))
            .clickable(enabled = enabled && !isLoading) { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun FreshSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
            .background(Color.White)
            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Forest,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                color = Forest,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun FreshAccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(PrimaryGreen, PrimaryDark)
                )
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

// =========================================================================
// Technical Badges (Temperature, Empirical, Extrapolation)
// =========================================================================

@Composable
fun TemperatureBadge(
    temperatureC: Float,
    modifier: Modifier = Modifier,
    isExtrapolated: Boolean = false
) {
    val bgColor = if (isExtrapolated) BlueIceBadge else PrimaryLight
    val textColor = if (isExtrapolated) BlueIceBadgeText else PrimaryDark
    val label = if (temperatureC <= 5f) "${temperatureC.toInt()}°C Chilled" else "${temperatureC.toInt()}°C Ambient"

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(FreshIQRadius.radiusFull))
            .border(1.dp, if (isExtrapolated) BlueIceBorder else BorderSubtle, RoundedCornerShape(FreshIQRadius.radiusFull))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isExtrapolated) Icons.Default.AcUnit else Icons.Default.DeviceThermostat,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun EmpiricalModelBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            tint = PrimaryDark,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "Validated Empirical Model",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDark
        )
    }
}

@Composable
fun ExtrapolationModelBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(BlueIceBadge, RoundedCornerShape(FreshIQRadius.radiusFull))
            .border(1.dp, BlueIceBorder, RoundedCornerShape(FreshIQRadius.radiusFull))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AcUnit,
            contentDescription = null,
            tint = BlueIceAccent,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "Model-Based Extrapolation (Q10 = 2.38)",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = BlueIceBadgeText
        )
    }
}

// =========================================================================
// 5-Stage Ripening Progression Bar Track
// =========================================================================

@Composable
fun RipenessProgressTrack(
    currentStage: Int,
    modifier: Modifier = Modifier
) {
    val stageColors = listOf(Stage1Fill, Stage2Fill, Stage3Fill, Stage4Fill, Stage5Fill)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (stageIndex in 1..5) {
                val isCompletedOrCurrent = stageIndex <= currentStage
                val isCurrent = stageIndex == currentStage
                val color = if (isCompletedOrCurrent) stageColors[stageIndex - 1] else Color(0xFFE2E8F0)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isCurrent) 8.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("S1: Underripe", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSubtle)
            Text("S3: Ripe", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSubtle)
            Text("S5: Overripe", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSubtle)
        }
    }
}

// =========================================================================
// Empty State
// =========================================================================

@Composable
fun FreshEmptyState(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    FreshCard(
        modifier = modifier.fillMaxWidth(),
        variant = FreshCardVariant.Subtle
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, BorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Forest
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                FreshPrimaryButton(
                    text = actionText,
                    onClick = onAction
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommonUIHelpersPreview() {
    FreshIQTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FreshSectionHeader(
                title = "Biophysical Telemetry",
                subtitle = "Optical Exocarp Analysis"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FreshMetricCard(
                    value = "94.8%",
                    label = "CONFIDENCE",
                    subValue = "\u00B10.3d MAE",
                    modifier = Modifier.weight(1f)
                )
                FreshMetricCard(
                    value = "4.2d",
                    label = "RUL ESTIMATE",
                    subValue = "Ambient (20°C)",
                    modifier = Modifier.weight(1f)
                )
            }

            RipenessProgressTrack(currentStage = 3)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EmpiricalModelBadge()
                TemperatureBadge(20f)
            }

            ExtrapolationModelBadge()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FreshPrimaryButton(
                    text = "Analyze Produce",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                FreshSecondaryButton(
                    text = "Configure",
                    onClick = {}
                )
            }
        }
    }
}
