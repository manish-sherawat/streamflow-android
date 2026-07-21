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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Sizes
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

/**
 * Primary CTA — solid white pill, black text.
 * Clean and minimal — no gradient, no glow.
 */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.PlayArrow
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.primaryButtonHeight)
            .clip(Radius.pill)
            .background(TextPrimary)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.Black.copy(alpha = 0.12f)),
                onClick = onClick
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BgBase,
            modifier = Modifier
                .size(18.dp)
                .padding(end = 0.dp)
        )
        Text(
            text = label,
            style = StreamFlowType.buttonLabel,
            color = BgBase,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/**
 * Secondary action — ghost pill with hairline border, white text.
 */
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
            .clip(Radius.pill)
            .background(Color.Transparent)
            .border(0.5.dp, GlassBorder, Radius.pill)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = TextSecondary.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .padding(horizontal = 20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = StreamFlowType.pillLabel,
            color = TextPrimary,
            modifier = Modifier.padding(start = 7.dp)
        )
    }
}

/**
 * Blue accent button — used for highlight CTAs (e.g. Sign In).
 */
@Composable
fun AccentButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.primaryButtonHeight)
            .clip(Radius.pill)
            .background(AccentPrimary)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            style = StreamFlowType.buttonLabel,
            color = Color.White,
            modifier = if (icon != null) Modifier.padding(start = 6.dp) else Modifier
        )
    }
}
