package com.freshiq.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Dedicated 3-class ripeness stages for Banana (Musa acuminata, Cavendish).
 *
 * Ordered biological ripening progression:
 * 0. Unripe
 * 1. Semi-ripe
 * 2. Ripe
 */
enum class BananaRipenessStage(
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
        description = "Firm green exocarp with high starch content and minimal aroma. Rigid cellular structure.",
        recommendation = "Store at ambient room temperature away from direct sunlight to allow natural ripening."
    ),
    SEMI_RIPE(
        stageNumber = 1,
        label = "Semi-ripe",
        color = Color(0xFF84CC16), // Olive Lime
        description = "Yellowing peel with distinct green tips. Starch-to-sugar conversion actively progressing.",
        recommendation = "Approaching optimal sweetness; hold at room temperature or consume if firmer texture is preferred."
    ),
    RIPE(
        stageNumber = 2,
        label = "Ripe",
        color = Color(0xFFEAB308), // Golden Yellow
        description = "Full golden-yellow peel with subtle sugar flecks. Peak sugar concentration and varietal aroma.",
        recommendation = "Ready to eat fresh, or use in smoothies, fruit salads, and baking."
    );

    companion object {
        fun fromNumber(stage: Int): BananaRipenessStage {
            return entries.find { it.stageNumber == stage } ?: RIPE
        }

        fun fromLabel(label: String?): BananaRipenessStage {
            if (label.isNullOrBlank()) return RIPE
            val clean = label.replace("-", "").replace(" ", "").lowercase()
            return when (clean) {
                "unripe" -> UNRIPE
                "semiripe" -> SEMI_RIPE
                "ripe" -> RIPE
                else -> entries.find { it.label.equals(label, ignoreCase = true) } ?: RIPE
            }
        }
    }
}
