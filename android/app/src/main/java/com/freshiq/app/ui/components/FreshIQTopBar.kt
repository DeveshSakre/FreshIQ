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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.BgPage
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.Lime
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.TextMuted

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
            .background(BgPage.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = BorderLight.copy(alpha = 0.6f)
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Back action or Brand Mark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (canNavigateBack) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Forest
                        )
                    }
                } else {
                    // Botanical Logo Vector Mark
                    AvocadoLogoMark(modifier = Modifier.size(32.dp))
                }

                if (title != null) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Forest
                    )
                } else {
                    // Brand Name & Subtitle
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Forest, fontWeight = FontWeight.ExtraBold)) {
                                    append("Fresh")
                                }
                                withStyle(SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.ExtraBold)) {
                                    append("IQ")
                                }
                            },
                            fontSize = 19.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "PRECISION SHELF-LIFE AI",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Right Side: Health Status Indicator
            if (showHealthIndicator) {
                if (healthIndicatorState != null) {
                    HealthIndicatorBadge(state = healthIndicatorState)
                } else {
                    HealthIndicatorBadge()
                }
            }
        }
    }
}

@Composable
fun AvocadoLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer avocado silhouette (Forest Green)
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            cubicTo(w * 0.28f, h * 0.08f, w * 0.12f, h * 0.28f, w * 0.12f, h * 0.5f)
            cubicTo(w * 0.12f, h * 0.78f, w * 0.5f, h * 0.98f, w * 0.5f, h * 0.98f)
            cubicTo(w * 0.5f, h * 0.98f, w * 0.88f, h * 0.78f, w * 0.88f, h * 0.5f)
            cubicTo(w * 0.88f, h * 0.28f, w * 0.72f, h * 0.08f, w * 0.5f, h * 0.08f)
            close()
        }
        drawPath(path, color = Forest)

        // Inner flesh (Lime)
        drawCircle(
            color = Lime,
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.52f)
        )

        // Seed core (Deep olive)
        drawCircle(
            color = Color(0xFF4D7C0F),
            radius = w * 0.10f,
            center = Offset(w * 0.5f, h * 0.52f)
        )

        // Sensor AI pulse node
        drawCircle(
            color = LimeGlow,
            radius = w * 0.05f,
            center = Offset(w * 0.78f, h * 0.44f)
        )
    }
}

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
