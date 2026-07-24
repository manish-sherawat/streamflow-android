package com.streamflow.app.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    var showControlsSheet by remember { mutableStateOf(false) }

    var isTouchLocked by remember { mutableStateOf(false) }
    var isSkipIntroVisible by remember { mutableStateOf(true) }
    var seekRippleText by remember { mutableStateOf<String?>(null) }
    var isSeekRippleLeft by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var gestureToastText by remember { mutableStateOf<String?>(null) }
    var isBrightnessToast by remember { mutableStateOf(true) }

    var countdownSeconds by remember { mutableStateOf(5) }
    val nextEp = uiState.nextEpisode

    var resizeMode by remember { mutableStateOf(androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var resizeModeLabel by remember { mutableStateOf("Fit (16:9)") }

    var isControlsOverlayVisible by remember { mutableStateOf(true) }
    var lastUserInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var sliderPositionMs by remember { mutableFloatStateOf(0f) }

    // Synchronize ExoPlayer playback state
    DisposableEffect(viewModel.player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
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

        onDispose {
            viewModel.player.removeListener(listener)
        }
    }

    // Continuously update position slider while playing
    LaunchedEffect(viewModel.player) {
        while (true) {
            isPlaying = viewModel.player.isPlaying
            if (!isSeeking) {
                currentPositionMs = viewModel.player.currentPosition.coerceAtLeast(0L)
                val dur = viewModel.player.duration
                if (dur > 0) durationMs = dur
            }
            delay(400)
        }
    }

    // Auto-hide controls overlay: ONLY hides when video is actively PLAYING! If PAUSED, buttons stay visible permanently!
    LaunchedEffect(lastUserInteractionTime, isControlsOverlayVisible, isPlaying) {
        if (isControlsOverlayVisible && isPlaying) {
            delay(4000)
            isControlsOverlayVisible = false
        }
    }

    LaunchedEffect(uiState.isNextEpisodeCountdownActive) {
        if (uiState.isNextEpisodeCountdownActive && nextEp != null) {
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
        if (gestureToastText != null) {
            delay(1200)
            gestureToastText = null
        }
    }

    // Auto-hide Skip Intro button after 10 seconds
    LaunchedEffect(Unit) {
        delay(10000)
        isSkipIntroVisible = false
    }

    LaunchedEffect(seekRippleText) {
        if (seekRippleText != null) {
            delay(800)
            seekRippleText = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    val activity = context.findActivity()
                    val isInPip = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        activity?.isInPictureInPictureMode == true
                    } else false
                    if (!isInPip) {
                        viewModel.player.pause()
                    }
                    viewModel.syncProgress()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Keep screen ON permanently during player activity so screen never dims or turns off while watching
    DisposableEffect(Unit) {
        val window = context.findActivity()?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            context.findActivity()?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    if (showControlsSheet && !isTouchLocked) {
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
            onDismiss = { showControlsSheet = false }
        )
    }

    var isHolding2xSpeed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isTouchLocked) {
                if (!isTouchLocked) {
                    detectTapGestures(
                        onTap = {
                            isControlsOverlayVisible = !isControlsOverlayVisible
                            lastUserInteractionTime = System.currentTimeMillis()
                        },
                        onPress = {
                            tryAwaitRelease()
                            if (isHolding2xSpeed) {
                                isHolding2xSpeed = false
                                viewModel.player.setPlaybackSpeed(uiState.playbackSpeed)
                            }
                        },
                        onLongPress = {
                            isHolding2xSpeed = true
                            viewModel.player.setPlaybackSpeed(2.0f)
                            lastUserInteractionTime = System.currentTimeMillis()
                        },
                        onDoubleTap = { offset ->
                            lastUserInteractionTime = System.currentTimeMillis()
                            val screenWidth = size.width
                            if (offset.x < screenWidth / 2) {
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
                    var volumeAccumulator = 0f
                    detectDragGestures(
                        onDragStart = {
                            volumeAccumulator = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            lastUserInteractionTime = System.currentTimeMillis()
                            val screenWidth = size.width
                            val isLeftHalf = change.position.x < screenWidth / 2
                            val deltaY = dragAmount.y

                            if (isLeftHalf) {
                                // Smooth & Precise Brightness Gesture (Reduced sensitivity factor 1800f)
                                val activity = context.findActivity()
                                val window = activity?.window
                                val attributes = window?.attributes
                                var currentBrightness = attributes?.screenBrightness ?: 0.5f
                                if (currentBrightness < 0f) currentBrightness = 0.5f
                                val newBrightness = (currentBrightness - deltaY / 1800f).coerceIn(0.05f, 1.0f)
                                attributes?.screenBrightness = newBrightness
                                window?.attributes = attributes
                                gestureToastText = "☀️ ${(newBrightness * 100).toInt()}%"
                                isBrightnessToast = true
                            } else {
                                // Smooth & Controlled Volume Gesture (Accumulate 80px per volume step)
                                volumeAccumulator += deltaY
                                val stepThreshold = 80f
                                if (kotlin.math.abs(volumeAccumulator) >= stepThreshold) {
                                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                                    if (audioManager != null) {
                                        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                        val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        val steps = (volumeAccumulator / stepThreshold).toInt()
                                        val newVol = (currentVol - steps).coerceIn(0, maxVol)
                                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                        val volPercent = (newVol.toFloat() / maxVol * 100).toInt()
                                        gestureToastText = "🔊 $volPercent%"
                                        isBrightnessToast = false
                                        volumeAccumulator %= stepThreshold
                                    }
                                }
                            }
                        }
                    )
                }
            }
    ) {
        // Video Render Surface (TextureView inflated from custom_player_view.xml)
        AndroidView(
            factory = { ctx ->
                (android.view.LayoutInflater.from(ctx)
                    .inflate(com.streamflow.app.R.layout.custom_player_view, null) as PlayerView).apply {
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    player = viewModel.player
                    useController = false
                    this.resizeMode = resizeMode
                    findViewById<android.view.View>(androidx.media3.ui.R.id.exo_settings)?.visibility = android.view.View.GONE
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
                if (view.player != viewModel.player) {
                    view.player = viewModel.player
                }
                view.useController = false
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

        // Screen Touch Lock Active Overlay Pill (Material 3 Expressive Monochrome)
        if (isTouchLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), MaterialTheme.shapes.extraLarge)
                    .clickable {
                        isTouchLocked = false
                        isControlsOverlayVisible = true
                        lastUserInteractionTime = System.currentTimeMillis()
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = "Unlock Screen", tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Screen Locked • Tap to Unlock",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2X Speed Hold Badge (Top-Center, non-overlapping)
        AnimatedVisibility(
            visible = isHolding2xSpeed && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.Black.copy(alpha = 0.90f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "2X SPEED ▶▶",
                        style = StreamFlowType.caption.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Gesture Toast & Vertical Edge Gauges (Non-overlapping Side Anchors)
        AnimatedVisibility(
            visible = gestureToastText != null && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(
                if (gestureToastText?.contains("Aspect") == true || gestureToastText?.contains("Orientation") == true)
                    Alignment.TopCenter
                else if (isBrightnessToast)
                    Alignment.CenterStart
                else
                    Alignment.CenterEnd
            )
        ) {
            Box(
                modifier = Modifier
                    .padding(
                        start = if (isBrightnessToast && gestureToastText?.contains("Aspect") == false && gestureToastText?.contains("Orientation") == false) 28.dp else 0.dp,
                        end = if (!isBrightnessToast && gestureToastText?.contains("Aspect") == false && gestureToastText?.contains("Orientation") == false) 28.dp else 0.dp,
                        top = if (gestureToastText?.contains("Aspect") == true || gestureToastText?.contains("Orientation") == true) 70.dp else 0.dp
                    )
                    .clip(MaterialTheme.shapes.large)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), MaterialTheme.shapes.large)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBrightnessToast) Icons.Filled.Brightness6 else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = gestureToastText ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Double-Tap Seek Ripple Overlay
        AnimatedVisibility(
            visible = seekRippleText != null && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(if (isSeekRippleLeft) Alignment.CenterStart else Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .size(96.dp)
                    .background(Color.Black.copy(alpha = 0.75f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isSeekRippleLeft) Icons.Filled.FastRewind else Icons.Filled.FastForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = seekRippleText ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Next Episode Auto-Play Countdown Card (Material 3 Expressive Monochrome)
        AnimatedVisibility(
            visible = uiState.isNextEpisodeCountdownActive && nextEp != null && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 90.dp)
        ) {
            nextEp?.let { ep ->
                Box(
                    modifier = Modifier
                        .width(320.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(Color.Black.copy(alpha = 0.92f))
                        .border(1.dp, Color.White.copy(alpha = 0.6f), MaterialTheme.shapes.large)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEXT EPISODE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { viewModel.dismissNextEpisodeCountdown() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "S${ep.seasonNumber}: E${ep.episodeNumber} - ${ep.title}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
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
                                text = "Auto-playing in ${countdownSeconds}s",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color.White)
                                    .clickable { viewModel.playNextEpisode(ep) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "PLAY NOW",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Skip Intro Button (Material 3 Expressive Monochrome)
        AnimatedVisibility(
            visible = isSkipIntroVisible && !uiState.isNextEpisodeCountdownActive && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 90.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), MaterialTheme.shapes.large)
                    .clickable {
                        val newPos = (viewModel.player.currentPosition + 85_000)
                        viewModel.player.seekTo(newPos)
                        isSkipIntroVisible = false
                        lastUserInteractionTime = System.currentTimeMillis()
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "SKIP INTRO (+85s)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.isLoading || isBuffering) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    StreamFlowSpinner(
                        size = 44.dp,
                        strokeWidth = 3.dp,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isBuffering && !uiState.isLoading) "BUFFERING..." else "LOADING STREAM...",
                        style = StreamFlowType.caption.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        uiState.errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(BgElevated)
                        .border(1.dp, GlassBorder, MaterialTheme.shapes.large)
                        .padding(28.dp)
                ) {
                    Icon(
                        Icons.Filled.SignalWifiOff,
                        contentDescription = null,
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Playback Error",
                        style = StreamFlowType.sheetHeader.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = StreamFlowType.body,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    PrimaryButton(
                        label = "Retry Playback",
                        icon = Icons.Filled.Refresh,
                        onClick = { viewModel.retryPlayback() }
                    )
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════════════
        // MATERIAL 3 EXPRESSIVE EXOPLAYER CONTROLS OVERLAY (NO BLUE / NEUTRAL TONES)
        // ══════════════════════════════════════════════════════════════════════════
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
                // ── Top Gradient Bar ───────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.9f), Color.Transparent)
                            )
                        )
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button & Title Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        lastUserInteractionTime = System.currentTimeMillis()
                                        onBack()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column {
                                if (uiState.titleName.isNotEmpty()) {
                                    Text(
                                        text = uiState.titleName,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (uiState.episodeName.isNotEmpty()) {
                                    Text(
                                        text = uiState.episodeName,
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        // Top Right Action Buttons (Material 3 Expressive Neutral Badges)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Speed Indicator Badge
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.large)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), MaterialTheme.shapes.large)
                                    .clickable {
                                        lastUserInteractionTime = System.currentTimeMillis()
                                        showControlsSheet = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Speed,
                                        contentDescription = "Speed",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${uiState.playbackSpeed}x",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            // Aspect Ratio Toggle Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        lastUserInteractionTime = System.currentTimeMillis()
                                        when (resizeMode) {
                                            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                                resizeModeLabel = "Crop to Fill"
                                            }
                                            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> {
                                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                                                resizeModeLabel = "Stretch"
                                            }
                                            else -> {
                                                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                                                resizeModeLabel = "Fit (16:9)"
                                            }
                                        }
                                        gestureToastText = "Aspect Ratio: $resizeModeLabel"
                                        isBrightnessToast = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AspectRatio,
                                    contentDescription = "Aspect Ratio",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // PiP Mode Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        lastUserInteractionTime = System.currentTimeMillis()
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            try {
                                                val activity = context.findActivity()
                                                val builder = android.app.PictureInPictureParams.Builder()
                                                    .setAspectRatio(android.util.Rational(16, 9))
                                                activity?.enterPictureInPictureMode(builder.build())
                                            } catch (e: Exception) {
                                                // Ignore if not supported
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureInPictureAlt,
                                    contentDescription = "Picture-in-Picture Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Orientation (Portrait <-> Landscape) Toggle Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        lastUserInteractionTime = System.currentTimeMillis()
                                        val activity = context.findActivity()
                                        val currentOrientation = activity?.requestedOrientation ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                                        val isLandscape = currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                                                currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE ||
                                                currentOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
                                        if (isLandscape) {
                                            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                            gestureToastText = "Orientation: Portrait"
                                        } else {
                                            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            gestureToastText = "Orientation: Landscape"
                                        }
                                        isBrightnessToast = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ScreenRotation,
                                    contentDescription = "Toggle Orientation (Portrait / Landscape)",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Lock Touch Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        lastUserInteractionTime = System.currentTimeMillis()
                                        isTouchLocked = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LockOpen,
                                    contentDescription = "Lock Touch Screen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Settings Gear Icon
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable {
                                        lastUserInteractionTime = System.currentTimeMillis()
                                        showControlsSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // ── Center Playback Controls (Material 3 Expressive Pure White CTA Button) ──
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind -10s Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            lastUserInteractionTime = System.currentTimeMillis()
                            val newPos = (viewModel.player.currentPosition - 10_000).coerceAtLeast(0)
                            viewModel.player.seekTo(newPos)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FastRewind,
                                contentDescription = "Rewind 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "-10s",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Main Big Play / Pause Button (Pure White M3 Expressive Action Pill)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                            .clickable {
                                lastUserInteractionTime = System.currentTimeMillis()
                                if (viewModel.player.isPlaying) {
                                    viewModel.player.pause()
                                } else {
                                    viewModel.player.play()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Fast Forward +10s Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            lastUserInteractionTime = System.currentTimeMillis()
                            val duration = viewModel.player.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                            val newPos = (viewModel.player.currentPosition + 10_000).coerceAtMost(duration)
                            viewModel.player.seekTo(newPos)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FastForward,
                                contentDescription = "Fast forward 10 seconds",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "+10s",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ── Bottom Gradient Bar & Scrubber Controls ─────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                        .navigationBarsPadding()
                        .displayCutoutPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Time Row (Current Position / Total Duration & Quality Badge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatTime(if (isSeeking) sliderPositionMs.toLong() else currentPositionMs),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " / ",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = formatTime(durationMs),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }

                            val selectedQuality = uiState.availableVideoQualities.find { it.isSelected }?.label ?: "Auto"
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedQuality,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Interactive Smooth Seek Slider (Pure White Neutral Accent)
                        Slider(
                            value = if (isSeeking) sliderPositionMs else currentPositionMs.toFloat(),
                            onValueChange = {
                                isSeeking = true
                                sliderPositionMs = it
                                lastUserInteractionTime = System.currentTimeMillis()
                            },
                            onValueChangeFinished = {
                                viewModel.player.seekTo(sliderPositionMs.toLong())
                                isSeeking = false
                                lastUserInteractionTime = System.currentTimeMillis()
                            },
                            valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1f)),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
