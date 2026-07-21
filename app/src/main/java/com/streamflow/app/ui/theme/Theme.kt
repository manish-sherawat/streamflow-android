package com.streamflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// StreamFlow — dark-only minimalist theme.
// Components reference MaterialTheme.colorScheme exclusively; never raw hex values.
private val StreamFlowDarkColors = darkColorScheme(
    background          = BgBase,
    surface             = BgCard,
    surfaceVariant      = BgElevated,
    onBackground        = TextPrimary,
    onSurface           = TextPrimary,
    onSurfaceVariant    = TextSecondary,
    primary             = AccentPrimary,
    onPrimary           = BgBase,
    secondary           = AccentSecondary,
    onSecondary         = BgBase,
    outline             = GlassBorder,
    outlineVariant      = Divider,
    scrim               = BgBase,
    inverseSurface      = TextPrimary,
    inverseOnSurface    = BgBase,
    error               = Color(0xFFFF5252),
    onError             = BgBase
)

@Composable
fun StreamFlowTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StreamFlowDarkColors,
        typography  = StreamFlowMaterialType,
        content     = content
    )
}
