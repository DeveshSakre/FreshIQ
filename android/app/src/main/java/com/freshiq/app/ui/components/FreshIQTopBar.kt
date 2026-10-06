package com.freshiq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.BotanicalOnPrimary
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSurface
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.FreshIQTypography

@Composable
fun FreshIQTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    showHealthIndicator: Boolean = true,
    healthIndicatorState: HealthUiState? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BotanicalSurface.copy(alpha = 0.92f))
            .border(
                width = 1.dp,
                color = Color(0xFFE6E6DF)
            )
            .statusBarsPadding()
            .padding(horizontal = FreshIQSpacing.margin, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Back action or Botanical Logo & Brand Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (canNavigateBack) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BotanicalOnSurface
                        )
                    }
                } else {
                    BotanicalLogoMark(modifier = Modifier.size(28.dp))
                }

                if (title != null) {
                    Text(
                        text = title,
                        style = FreshIQTypography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BotanicalOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Fresh",
                                style = FreshIQTypography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = BotanicalOnSurface
                            )
                            Text(
                                text = "IQ",
                                style = FreshIQTypography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = BotanicalPrimaryContainer
                            )
                        }
                        Text(
                            text = "AI Botanical Intelligence",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            color = BotanicalOnSurfaceVariant
                        )
                    }
                }
            }

            // Right Side: Health Status Indicator & Profile Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FreshIQSpacing.spaceSm)
            ) {
                if (showHealthIndicator) {
                    if (healthIndicatorState != null) {
                        HealthIndicatorBadge(state = healthIndicatorState)
                    } else {
                        HealthIndicatorBadge()
                    }
                }

                // Profile Avatar Pill
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = BotanicalOnPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Clean botanical leaf vector mark for the top bar.
 */
@Composable
fun BotanicalLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            cubicTo(w * 0.26f, h * 0.12f, w * 0.10f, h * 0.32f, w * 0.10f, h * 0.54f)
            cubicTo(w * 0.10f, h * 0.80f, w * 0.5f, h * 0.98f, w * 0.5f, h * 0.98f)
            cubicTo(w * 0.5f, h * 0.98f, w * 0.90f, h * 0.80f, w * 0.90f, h * 0.54f)
            cubicTo(w * 0.90f, h * 0.32f, w * 0.74f, h * 0.12f, w * 0.5f, h * 0.08f)
            close()
        }
        drawPath(path, color = BotanicalPrimaryContainer)

        // Seed / Inner Core
        drawCircle(
            color = BotanicalPrimaryFixed,
            radius = w * 0.20f,
            center = Offset(w * 0.5f, h * 0.56f)
        )

        drawCircle(
            color = BotanicalPrimary,
            radius = w * 0.10f,
            center = Offset(w * 0.5f, h * 0.56f)
        )
    }
}

// Retain legacy alias for existing references
@Composable
fun AvocadoLogoMark(modifier: Modifier = Modifier) = BotanicalLogoMark(modifier)

@Preview(showBackground = true)
@Composable
fun FreshIQTopBarPreview() {
    FreshIQTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FreshIQTopBar(
                healthIndicatorState = HealthUiState.Healthy()
            )
            FreshIQTopBar(
                title = "Produce Analysis",
                canNavigateBack = true,
                healthIndicatorState = HealthUiState.Healthy()
            )
        }
    }
}
