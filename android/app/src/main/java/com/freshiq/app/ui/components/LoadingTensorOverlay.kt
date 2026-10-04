package com.freshiq.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryGreen

@Composable
fun LoadingTensorOverlay(
    modifier: Modifier = Modifier,
    title: String = "Running Produce AI Inference",
    modelArchitecture: String = "MobileNetV3-Small \u2022 HistGradientBoosting",
    caption: String = "Extracting botanical features & biophysical tensors..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tensor_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD90F291E)), // Translucent Forest backdrop
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .background(
                    color = Color(0x33000000),
                    shape = RoundedCornerShape(FreshIQRadius.radiusLg)
                )
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            // Orbital Dual-Ring Tensor Canvas
            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating gradient arc
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                ) {
                    val stroke = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, PrimaryGreen, LimeGlow)
                        ),
                        startAngle = 0f,
                        sweepAngle = 280f,
                        useCenter = false,
                        style = stroke
                    )
                }

                // Inner counter-rotating ring
                Canvas(
                    modifier = Modifier
                        .size(46.dp)
                        .rotate(-rotation * 1.5f)
                ) {
                    val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    drawArc(
                        color = LimeGlow.copy(alpha = 0.8f),
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = stroke
                    )
                }

                // Center core pulsing dot
                Canvas(
                    modifier = Modifier.size(16.dp * pulseScale)
                ) {
                    drawCircle(color = LimeGlow)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Inference Header
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Dual Model Architecture Tag
            Text(
                text = modelArchitecture,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = LimeGlow,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Analytical Telemetry Subtitle
            Text(
                text = caption,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingTensorOverlayPreview() {
    FreshIQTheme {
        Box(modifier = Modifier.size(360.dp, 400.dp)) {
            LoadingTensorOverlay()
        }
    }
}
