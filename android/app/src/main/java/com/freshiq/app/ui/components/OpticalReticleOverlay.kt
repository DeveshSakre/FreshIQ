package com.freshiq.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryGreen

@Composable
fun OpticalReticleOverlay(
    modifier: Modifier = Modifier,
    roiLabel: String = "ROI 1: Epicarp Surface",
    telemetryTopRight: String = "Optical Calibration",
    telemetryBottomLeft: String = "Format: JPEG/RGB",
    telemetryBottomRight: String = "Coverage: Centered",
    lineColor: Color = PrimaryGreen,
    reticleInset: Dp = 16.dp,
    cornerLength: Dp = 24.dp,
    strokeWidth: Dp = 2.dp,
    showCrosshairs: Boolean = true,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reticle_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A))
    ) {
        // Underneath produce image or camera viewfinder content
        content?.invoke(this)

        // Reticle Canvas for Corner Brackets, Dashed Frame, Crosshairs
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(reticleInset)
        ) {
            val w = size.width
            val h = size.height
            val cLen = cornerLength.toPx()
            val sWidth = strokeWidth.toPx()

            // Dashed outer bounding box
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            drawRect(
                color = lineColor.copy(alpha = 0.45f),
                style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
            )

            // 4 Corner brackets (L-shapes) with full opacity
            val cornerColor = lineColor

            // Top-Left corner
            drawLine(cornerColor, Offset(0f, 0f), Offset(cLen, 0f), sWidth)
            drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cLen), sWidth)

            // Top-Right corner
            drawLine(cornerColor, Offset(w, 0f), Offset(w - cLen, 0f), sWidth)
            drawLine(cornerColor, Offset(w, 0f), Offset(w, cLen), sWidth)

            // Bottom-Left corner
            drawLine(cornerColor, Offset(0f, h), Offset(cLen, h), sWidth)
            drawLine(cornerColor, Offset(0f, h), Offset(0f, h - cLen), sWidth)

            // Bottom-Right corner
            drawLine(cornerColor, Offset(w, h), Offset(w - cLen, h), sWidth)
            drawLine(cornerColor, Offset(w, h), Offset(w, h - cLen), sWidth)

            // Subtle center crosshairs and circular reticle
            if (showCrosshairs) {
                val cx = w / 2f
                val cy = h / 2f
                val radius = 48.dp.toPx()

                // Center circular target ring
                drawCircle(
                    color = lineColor.copy(alpha = 0.5f),
                    radius = radius,
                    style = Stroke(width = 1.dp.toPx(), pathEffect = dashEffect)
                )

                // Crosshair marks
                val crossLen = 14.dp.toPx()
                drawLine(lineColor.copy(alpha = 0.6f), Offset(cx - crossLen, cy), Offset(cx + crossLen, cy), 1.5f)
                drawLine(lineColor.copy(alpha = 0.6f), Offset(cx, cy - crossLen), Offset(cx, cy + crossLen), 1.5f)
            }
        }

        // Telemetry & ROI HUD Overlay Elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(reticleInset + 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left ROI badge
                Row(
                    modifier = Modifier
                        .background(
                            color = Color(0xF2FFFFFF),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(pulseAlpha)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                    )
                    Text(
                        text = roiLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                }

                // Top-Right Telemetry
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xD90F291E),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = telemetryTopRight,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            // Bottom HUD row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xF2FFFFFF),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = telemetryBottomLeft,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Forest
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xF2FFFFFF),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = telemetryBottomRight,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OpticalReticleOverlayPreview() {
    FreshIQTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp)
        ) {
            OpticalReticleOverlay(
                modifier = Modifier.fillMaxSize()
            ) {
                // Background simulated camera preview
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[Produce Camera Viewport]",
                        color = Color.LightGray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
