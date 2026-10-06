package com.freshiq.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.freshiq.app.domain.model.ProduceType
import com.freshiq.app.ui.components.FoodSafetyDisclaimer
import com.freshiq.app.ui.components.FreshCard
import com.freshiq.app.ui.components.FreshCardVariant
import com.freshiq.app.ui.components.FreshPrimaryButton
import com.freshiq.app.ui.components.ProduceRipenessSpectrums
import com.freshiq.app.ui.components.SegmentedRipenessSpectrum
import com.freshiq.app.ui.navigation.Screen
import com.freshiq.app.ui.theme.BlueIceAccent
import com.freshiq.app.ui.theme.BlueIceBadge
import com.freshiq.app.ui.theme.BlueIceBadgeText
import com.freshiq.app.ui.theme.BlueIceBg
import com.freshiq.app.ui.theme.BlueIceBorder
import com.freshiq.app.ui.theme.BlueIceText
import com.freshiq.app.ui.theme.BotanicalOnPrimary
import com.freshiq.app.ui.theme.BotanicalOnSurface
import com.freshiq.app.ui.theme.BotanicalOnSurfaceVariant
import com.freshiq.app.ui.theme.BotanicalOutline
import com.freshiq.app.ui.theme.BotanicalPrimary
import com.freshiq.app.ui.theme.BotanicalPrimaryContainer
import com.freshiq.app.ui.theme.BotanicalPrimaryFixed
import com.freshiq.app.ui.theme.BotanicalSecondaryContainer
import com.freshiq.app.ui.theme.BotanicalSurface
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerHigh
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLow
import com.freshiq.app.ui.theme.BotanicalSurfaceContainerLowest
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQSpacing
import com.freshiq.app.ui.theme.FreshIQTypography
import com.freshiq.app.ui.theme.Stage1Fill
import com.freshiq.app.ui.theme.Stage2Fill
import com.freshiq.app.ui.theme.Stage3Fill
import com.freshiq.app.ui.theme.Stage4Fill
import com.freshiq.app.ui.theme.Stage5Fill

@Composable
fun InsightsScreen(
    navController: NavController
) {
    var selectedStageIndex by remember { mutableIntStateOf(2) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BotanicalSurface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FreshIQSpacing.margin),
            verticalArrangement = Arrangement.spacedBy(FreshIQSpacing.gutter)
        ) {
            item {
                Spacer(modifier = Modifier.height(FreshIQSpacing.spaceSm))
            }

            // 1. Editorial Header
            item {
                InsightsHeaderSection()
            }

            // 2. Optical Ripening Classification (How It Works)
            item {
                OpticalClassificationSection()
            }

            // 3. Ripeness Spectrum Interactive Guide
            item {
                RipenessStagesGuideSection(
                    selectedStageIndex = selectedStageIndex,
                    onStageSelected = { selectedStageIndex = it }
                )
            }

            // 4. Produce Capability Matrix
            item {
                ProduceCapabilityMatrixSection()
            }

            // 5. Postharvest Storage Science
            item {
                PostharvestStorageScienceSection()
            }

            // 6. Model-Based Extrapolation Primer (4°C)
            item {
                ModelExtrapolationPrimerSection()
            }

            // 7. Responsible AI & Food Safety
            item {
                FoodSafetyEducationalSection()
            }

            // 8. Call to Action: Start Scan
            item {
                FreshPrimaryButton(
                    text = "Scan Fresh Produce",
                    icon = Icons.Default.CameraAlt,
                    onClick = { navController.navigate(Screen.Scan.route) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(FreshIQSpacing.spaceLg))
            }
        }
    }
}

// =========================================================================
// 1. Editorial Header Section
// =========================================================================

@Composable
private fun InsightsHeaderSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = BotanicalPrimary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "BOTANICAL INTELLIGENCE PRIMER",
                style = FreshIQTypography.labelSmall.copy(fontSize = 10.5.sp),
                fontWeight = FontWeight.Bold,
                color = BotanicalPrimary,
                letterSpacing = 0.08.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Produce Science & Postharvest Intelligence",
            style = FreshIQTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BotanicalOnSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Understand how computer vision assesses produce maturity, why temperature governs shelf life, and where modeling boundaries lie.",
            style = FreshIQTypography.bodyMedium,
            color = BotanicalOnSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

// =========================================================================
// 2. Optical Classification (How It Works)
// =========================================================================

