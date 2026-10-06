package com.freshiq.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalSurfaceContainer
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.BotanicalTertiary
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography

/**
 * Reusable expandable card for model classification probabilities.
 *
 * Displays ONLY the probabilities directly provided by the backend response.
 * Performs zero probability calculations on the client.
 */
@Composable
fun ExpandableOpticalDetails(
    probabilities: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    modelId: String? = null,
    title: String = "AI Optical Analysis",
    subtitle: String = "Spectral colorimetry model confidence",
    initiallyExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "chevronRotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BotanicalSurfaceContainerLowest)
            .border(1.dp, Color(0xFFE6E6DF), RoundedCornerShape(FreshIQRadius.radiusLg))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Toggle Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(FreshIQSpacing.spaceMd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BotanicalSurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Biotech,
                            contentDescription = "Optical Analysis",
                            tint = BotanicalPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = title,
                            style = FreshIQTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BotanicalOnSurface
                        )
                        Text(
                            text = subtitle,
                            style = FreshIQTypography.labelSmall,
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = BotanicalOnSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotationAngle)
                )
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FreshIQSpacing.spaceMd, vertical = FreshIQSpacing.spaceSm),
                    verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
                ) {
                    probabilities.forEach { (className, prob) ->
                        val percentValue = if (prob in 0.0f..1.0f) prob * 100f else prob
                        val percentFormatted = String.format("%.1f%%", percentValue)
                        val fraction = (percentValue / 100f).coerceIn(0f, 1f)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = className,
                                    style = FreshIQTypography.bodyMedium,
                                    color = BotanicalOnSurface
                                )
                                Text(
                                    text = percentFormatted,
                                    style = FreshIQTypography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fraction > 0.5f) BotanicalPrimaryContainer else BotanicalOnSurfaceVariant
                                )
                            }

                            // Progress Track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(9999.dp))
                                    .background(BotanicalSurfaceContainerLow)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(if (fraction > 0.5f) BotanicalPrimaryContainer else BotanicalTertiary)
                                )
                            }
                        }
                    }

                    if (!modelId.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Model ID: $modelId • Optical wavelength calibrated",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOutline
                        )
                    }
                }
            }
        }
    }
}
