package com.streamflow.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing

// Modern light theme silver-white metallic shimmer gradient
private val shimmerBase      = Color(0xFFE2E8F0)
private val shimmerMid       = Color(0xFFF1F5F9)
private val shimmerHighlight = Color(0xFFFFFFFF)

@Composable
fun ShimmerBrush(
    targetValue: Float = 1200f,
    showShimmer: Boolean = true
): Brush {
    if (!showShimmer) {
        return Brush.linearGradient(colors = listOf(shimmerBase, shimmerBase))
    }
    val shimmerColors = listOf(
        shimmerBase,
        shimmerMid,
        shimmerHighlight,
        shimmerMid,
        shimmerBase
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue  = targetValue,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start  = Offset(x = translateAnimation - 400f, y = translateAnimation * 0.5f - 200f),
        end    = Offset(x = translateAnimation, y = translateAnimation * 0.75f)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val brush = ShimmerBrush()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Composable
fun RailShimmerSkeleton(
    modifier: Modifier = Modifier,
    cardWidth: Dp = 120.dp,
    cardHeight: Dp = 178.dp
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm)) {
        // Rail header placeholder
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = Spacing.md)
                .width(100.dp)
                .height(14.dp),
            cornerRadius = 4.dp
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        // Card row placeholders
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight),
                    cornerRadius = 10.dp
                )
            }
        }
    }
}
