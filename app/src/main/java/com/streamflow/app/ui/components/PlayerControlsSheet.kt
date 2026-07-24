package com.streamflow.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.streamflow.app.ui.screens.player.TrackOption
import com.streamflow.app.ui.screens.player.VideoQualityOption
import com.streamflow.app.ui.theme.AccentStar
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

enum class SettingsTab(val title: String) {
    QUALITY("🎥 Quality"),
    SPEED("⚡ Speed"),
    AUDIO("🎧 Audio & EQ"),
    SUBTITLES("💬 Subtitles")
}

@Composable
fun PlayerControlsSheet(
    currentSpeed: Float,
    audioTracks: List<TrackOption>,
    subtitleTracks: List<TrackOption>,
    videoQualities: List<VideoQualityOption>,
    subtitleStyle: com.streamflow.app.ui.screens.player.SubtitleStyleConfig = com.streamflow.app.ui.screens.player.SubtitleStyleConfig(),
    onSelectSpeed: (Float) -> Unit,
    onSelectAudioTrack: (TrackOption) -> Unit,
    onSelectSubtitleTrack: (TrackOption) -> Unit,
    onSelectVideoQuality: (VideoQualityOption) -> Unit,
    onUpdateSubtitleStyle: (com.streamflow.app.ui.screens.player.SubtitleStyleConfig) -> Unit = {},
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(SettingsTab.QUALITY) }
    val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(BgElevated)
                .border(1.dp, GlassBorder, MaterialTheme.shapes.large)
                .padding(Spacing.lg)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playback Settings",
                    style = StreamFlowType.sheetHeader.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Settings Navigation Tabs (Material 3 Expressive Monochrome)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SettingsTab.values()) { tab ->
                    val isSelected = tab == activeTab
                    Text(
                        text = tab.title,
                        style = StreamFlowType.caption.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(if (isSelected) Color.Black else BgCard)
                            .border(0.5.dp, if (isSelected) Color.Transparent else GlassBorder, MaterialTheme.shapes.small)
                            .clickable { activeTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Tab Content
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    SettingsTab.QUALITY -> {
                        Column {
                            Text(
                                text = "STREAM VIDEO QUALITY (SERVER DETECTED)",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            if (videoQualities.isEmpty()) {
                                TrackOptionRow(trackName = "Auto (Adaptive)", isSelected = true, onClick = {})
                            } else {
                                videoQualities.forEach { quality ->
                                    QualityOptionRow(
                                        quality = quality,
                                        onClick = { onSelectVideoQuality(quality) }
                                    )
                                }
                            }
                        }
                    }

                    SettingsTab.SPEED -> {
                        Column {
                            Text(
                                text = "PLAYBACK SPEED RATE",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                items(speeds) { speed ->
                                    val isSelected = speed == currentSpeed
                                    Text(
                                        text = if (speed == 1.0f) "1.0x (Normal)" else "${speed}x",
                                        style = StreamFlowType.pillLabel,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .background(if (isSelected) Color.Black else BgCard)
                                            .border(1.dp, if (isSelected) Color.Black else GlassBorder, MaterialTheme.shapes.small)
                                            .clickable { onSelectSpeed(speed) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    SettingsTab.AUDIO -> {
                        Column {
                            Text(
                                text = "AUDIO TRACKS",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            if (audioTracks.isEmpty()) {
                                TrackOptionRow(trackName = "Default Audio (Stereo)", isSelected = true, onClick = {})
                            } else {
                                audioTracks.forEach { track ->
                                    TrackOptionRow(
                                        trackName = track.name,
                                        isSelected = track.isSelected,
                                        onClick = { onSelectAudioTrack(track) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(Spacing.md))
                            Text(
                                text = "EQUALIZER SOUND MODE PRESETS",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            var selectedEqPreset by remember { mutableStateOf("Standard") }
                            val eqPresets = listOf("Standard", "Vocal Clarity", "Bass Boost", "Night Mode")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                items(eqPresets) { preset ->
                                    val isSelected = preset == selectedEqPreset
                                    Text(
                                        text = preset,
                                        style = StreamFlowType.pillLabel,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .background(if (isSelected) Color.Black else BgCard)
                                            .clickable { selectedEqPreset = preset }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    SettingsTab.SUBTITLES -> {
                        Column {
                            Text(
                                text = "SUBTITLES & CLOSED CAPTIONS",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            if (subtitleTracks.isEmpty()) {
                                TrackOptionRow(trackName = "Off", isSelected = true, onClick = {})
                            } else {
                                subtitleTracks.forEach { track ->
                                    TrackOptionRow(
                                        trackName = track.name,
                                        isSelected = track.isSelected,
                                        onClick = { onSelectSubtitleTrack(track) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(Spacing.md))
                            Text(
                                text = "CAPTION FONT SIZE",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            val fontSizes = listOf(14 to "Small", 18 to "Medium", 22 to "Large")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                items(fontSizes) { (spVal, label) ->
                                    val isSelected = subtitleStyle.fontSizeSp == spVal
                                    Text(
                                        text = label,
                                        style = StreamFlowType.pillLabel,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .background(if (isSelected) Color.Black else BgCard)
                                            .clickable { onUpdateSubtitleStyle(subtitleStyle.copy(fontSizeSp = spVal)) }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(Spacing.md))
                            Text(
                                text = "TEXT COLOR",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            val colors = listOf(
                                0xFFFFFFFFL to "White",
                                0xFFFFCC00L to "Yellow",
                                0xFF00E5FFL to "Cyan"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                items(colors) { (colorVal, label) ->
                                    val isSelected = subtitleStyle.textColorArgb == colorVal
                                    Text(
                                        text = label,
                                        style = StreamFlowType.pillLabel,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .background(if (isSelected) Color.Black else BgCard)
                                            .clickable { onUpdateSubtitleStyle(subtitleStyle.copy(textColorArgb = colorVal)) }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(Spacing.md))
                            Text(
                                text = "SUBTITLE SYNC DELAY (CALIBRATION)",
                                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(Spacing.xs))
                            var subtitleDelaySec by remember { mutableStateOf(0.0f) }
                            val delays = listOf(-2.0f, -1.0f, -0.5f, 0.0f, 0.5f, 1.0f, 2.0f)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                items(delays) { sec ->
                                    val isSelected = sec == subtitleDelaySec
                                    Text(
                                        text = if (sec == 0f) "Sync (0s)" else if (sec > 0) "+${sec}s" else "${sec}s",
                                        style = StreamFlowType.pillLabel,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        modifier = Modifier
                                            .clip(MaterialTheme.shapes.small)
                                            .background(if (isSelected) Color.Black else BgCard)
                                            .clickable { subtitleDelaySec = sec }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            SecondaryButton(
                label = "Apply & Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QualityOptionRow(quality: VideoQualityOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (quality.isSelected) Color.Black.copy(alpha = 0.08f) else BgCard)
            .border(1.dp, if (quality.isSelected) TextPrimary else Color.Transparent, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = quality.label,
                style = StreamFlowType.body.copy(fontWeight = if (quality.isSelected) FontWeight.Bold else FontWeight.Normal),
                color = TextPrimary
            )
            if (quality.is4K) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(Radius.chip)
                        .background(AccentStar)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "4K HDR",
                        style = StreamFlowType.caption.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                        color = Color.Black
                    )
                }
            }
        }
        if (quality.isSelected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TrackOptionRow(trackName: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (isSelected) Color.Black.copy(alpha = 0.08f) else BgCard)
            .border(1.dp, if (isSelected) TextPrimary else Color.Transparent, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = trackName,
            style = StreamFlowType.body.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            color = TextPrimary
        )
        if (isSelected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
    }
}
