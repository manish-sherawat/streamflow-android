package com.streamflow.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * StreamFlow — Material 3 Expressive Light Color System (NO RED, Pure Light Theme).
 */

/** Brand Seed — Electric Blue / Vibrant Azure (NO RED) */
val StreamFlowSeed = Color(0xFF2563EB)

// ---- Pure Light Theme Color Scheme Tokens ----
val md_theme_light_background = Color(0xFFF8FAFC)
val md_theme_light_surface = Color(0xFFFFFFFF)
val md_theme_light_surfaceDim = Color(0xFFE2E8F0)
val md_theme_light_surfaceBright = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
val md_theme_light_surfaceContainerLow = Color(0xFFF1F5F9)
val md_theme_light_surfaceContainer = Color(0xFFE2E8F0)
val md_theme_light_surfaceContainerHigh = Color(0xFFCBD5E1)
val md_theme_light_surfaceContainerHighest = Color(0xFF94A3B8)

val md_theme_light_onSurface = Color(0xFF0F172A)
val md_theme_light_onSurfaceVariant = Color(0xFF334155)
val md_theme_light_outline = Color(0xFF94A3B8)
val md_theme_light_outlineVariant = Color(0xFFE2E8F0)

val md_theme_light_primary = Color(0xFF2563EB)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFDBEAFE)
val md_theme_light_onPrimaryContainer = Color(0xFF1E40AF)

val md_theme_light_secondary = Color(0xFF3B82F6)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFEFF6FF)
val md_theme_light_onSecondaryContainer = Color(0xFF1D4ED8)

val md_theme_light_tertiary = Color(0xFFD97706)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFEF3C7)
val md_theme_light_onTertiaryContainer = Color(0xFF92400E)

val md_theme_light_error = Color(0xFFDC2626)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFEE2E2)
val md_theme_light_onErrorContainer = Color(0xFF991B1B)

// ---- Legacy Compatibility Tokens (Remapped to Pure Light Theme) ----
val BgBase = md_theme_light_background
val BgCard = md_theme_light_surfaceContainerLow
val BgElevated = md_theme_light_surface
val BgSurface = md_theme_light_surfaceContainer
val BgInput = md_theme_light_surfaceContainerLow
val BgGradientTop = BgBase

val TextPrimary = md_theme_light_onSurface
val TextSecondary = md_theme_light_onSurfaceVariant
val TextTertiary = md_theme_light_outline
val TextMuted = Color(0xFF64748B)

val AccentPrimary = StreamFlowSeed
val AccentSecondary = md_theme_light_secondary
val AccentFocus = StreamFlowSeed
val AccentMagenta = StreamFlowSeed
val AccentStar = md_theme_light_tertiary
val AccentCream = TextPrimary
val AccentGlow = Color(0x202563EB)

val Divider = md_theme_light_outlineVariant
val GlassBorder = md_theme_light_outlineVariant
val GlassHighlight = md_theme_light_outlineVariant

val PillBg = md_theme_light_surfaceContainerLow
val PillBorder = md_theme_light_outlineVariant
val GlassFill = Color(0xFDF8FAFC)
