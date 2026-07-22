package com.streamflow.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

/**
 * Custom minimalist dual-arc luxury spinner.
 * Smooth 360 rotation with sweep gradient arc and glowing track.
 */
@Composable
fun StreamFlowSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    strokeWidth: Dp = 2.5.dp,
    color: Color = AccentPrimary,
    trackColor: Color = Color.White.copy(alpha = 0.08f)
) {
    val transition = rememberInfiniteTransition(label = "spinnerTransition")
    
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val sweepAngle by transition.animateFloat(
        initialValue = 40f,
        targetValue = 260f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweepAngle"
    )

    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        
        // Background track
        drawCircle(
            color = trackColor,
            style = Stroke(width = strokePx)
        )

        // Animated active arc
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

/**
 * Full-screen sleek loading experience with pulse wordmark and minimalist spinner.
 */
@Composable
fun StreamFlowLoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    val transition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "STREAMFLOW",
                style = StreamFlowType.brandTitle.copy(
                    fontSize = 22.sp,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Black
                ),
                color = TextPrimary,
                modifier = Modifier.alpha(pulseAlpha)
            )
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            StreamFlowSpinner(
                size = 36.dp,
                strokeWidth = 2.5.dp,
                color = AccentPrimary
            )

            if (message != null) {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = message,
                    style = StreamFlowType.caption,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Hero Carousel Shimmer Skeleton placeholder (380dp hero height).
 */
@Composable
fun HeroShimmerSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(460.dp)
            .padding(horizontal = Spacing.lg)
            .clip(Radius.card)
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 16.dp
        )
    }
}

/**
 * Title Detail Screen Skeleton placeholder.
 */
@Composable
fun DetailShimmerSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        // Backdrop shimmer
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            cornerRadius = 0.dp
        )

        Column(modifier = Modifier.padding(Spacing.md)) {
            // Title shimmer
            ShimmerBox(
                modifier = Modifier
                    .width(200.dp)
                    .height(24.dp),
                cornerRadius = 6.dp
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Pills row shimmer
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(50.dp)
                            .height(20.dp),
                        cornerRadius = 6.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Button shimmer
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                cornerRadius = 50.dp
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Text paragraph lines
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 2) 0.6f else 1f)
                        .height(14.dp),
                    cornerRadius = 4.dp
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
            }
        }
    }
}
