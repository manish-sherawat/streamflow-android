package com.streamflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── StreamFlow Minimalist Palette ──────────────────────────────────────────
// Philosophy: Pure black base, single electric-blue accent, near-zero saturation.
// Depth is achieved through contrast, not color or glass effects.

// Backgrounds — layered darkness
val BgBase      = Color(0xFF0A0A0A)   // True black canvas
val BgCard      = Color(0xFF161616)   // Card surface
val BgElevated  = Color(0xFF1C1C1C)   // Elevated sheets / dialogs
val BgInput     = Color(0xFF141414)   // Input field fill
val BgGradientTop = Color(0xFF0A0A0A) // kept for compat (same as base)

// Text hierarchy
val TextPrimary   = Color(0xFFF0F0F0) // Near-white primary
val TextSecondary = Color(0xFF8A8A8A) // Mid-gray labels
val TextTertiary  = Color(0xFF444444) // Muted hints / placeholders
val TextMuted     = Color(0xFF3A3A3A) // Disabled / inactive icons

// Single accent — Electric Blue
val AccentPrimary   = Color(0xFF4F8EF7) // Electric blue CTA
val AccentSecondary = Color(0xFF7BB0FF) // Lighter blue hover/secondary
val AccentFocus     = Color(0xFF4F8EF7) // kept alias for compat
val AccentMagenta   = Color(0xFF4F8EF7) // repurposed alias → blue
val AccentStar      = Color(0xFFFACC15) // Amber — rating stars only
val AccentCream     = Color(0xFFF0F0F0) // alias → TextPrimary

// Dividers & borders (ultra-subtle)
val Divider     = Color(0xFF1E1E1E)   // Hairline separator
val GlassBorder = Color(0xFF2A2A2A)   // Subtle card outline
val GlassHighlight = Color(0xFF2A2A2A) // compat alias

// Chip / Pill surfaces
val PillBg     = Color(0xFF1E1E1E)    // Flat dark chip background
val PillBorder = Color(0xFF2A2A2A)    // Chip border (mostly unused)

// Glass fill (kept for compat — mapped to card bg)
val GlassFill = Color(0xCC161616.toInt()) // 80% opaque card bg, for compat
