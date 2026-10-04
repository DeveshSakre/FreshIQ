package com.freshiq.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.BlueIceAccent
import com.freshiq.app.ui.theme.BlueIceBadge
import com.freshiq.app.ui.theme.BlueIceBadgeText
import com.freshiq.app.ui.theme.BlueIceBg
import com.freshiq.app.ui.theme.BlueIceBorder
import com.freshiq.app.ui.theme.BlueIceText
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme

@Composable
fun ExtrapolationNoticeCard(
    modifier: Modifier = Modifier,
    customUncertaintyNote: String? = null,
    customDisclaimer: String? = null,
    isTerminalStage5: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BlueIceBg, RoundedCornerShape(FreshIQRadius.radiusMd))
            .border(1.dp, BlueIceBorder, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "4°C Extrapolation Notice",
                tint = BlueIceAccent,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                // Top Tag: Model-based extrapolation badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(
                            color = BlueIceBadge,
                            shape = RoundedCornerShape(FreshIQRadius.radiusFull)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        tint = BlueIceAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Model-based extrapolation",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueIceBadgeText
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Core mandatory biophysical notice
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BlueIceText)) {
                            append("Model-Based Extrapolation Notice: ")
                        }
                        if (customUncertaintyNote != null) {
                            append(customUncertaintyNote)
                        } else {
                            append("4°C Domestic Refrigerator shelf-life is mathematically extrapolated via biochemical respiration kinetics (Q10 = 2.38). ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BlueIceText)) {
                                append("4°C is not an empirically observed training condition in the dataset.")
                            }
                        }
                        if (isTerminalStage5) {
                            append(" Produce has reached senescent maturity (RUL = 0); chilling cannot reverse overripeness.")
                        }
                    },
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = BlueIceText
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Biophysical kinetic simulation disclaimer
                Text(
                    text = customDisclaimer ?: "AI biophysical kinetic simulation based on avocado respiration slowing (Q10 = 2.38).",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF475569),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExtrapolationNoticeCardPreview() {
    FreshIQTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExtrapolationNoticeCard()
            ExtrapolationNoticeCard(isTerminalStage5 = true)
        }
    }
}
