package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun PlayerControlsSheet(
    currentSpeed: Float,
    currentAudioTrack: String,
    currentSubtitleTrack: String,
    onSelectSpeed: (Float) -> Unit,
    onSelectAudioTrack: (String) -> Unit,
    onSelectSubtitleTrack: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
    val audioTracks = listOf("English (Original 5.1)", "English (Stereo)", "Spanish (Latin)", "French")
    val subtitleTracks = listOf("Off", "English (CC)", "Spanish", "French", "German")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.card)
                .background(BgElevated)
                .border(1.dp, GlassBorder, Radius.card)
                .padding(Spacing.lg)
        ) {
            Text("Playback Settings", style = StreamFlowType.sheetHeader, color = TextPrimary)
            Spacer(modifier = Modifier.height(Spacing.md))

            // Speed Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Speed, contentDescription = null, tint = AccentPrimary)
                Text(" Playback Speed", style = StreamFlowType.titleHeader, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(speeds) { speed ->
                    val isSelected = speed == currentSpeed
                    Text(
                        text = "${speed}x",
                        style = StreamFlowType.pillLabel,
                        color = if (isSelected) AccentPrimary else TextPrimary,
                        modifier = Modifier
                            .clip(Radius.pill)
                            .background(if (isSelected) AccentPrimary.copy(alpha = 0.2f) else GlassBorder)
                            .border(1.dp, if (isSelected) AccentPrimary else GlassBorder, Radius.pill)
                            .clickable { onSelectSpeed(speed) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Audio Track Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.HighQuality, contentDescription = null, tint = AccentPrimary)
                Text(" Audio Track", style = StreamFlowType.titleHeader, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            audioTracks.forEach { track ->
                val isSelected = track == currentAudioTrack
                TrackOptionRow(trackName = track, isSelected = isSelected, onClick = { onSelectAudioTrack(track) })
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Subtitle Track Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Subtitles, contentDescription = null, tint = AccentPrimary)
                Text(" Subtitles", style = StreamFlowType.titleHeader, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            subtitleTracks.forEach { track ->
                val isSelected = track == currentSubtitleTrack
                TrackOptionRow(trackName = track, isSelected = isSelected, onClick = { onSelectSubtitleTrack(track) })
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            SecondaryButton(
                label = "Done",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TrackOptionRow(trackName: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(Radius.pill)
            .background(if (isSelected) AccentPrimary.copy(alpha = 0.15f) else BgElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = trackName,
            style = StreamFlowType.body,
            color = if (isSelected) AccentPrimary else TextSecondary
        )
        if (isSelected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = AccentPrimary)
        }
    }
}
