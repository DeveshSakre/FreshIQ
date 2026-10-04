package com.freshiq.app.domain.model

import androidx.compose.ui.graphics.Color

enum class RipenessStage(
    val stageNumber: Int,
    val label: String,
    val color: Color,
    val description: String
) {
    STAGE_1(
        1,
        "Stage 1 — Underripe",
        Color(0xFF16A34A),
        "Firm botanical condition with rigid exocarp. Starches intact with slow ripening progression."
    ),
    STAGE_2(
        2,
        "Stage 2 — Breaking",
        Color(0xFF65A30D),
        "Breaking maturity exhibiting initial skin transition from bright emerald to dark olive."
    ),
    STAGE_3(
        3,
        "Stage 3 — Ripe First Stage",
        Color(0xFFCA8A04),
        "Firm-ripe botanical condition with creamy lipid core beginning. Optimal structural slicing."
    ),
    STAGE_4(
        4,
        "Stage 4 — Ripe Second Stage",
        Color(0xFFEA580C),
        "Soft-ripe peak eating window. Rich, buttery texture and full varietal aroma."
    ),
    STAGE_5(
        5,
        "Stage 5 — Overripe",
        Color(0xFF991B1B),
        "Terminal senescent state with cellular breakdown. RUL = 0.0 days across all conditions."
    );

    companion object {
        fun fromNumber(stage: Int): RipenessStage = values().find { it.stageNumber == stage } ?: STAGE_3
    }
}
