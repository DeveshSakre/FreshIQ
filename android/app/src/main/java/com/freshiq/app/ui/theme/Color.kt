package com.freshiq.app.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// FreshIQ Botanical Intelligence Specification (Stitch Visual Design)
// =========================================================================

// Botanical Intelligence Primary (Crisp botanical leaf green)
val BotanicalPrimary = Color(0xFF00653A)
val BotanicalOnPrimary = Color(0xFFFFFFFF)
val BotanicalPrimaryContainer = Color(0xFF1B804E)
val BotanicalOnPrimaryContainer = Color(0xFFD8FFE1)
val BotanicalPrimaryFixed = Color(0xFF98F6B9)
val BotanicalPrimaryFixedDim = Color(0xFF7CDA9F)
val BotanicalOnPrimaryFixed = Color(0xFF00210F)
val BotanicalOnPrimaryFixedVariant = Color(0xFF00522E)

// Botanical Intelligence Secondary (Sun-ripened amber)
val BotanicalSecondary = Color(0xFF805600)
val BotanicalOnSecondary = Color(0xFFFFFFFF)
val BotanicalSecondaryContainer = Color(0xFFFEB63A)
val BotanicalOnSecondaryContainer = Color(0xFF6E4900)
val BotanicalSecondaryFixed = Color(0xFFFFDDB0)
val BotanicalSecondaryFixedDim = Color(0xFFFFBA45)
val BotanicalOnSecondaryFixed = Color(0xFF281800)
val BotanicalOnSecondaryFixedVariant = Color(0xFF614000)

// Botanical Intelligence Tertiary (Sage forest)
val BotanicalTertiary = Color(0xFF25624C)
val BotanicalOnTertiary = Color(0xFFFFFFFF)
val BotanicalTertiaryContainer = Color(0xFF407B64)
val BotanicalOnTertiaryContainer = Color(0xFFD3FFEA)
val BotanicalTertiaryFixed = Color(0xFFB1F0D4)
val BotanicalTertiaryFixedDim = Color(0xFF96D3B8)
val BotanicalOnTertiaryFixed = Color(0xFF002116)
val BotanicalOnTertiaryFixedVariant = Color(0xFF0F513C)

// Botanical Surfaces (Organic off-white & tonal limestone layering)
val BotanicalSurface = Color(0xFFF9F9F6)
val BotanicalOnSurface = Color(0xFF1A1C1B)
val BotanicalSurfaceVariant = Color(0xFFE2E3E0)
val BotanicalOnSurfaceVariant = Color(0xFF3F4941)
val BotanicalSurfaceContainerLowest = Color(0xFFFFFFFF)
val BotanicalSurfaceContainerLow = Color(0xFFF4F4F1)
val BotanicalSurfaceContainer = Color(0xFFEEEEEB)
val BotanicalSurfaceContainerHigh = Color(0xFFE8E8E5)
val BotanicalSurfaceContainerHighest = Color(0xFFE2E3E0)
val BotanicalSurfaceBright = Color(0xFFF9F9F6)
val BotanicalSurfaceDim = Color(0xFFDADAD7)
val BotanicalSurfaceTint = Color(0xFF006D3F)

// Botanical Inverses & Structure
val BotanicalInverseSurface = Color(0xFF2F312F)
val BotanicalInverseOnSurface = Color(0xFFF1F1EE)
val BotanicalInversePrimary = Color(0xFF7CDA9F)
val BotanicalOutline = Color(0xFF6F7A70)
val BotanicalOutlineVariant = Color(0xFFBECABE)

// Botanical Error Semantics
val BotanicalError = Color(0xFFBA1A1A)
val BotanicalOnError = Color(0xFFFFFFFF)
val BotanicalErrorContainer = Color(0xFFFFDAD6)
val BotanicalOnErrorContainer = Color(0xFF93000A)

// =========================================================================
// Legacy / Compatibility Aliases & Semantic Tokens
// =========================================================================

