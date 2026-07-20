package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.PillBg
import com.streamflow.app.ui.theme.PillBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.StreamFlowType

/**
 * Frosted-glass pill used for metadata badges (IMDb rating, Subtitles, 4K HDR).
 */
@Composable
fun GlassPill(
    label: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    textColor: Color = Color.White
) {
    val bg = if (isHighlighted) AccentPrimary.copy(alpha = 0.2f) else PillBg
    val border = if (isHighlighted) AccentPrimary.copy(alpha = 0.8f) else PillBorder

    Text(
        text = label,
        style = StreamFlowType.pillLabel.copy(
            color = if (isHighlighted) AccentPrimary else textColor,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
        ),
        modifier = modifier
            .background(bg, Radius.pill)
            .border(1.dp, border, Radius.pill)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}
