package com.streamflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── StreamFlow Premium Dark Palette ────────────────────────────────────────
// Philosophy: Near-black base, single electric-blue accent, micro-depth surfaces.
// Depth through layered surface elevation, not color saturation.

// Backgrounds — layered depth system
val BgBase      = Color(0xFF080810)   // Deep true-black with subtle blue tint
val BgCard      = Color(0xFF13131F)   // Card surface — elevated from base
val BgElevated  = Color(0xFF1A1A2A)   // Elevated sheets / bottom sheets / dialogs
val BgSurface   = Color(0xFF1F1F30)   // Mid-level surface (section headers, chips)
val BgInput     = Color(0xFF14141E)   // Input field fill

// kept for compat
val BgGradientTop = BgBase

// Text hierarchy — crisp & legible
val TextPrimary   = Color(0xFFF2F2FF) // Near-white with blue tint
val TextSecondary = Color(0xFF7E7E9A) // Mid-tone muted blue-gray
val TextTertiary  = Color(0xFF45455A) // Disabled / hint text
val TextMuted     = Color(0xFF3A3A50) // Inactive icons

// Accent system — single electric indigo-blue
val AccentPrimary   = Color(0xFF6C8EF5) // Refined electric blue-indigo
val AccentSecondary = Color(0xFF8BAAFF) // Lighter accent (hover/secondary states)
val AccentFocus     = Color(0xFF6C8EF5) // compat alias
val AccentMagenta   = Color(0xFF6C8EF5) // compat alias → blue
val AccentStar      = Color(0xFFFFCC30) // Warm amber — rating stars only
val AccentCream     = Color(0xFFF2F2FF) // compat alias → TextPrimary
val AccentGlow      = Color(0x336C8EF5) // Transparent glow for accented elements

// Dividers & borders — ultra-subtle
val Divider        = Color(0xFF1E1E2E)  // Hairline separator
val GlassBorder    = Color(0xFF2C2C42)  // Subtle card outline with blue tint
val GlassHighlight = Color(0xFF2C2C42)  // compat alias

// Chip / Pill surfaces
val PillBg     = Color(0xFF1E1E30)     // Filled dark chip background
val PillBorder = Color(0xFF2C2C42)     // Chip border

// Glass fill (kept for compat)
val GlassFill = Color(0xCC13131F.toInt()) // 80% opaque card bg, for compat
