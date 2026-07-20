package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.AccentSecondary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.GlassFill
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Sizes
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary

/** Primary CTA — Play / Continue Watching with Gradient Glow. */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.PlayArrow
) {
    val interactionSource = remember { MutableInteractionSource() }
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(AccentPrimary, AccentSecondary)
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.primaryButtonHeight)
            .clip(Radius.pill)
            .background(brush = gradientBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White),
                onClick = onClick
            )
    ) {
        Icon(icon, contentDescription = null, tint = BgBase, modifier = Modifier.padding(end = 8.dp))
        Text(text = label, style = StreamFlowType.buttonLabel, color = BgBase)
    }
}

/** Secondary action — Download for Offline / Watch Trailer / Add to My List. */
@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .height(Sizes.secondaryButtonHeight)
            .clip(Radius.secondaryButton)
            .background(GlassFill)
            .border(1.dp, GlassBorder, Radius.secondaryButton)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = TextPrimary),
                onClick = onClick
            )
            .padding(horizontal = 16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AccentPrimary, modifier = Modifier.width(18.dp))
        Text(
            text = label,
            style = StreamFlowType.pillLabel,
            color = TextPrimary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
