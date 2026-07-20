package com.streamflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Dark is the only supported theme for v1 (see design.md §7).
// Components should reference MaterialTheme.colorScheme, never these hex
// values directly, so a light theme can be added later without touching UI code.
private val StreamFlowDarkColors = darkColorScheme(
    background = BgBase,
    surface = BgElevated,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    secondary = AccentFocus,
    onSecondary = TextPrimary,
    outline = GlassBorder,
    surfaceVariant = BgGradientTop,
    onSurfaceVariant = TextSecondary
)

@Composable
fun StreamFlowTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(), // reserved for future light theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StreamFlowDarkColors,
        typography = StreamFlowMaterialType,
        content = content
    )
}
