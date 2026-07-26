package com.streamflow.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme

/**
 * StreamFlow — Material 3 Expressive DARK Theme.
 *
 * Pure dark cinematic theme with Electric Blue seed.
 * PaletteStyle.TonalSpot is used to prevent purple/violet tones
 * that PaletteStyle.Expressive generates on blue seeds.
 */

private val StreamFlowAmoledFallback = darkColorScheme(
    background                = md_theme_dark_background,       // #000000 AMOLED
    surface                   = md_theme_dark_surface,          // #0D0D0D
    surfaceContainerLowest    = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow       = md_theme_dark_surfaceContainerLow,
    surfaceContainer          = md_theme_dark_surfaceContainer,
    surfaceContainerHigh      = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest   = md_theme_dark_surfaceContainerHighest,
    onSurface                 = md_theme_dark_onSurface,
    onSurfaceVariant          = md_theme_dark_onSurfaceVariant,
    outline                   = md_theme_dark_outline,
    outlineVariant            = md_theme_dark_outlineVariant,
    primary                   = md_theme_dark_primary,
    onPrimary                 = md_theme_dark_onPrimary,
    primaryContainer          = md_theme_dark_primaryContainer,
    onPrimaryContainer        = md_theme_dark_onPrimaryContainer,
    secondary                 = md_theme_dark_secondary,
    onSecondary               = md_theme_dark_onSecondary,
    secondaryContainer        = md_theme_dark_secondaryContainer,
    onSecondaryContainer      = md_theme_dark_onSecondaryContainer,
    tertiary                  = md_theme_dark_tertiary,
    onTertiary                = md_theme_dark_onTertiary,
    tertiaryContainer         = md_theme_dark_tertiaryContainer,
    onTertiaryContainer       = md_theme_dark_onTertiaryContainer,
    error                     = md_theme_dark_error,
    onError                   = md_theme_dark_onError,
    errorContainer            = md_theme_dark_errorContainer,
    onErrorContainer          = md_theme_dark_onErrorContainer,
)

@Composable
fun StreamFlowTheme(
    darkTheme: Boolean = true,   // Always dark — streaming app
    dynamicColor: Boolean = true,
    forceDark: Boolean = false,
    content: @Composable () -> Unit,
) {
    // TonalSpot: clean Electric Blue tones, no purple. isDark=true → dark bg.
    val colorScheme = if (dynamicColor) {
        rememberDynamicColorScheme(
            seedColor = StreamFlowSeed,
            isDark = true,
            style = PaletteStyle.TonalSpot,
        )
    } else {
        StreamFlowAmoledFallback
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StreamFlowTypography,
        shapes = StreamFlowShapes,
        content = content,
    )
}
