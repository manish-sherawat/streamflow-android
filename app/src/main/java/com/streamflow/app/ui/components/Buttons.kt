package com.streamflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.streamflow.app.ui.theme.AccentGlow
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.AccentSecondary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Sizes
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Primary CTA — crisp white pill button with deep play icon.
 * Used for Watch Now, Play Now, primary actions.
 */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.PlayArrow
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "primaryButtonScale"
    )
    val haptic = LocalHapticFeedback.current

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Radius.pill)
            .background(TextPrimary)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.Black.copy(alpha = 0.15f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BgBase,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = label,
            style = StreamFlowType.buttonLabel,
            color = BgBase,
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}

/**
 * Secondary action — ghost pill with animated blue border for saved state.
 */
@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Add
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "secondaryButtonScale"
    )
    val haptic = LocalHapticFeedback.current
    val isSaved = label.contains("Saved", ignoreCase = true)

    val borderColor by animateColorAsState(
        targetValue = if (isSaved) AccentPrimary else GlassBorder,
        animationSpec = tween(300),
        label = "secondaryBorderColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSaved) AccentGlow else Color.Transparent,
        animationSpec = tween(300),
        label = "secondaryBgColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSaved) AccentPrimary else TextPrimary,
        animationSpec = tween(300),
        label = "secondaryIconTint"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSaved) AccentPrimary else TextPrimary,
        animationSpec = tween(300),
        label = "secondaryLabelColor"
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .height(46.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Radius.pill)
            .background(bgColor)
            .border(1.dp, borderColor, Radius.pill)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = AccentPrimary.copy(alpha = 0.15f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = StreamFlowType.pillLabel,
            color = labelColor,
            modifier = Modifier.padding(start = 7.dp)
        )
    }
}

/**
 * Blue accent button — primary branded CTA (e.g. Sign In, Continue).
 */
@Composable
fun AccentButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "accentButtonScale"
    )
    val haptic = LocalHapticFeedback.current

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(Radius.pill)
            .background(
                Brush.horizontalGradient(
                    listOf(AccentPrimary, AccentSecondary)
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.2f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
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
