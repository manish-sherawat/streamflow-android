package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamflow.app.ui.theme.PillBg
import com.streamflow.app.ui.theme.PillBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.StreamFlowType

/**
 * Frosted-glass pill used for metadata badges (IMDb rating, Subtitles, 4K HDR)
 * per design.md §5 — GlassPill. On API 31+ this would layer a real backdrop
 * blur via RenderEffect; here we use a translucent fill for broad compatibility.
 */
@Composable
fun GlassPill(
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        style = StreamFlowType.pillLabel,
        modifier = modifier
            .background(PillBg, Radius.pill)
            .border(1.dp, PillBorder, Radius.pill)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}
