package com.freshiq.app.ui.screens.home

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.freshiq.app.ui.components.FreshAccentButton
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshCardVariant
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.FreshSecondaryButton
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BgCard
import com.freshiq.app.ui.theme.BgPage
import com.freshiq.app.ui.theme.BgSubtle
import com.freshiq.app.ui.theme.BorderLight
import com.freshiq.app.ui.theme.Forest
import com.freshiq.app.ui.theme.ForestDark
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.Lime
import com.freshiq.app.ui.theme.LimeGlow
import com.freshiq.app.ui.theme.PrimaryDark
import com.freshiq.app.ui.theme.PrimaryGreen
import com.freshiq.app.ui.theme.PrimaryLight
import com.freshiq.app.ui.theme.TextMain
import com.freshiq.app.ui.theme.TextMuted
import com.freshiq.app.ui.theme.TextSubtle

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgPage),
        verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.space2xl)
    ) {
        // =========================================================================
        // 1. HERO SECTION (Pitch, CTAs, Calibrated Trust)
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Status Pill
                StatusPill()

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Headline
                Text(
                    text = buildAnnotatedString {
                        append("Know your produce.\n")
                        withStyle(SpanStyle(color = PrimaryGreen)) {
                            append("Waste less.")
                        }
                    },
                    style = FreshIQTypography.displayLarge.copy(
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Forest
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Value Proposition Paragraph
                Text(
                    text = "Computer vision and biophysical shelf-life modeling quantify ripeness in seconds, calculate remaining usable life across storage conditions, and deliver exact culinary timing to eat, chill, or freeze.",
                    style = FreshIQTypography.bodyLarge.copy(
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = TextMuted
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary & Secondary CTA Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FreshPrimaryButton(
                        text = "Scan Your Produce",
                        icon = Icons.Default.CameraAlt,
                        onClick = { navController.navigate(Screen.Scan.route) },
                        modifier = Modifier.weight(1.3f)
                    )
                    FreshSecondaryButton(
                        text = "What-If Lab",
                        icon = Icons.Default.PlayCircle,
                        onClick = { navController.navigate(Screen.WhatIf.route) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Calibration Trust Metadata
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Calibration",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Calibrated on 14,710 fruit time-series across ambient (20°C) and commercial cold storage (10°C).",
                        fontSize = 11.5.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // =========================================================================
        // 2. ILLUSTRATIVE HASS AVOCADO SPECIMEN & DIAGNOSTIC TELEMETRY
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                DemonstrationSpecimenWithTelemetry()
            }
        }

        // =========================================================================
        // 3. EMPIRICAL BENCHMARKS (Performance Highlight Strip)
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = BorderLight
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "EMPIRICAL BENCHMARKS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rigorous Evaluation on Untouched Test Split",
                        style = FreshIQTypography.headlineSmall,
                        color = Forest
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Evaluated on 73 strictly isolated test specimens with zero overlap across train, validation, and test sets.",
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }

                BenchmarkMetricCard(
                    value = "66.15%",
                    label = "EXACT STAGE ACCURACY",
                    description = "5-class ordinal classification using MobileNetV3-Small without synthetic augmentation inflation.",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    accentColor = PrimaryGreen,
                    containerColor = PrimaryLight
                )

                BenchmarkMetricCard(
                    value = "1.75 Days",
                    label = "RUL MEAN ABSOLUTE ERROR",
                    description = "Test MAE (~41.9h) across 2,112 test images with R² = 0.8420 tracking degradation to terminal Stage 5.",
                    icon = Icons.Default.Timer,
                    accentColor = Color(0xFF65A30D),
                    containerColor = Color(0xFFECFCCB)
                )

                BenchmarkMetricCard(
                    value = "98.13%",
                    label = "EXACT OR ADJACENT ACCURACY",
                    description = "98.13% of all predictions are within ±1 developmental stage margin on test cohorts (0.358 stage ordinal MAE).",
                    icon = Icons.Default.CheckCircle,
                    accentColor = Color(0xFF1E40AF),
                    containerColor = Color(0xFFDBEAFE)
                )
            }
        }

        // =========================================================================
        // 4. THREE-STEP FRESHIQ PIPELINE (From Snapshot to Pantry)
        // =========================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text(
                        text = "SYSTEM PIPELINE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "From snapshot to waste-free pantry in 5 scientific steps.",
                        style = FreshIQTypography.headlineSmall,
                        color = Forest
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Harnessing computer vision and post-harvest degradation kinetics to eliminate culinary guesswork.",
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                }

                PipelineStepCard(
                    stepNumber = "01",
                    title = "Capture Image",
                    description = "Snap produce under natural room lighting. Multi-spectral chromatic features map skin reflectance and micro-texture.",
                    telemetry = "EXIF & LIGHT CALIBRATION",
                    icon = Icons.Default.CameraAlt
                )

                PipelineStepCard(
                    stepNumber = "02",
                    title = "Ripeness Stage (Vision AI)",
                    description = "The MobileNetV3 neural backbone classifies 5 discrete developmental stages and computes a continuous ripeness index.",
                    telemetry = "STAGES 1 TO 5 ORDINAL",
                    icon = Icons.Default.Layers
                )

                PipelineStepCard(
                    stepNumber = "03",
                    title = "RUL Modeling (What-If)",
                    description = "Calculates Remaining Usable Life using kinetic regression to forecast days until terminal Stage 5 overripeness.",
                    telemetry = "±1.75d TEST MAE REGRESSION",
                    icon = Icons.Default.HourglassEmpty
                )

                PipelineStepCard(
                    stepNumber = "04",
                    title = "Thermal Simulation",
                    description = "Simulates ambient (20°C), commercial cold (10°C), and refrigerator (4°C) with Arrhenius respiration kinetics.",
                    telemetry = "AMBIENT VS REFRIGERATED",
                    icon = Icons.Default.DeviceThermostat
                )

                PipelineStepCard(
                    stepNumber = "05",
                    title = "Action Directive",
                    description = "Actionable kitchen directive: consume immediately, refrigerate to hold optimal peak, or transition to baking/guacamole.",
                    telemetry = "ZERO-WASTE EXECUTION",
                    icon = Icons.Default.Restaurant
                )
            }
        }

        // =========================================================================
        // 5. PRODUCE TAXONOMY & COVERAGE STATUS
        // =========================================================================
        item {
            FreshCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                variant = FreshCardVariant.Default
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Forest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = "Produce Taxonomy",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Species Coverage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Forest
                            )
                            Text(
                                text = "ACTIVE PRODUCTION",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDark,
                                modifier = Modifier
                                    .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = buildAnnotatedString {
                                append("Currently optimized for ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Forest)) {
                                    append("Hass Avocado (Persea americana)")
                                }
                                append(". Climacteric suites for Tomato, Banana, and Mango are currently in acquisition & validation.")
                            },
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusFull))
                                .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryGreen)
                            )
                            Text(
                                text = "Next: Roma Tomato (In Research)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.5.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 6. SCIENTIFIC IMPACT CALLOUT & FINAL CTA
        // =========================================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(FreshIQRadius.radiusXl))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Forest, ForestDark)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SUSTAINABLE HOUSEHOLD IMPACT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGlow,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\u201COver 40% of fresh food waste occurs in home refrigerators because timing ripeness is purely guesswork. FreshIQ transforms perishable produce management into an exact predictive science.\u201D",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FQ",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = LimeGlow,
                                fontSize = 12.sp
                            )
                        }

                        Column {
                            Text(
                                text = "FreshIQ Biophysical Labs",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Post-Harvest Degradation Research Unit",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Final Scanner CTA Button
                    FreshAccentButton(
                        text = "Launch FreshIQ Scanner",
                        icon = Icons.Default.CameraAlt,
                        onClick = { navController.navigate(Screen.Scan.route) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FreshSecondaryButton(
                        text = "Explore Scientific Insights",
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        onClick = { navController.navigate(Screen.Insights.route) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Lime,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Non-destructive \u2022 Calibrated on 14,710 fruits",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Bottom navigation padding
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// =========================================================================
// Sub-Components
// =========================================================================

@Composable
private fun StatusPill() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_pulse_alpha"
    )

    Row(
        modifier = Modifier
            .background(BgCard, RoundedCornerShape(FreshIQRadius.radiusFull))
            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusFull))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .alpha(pulseAlpha)
                .clip(CircleShape)
                .background(PrimaryGreen)
        )
        Text(
            text = "AI-POWERED FRESHNESS \u2022 HASS AVOCADO V1.2",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Forest,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
private fun DemonstrationSpecimenWithTelemetry() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusXl))
            .background(Color.White)
            .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusXl))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Specimen Image with Top Optical Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // Specimen Hass Avocado Image (demonstrative preview)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("https://lh3.googleusercontent.com/aida-public/AB6AXuDjF1Vxmny1tot6unZwd76wWmkO_lP0Kf0wjY0nWm9CZzDA69HL3wpPrBtcSbqh_F9WVF1aOrFuJilz-Gpsqa0IHF6GhsIppPEXYxv44ZfGyO0ACC8kO_6N7d7x9e7_4vMKxNa54oeWj8IzFXeYp_S7IgIBTSm2Rw-o63cvvsQdrup1N2KUzesLrU_xaDnlwpS38q_VkLQoSlbYjXeroNzDQOU1g-_nIZFs_C1cfZD2dFs1-fcGSAVDug")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Hass avocado demonstration specimen",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Fallback Botanical Gradient overlay in case image takes time to load
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Forest.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Optical Calibration Badge (Top Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color(0xE6FFFFFF), RoundedCornerShape(FreshIQRadius.radiusFull))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                    )
                    Text(
                        text = "CALIBRATED : OPTICAL V4",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                }

                // Botanical Specimen Taxonomy Tag (Bottom Left)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "BOTANICAL SPECIMEN",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = LimeGlow,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Persea americana (Hass Cultivar)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Diagnostic Demonstration Telemetry Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CLASSIFICATION (SAMPLE)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    Text(
                        text = "94.8% CONFIDENCE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark,
                        modifier = Modifier
                            .background(PrimaryLight, RoundedCornerShape(FreshIQRadius.radiusFull))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Stage 3 \u2014 Ripe First Stage",
                    style = FreshIQTypography.titleLarge.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                )

                Text(
                    text = "Continuous Ripeness Index: 3.12 / 5.0",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5-Stage Segmented Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(Forest))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(Forest))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(PrimaryGreen))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(BgSubtle))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(BgSubtle))
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hard (1)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSubtle)
                    Text("Eating Peak", fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = PrimaryDark)
                    Text("Past Peak (5)", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = TextSubtle)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Degradation Forecast Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgSubtle, RoundedCornerShape(FreshIQRadius.radiusSm))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RUL AMBIENT (20°C)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextMuted)
                        Text("3.5 DAYS", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Forest)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RUL CHILLED (10°C)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = PrimaryGreen)
                        Text("+5.5 DAYS EXT.", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Culinary Directive
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Optimal for slicing, toast, and salads today.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Forest
                    )
                }
            }
        }
    }
}

@Composable
private fun BenchmarkMetricCard(
    value: String,
    label: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    containerColor: Color
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Default
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    fontFamily = FontFamily.Default,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Forest,
                    lineHeight = 28.sp
                )
                Text(
                    text = label,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.4.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun PipelineStepCard(
    stepNumber: String,
    title: String,
    description: String,
    telemetry: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Default
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(BgSubtle),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Forest
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = telemetry,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .border(1.dp, BorderLight, RoundedCornerShape(FreshIQRadius.radiusSm))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FreshIQTheme {
        val navController = rememberNavController()
        HomeScreen(navController = navController)
    }
}
