package com.streamflow.app.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SkipNext

import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.streamflow.app.ui.components.PlayerControlsSheet
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.StreamFlowSpinner
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val hours = totalSec / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

// ─── Minimal icon button for top bar ─────────────────────────────────────────
@Composable
private fun TopBarIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tinted: Boolean = false
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (tinted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            else
                Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = Color.White
        )
    }
}

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var showControlsSheet by remember { mutableStateOf(false) }
    var isTouchLocked by remember { mutableStateOf(false) }
    var isSkipIntroVisible by remember { mutableStateOf(true) }
    var seekRippleText by remember { mutableStateOf<String?>(null) }
    var isSeekRippleLeft by remember { mutableStateOf(false) }
    var gestureToastText by remember { mutableStateOf<String?>(null) }
    var isBrightnessGesture by remember { mutableStateOf(true) }
    var countdownSeconds by remember { mutableStateOf(5) }
    val nextEp = uiState.nextEpisode

    var resizeMode by remember { mutableStateOf(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var isControlsOverlayVisible by remember { mutableStateOf(true) }
    var lastUserInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isBuffering by remember { mutableStateOf(false) }

    // ── Player state listener ─────────────────────────────────────────────────
    DisposableEffect(viewModel.player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                isPlaying = viewModel.player.isPlaying
                isBuffering = (state == Player.STATE_BUFFERING)
                val dur = viewModel.player.duration
                if (dur > 0) durationMs = dur
            }
        }
        viewModel.player.addListener(listener)
        isPlaying = viewModel.player.isPlaying
        isBuffering = (viewModel.player.playbackState == Player.STATE_BUFFERING)
        val dur = viewModel.player.duration
        if (dur > 0) durationMs = dur
        onDispose { viewModel.player.removeListener(listener) }
    }

    // ── Position polling ──────────────────────────────────────────────────────
    LaunchedEffect(viewModel.player) {
        while (true) {
            isPlaying = viewModel.player.isPlaying
            currentPositionMs = viewModel.player.currentPosition.coerceAtLeast(0L)
            val dur = viewModel.player.duration
            if (dur > 0) durationMs = dur
            delay(400)
        }
    }

    // ── Auto-hide controls ────────────────────────────────────────────────────
    // Keyed ONLY on lastUserInteractionTime — avoids the race where changing
    // isControlsOverlayVisible re-triggers the coroutine and hides the overlay
    // almost immediately after the user taps to reveal it.
    LaunchedEffect(lastUserInteractionTime) {
        if (isControlsOverlayVisible && isPlaying) {
            delay(4000)
            isControlsOverlayVisible = false
        }
    }

    val isAutoPlayNextEnabled by viewModel.authRepository.isAutoPlayNextEnabled.collectAsState()

    LaunchedEffect(uiState.isNextEpisodeCountdownActive, isAutoPlayNextEnabled) {
        if (isAutoPlayNextEnabled && uiState.isNextEpisodeCountdownActive && nextEp != null) {
            countdownSeconds = 5
            while (countdownSeconds > 0 && uiState.isNextEpisodeCountdownActive) {
                delay(1000)
                countdownSeconds--
            }
            if (uiState.isNextEpisodeCountdownActive && nextEp != null) {
                viewModel.playNextEpisode(nextEp)
            }
        }
    }

    LaunchedEffect(gestureToastText) {
        if (gestureToastText != null) { delay(1200); gestureToastText = null }
    }

    LaunchedEffect(Unit) { delay(10000); isSkipIntroVisible = false }

    LaunchedEffect(seekRippleText) {
        if (seekRippleText != null) { delay(700); seekRippleText = null }
    }

    // ── Lifecycle: pause on background ────────────────────────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.player.pause()
                viewModel.syncProgress()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Noisy audio: pause on disconnect ─────────────────────────────────────
    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: android.content.Context?, intent: android.content.Intent?) {
                if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    viewModel.player.pause()
                }
            }
        }
        val filter = android.content.IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        context.registerReceiver(receiver, filter)
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    // ── Keep screen on ────────────────────────────────────────────────────────
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            context.findActivity()?.requestedOrientation =
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // ── Controls settings sheet ───────────────────────────────────────────────
    if (showControlsSheet && !isTouchLocked) {
        val currentOrientation = context.findActivity()?.requestedOrientation
        val isCurrentlyLandscape = currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
            currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ||
            currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE

        PlayerControlsSheet(
            currentSpeed = uiState.playbackSpeed,
            audioTracks = uiState.audioTracks,
            subtitleTracks = uiState.subtitleTracks,
            videoQualities = uiState.availableVideoQualities,
            subtitleStyle = uiState.subtitleStyle,
            onSelectSpeed = viewModel::setPlaybackSpeed,
            onSelectAudioTrack = viewModel::setAudioTrack,
            onSelectSubtitleTrack = viewModel::setSubtitleTrack,
            onSelectVideoQuality = viewModel::setVideoQuality,
            onUpdateSubtitleStyle = viewModel::setSubtitleStyle,
            // Display tab wiring
            currentResizeMode = resizeMode,
            onResizeModeChange = { mode ->
                resizeMode = mode
                gestureToastText = when (mode) {
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT  -> "Aspect: Fit"
                    androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Aspect: Crop"
                    else -> "Aspect: Stretch"
                }
                isBrightnessGesture = true
            },
            isNerdStatsVisible = uiState.isNerdStatsVisible,
            onToggleNerdStats = viewModel::toggleNerdStats,
            onOrientationChange = { landscape ->
                context.findActivity()?.requestedOrientation = if (landscape)
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else
                    android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            },
            currentOrientationIsLandscape = isCurrentlyLandscape,
            onDismiss = {
                showControlsSheet = false
                // Re-show overlay and reset auto-hide timer when sheet closes
                isControlsOverlayVisible = true
                lastUserInteractionTime = System.currentTimeMillis()
            }
        )
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ROOT
    // ══════════════════════════════════════════════════════════════════════════
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Touch gestures: tap to toggle overlay, drag for brightness/volume
            .pointerInput(isTouchLocked) {
                if (!isTouchLocked) {
                    detectTapGestures(
                        onTap = {
                            // Always SHOW controls on tap and reset the 4-second hide timer.
                            // Never toggle-hide on tap — the overlay hides itself automatically.
                            isControlsOverlayVisible = true
                            lastUserInteractionTime = System.currentTimeMillis()
                        },
                        onDoubleTap = { offset ->
                            lastUserInteractionTime = System.currentTimeMillis()
                            if (offset.x < size.width / 2) {
                                val newPos = (viewModel.player.currentPosition - 10_000).coerceAtLeast(0)
                                viewModel.player.seekTo(newPos)
                                seekRippleText = "-10s"
                                isSeekRippleLeft = true
                            } else {
                                val duration = viewModel.player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                                val newPos = (viewModel.player.currentPosition + 10_000).coerceAtMost(duration)
                                viewModel.player.seekTo(newPos)
                                seekRippleText = "+10s"
                                isSeekRippleLeft = false
                            }
                        }
                    )
                }
            }
            .pointerInput(isTouchLocked) {
                if (!isTouchLocked) {
                    var volAccum = 0f
                    detectDragGestures(
                        onDragStart = { volAccum = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            lastUserInteractionTime = System.currentTimeMillis()
                            val isLeft = change.position.x < size.width / 2
                            val dy = dragAmount.y
                            if (isLeft) {
                                val activity = context.findActivity()
                                val attrs = activity?.window?.attributes
                                var brightness = attrs?.screenBrightness?.takeIf { it >= 0f } ?: 0.5f
                                brightness = (brightness - dy / 1800f).coerceIn(0.05f, 1.0f)
                                attrs?.screenBrightness = brightness
                                activity?.window?.attributes = attrs
                                gestureToastText = "☀ ${(brightness * 100).toInt()}%"
                                isBrightnessGesture = true
                            } else {
                                volAccum += dy
                                if (kotlin.math.abs(volAccum) >= 80f) {
                                    val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                                    am?.let {
                                        val max = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        val cur = it.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        val steps = (volAccum / 80f).toInt()
                                        val newVol = (cur - steps).coerceIn(0, max)
                                        it.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                        gestureToastText = "🔊 ${(newVol.toFloat() / max * 100).toInt()}%"
                                        isBrightnessGesture = false
                                        volAccum %= 80f
                                    }
                                }
                            }
                        }
                    )
                }
            }
    ) {

        // ── ExoPlayer Surface ──────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                (android.view.LayoutInflater.from(ctx)
                    .inflate(com.streamflow.app.R.layout.custom_player_view, null) as PlayerView).apply {
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    player = viewModel.player
                    useController = true
                    this.resizeMode = resizeMode
                    subtitleView?.apply {
                        val style = uiState.subtitleStyle
                        setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, style.fontSizeSp.toFloat())
                        setStyle(
                            androidx.media3.ui.CaptionStyleCompat(
                                style.textColorArgb.toInt(),
                                style.backgroundColorArgb.toInt(),
                                android.graphics.Color.TRANSPARENT,
                                androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                                android.graphics.Color.BLACK,
                                null
                            )
                        )
                    }
                }
            },
            update = { view ->
                if (view.player != viewModel.player) view.player = viewModel.player
                view.useController = true
                view.resizeMode = resizeMode
                view.subtitleView?.apply {
                    val style = uiState.subtitleStyle
                    setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, style.fontSizeSp.toFloat())
                    setStyle(
                        androidx.media3.ui.CaptionStyleCompat(
                            style.textColorArgb.toInt(),
                            style.backgroundColorArgb.toInt(),
                            android.graphics.Color.TRANSPARENT,
                            androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                            android.graphics.Color.BLACK,
                            null
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Buffering / Loading Indicator ──────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isLoading || isBuffering,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 0.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
                ) {
                    StreamFlowSpinner(
                        size = 40.dp,
                        strokeWidth = 3.dp,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isBuffering && !uiState.isLoading) "Buffering" else "Loading",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }

        // ── Error Screen ──────────────────────────────────────────────────────
        uiState.errorMessage?.let { message ->
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = BgElevated,
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.SignalWifiOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Playback Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        PrimaryButton(
                            label = "Retry",
                            icon = Icons.Filled.Refresh,
                            onClick = { viewModel.retryPlayback() }
                        )
                    }
                }
            }
        }

        // ── Touch Locked Pill ─────────────────────────────────────────────────
        if (isTouchLocked) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.Black.copy(alpha = 0.82f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isTouchLocked = false
                        isControlsOverlayVisible = true
                        lastUserInteractionTime = System.currentTimeMillis()
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Screen Locked · Tap to unlock",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }

        // ── Gesture Toast ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = gestureToastText != null && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(if (isBrightnessGesture) Alignment.CenterStart else Alignment.CenterEnd)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = Color.Black.copy(alpha = 0.78f),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = if (isBrightnessGesture) Icons.Filled.Brightness6 else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = gestureToastText ?: "",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }

        // ── Nerd Stats ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isNerdStatsVisible && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 72.dp, start = 16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.Black.copy(alpha = 0.85f),
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "DIAGNOSTICS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${uiState.videoWidth}×${uiState.videoHeight}  ${uiState.currentQualityBadge}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Text("${if (uiState.bitrateKbps > 0) "${uiState.bitrateKbps} kbps" else "Adaptive"}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Text("Buffer: ${uiState.bufferDurationMs / 1000}s", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // ── Seek Ripple ───────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = seekRippleText != null && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(if (isSeekRippleLeft) Alignment.CenterStart else Alignment.CenterEnd)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .size(88.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isSeekRippleLeft) Icons.Filled.FastRewind else Icons.Filled.FastForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = seekRippleText ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Skip Intro ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = isSkipIntroVisible && !uiState.isNextEpisodeCountdownActive && !isTouchLocked,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 88.dp)
        ) {
            Surface(
                onClick = {
                    val newPos = (viewModel.player.currentPosition + 85_000)
                    viewModel.player.seekTo(newPos)
                    isSkipIntroVisible = false
                    lastUserInteractionTime = System.currentTimeMillis()
                },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Skip Intro",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Next Episode Countdown Card ───────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isNextEpisodeCountdownActive && nextEp != null && !isTouchLocked,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 88.dp)
        ) {
            nextEp?.let { ep ->
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.Black.copy(alpha = 0.88f),
                    tonalElevation = 0.dp,
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Up Next",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(
                                onClick = { viewModel.dismissNextEpisodeCountdown() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "S${ep.seasonNumber} E${ep.episodeNumber} · ${ep.title}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Playing in ${countdownSeconds}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                            Surface(
                                onClick = { viewModel.playNextEpisode(ep) },
                                shape = MaterialTheme.shapes.medium,
                                color = Color.White,
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Play Now", color = Color.Black, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        //  MINIMAL TOP CONTROLS OVERLAY (auto-hide with ExoPlayer controls)
        // ══════════════════════════════════════════════════════════════════════
        val isInPipMode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.findActivity()?.isInPictureInPictureMode == true
        } else false

        AnimatedVisibility(
            visible = !isTouchLocked && !isInPipMode && isControlsOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // ── Top gradient + nav row ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                            )
                        )
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: back + title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            TopBarIconButton(
                                onClick = { lastUserInteractionTime = System.currentTimeMillis(); onBack() },
                                contentDescription = "Back",
                                icon = Icons.AutoMirrored.Filled.ArrowBack
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                if (uiState.titleName.isNotEmpty()) {
                                    Text(
                                        text = uiState.titleName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (uiState.episodeName.isNotEmpty()) {
                                    Text(
                                        text = uiState.episodeName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Right: Lock + Settings only — everything else lives in the Settings sheet
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Lock touch
                            TopBarIconButton(
                                onClick = {
                                    lastUserInteractionTime = System.currentTimeMillis()
                                    isTouchLocked = true
                                },
                                contentDescription = "Lock Screen",
                                icon = Icons.Filled.LockOpen
                            )

                            // Settings (Quality / Speed / Audio / Subs / Display)
                            TopBarIconButton(
                                onClick = {
                                    lastUserInteractionTime = System.currentTimeMillis()
                                    showControlsSheet = true
                                },
                                contentDescription = "Settings",
                                icon = Icons.Filled.Settings
                            )
                        }
                    }
                }
            }
        }
    }
}
