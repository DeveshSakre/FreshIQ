package com.freshiq.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BlueIceBg
import com.freshiq.app.ui.theme.BlueIceBorder
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.BorderSubtle
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.FreshIQElevation
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.StatusWarningBg
import com.freshiq.app.ui.theme.StatusWarningBorder
import com.freshiq.app.ui.theme.TextMuted

enum class FreshCardVariant {
    Default,
    Subtle,
    Elevated,
    Extrapolated4C,
    Warning,
    ForestHero
}

@Composable
fun FreshCard(
    modifier: Modifier = Modifier,
    variant: FreshCardVariant = FreshCardVariant.Default,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(FreshIQRadius.radiusLg),
    elevation: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(FreshIQSpacing.spaceLg),
    content: @Composable () -> Unit
) {
    val resolvedBgColor = backgroundColor ?: when (variant) {
        FreshCardVariant.Default -> BgCard
        FreshCardVariant.Subtle -> BgSubtle
        FreshCardVariant.Elevated -> BgCard
        FreshCardVariant.Extrapolated4C -> BlueIceBg
        FreshCardVariant.Warning -> StatusWarningBg
        FreshCardVariant.ForestHero -> Forest
    }

    val resolvedBorderColor = borderColor ?: when (variant) {
        FreshCardVariant.Default -> BorderLight
        FreshCardVariant.Subtle -> BorderSubtle
        FreshCardVariant.Elevated -> BorderLight
        FreshCardVariant.Extrapolated4C -> BlueIceBorder
        FreshCardVariant.Warning -> StatusWarningBorder
        FreshCardVariant.ForestHero -> Color.Transparent
    }

    val resolvedElevation = elevation ?: when (variant) {
        FreshCardVariant.Default -> FreshIQElevation.sm
        FreshCardVariant.Subtle -> FreshIQElevation.none
        FreshCardVariant.Elevated -> FreshIQElevation.md
        FreshCardVariant.Extrapolated4C -> FreshIQElevation.xs
        FreshCardVariant.Warning -> FreshIQElevation.none
        FreshCardVariant.ForestHero -> FreshIQElevation.lg
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = resolvedBgColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = resolvedElevation
        ),
        border = if (resolvedBorderColor != Color.Transparent && borderWidth > 0.dp) {
            BorderStroke(borderWidth, resolvedBorderColor)
        } else null
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FreshCardVariantsPreview() {
    FreshIQTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            FreshCard(variant = FreshCardVariant.Default) {
                Column {
                    Text("Default Card", style = FreshIQTypography.titleMedium)
                    Text("Standard FreshIQ botanical surface", style = FreshIQTypography.bodyMedium)
                }
            }

            FreshCard(variant = FreshCardVariant.Subtle) {
                Text("Subtle Card Variant", color = TextMuted)
            }

            FreshCard(variant = FreshCardVariant.Extrapolated4C) {
                Text("4°C Extrapolated Card Variant", color = Forest)
            }

            FreshCard(variant = FreshCardVariant.ForestHero) {
                Text("Forest Hero Card Variant", color = Color.White)
            }
        }
    }
}
