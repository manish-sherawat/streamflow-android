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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SkipNext
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import com.streamflow.app.ui.components.PlayerControlsSheet
import com.streamflow.app.ui.components.StreamFlowSpinner
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.StreamFlowType
import kotlinx.coroutines.delay

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
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
    var lastUserInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastUserInteractionTime, isControlsOverlayVisible) {
        if (isControlsOverlayVisible) {
            delay(3500)
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

    // Smart auto-hide timer for Skip Intro button (fades out after 8 seconds)
    LaunchedEffect(Unit) {
        delay(8000)
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
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            lastUserInteractionTime = System.currentTimeMillis()
                            val screenWidth = size.width
                            val isLeftHalf = change.position.x < screenWidth / 2
                            val deltaY = dragAmount.y
                            if (isLeftHalf) {
                                val activity = context.findActivity()
                                val window = activity?.window
                                val attributes = window?.attributes
                                var currentBrightness = attributes?.screenBrightness ?: 0.5f
                                if (currentBrightness < 0f) currentBrightness = 0.5f
                                val newBrightness = (currentBrightness - deltaY / 500f).coerceIn(0.05f, 1.0f)
                                attributes?.screenBrightness = newBrightness
                                window?.attributes = attributes
                                gestureToastText = "☀️ ${(newBrightness * 100).toInt()}%"
                                isBrightnessToast = true
                            } else {
                                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                                if (audioManager != null) {
                                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                    val deltaVol = if (deltaY < 0) 1 else -1
                                    val newVol = (currentVol + deltaVol).coerceIn(0, maxVol)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                                    val volPercent = (newVol.toFloat() / maxVol * 100).toInt()
                                    gestureToastText = "🔊 $volPercent%"
                                    isBrightnessToast = false
                                }
                            }
                        }
                    )
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.player
                    useController = !isTouchLocked
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
                view.useController = !isTouchLocked
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

        // Screen Touch Lock Active Overlay
        if (isTouchLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, AccentPrimary, RoundedCornerShape(30.dp))
                    .clickable { isTouchLocked = false }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = "Unlock", tint = AccentPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "Screen Locked • Tap to Unlock",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Gesture Indicator Toast (Brightness / Volume)
        AnimatedVisibility(
            visible = gestureToastText != null && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(1.dp, AccentPrimary.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isBrightnessToast) Icons.Filled.Brightness6 else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = gestureToastText ?: "",
                        color = Color.White,
                        fontSize = 16.sp,
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
                    .padding(32.dp)
                    .size(100.dp)
                    .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (isSeekRippleLeft) Icons.Filled.FastRewind else Icons.Filled.FastForward,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = seekRippleText ?: "",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Next Episode Auto-Play Countdown Card
        AnimatedVisibility(
            visible = uiState.isNextEpisodeCountdownActive && nextEp != null && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        ) {
            nextEp?.let { ep ->
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.9f))
                        .border(1.5.dp, AccentPrimary, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEXT EPISODE",
                                color = AccentPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { viewModel.dismissNextEpisodeCountdown() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "S${ep.seasonNumber}: E${ep.episodeNumber} - ${ep.title}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-playing in ${countdownSeconds}s",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AccentPrimary)
                                    .clickable { viewModel.playNextEpisode(ep) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text(
                                        text = "PLAY NOW",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Smart Auto-Hiding Skip Intro Floating Button
        AnimatedVisibility(
            visible = isSkipIntroVisible && !uiState.isNextEpisodeCountdownActive && !isTouchLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clickable {
                        val newPos = (viewModel.player.currentPosition + 85_000)
                        viewModel.player.seekTo(newPos)
                        isSkipIntroVisible = false
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "SKIP INTRO (+85s)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (uiState.isLoading) {
            StreamFlowSpinner(
                size = 40.dp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        uiState.errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BgElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Icon(
                        Icons.Filled.SignalWifiOff,
                        contentDescription = null,
                        tint = Color(0xFFFF3B30),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Playback Error",
                        style = StreamFlowType.sheetHeader.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = message,
                        style = StreamFlowType.body,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(
                        label = "Retry Connection",
                        icon = Icons.Filled.Refresh,
                        onClick = { viewModel.retryPlayback() }
                    )
                }
            }
        }

        // Top bar overlays — Back, Aspect Ratio, PiP, Touch Lock, & Settings
        val isInPipMode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            context.findActivity()?.isInPictureInPictureMode == true
        } else false

        AnimatedVisibility(
            visible = !isTouchLocked && !isInPipMode && isControlsOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
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
                        Icon(Icons.Filled.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.size(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                lastUserInteractionTime = System.currentTimeMillis()
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    try {
                                        val activity = context.findActivity()
                                        val builder = android.app.PictureInPictureParams.Builder()
                                            .setAspectRatio(android.util.Rational(16, 9))
                                        activity?.enterPictureInPictureMode(builder.build())
                                    } catch (e: Exception) {
                                        // Ignore if PiP not supported
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PictureInPictureAlt, contentDescription = "Picture-in-Picture Mode", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.size(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                lastUserInteractionTime = System.currentTimeMillis()
                                isTouchLocked = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.LockOpen, contentDescription = "Lock Touch Screen", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.size(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .clickable {
                                lastUserInteractionTime = System.currentTimeMillis()
                                showControlsSheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            }
        }
    }
}
