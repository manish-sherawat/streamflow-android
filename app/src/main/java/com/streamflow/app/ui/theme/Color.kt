package com.streamflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── StreamFlow High-Contrast Palette (Black, Red, Yellow, White) ─────────────
// Philosophy: True deep black background, cinematic red accent, golden yellow ratings/badges, crisp white text.
// 0% purple or blue tints.

// Backgrounds — pure dark layers
val BgBase      = Color(0xFF0A0A0A)   // Pure deep pitch black
val BgCard      = Color(0xFF141414)   // Card surface
val BgElevated  = Color(0xFF1E1E1E)   // Elevated sheets / bottom nav / dialogs
val BgSurface   = Color(0xFF222222)   // Mid-level surface (chips, headers)
val BgInput     = Color(0xFF181818)   // Input field fill

// kept for compat
val BgGradientTop = BgBase

// Text hierarchy — pure high contrast
val TextPrimary   = Color(0xFFFFFFFF) // Crisp pure white
val TextSecondary = Color(0xFFA0A0A0) // Clean light gray
val TextTertiary  = Color(0xFF666666) // Muted hint text
val TextMuted     = Color(0xFF444444) // Inactive icon gray

// Accent system — Cinematic Red & Golden Yellow
val AccentPrimary   = Color(0xFFE50914) // Vibrant cinematic red
val AccentSecondary = Color(0xFFFF3B30) // Bright secondary red
val AccentFocus     = Color(0xFFE50914) // compat alias
val AccentMagenta   = Color(0xFFE50914) // compat alias → red
val AccentStar      = Color(0xFFFFCC00) // Electric golden yellow — ratings & badges
val AccentCream     = Color(0xFFFFFFFF) // compat alias → TextPrimary
val AccentGlow      = Color(0x33E50914) // Transparent red glow

// Dividers & borders — subtle dark gray
val Divider        = Color(0xFF222222)  // Hairline separator
val GlassBorder    = Color(0xFF2A2A2A)  // Subtle card outline
val GlassHighlight = Color(0xFF2A2A2A)  // compat alias

// Chip / Pill surfaces
val PillBg     = Color(0xFF1F1F1F)     // Filled dark chip background
val PillBorder = Color(0xFF2A2A2A)     // Chip border

// Glass fill (kept for compat)
val GlassFill = Color(0xCC141414.toInt()) // 80% opaque card bg
