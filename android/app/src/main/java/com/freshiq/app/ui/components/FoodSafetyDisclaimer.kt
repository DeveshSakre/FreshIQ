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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.StatusWarningBg
import com.freshiq.app.ui.theme.StatusWarningBorder
import com.freshiq.app.ui.theme.StatusWarningIcon
import com.freshiq.app.ui.theme.StatusWarningText

@Composable
fun FoodSafetyDisclaimer(
    modifier: Modifier = Modifier,
    backendDisclaimer: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StatusWarningBg, RoundedCornerShape(FreshIQRadius.radiusMd))
            .border(1.5.dp, StatusWarningBorder, RoundedCornerShape(FreshIQRadius.radiusMd))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Food Safety Disclaimer",
                tint = StatusWarningIcon,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 2.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = ColorDarkAmber)) {
                            append("Responsible AI Food Safety Disclaimer: ")
                        }
                        if (!backendDisclaimer.isNullOrBlank()) {
                            append(backendDisclaimer)
                            append(" ")
                        }
                        append("FreshIQ models provide non-destructive statistical guidance and estimate physical maturity. ")
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = ColorDarkAmber)) {
                            append("This is not a guarantee of biological food safety.")
                        }
                        append(" Always perform sensory checks (inspect for mold, discoloration, rancid odor, or severe tissue collapse) before consuming perishable produce.")
                    },
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = StatusWarningText
                )
            }
        }
    }
}

private val ColorDarkAmber = androidx.compose.ui.graphics.Color(0xFF78350F)

@Preview(showBackground = true)
@Composable
fun FoodSafetyDisclaimerPreview() {
    FreshIQTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FoodSafetyDisclaimer()
        }
    }
}
