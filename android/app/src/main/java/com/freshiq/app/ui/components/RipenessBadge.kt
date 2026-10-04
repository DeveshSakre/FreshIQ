package com.freshiq.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freshiq.app.ui.theme.FreshIQRadius
import com.freshiq.app.ui.theme.FreshIQTheme
import com.freshiq.app.ui.theme.Stage1Bg
import com.freshiq.app.ui.theme.Stage1Border
import com.freshiq.app.ui.theme.Stage1Fill
import com.freshiq.app.ui.theme.Stage1Text
import com.freshiq.app.ui.theme.Stage2Bg
import com.freshiq.app.ui.theme.Stage2Border
import com.freshiq.app.ui.theme.Stage2Fill
import com.freshiq.app.ui.theme.Stage2Text
import com.freshiq.app.ui.theme.Stage3Bg
import com.freshiq.app.ui.theme.Stage3Border
import com.freshiq.app.ui.theme.Stage3Fill
import com.freshiq.app.ui.theme.Stage3Text
import com.freshiq.app.ui.theme.Stage4Bg
import com.freshiq.app.ui.theme.Stage4Border
import com.freshiq.app.ui.theme.Stage4Fill
import com.freshiq.app.ui.theme.Stage4Text
import com.freshiq.app.ui.theme.Stage5Bg
import com.freshiq.app.ui.theme.Stage5Border
import com.freshiq.app.ui.theme.Stage5Fill
import com.freshiq.app.ui.theme.Stage5Text

enum class BadgeSize {
    Compact,
    Normal,
    Large
}

data class StageSemanticConfig(
    val stage: Int,
    val defaultLabel: String,
    val shortLabel: String,
    val bgColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val dotColor: Color
)

fun getStageConfig(stage: Int): StageSemanticConfig {
    return when (stage) {
        1 -> StageSemanticConfig(
            stage = 1,
            defaultLabel = "Stage 1 — Underripe",
            shortLabel = "Stage 1",
            bgColor = Stage1Bg,
            textColor = Stage1Text,
            borderColor = Stage1Border,
            dotColor = Stage1Fill
        )
        2 -> StageSemanticConfig(
            stage = 2,
            defaultLabel = "Stage 2 — Breaking",
            shortLabel = "Stage 2",
            bgColor = Stage2Bg,
            textColor = Stage2Text,
            borderColor = Stage2Border,
            dotColor = Stage2Fill
        )
        3 -> StageSemanticConfig(
            stage = 3,
            defaultLabel = "Stage 3 — Ripe First Stage",
            shortLabel = "Stage 3",
            bgColor = Stage3Bg,
            textColor = Stage3Text,
            borderColor = Stage3Border,
            dotColor = Stage3Fill
        )
        4 -> StageSemanticConfig(
            stage = 4,
            defaultLabel = "Stage 4 — Ripe Second Stage",
            shortLabel = "Stage 4",
            bgColor = Stage4Bg,
            textColor = Stage4Text,
            borderColor = Stage4Border,
            dotColor = Stage4Fill
        )
        5 -> StageSemanticConfig(
            stage = 5,
            defaultLabel = "Stage 5 — Overripe",
            shortLabel = "Stage 5",
            bgColor = Stage5Bg,
            textColor = Stage5Text,
            borderColor = Stage5Border,
            dotColor = Stage5Fill
        )
        else -> StageSemanticConfig(
            stage = stage,
            defaultLabel = "Stage $stage",
            shortLabel = "Stage $stage",
            bgColor = Stage1Bg,
            textColor = Stage1Text,
            borderColor = Stage1Border,
            dotColor = Stage1Fill
        )
    }
}

@Composable
fun RipenessBadge(
    stage: Int,
    modifier: Modifier = Modifier,
    customLabel: String? = null,
    size: BadgeSize = BadgeSize.Normal,
    showDot: Boolean = true,
    pulseDot: Boolean = false
) {
    val config = getStageConfig(stage)
    val textToDisplay = customLabel ?: when (size) {
        BadgeSize.Compact -> config.shortLabel
        BadgeSize.Normal, BadgeSize.Large -> config.defaultLabel
    }

    val (horizontalPadding, verticalPadding, dotSize, fontSize) = when (size) {
        BadgeSize.Compact -> Quadruple(8.dp, 3.dp, 6.dp, 11.sp)
        BadgeSize.Normal -> Quadruple(12.dp, 5.dp, 8.dp, 12.sp)
        BadgeSize.Large -> Quadruple(16.dp, 7.dp, 10.dp, 14.sp)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by if (pulseDot) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(900),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    Row(
        modifier = modifier
            .background(
                color = config.bgColor,
                shape = RoundedCornerShape(FreshIQRadius.radiusFull)
            )
            .border(
                width = 1.dp,
                color = config.borderColor,
                shape = RoundedCornerShape(FreshIQRadius.radiusFull)
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(config.dotColor)
            )
        }

        Text(
            text = textToDisplay,
            color = config.textColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Preview(showBackground = true)
@Composable
fun RipenessBadgeAllStagesPreview() {
    FreshIQTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RipenessBadge(stage = 1)
            RipenessBadge(stage = 2)
            RipenessBadge(stage = 3)
            RipenessBadge(stage = 4)
            RipenessBadge(stage = 5)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RipenessBadge(stage = 1, size = BadgeSize.Compact)
                RipenessBadge(stage = 2, size = BadgeSize.Compact)
                RipenessBadge(stage = 3, size = BadgeSize.Compact)
                RipenessBadge(stage = 4, size = BadgeSize.Compact)
                RipenessBadge(stage = 5, size = BadgeSize.Compact)
            }
        }
    }
}