// Primary Brand Colors
val Forest = Color(0xFF0F291E)
val ForestDark = Color(0xFF0A1D15)
val ForestLight = Color(0xFF1A3826)
val PrimaryGreen = BotanicalPrimaryContainer
val PrimaryDark = BotanicalPrimary
val PrimaryLight = BotanicalOnPrimaryContainer
val Lime = Color(0xFF84CC16)
val LimeLight = Color(0xFFECFCCB)
val LimeGlow = BotanicalPrimaryFixed

// Neutral Organic Slate/Mint Surface Palette
val BgPage = BotanicalSurface
val BgCard = BotanicalSurfaceContainerLowest
val BgSubtle = BotanicalSurfaceContainerLow
val BgSubtleHover = BotanicalSurfaceContainer
val BgElevated = BotanicalSurfaceContainerLowest

// Text Colors
val TextMain = BotanicalOnSurface
val TextMuted = BotanicalOnSurfaceVariant
val TextSubtle = BotanicalOutline
val TextLight = Color(0xFFFFFFFF)

// Borders & Dividers
val BorderLight = Color(0xFFE6E6DF)
val BorderSubtle = BotanicalSurfaceContainerHighest
val BorderAccent = BotanicalPrimaryFixed

// Ripening Stage Progression Semantics (5-Stage Decay Scale)
// Stage 1: Underripe - Firm Green
val Stage1Fill = Color(0xFF16A34A)
val Stage1Bg = Color(0xFFDCFCE7)
val Stage1Text = Color(0xFF166534)
val Stage1Border = Color(0xFF86EFAC)

// Stage 2: Breaking - Olive Green
val Stage2Fill = Color(0xFF65A30D)
val Stage2Bg = Color(0xFFECFCCB)
val Stage2Text = Color(0xFF3F6212)
val Stage2Border = Color(0xFFBEF264)

// Stage 3: Ripe First Stage - Amber Gold
val Stage3Fill = Color(0xFFCA8A04)
val Stage3Bg = Color(0xFFFEF9C3)
val Stage3Text = Color(0xFF854D0E)
val Stage3Border = Color(0xFFFDE047)

// Stage 4: Peak Ripe - Vibrant Orange / Consume Soon
val Stage4Fill = Color(0xFFEA580C)
val Stage4Bg = Color(0xFFFFEDD5)
val Stage4Text = Color(0xFF9A3412)
val Stage4Border = Color(0xFFFDBA74)

// Stage 5: Overripe - Deep Wine / Terminal Decay
val Stage5Fill = Color(0xFF991B1B)
val Stage5Bg = Color(0xFFFEE2E2)
val Stage5Text = Color(0xFF991B1B)
val Stage5Border = Color(0xFFFCA5A5)

// Legacy alias compatibility
val Stage1Color = Stage1Fill
val Stage2Color = Stage2Fill
val Stage3Color = Stage3Fill
val Stage4Color = Stage4Fill
val Stage5Color = Stage5Fill

// 4°C Domestic Refrigerator Extrapolation Palette (MANDATORY DISTINCT BLUE)
val BlueIceBg = Color(0xFFEFF6FF)
val BlueIceBorder = Color(0xFF93C5FD)
val BlueIceText = Color(0xFF1E3A8A)
val BlueIceBadge = Color(0xFFDBEAFE)
val BlueIceAccent = Color(0xFF2563EB)
val BlueIceBadgeText = Color(0xFF1E40AF)

// Status & Alert Semantics
val StatusWarningBg = Color(0xFFFFFBEB)
val StatusWarningBorder = Color(0xFFFDE68A)
val StatusWarningText = Color(0xFF92400E)
val StatusWarningIcon = Color(0xFFD97706)

val StatusErrorBg = BotanicalErrorContainer
val StatusErrorBorder = Color(0xFFFCA5A5)
val StatusErrorText = BotanicalError
val StatusErrorIcon = BotanicalError

val StatusSuccessBg = Color(0xFFDCFCE7)
val StatusSuccessBorder = Color(0xFF86EFAC)
val StatusSuccessText = Color(0xFF166534)
val StatusSuccessIcon = BotanicalPrimaryContainer
