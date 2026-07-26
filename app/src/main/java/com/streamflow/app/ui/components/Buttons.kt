package com.streamflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ── Premium AMOLED Button Colours ─────────────────────────────────────────────
// Primary gradient: deep space blue → electric blue
private val PrimaryGradientStart = Color(0xFF1A4FCC)
private val PrimaryGradientEnd   = Color(0xFF5B9BFF)
private val PrimaryGlow          = Color(0x405B9BFF)

// Secondary surface: pure black glass
private val SecondaryBg          = Color(0xFF0D0D0D)
private val SecondaryBorder      = Color(0xFF2A2A2A)
private val SecondaryBorderSaved = Color(0xFF5B9BFF)

/**
 * Premium Primary CTA Button — AMOLED Edition.
 *
 * Features:
 * - Deep blue → electric blue gradient fill (never flat, never cheap)
 * - Subtle top-edge highlight line (glass shimmer effect)
 * - Blue glow drop-shadow on press
 * - Spring scale press feedback + haptic
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
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "primaryScale"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.7f else 0.0f,
        animationSpec = tween(180),
        label = "primaryGlow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // Blue glow shadow on press
            .shadow(
                elevation = if (pressed) 12.dp else 0.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = PrimaryGlow,
                spotColor = PrimaryGlow
            )
            .clip(MaterialTheme.shapes.extraLarge)
            // Deep blue → electric blue gradient
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to PrimaryGradientStart,
                        0.55f to Color(0xFF3A76E8),
                        1.0f to PrimaryGradientEnd
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.18f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
    ) {
        // Top-edge glass highlight shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(start = 7.dp)
            )
        }
    }
}

/**
 * Premium Secondary / Ghost Button — AMOLED Edition.
 *
 * Features:
 * - True-black glass surface (#0D0D0D) with thin border
 * - Animated border: neutral (#2A2A2A) → electric blue when "Saved"
 * - Icon + label tint animate smoothly with the state
 * - Spring scale + haptic
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
    val haptic = LocalHapticFeedback.current
    val isSaved = label.contains("Saved", ignoreCase = true)

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "secondaryScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSaved) SecondaryBorderSaved else SecondaryBorder,
        animationSpec = tween(320),
        label = "secondaryBorder"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSaved) AccentPrimaryColor else Color(0xFFB0B8C8),
        animationSpec = tween(320),
        label = "secondaryIconTint"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSaved) AccentPrimaryColor else Color(0xFFB0B8C8),
        animationSpec = tween(320),
        label = "secondaryLabel"
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .height(48.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(MaterialTheme.shapes.extraLarge)
            .background(SecondaryBg)
            .border(1.dp, borderColor, MaterialTheme.shapes.extraLarge)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.08f)),
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
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            modifier = Modifier.padding(start = 7.dp)
        )
    }
}

// Expose the colour constant so SecondaryButton can reference it without importing the full theme
private val AccentPrimaryColor = Color(0xFF5B9BFF)

/**
 * Premium Accent Button — Full-width gradient CTA (used in auth/onboarding screens).
 * Stronger gradient, larger height for hero placement.
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
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 550f),
        label = "accentScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = if (pressed) 16.dp else 4.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = PrimaryGlow,
                spotColor = PrimaryGlow
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0.0f to PrimaryGradientStart,
                        0.5f to Color(0xFF3A76E8),
                        1.0f to PrimaryGradientEnd
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White.copy(alpha = 0.18f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
    ) {
        // Glass shimmer highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = if (icon != null) Modifier.padding(start = 7.dp) else Modifier
            )
        }
    }
}
