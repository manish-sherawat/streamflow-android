package com.streamflow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Sizes
import com.streamflow.app.ui.theme.StreamFlowType

/**
 * Rail poster item with glass border and smooth gradient scrim.
 */
@Composable
fun PosterCard(
    posterUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleLabel: String? = null,
    width: Dp = Sizes.posterCardWidth,
    height: Dp = Sizes.posterCardHeight
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "posterPressScale"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .scale(scale)
            .clip(Radius.posterCard)
            .border(1.dp, GlassBorder, Radius.posterCard)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        AsyncImage(
            model = posterUrl,
            contentDescription = titleLabel,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom gradient overlay for title legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 100f
                    )
                )
        )

        if (titleLabel != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = titleLabel,
                    style = StreamFlowType.cardTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
