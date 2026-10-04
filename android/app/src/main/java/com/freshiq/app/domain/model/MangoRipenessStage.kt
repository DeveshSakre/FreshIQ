package com.freshiq.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Dedicated 5-class ripeness stages for Mango (Mangifera indica, White Chaunsa Late).
 *
 * Ordered biological ripening progression:
 * 0. Unripe
 * 1. Semiripe
 * 2. Fully Ripe
 * 3. Overripe
 * 4. Perished
 */
enum class MangoRipenessStage(
    val stageNumber: Int,
    val label: String,
    val color: Color,
    val description: String,
    val recommendation: String
) {
    UNRIPE(
        stageNumber = 0,
        label = "Unripe",
        color = Color(0xFF16A34A), // Emerald Green
        description = "Firm green exocarp with rigid cellular structure. Starches intact with minimal aroma.",
        recommendation = "Store at ambient room temperature away from direct sunlight to allow natural ripening."
    ),
    SEMIRIPE(
        stageNumber = 1,
        label = "Semiripe",
        color = Color(0xFF84CC16), // Olive Lime
        description = "Initial color break and slight yielding upon gentle pressure. Ethylene synthesis active.",
        recommendation = "Maintain at ambient room temperature; approaching optimal table sweetness."
    ),
    FULLY_RIPE(
        stageNumber = 2,
        label = "Fully Ripe",
        color = Color(0xFFEAB308), // Golden Yellow
        description = "Peak eating window. Maximum varietal sweetness, tender flesh, and full aromatic bouquet.",
        recommendation = "Best consumed fresh or processed immediately for peak flavor and nutrition."
    ),
    OVERRIPE(
        stageNumber = 3,
        label = "Overripe",
        color = Color(0xFFF97316), // Deep Amber-Orange
        description = "Advanced softening with skin darkening or wrinkling. High sugar content with decreasing acidity.",
        recommendation = "Consume immediately or utilize for culinary purees, baking, or smoothies."
    ),
    PERISHED(
        stageNumber = 4,
        label = "Perished",
        color = Color(0xFF991B1B), // Dark Crimson / Necrotic
        description = "Terminal senescence or decay. Complete cellular breakdown; unfit for consumption.",
        recommendation = "Fruit has expired and is unsuitable for consumption. Discard or compost."
    );

    companion object {
        fun fromNumber(stage: Int): MangoRipenessStage {
            return entries.find { it.stageNumber == stage } ?: FULLY_RIPE
        }

        fun fromLabel(label: String?): MangoRipenessStage {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: FULLY_RIPE
        }
    }
}
