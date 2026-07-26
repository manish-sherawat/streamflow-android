package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.PillBg
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextSecondary

/**
 * Minimalist dark chip for metadata badges (year, quality, genre, subtitles).
 * Dark surface background matching the cinematic dark theme.
 * Highlighted variant uses the blue accent.
 */
@Composable
fun GlassPill(
    label: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    textColor: Color = TextSecondary
) {
    val bg    = if (isHighlighted) AccentPrimary.copy(alpha = 0.22f) else PillBg
    val color = if (isHighlighted) AccentPrimary else textColor

    Text(
        text = label,
        style = StreamFlowType.pillLabel.copy(
            color = color,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal
        ),
        modifier = modifier
            .background(bg, Radius.chip)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}
