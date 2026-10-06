package com.freshiq.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val FreshIQColorScheme = lightColorScheme(
    primary = BotanicalPrimary,
    onPrimary = BotanicalOnPrimary,
    primaryContainer = BotanicalPrimaryContainer,
    onPrimaryContainer = BotanicalOnPrimaryContainer,
    secondary = BotanicalSecondary,
    onSecondary = BotanicalOnSecondary,
    secondaryContainer = BotanicalSecondaryContainer,
    onSecondaryContainer = BotanicalOnSecondaryContainer,
    tertiary = BotanicalTertiary,
    onTertiary = BotanicalOnTertiary,
    tertiaryContainer = BotanicalTertiaryContainer,
    onTertiaryContainer = BotanicalOnTertiaryContainer,
    background = BotanicalSurface,
    onBackground = BotanicalOnSurface,
    surface = BotanicalSurface,
    onSurface = BotanicalOnSurface,
    surfaceVariant = BotanicalSurfaceContainerHighest,
    onSurfaceVariant = BotanicalOnSurfaceVariant,
    inverseSurface = BotanicalInverseSurface,
    inverseOnSurface = BotanicalInverseOnSurface,
    inversePrimary = BotanicalInversePrimary,
    outline = BotanicalOutline,
    outlineVariant = BotanicalOutlineVariant,
    error = BotanicalError,
    onError = BotanicalOnError,
    errorContainer = BotanicalErrorContainer,
    onErrorContainer = BotanicalOnErrorContainer
)

val FreshIQShapes = Shapes(
    extraSmall = RoundedCornerShape(FreshIQRadius.radius2xs),
    small = RoundedCornerShape(FreshIQRadius.radiusSm),
    medium = RoundedCornerShape(FreshIQRadius.radiusMd),
    large = RoundedCornerShape(FreshIQRadius.radiusLg),
    extraLarge = RoundedCornerShape(FreshIQRadius.radius2xl)
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
