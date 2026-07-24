package com.streamflow.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

/**
 * StreamFlow — Expressive shape scale. See design.md §3.
 * One shared ramp instead of per-component ad hoc radii.
 */
val StreamFlowShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // pills, meta badges (IMDb / Subtitles / 4K HDR)
    small = RoundedCornerShape(12.dp),       // poster thumbnails, episode cards
    medium = RoundedCornerShape(16.dp),      // content cards, sheets
    large = RoundedCornerShape(24.dp),       // hero / detail sheet top corners
    extraLarge = RoundedCornerShape(28.dp),  // full-bleed bottom sheets, floating bottom nav container
)

/**
 * Expressive "morph" shape reserved for the single highest-emphasis control on a screen
 * (the Play CTA). Built from androidx.graphics.shapes as a soft 9-point cookie/burst
 * polygon, then converted to a Compose Shape — corner "points" animate subtly on press
 * via MaterialTheme.motionScheme, which a plain RoundedCornerShape can't express.
 * Used sparingly per design.md §3 — everything else stays on the rounded-rect ramp above.
 */
val PlayButtonMorphShape = RoundedPolygon.star(
    numVerticesPerRadius = 9,
    innerRadius = 0.92f, // low contrast between inner/outer radius = soft "cookie", not a sharp star
    rounding = CornerRounding(radius = 0.18f),
).toComposeShape()

fun RoundedPolygon.toComposeShape(): Shape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = toPath().asComposePath()
        return Outline.Generic(path)
    }
}
