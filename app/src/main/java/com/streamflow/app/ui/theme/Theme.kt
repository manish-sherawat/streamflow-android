package com.streamflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

/**
 * StreamFlow — Material 3 Expressive Pure Light Theme.
 *
 * Dark theme shifted 100% to Light Theme with clean light colors and Electric Blue seed (NO RED).
 */

private val StreamFlowLightFallback = lightColorScheme(
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
    onSurface = md_theme_light_onSurface,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
)

@Composable
fun StreamFlowTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = true,
    forceDark: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Force Light Theme 100% across the app
    val colorScheme = if (dynamicColor) {
        rememberDynamicColorScheme(
            seedColor = StreamFlowSeed,
            isDark = false,
            style = PaletteStyle.Expressive,
        )
    } else {
        StreamFlowLightFallback
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StreamFlowTypography,
        shapes = StreamFlowShapes,
        content = content,
    )
}
