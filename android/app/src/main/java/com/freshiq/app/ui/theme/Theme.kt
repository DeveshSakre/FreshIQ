package com.freshiq.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FreshIQColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Forest,
    onSecondary = Color.White,
    secondaryContainer = LimeLight,
    onSecondaryContainer = ForestDark,
    tertiary = Lime,
    onTertiary = ForestDark,
    background = BgPage,
    onBackground = TextMain,
    surface = BgCard,
    onSurface = TextMain,
    surfaceVariant = BgSubtle,
    onSurfaceVariant = TextMuted,
    outline = BorderLight,
    outlineVariant = BorderSubtle,
    error = StatusErrorText,
    onError = Color.White,
    errorContainer = StatusErrorBg,
    onErrorContainer = StatusErrorText
)

val FreshIQShapes = Shapes(
    small = RoundedCornerShape(FreshIQRadius.radiusSm),
    medium = RoundedCornerShape(FreshIQRadius.radiusMd),
    large = RoundedCornerShape(FreshIQRadius.radiusLg),
    extraLarge = RoundedCornerShape(FreshIQRadius.radiusXl)
)

@Composable
fun FreshIQTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FreshIQColorScheme,
        typography = FreshIQTypography,
        shapes = FreshIQShapes,
        content = content
    )
}
