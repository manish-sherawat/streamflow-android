package com.streamflow.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * StreamFlow — Pure AMOLED Dark Color System.
 * True black backgrounds (#000000) for OLED battery saving.
 * Electric Blue primary. No purple, no red.
 */

/** Brand Seed — Electric Blue */
val StreamFlowSeed = Color(0xFF2563EB)

// ── Pure AMOLED Black Palette ─────────────────────────────────────────────────
val md_theme_dark_background              = Color(0xFF000000)   // true black — AMOLED
val md_theme_dark_surface                 = Color(0xFF0D0D0D)   // near-black surface
val md_theme_dark_surfaceDim              = Color(0xFF050505)
val md_theme_dark_surfaceBright           = Color(0xFF141414)
val md_theme_dark_surfaceContainerLowest  = Color(0xFF000000)   // AMOLED absolute
val md_theme_dark_surfaceContainerLow     = Color(0xFF0D0D0D)
val md_theme_dark_surfaceContainer        = Color(0xFF121212)
val md_theme_dark_surfaceContainerHigh    = Color(0xFF181818)
val md_theme_dark_surfaceContainerHighest = Color(0xFF1E1E1E)

val md_theme_dark_onSurface              = Color(0xFFF0F2FF)   // near-white
val md_theme_dark_onSurfaceVariant       = Color(0xFF9099B0)   // muted secondary
val md_theme_dark_outline                = Color(0xFF3A3F52)
val md_theme_dark_outlineVariant         = Color(0xFF1E2130)   // barely visible divider

// Primary — Electric Blue (slightly lighter for AMOLED contrast)
val md_theme_dark_primary                = Color(0xFF5B9BFF)
val md_theme_dark_onPrimary              = Color(0xFF000000)
val md_theme_dark_primaryContainer       = Color(0xFF0F2A5C)
val md_theme_dark_onPrimaryContainer     = Color(0xFFB8D4FF)

// Secondary — Sky Blue
val md_theme_dark_secondary              = Color(0xFF74B3FF)
val md_theme_dark_onSecondary            = Color(0xFF001D45)
val md_theme_dark_secondaryContainer     = Color(0xFF0A2050)
val md_theme_dark_onSecondaryContainer   = Color(0xFFCCE0FF)

// Tertiary — Warm Amber (ratings / stars — no purple)
val md_theme_dark_tertiary               = Color(0xFFF5A623)
val md_theme_dark_onTertiary             = Color(0xFF2D1800)
val md_theme_dark_tertiaryContainer      = Color(0xFF3D2500)
val md_theme_dark_onTertiaryContainer    = Color(0xFFFFDFA0)

// Error
val md_theme_dark_error                  = Color(0xFFFF6B6B)
val md_theme_dark_onError                = Color(0xFF400000)
val md_theme_dark_errorContainer         = Color(0xFF5A1A1A)
val md_theme_dark_onErrorContainer       = Color(0xFFFFB3B3)

// ── Semantic Tokens ───────────────────────────────────────────────────────────
val BgBase     = md_theme_dark_background              // #000000 AMOLED true black
val BgCard     = md_theme_dark_surfaceContainerLow     // #0D0D0D
val BgElevated = md_theme_dark_surface                 // #0D0D0D
val BgSurface  = md_theme_dark_surfaceContainer        // #121212
val BgInput    = md_theme_dark_surfaceContainerHigh    // #181818
val BgGradientTop = BgBase

val TextPrimary   = md_theme_dark_onSurface            // #F0F2FF
val TextSecondary = md_theme_dark_onSurfaceVariant     // #9099B0
val TextTertiary  = md_theme_dark_outline
val TextMuted     = Color(0xFF565D70)

val AccentPrimary   = md_theme_dark_primary            // #5B9BFF
val AccentSecondary = md_theme_dark_secondary          // #74B3FF
val AccentFocus     = AccentPrimary
val AccentMagenta   = AccentPrimary                    // mapped to blue
val AccentStar      = md_theme_dark_tertiary           // Amber
val AccentCream     = TextPrimary
val AccentGlow      = Color(0x285B9BFF)

// ── AMOLED-tuned borders — ultra-thin on true black ──────────────────────────
val Divider         = Color(0xFF161616)
val GlassBorder     = Color(0xFF1E1E1E)
val GlassHighlight  = Color(0xFF262626)

val PillBg          = md_theme_dark_surfaceContainerHigh  // #181818
val PillBorder      = Color(0xFF242424)
val GlassFill       = md_theme_dark_surfaceContainer
