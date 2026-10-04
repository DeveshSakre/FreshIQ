package com.freshiq.app.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// FreshIQ Botanical Scientific Color Palette (Matching Web & Stitch Spec)
// =========================================================================

// Primary Brand Colors
val Forest = Color(0xFF0F291E)
val ForestDark = Color(0xFF0A1D15)
val ForestLight = Color(0xFF1A3826)
val PrimaryGreen = Color(0xFF16A34A)
val PrimaryDark = Color(0xFF15803D)
val PrimaryLight = Color(0xFFDCFCE7)
val Lime = Color(0xFF84CC16)
val LimeLight = Color(0xFFECFCCB)
val LimeGlow = Color(0xFFA3E635)

// Neutral Organic Slate/Mint Surface Palette
val BgPage = Color(0xFFF4FBF3)
val BgCard = Color(0xFFFFFFFF)
val BgSubtle = Color(0xFFEEF6ED)
val BgSubtleHover = Color(0xFFE3ECE2)
val BgElevated = Color(0xFFFFFFFF)

// Text Colors
val TextMain = Color(0xFF161D18)
val TextMuted = Color(0xFF526056)
val TextSubtle = Color(0xFF7E8E82)
val TextLight = Color(0xFFFFFFFF)

// Borders & Dividers
val BorderLight = Color(0xFFDEE5DD)
val BorderSubtle = Color(0xFFE8EFE7)
val BorderAccent = Color(0xFFBBF7D0)

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

val StatusErrorBg = Color(0xFFFEE2E2)
val StatusErrorBorder = Color(0xFFFCA5A5)
val StatusErrorText = Color(0xFFB91C1C)
val StatusErrorIcon = Color(0xFFDC2626)

val StatusSuccessBg = Color(0xFFDCFCE7)
val StatusSuccessBorder = Color(0xFF86EFAC)
val StatusSuccessText = Color(0xFF166534)
val StatusSuccessIcon = Color(0xFF16A34A)
