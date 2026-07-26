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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.streamflow.app.ui.screens.player.TrackOption
import com.streamflow.app.ui.screens.player.VideoQualityOption
import com.streamflow.app.ui.theme.AccentStar
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

// ── Dark player-matching surface colours ─────────────────────────────────────
private val SheetBg          = Color(0xFF111318)
private val SheetSurface     = Color(0xFF1C1E26)
private val SheetBorder      = Color.White.copy(alpha = 0.10f)
private val ChipSelectedBg   = Color.White
private val ChipSelectedText = Color.Black
private val ChipIdleBg       = Color(0xFF252830)
private val ChipIdleText     = Color.White.copy(alpha = 0.75f)
private val LabelColor       = Color.White.copy(alpha = 0.45f)
private val HeaderColor      = Color.White

enum class SettingsTab(val label: String, val icon: ImageVector) {
    QUALITY("Quality",   Icons.Filled.Hd),
    SPEED("Speed",       Icons.Filled.Speed),
    AUDIO("Audio",       Icons.Filled.Headphones),
    SUBTITLES("Subs",    Icons.Filled.Subtitles),
    DISPLAY("Display",   Icons.Filled.AspectRatio)
}

@Composable
fun PlayerControlsSheet(
    currentSpeed: Float,
    audioTracks: List<TrackOption>,
    subtitleTracks: List<TrackOption>,
    videoQualities: List<VideoQualityOption>,
    subtitleStyle: com.streamflow.app.ui.screens.player.SubtitleStyleConfig =
        com.streamflow.app.ui.screens.player.SubtitleStyleConfig(),
    onSelectSpeed: (Float) -> Unit,
    onSelectAudioTrack: (TrackOption) -> Unit,
    onSelectSubtitleTrack: (TrackOption) -> Unit,
    onSelectVideoQuality: (VideoQualityOption) -> Unit,
    onUpdateSubtitleStyle: (com.streamflow.app.ui.screens.player.SubtitleStyleConfig) -> Unit = {},
    // Display callbacks (new)
    currentResizeMode: Int = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT,
    onResizeModeChange: (Int) -> Unit = {},
    isNerdStatsVisible: Boolean = false,
    onToggleNerdStats: () -> Unit = {},
    onOrientationChange: (Boolean) -> Unit = {},  // true = landscape
    currentOrientationIsLandscape: Boolean = true,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(SettingsTab.QUALITY) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(SheetBg)
                .border(1.dp, SheetBorder, MaterialTheme.shapes.extraLarge)
                .padding(20.dp)
        ) {

            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Playback Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HeaderColor
                )
                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = SheetSurface,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Tab Row ────────────────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SettingsTab.values()) { tab ->
                    val isSelected = tab == activeTab
                    Surface(
                        onClick = { activeTab = tab },
                        shape = MaterialTheme.shapes.medium,
                        color = if (isSelected) ChipSelectedBg else ChipIdleBg,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) ChipSelectedText else ChipIdleText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ChipSelectedText else ChipIdleText
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Divider ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(SheetBorder)
            )

            Spacer(Modifier.height(14.dp))

            // ── Tab Content ────────────────────────────────────────────────
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { tab ->
                when (tab) {

                    // ── Quality ──────────────────────────────────────────
                    SettingsTab.QUALITY -> {
                        Column {
                            SheetSectionLabel("VIDEO QUALITY")
                            Spacer(Modifier.height(8.dp))
                            if (videoQualities.isEmpty()) {
                                DarkTrackRow(name = "Auto (Adaptive)", isSelected = true, onClick = {})
                            } else {
                                videoQualities.forEach { q ->
                                    DarkQualityRow(quality = q, onClick = { onSelectVideoQuality(q) })
                                }
                            }
                        }
                    }

                    // ── Speed ─────────────────────────────────────────────
                    SettingsTab.SPEED -> {
                        Column {
                            SheetSectionLabel("PLAYBACK SPEED")
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(speeds) { speed ->
                                    val isSelected = speed == currentSpeed
                                    DarkChip(
                                        label = if (speed == 1.0f) "1× Normal" else "${speed}×",
                                        isSelected = isSelected,
                                        onClick = { onSelectSpeed(speed) }
                                    )
                                }
                            }
                        }
                    }

                    // ── Audio ─────────────────────────────────────────────
                    SettingsTab.AUDIO -> {
                        Column {
                            SheetSectionLabel("AUDIO TRACKS")
                            Spacer(Modifier.height(8.dp))
                            if (audioTracks.isEmpty()) {
                                DarkTrackRow(name = "Default (Stereo)", isSelected = true, onClick = {})
                            } else {
                                audioTracks.forEach { track ->
                                    DarkTrackRow(
                                        name = track.name,
                                        isSelected = track.isSelected,
                                        onClick = { onSelectAudioTrack(track) }
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            SheetSectionLabel("SOUND MODE")
                            Spacer(Modifier.height(8.dp))
                            var selectedEq by remember { mutableStateOf("Standard") }
                            val eqPresets = listOf("Standard", "Vocal Clarity", "Bass Boost", "Night Mode")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(eqPresets) { preset ->
                                    DarkChip(
                                        label = preset,
                                        isSelected = preset == selectedEq,
                                        onClick = { selectedEq = preset }
                                    )
                                }
                            }
                        }
                    }

                    // ── Subtitles ─────────────────────────────────────────
                    SettingsTab.SUBTITLES -> {
                        Column {
                            SheetSectionLabel("SUBTITLE TRACKS")
                            Spacer(Modifier.height(8.dp))
                            if (subtitleTracks.isEmpty()) {
                                DarkTrackRow(name = "Off", isSelected = true, onClick = {})
                            } else {
                                subtitleTracks.forEach { track ->
                                    DarkTrackRow(
                                        name = track.name,
                                        isSelected = track.isSelected,
                                        onClick = { onSelectSubtitleTrack(track) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            SheetSectionLabel("FONT SIZE")
                            Spacer(Modifier.height(8.dp))
                            val fontSizes = listOf(14 to "Small", 18 to "Medium", 22 to "Large")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(fontSizes) { (sp, label) ->
                                    DarkChip(
                                        label = label,
                                        isSelected = subtitleStyle.fontSizeSp == sp,
                                        onClick = { onUpdateSubtitleStyle(subtitleStyle.copy(fontSizeSp = sp)) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            SheetSectionLabel("TEXT COLOUR")
                            Spacer(Modifier.height(8.dp))
                            val colours = listOf(
                                0xFFFFFFFFL to "White",
                                0xFFFFCC00L to "Yellow",
                                0xFF00E5FFL to "Cyan"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(colours) { (argb, label) ->
                                    DarkChip(
                                        label = label,
                                        isSelected = subtitleStyle.textColorArgb == argb,
                                        onClick = { onUpdateSubtitleStyle(subtitleStyle.copy(textColorArgb = argb)) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            SheetSectionLabel("SYNC DELAY")
                            Spacer(Modifier.height(8.dp))
                            var delay by remember { mutableStateOf(0.0f) }
                            val delays = listOf(-2f, -1f, -0.5f, 0f, 0.5f, 1f, 2f)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(delays) { sec ->
                                    DarkChip(
                                        label = if (sec == 0f) "0s" else if (sec > 0) "+${sec}s" else "${sec}s",
                                        isSelected = sec == delay,
                                        onClick = { delay = sec }
                                    )
                                }
                            }
                        }
                    }

                    // ── Display ───────────────────────────────────────────
                    SettingsTab.DISPLAY -> {
                        Column {
                            // Aspect Ratio
                            SheetSectionLabel("ASPECT RATIO")
                            Spacer(Modifier.height(8.dp))
                            val aspectModes = listOf(
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT    to "Fit  (16:9)",
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM   to "Crop to Fill",
                                androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL   to "Stretch"
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(aspectModes) { (mode, label) ->
                                    DarkChip(
                                        label = label,
                                        isSelected = currentResizeMode == mode,
                                        onClick = { onResizeModeChange(mode) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // Orientation
                            SheetSectionLabel("SCREEN ORIENTATION")
                            Spacer(Modifier.height(8.dp))
                            val orientations = listOf(true to "Landscape", false to "Portrait")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(orientations) { (isLand, label) ->
                                    DarkChip(
                                        label = label,
                                        isSelected = currentOrientationIsLandscape == isLand,
                                        onClick = { onOrientationChange(isLand) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // Nerd Stats toggle row
                            SheetSectionLabel("DIAGNOSTICS")
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                onClick = onToggleNerdStats,
                                shape = MaterialTheme.shapes.medium,
                                color = if (isNerdStatsVisible) ChipSelectedBg else ChipIdleBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.BarChart,
                                            contentDescription = null,
                                            tint = if (isNerdStatsVisible) ChipSelectedText else ChipIdleText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Show Nerd Stats",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isNerdStatsVisible) ChipSelectedText else ChipIdleText
                                        )
                                    }
                                    if (isNerdStatsVisible) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = ChipSelectedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Close button ───────────────────────────────────────────────
            Surface(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.large,
                color = SheetSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 13.dp)
                )
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = LabelColor,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun DarkChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) ChipSelectedBg else ChipIdleBg,
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) ChipSelectedText else ChipIdleText
            )
        }
    }
}

@Composable
private fun DarkQualityRow(quality: VideoQualityOption, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (quality.isSelected) Color.White.copy(alpha = 0.10f) else SheetSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = quality.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (quality.isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White
                )
                if (quality.is4K) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = AccentStar
                    ) {
                        Text(
                            "4K HDR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (quality.isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DarkTrackRow(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) Color.White.copy(alpha = 0.10f) else SheetSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = Color.White
            )
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
