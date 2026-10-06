package com.freshiq.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.domain.model.ProduceType
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalPrimaryFixedDim
import com.freshiq.app.ui.theme.BotanicalSecondaryContainer
import com.freshiq.app.ui.theme.BotanicalSecondaryFixed
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHigh
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHighest
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalTertiaryContainer
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography

/**
 * Reusable horizontal segmented ripeness spectrum component matching the Botanical Intelligence spec.
 *
 * Supports arbitrary stage counts (e.g. 5 stages for Avocado/Mango, 3 stages for Banana).
 * Highlights the predicted stage with an active segment color and dropdown marker.
 *
 * NOTE: Presentation only. Stage index and labels must be supplied by the caller.
 */
@Composable
fun SegmentedRipenessSpectrum(
    activeStageIndex: Int,
    stageLabels: List<String>,
    modifier: Modifier = Modifier,
    spectrumTitle: String = "Ripeness Maturity Spectrum",
    stageColors: List<Color>? = null,
    activeColor: Color = BotanicalPrimaryContainer
) {
    val totalStages = stageLabels.size.coerceAtLeast(1)
    val clampedIndex = activeStageIndex.coerceIn(0, totalStages - 1)
    val displayStageNumber = clampedIndex + 1

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
    ) {
        // Header: Spectrum title + "Stage X of Y"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = spectrumTitle,
                style = FreshIQTypography.labelSmall,
                color = BotanicalOutline
            )
            Text(
                text = "Stage $displayStageNumber of $totalStages",
                style = FreshIQTypography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )
        }

        // Segmented Track
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(BotanicalSurfaceContainerLow)
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until totalStages) {
                val isActive = i == clampedIndex
                val segmentColor = when {
                    isActive -> activeColor
                    stageColors != null && i < stageColors.size -> stageColors[i]
                    i < clampedIndex -> BotanicalPrimaryFixedDim.copy(alpha = 0.6f)
                    else -> BotanicalSurfaceContainerHighest
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(segmentColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        // Subtle center pulse dot on the active segment
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        // Active Marker Arrow row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until totalStages) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (i == clampedIndex) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Active Stage",
                            tint = activeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Stage Labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0 until totalStages) {
                val isActive = i == clampedIndex
                Text(
                    text = stageLabels[i],
                    style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) activeColor else BotanicalOutline,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Factory helper for produce-specific ripeness spectrum configurations.
 */
object ProduceRipenessSpectrums {
    val avocadoLabels = listOf("Hard", "Breaking", "Firm Ripe", "Ripe", "Overripe")
    val avocadoColors = listOf(
        BotanicalPrimaryFixed,
        BotanicalPrimaryFixedDim,
        BotanicalTertiaryContainer,
        BotanicalPrimaryContainer,
        BotanicalSurfaceContainerHigh
    )

    val mangoLabels = listOf("Mature Green", "Early Yellow", "Ripe", "Peak Ripe", "Soft / Over")
    val mangoColors = listOf(
        BotanicalPrimaryFixedDim,
        BotanicalSecondaryFixed,
        BotanicalSecondaryContainer,
        BotanicalPrimaryContainer,
        BotanicalSurfaceContainerHigh
    )

    val bananaLabels = listOf("Unripe", "Semi-ripe", "Ripe")
    val bananaColors = listOf(
        BotanicalSurfaceContainerHighest,
        BotanicalSecondaryFixed,
        BotanicalSecondaryContainer
    )

    fun getLabelsFor(produceType: ProduceType): List<String> = when (produceType) {
        ProduceType.AVOCADO -> avocadoLabels
        ProduceType.MANGO -> mangoLabels
        ProduceType.BANANA -> bananaLabels
    }

    fun getColorsFor(produceType: ProduceType): List<Color> = when (produceType) {
        ProduceType.AVOCADO -> avocadoColors
        ProduceType.MANGO -> mangoColors
        ProduceType.BANANA -> bananaColors
    }
}