@Composable
private fun OpticalClassificationSection() {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FreshIQSpacing.spaceLg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = BotanicalPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Optical Ripening Classification",
                    style = FreshIQTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "FreshIQ utilizes non-destructive optical image evaluation to detect physical ripeness stages from external fruit skin features:",
                style = FreshIQTypography.bodyMedium,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpticalBulletItem(
                    title = "Exocarp Color & Pigmentation",
                    body = "Measures pigment transitions from chlorophyll green to anthocyanin purple-black in avocados, or carotenoid yellows in mangoes and bananas."
                )
                OpticalBulletItem(
                    title = "Surface Texture & Uniformity",
                    body = "Evaluates pebble density, skin gloss, and surface micro-features characteristic of biochemical softening phases."
                )
                OpticalBulletItem(
                    title = "Edge Neural Architecture",
                    body = "Runs lightweight deep vision classification architectures calibrated for consistent on-device execution."
                )
            }
        }
    }
}

@Composable
private fun OpticalBulletItem(title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(BotanicalPrimary)
        )
        Column {
            Text(
                text = title,
                style = FreshIQTypography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = BotanicalOnSurface
            )
            Text(
                text = body,
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

// =========================================================================
// 3. Ripeness Spectrum Guide (Interactive Visual Reference)
// =========================================================================

@Composable
private fun RipenessStagesGuideSection(
    selectedStageIndex: Int,
    onStageSelected: (Int) -> Unit
) {
    val stageNames = listOf("Underripe", "Breaking", "Firm Ripe", "Ripe", "Overripe")
    val stageDescriptions = listOf(
        "Stage 1 — Underripe: Firm, high tannin and starch content. Not ready for eating; ideal for pantry storage and holding.",
        "Stage 2 — Breaking: Beginning transitional softening. Subtle color change; cell wall pectin breakdown has commenced.",
        "Stage 3 — Firm Ripe: Yields slightly to gentle pressure. Optimal stage for clean slicing, dicing, salads, and toast.",
        "Stage 4 — Ripe / Peak: Yields smoothly. Maximum lipid richness, buttery mouthfeel, and peak aromatic bouquet (ideal for guacamole).",
        "Stage 5 — Overripe: Senescent tissue with extreme softening. Remaining usable days (RUL) is 0.0. Inspect flesh before culinary use."
    )

    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FreshIQSpacing.spaceLg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = BotanicalPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Understanding Ripeness Stages",
                    style = FreshIQTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Climacteric fruit ripens through distinct physiological stages. Tap any segment to inspect its culinary characteristics:",
                style = FreshIQTypography.bodyMedium,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Spectrum visualization
            SegmentedRipenessSpectrum(
                activeStageIndex = selectedStageIndex,
                stageLabels = ProduceRipenessSpectrums.avocadoLabels,
                stageColors = ProduceRipenessSpectrums.avocadoColors,
                spectrumTitle = "Avocado 5-Stage Maturity Scale"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stage selector pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stageNames.forEachIndexed { index, name ->
                    val isSelected = selectedStageIndex == index
                    val pillBg = if (isSelected) BotanicalPrimary else BotanicalSurfaceContainerLow
                    val pillText = if (isSelected) BotanicalOnPrimary else BotanicalOnSurfaceVariant

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                            .background(pillBg)
                            .clickable { onStageSelected(index) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "St. ${index + 1}",
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = pillText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected stage detail box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
                    .background(BotanicalSurfaceContainerLow)
                    .padding(FreshIQSpacing.spaceMd)
            ) {
                Text(
                    text = stageDescriptions[selectedStageIndex],
                    style = FreshIQTypography.bodySmall,
                    color = BotanicalOnSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// =========================================================================
// 4. Produce Capability Matrix (Avocado vs Mango vs Banana Boundaries)
// =========================================================================

@Composable
private fun ProduceCapabilityMatrixSection() {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FreshIQSpacing.spaceLg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = BotanicalPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Produce Capability Boundaries",
                    style = FreshIQTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Each supported produce item has specific modeling capabilities based on its calibrated training data:",
                style = FreshIQTypography.bodyMedium,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Avocado Card
            ProduceCapabilityCard(
                name = "Hass Avocado",
                features = "• 5-Stage optical ripeness classification\n• Remaining usable life (RUL) modeling\n• Multi-temperature storage scenarios (Ambient, 20°C, 10°C)\n• 4°C model-based extrapolation\n• Interactive What-If & Comparison tools",
                badge = "Full Kinetic RUL Modeling",
                badgeBg = BotanicalPrimaryFixed.copy(alpha = 0.35f),
                badgeColor = BotanicalPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mango Card
            ProduceCapabilityCard(
                name = "Mango",
                features = "• 5-Stage optical classification (Unripe, Semiripe, Fully Ripe, Overripe, Perished)\n• Qualitative ripeness assessment\n• No remaining usable life (RUL) modeling\n• No storage scenario simulation",
                badge = "Classification Only",
                badgeBg = BotanicalSurfaceContainerLow,
                badgeColor = BotanicalOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Banana Card
            ProduceCapabilityCard(
                name = "Banana",
                features = "• 3-Stage optical classification (Unripe, Semi-ripe, Ripe)\n• Qualitative ripeness assessment\n• No remaining usable life (RUL) modeling\n• No storage scenario simulation",
                badge = "Classification Only",
                badgeBg = BotanicalSurfaceContainerLow,
                badgeColor = BotanicalOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProduceCapabilityCard(
    name: String,
    features: String,
    badge: String,
    badgeBg: Color,
    badgeColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusMd))
            .background(BotanicalSurfaceContainerLow)
            .padding(FreshIQSpacing.spaceMd)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = FreshIQTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        style = FreshIQTypography.labelSmall.copy(fontSize = 9.5.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor
                    )
                }
            }

            Text(
                text = features,
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

// =========================================================================
// 5. Postharvest Storage Science
// =========================================================================

@Composable
private fun PostharvestStorageScienceSection() {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FreshIQSpacing.spaceLg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeviceThermostat,
                        contentDescription = null,
                        tint = BotanicalPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Postharvest Storage Principles",
                    style = FreshIQTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Three biological factors determine postharvest longevity:",
                style = FreshIQTypography.bodyMedium,
                color = BotanicalOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StoragePrincipleCard(
                    title = "Temperature & Respiration Rates",
                    body = "Avocado respiration accelerates with temperature. Transferring from ambient ~20°C to cold crisper (10°C) cuts cellular metabolic turnover roughly in half, preserving firm texture longer."
                )
                StoragePrincipleCard(
                    title = "Humidity & Desiccation Control",
                    body = "Crisper compartments maintain ~85% relative humidity (RH). This retards moisture loss through fruit exocarp pores and prevents skin wrinkling."
                )
                StoragePrincipleCard(
                    title = "Ethylene Gas Segregation",
                    body = "Climacteric fruits produce ethylene gas which autocatalyzes softening. Keeping avocados away from ripe apples and bananas prevents premature ripening."
                )
            }
        }
    }
}

@Composable
private fun StoragePrincipleCard(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusSm))
            .background(BotanicalSurfaceContainerLow)
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = FreshIQTypography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = BotanicalOnSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = body,
                style = FreshIQTypography.bodySmall,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

// =========================================================================
// 6. Model-Based Extrapolation Primer (4°C)
// =========================================================================

@Composable
private fun ModelExtrapolationPrimerSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FreshIQRadius.radiusLg))
            .background(BlueIceBg)
            .border(1.dp, BlueIceBorder, RoundedCornerShape(FreshIQRadius.radiusLg))
            .padding(FreshIQSpacing.spaceLg)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FreshIQRadius.radiusFull))
                        .background(BlueIceBadge)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            style = FreshIQTypography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = BlueIceBadgeText
                        )
                    }
                }
            }

            Text(
                text = "What Does 4°C Extrapolation Mean?",
                style = FreshIQTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BlueIceText
            )

            Text(
                text = "4°C domestic refrigeration was not an empirically observed training condition in the original laboratory dataset. While Ambient, 20°C, and 10°C conditions were directly validated with experimental decay cohorts, the 4°C estimate is a mathematical extrapolation based on avocado respiration slowing.\n\nPhysiological caveat: Domestic refrigeration below 5°C carries risk of chilling injury (internal vascular mesocarp browning upon warming). Extrapolated estimates are clearly distinguished throughout FreshIQ and never presented as ground-truth experimental data.",
                style = FreshIQTypography.bodySmall,
                color = BlueIceText,
                lineHeight = 18.sp
            )
        }
    }
}

// =========================================================================
// 7. Responsible AI & Food Safety
// =========================================================================

@Composable
private fun FoodSafetyEducationalSection() {
    FreshCard(
        modifier = Modifier.fillMaxWidth(),
        variant = FreshCardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FreshIQSpacing.spaceLg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BotanicalPrimaryFixed.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = BotanicalPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Responsible AI & Food Safety",
                    style = FreshIQTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BotanicalOnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "AI ripening assessments and shelf-life forecasts are non-destructive decision-support aids designed to minimize household food waste. They are not biological guarantees of food safety.\n\nAlways rely on sensory evaluation before consumption: inspect for mold, sour or rancid odor, discolored pulp, or severe tissue collapse. When in doubt, prioritize safety.",
                style = FreshIQTypography.bodyMedium,
                color = BotanicalOnSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            FoodSafetyDisclaimer(
                modifier = Modifier.fillMaxWidth(),
                backendDisclaimer = "FreshIQ estimates represent statistical maturity benchmarks. Biological safety must always be confirmed through direct sensory examination."
            )
        }
    }
}
