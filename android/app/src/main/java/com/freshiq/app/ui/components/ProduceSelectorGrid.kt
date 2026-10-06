package com.freshiq.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.freshiq.app.domain.model.ProduceType
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSurfaceContainer
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography

/**
 * Reusable 3-column Produce Selector Grid matching the Botanical Intelligence Stitch design.
 *
 * Supports ONLY the 3 validated produce types: Avocado, Mango, Banana.
 * Selection is UI-only and does not trigger prediction or API network traffic.
 */
@Composable
fun ProduceSelectorGrid(
    selectedProduce: ProduceType,
    onSelectProduce: (ProduceType) -> Unit,
    modifier: Modifier = Modifier
) {
    val supportedTypes = remember {
        listOf(ProduceType.AVOCADO, ProduceType.MANGO, ProduceType.BANANA)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SELECT PRODUCE TARGET",
                style = FreshIQTypography.labelSmall,
                color = BotanicalOnSurfaceVariant,
                letterSpacing = 0.08.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceXs)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryContainer)
                )
                Text(
                    text = "Ready to scan",
                    style = FreshIQTypography.labelSmall,
                    color = BotanicalPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 3-Column Produce Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
        ) {
            supportedTypes.forEach { type ->
                val isSelected = type == selectedProduce
                val varietyName = when (type) {
                    ProduceType.AVOCADO -> "Hass"
                    ProduceType.MANGO -> "White Chaunsa"
                    ProduceType.BANANA -> "Cavendish"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
                        .background(if (isSelected) BotanicalSurfaceContainerLowest else BotanicalSurfaceContainerLow)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) BotanicalPrimaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(FreshIQRadius.radiusLg)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectProduce(type)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Square Container with Produce Icon & Selected Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                                .background(if (isSelected) BotanicalPrimaryFixed.copy(alpha = 0.25f) else BotanicalSurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = type.displayName,
                                tint = if (isSelected) BotanicalPrimaryContainer else BotanicalOutline,
                                modifier = Modifier.size(28.dp)
                            )

                            // Top-Right Checkmark Badge when Selected
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(BotanicalPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Produce Title
                        Text(
                            text = type.displayName,
                            style = FreshIQTypography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BotanicalPrimaryContainer else BotanicalOnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Variety Subtitle
                        Text(
                            text = varietyName,
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                            color = BotanicalOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Bottom Indicator Pill
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 3.dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(if (isSelected) BotanicalPrimaryContainer else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
