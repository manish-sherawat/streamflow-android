package com.streamflow.app.ui.screens.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.streamflow.app.data.repository.AuthRepository
import com.streamflow.app.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

data class TrackOption(
    val id: String,
    val name: String,
    val isSelected: Boolean = false,
    val mediaTrackGroup: TrackGroup? = null,
    val trackIndex: Int = -1
)

data class VideoQualityOption(
    val id: String,
    val label: String,
    val maxHeight: Int,
    val is4K: Boolean = false,
    val isSelected: Boolean = false
)

data class SubtitleStyleConfig(
    val fontSizeSp: Int = 18,
    val textColorArgb: Long = 0xFFFFFFFF,
    val backgroundColorArgb: Long = 0x80000000
)

data class PlayerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val titleName: String = "",
    val episodeName: String = "",
    val playbackSpeed: Float = 1.0f,
    val audioTracks: List<TrackOption> = emptyList(),
    val subtitleTracks: List<TrackOption> = emptyList(),
    val availableVideoQualities: List<VideoQualityOption> = emptyList(),
    val subtitleStyle: SubtitleStyleConfig = SubtitleStyleConfig(),
    val nextEpisode: com.streamflow.app.data.model.Episode? = null,
    val isNextEpisodeCountdownActive: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val repository: CatalogRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val titleId: String = checkNotNull(savedStateHandle.get<String>("titleId"))
    private val episodeId: String? = savedStateHandle.get<String>("episodeId")?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val trackSelector = DefaultTrackSelector(application)

    private val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .build()

    private val renderersFactory = DefaultRenderersFactory(application)
        .setEnableDecoderFallback(true)
        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

    val player: ExoPlayer = ExoPlayer.Builder(application, renderersFactory)
        .setTrackSelector(trackSelector)
        .setAudioAttributes(audioAttributes, true)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(15_000, 50_000, 2_500, 5_000)
                .build()
        )
        .build()

    private val playerListener = object : Player.Listener {
        override fun onTracksChanged(tracks: Tracks) {
            updateTracksFromPlayer(tracks)
        }

        override fun onPlayerError(error: PlaybackException) {
            android.util.Log.e("ExoPlayerError", "Error playing movie: ${error.errorCodeName} - ${error.message}", error)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Playback Error (${error.errorCodeName}): ${error.localizedMessage ?: "Decoder failure"}"
            )
        }
    }

    init {
        player.addListener(playerListener)
        loadStream()
        startProgressSyncLoop()
        observeDataSaver()
    }

    private fun observeDataSaver() {
        viewModelScope.launch {
            authRepository.isDataSaverEnabled.collectLatest { isDataSaver ->
                val builder = trackSelector.buildUponParameters()
                if (isDataSaver) {
                    builder.setMaxVideoBitrate(1_500_000)
                } else {
                    builder.setMaxVideoBitrate(Int.MAX_VALUE)
                }
                trackSelector.setParameters(builder)
            }
        }
    }

    private fun loadStream() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Fetch title detail to check server capabilities (e.g. is4kHdr)
            val titleDetails = runCatching { repository.getTitle(titleId) }.getOrNull()
            val is4kSupported = titleDetails?.is4kHdr == true

            // Build dynamic server quality options — 4K UHD is ONLY included if server/title supports 4K!
            val qualities = mutableListOf<VideoQualityOption>()
            qualities.add(VideoQualityOption("auto", "Auto (Adaptive)", Int.MAX_VALUE, isSelected = true))
            if (is4kSupported) {
                qualities.add(VideoQualityOption("2160p", "4K Ultra HD (2160p)", 2160, is4K = true))
            }
            qualities.add(VideoQualityOption("1080p", "1080p Full HD", 1080))
            qualities.add(VideoQualityOption("720p", "720p HD", 720))
            qualities.add(VideoQualityOption("480p", "480p Data Saver", 480))

            // Resolve next episode if this is a series
            val nextEp = if (titleDetails != null && episodeId != null) {
                val epIndex = titleDetails.episodes.indexOfFirst { it.id == episodeId }
                if (epIndex >= 0 && epIndex + 1 < titleDetails.episodes.size) {
                    titleDetails.episodes[epIndex + 1]
                } else null
            } else null

            val epDetail = if (titleDetails != null && episodeId != null) {
                val ep = titleDetails.episodes.find { it.id == episodeId }
                if (ep != null) "S${ep.seasonNumber}:E${ep.episodeNumber} • ${ep.title}" else ""
            } else ""

            runCatching {
                repository.getPlaybackUrl(titleId, episodeId)
            }.onSuccess { url ->
                val builder = MediaItem.Builder().setUri(url)
                if (url.contains(".m3u8", ignoreCase = true) || url.contains("hls", ignoreCase = true)) {
                    builder.setMimeType(MimeTypes.APPLICATION_M3U8)
                }
                val mediaItem = builder.build()
                player.stop()
                player.clearMediaItems()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    titleName = titleDetails?.name ?: "",
                    episodeName = epDetail,
                    availableVideoQualities = qualities,
                    nextEpisode = nextEp
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    private fun updateTracksFromPlayer(tracks: Tracks) {
        val audioTrackList = mutableListOf<TrackOption>()
        val subtitleTrackList = mutableListOf<TrackOption>()
        var isAnySubtitleSelected = false

        var audioCount = 0
        var subtitleCount = 0

        for (group in tracks.groups) {
            val type = group.type
            val mediaTrackGroup = group.mediaTrackGroup

            if (type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    if (!group.isTrackSupported(i)) continue
                    val format = group.getTrackFormat(i)
                    val isSelected = group.isTrackSelected(i)
                    audioCount++

                    val name = buildAudioTrackName(format, audioCount)
                    audioTrackList.add(
                        TrackOption(
                            id = "audio_${mediaTrackGroup.id}_$i",
                            name = name,
                            isSelected = isSelected,
                            mediaTrackGroup = mediaTrackGroup,
                            trackIndex = i
                        )
                    )
                }
            } else if (type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    if (!group.isTrackSupported(i)) continue
                    val format = group.getTrackFormat(i)
                    val isSelected = group.isTrackSelected(i)
                    if (isSelected) isAnySubtitleSelected = true
                    subtitleCount++

                    val name = buildSubtitleTrackName(format, subtitleCount)
                    subtitleTrackList.add(
                        TrackOption(
                            id = "subtitle_${mediaTrackGroup.id}_$i",
                            name = name,
                            isSelected = isSelected,
                            mediaTrackGroup = mediaTrackGroup,
                            trackIndex = i
                        )
                    )
                }
            }
        }

        val finalSubtitleTracks = mutableListOf<TrackOption>()
        finalSubtitleTracks.add(
            TrackOption(
                id = "off",
                name = "Off",
                isSelected = !isAnySubtitleSelected,
                mediaTrackGroup = null,
                trackIndex = -1
            )
        )
        finalSubtitleTracks.addAll(subtitleTrackList)

        _uiState.value = _uiState.value.copy(
            audioTracks = audioTrackList,
            subtitleTracks = finalSubtitleTracks
        )
    }

    private fun buildAudioTrackName(format: Format, trackNumber: Int): String {
        val label = format.label?.trim()
        val language = format.language?.trim()

        val baseName = when {
            !label.isNullOrEmpty() -> label
            !language.isNullOrEmpty() && language != "und" -> getLanguageDisplayName(language)
            else -> "Audio Track $trackNumber"
        }

        val channels = when (format.channelCount) {
            1 -> "Mono"
            2 -> "Stereo"
            6 -> "5.1"
            8 -> "7.1"
            in 3..5 -> "${format.channelCount} ch"
            in 7..7 -> "${format.channelCount} ch"
            else -> null
        }

        return if (channels != null && !baseName.contains(channels, ignoreCase = true)) {
            "$baseName ($channels)"
        } else {
            baseName
        }
    }

    private fun buildSubtitleTrackName(format: Format, trackNumber: Int): String {
        val label = format.label?.trim()
        val language = format.language?.trim()

        var baseName = when {
            !label.isNullOrEmpty() -> label
            !language.isNullOrEmpty() && language != "und" -> getLanguageDisplayName(language)
            else -> "Subtitle $trackNumber"
        }

        val isCc = (format.roleFlags and C.ROLE_FLAG_CAPTION) != 0 ||
                (format.roleFlags and C.ROLE_FLAG_DESCRIBES_MUSIC_AND_SOUND) != 0
        val isForced = (format.selectionFlags and C.SELECTION_FLAG_FORCED) != 0

        if (isCc && !baseName.contains("CC", ignoreCase = true)) {
            baseName += " (CC)"
        } else if (isForced && !baseName.contains("Forced", ignoreCase = true)) {
            baseName += " (Forced)"
        }

        return baseName
    }

    private fun getLanguageDisplayName(languageTag: String): String {
        return try {
            val loc = Locale.forLanguageTag(languageTag)
            val name = loc.getDisplayName(Locale.ENGLISH)
            if (name.isNotBlank() && name != languageTag) {
                name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
            } else {
                languageTag.uppercase(Locale.ENGLISH)
            }
        } catch (e: Exception) {
            languageTag.uppercase(Locale.ENGLISH)
        }
    }

    private fun startProgressSyncLoop() {
        viewModelScope.launch {
            var syncCounter = 0
            while (true) {
                delay(1000)
                if (player.playbackState == Player.STATE_READY && player.isPlaying) {
                    val duration = player.duration
                    val currentPos = player.currentPosition
                    if (duration > 0 && (duration - currentPos) <= 15_000) {
                        if (_uiState.value.nextEpisode != null && !_uiState.value.isNextEpisodeCountdownActive) {
                            _uiState.value = _uiState.value.copy(isNextEpisodeCountdownActive = true)
                        }
                    }
                    syncCounter++
                    if (syncCounter >= 10) {
                        syncCounter = 0
                        syncProgress()
                    }
                }
            }
        }
    }

    fun dismissNextEpisodeCountdown() {
        _uiState.value = _uiState.value.copy(isNextEpisodeCountdownActive = false)
    }

    fun playNextEpisode(nextEp: com.streamflow.app.data.model.Episode) {
        _uiState.value = _uiState.value.copy(isNextEpisodeCountdownActive = false, isLoading = true)
        viewModelScope.launch {
            runCatching {
                repository.getPlaybackUrl(titleId, nextEp.id)
            }.onSuccess { url ->
                val builder = MediaItem.Builder().setUri(url)
                if (url.contains(".m3u8", ignoreCase = true) || url.contains("hls", ignoreCase = true)) {
                    builder.setMimeType(MimeTypes.APPLICATION_M3U8)
                }
                val mediaItem = builder.build()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true

                val titleDetails = runCatching { repository.getTitle(titleId) }.getOrNull()
                val epIndex = titleDetails?.episodes?.indexOfFirst { it.id == nextEp.id } ?: -1
                val newNextEp = if (titleDetails != null && epIndex >= 0 && epIndex + 1 < titleDetails.episodes.size) {
                    titleDetails.episodes[epIndex + 1]
                } else null

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nextEpisode = newNextEp,
                    isNextEpisodeCountdownActive = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setAudioTrack(option: TrackOption) {
        val mediaTrackGroup = option.mediaTrackGroup ?: return
        val trackIndex = option.trackIndex
        if (trackIndex < 0) return

        val override = TrackSelectionOverride(mediaTrackGroup, trackIndex)
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_AUDIO)
            .addOverride(override)
            .build()

        _uiState.value = _uiState.value.copy(
            audioTracks = _uiState.value.audioTracks.map {
                it.copy(isSelected = (it.id == option.id))
            }
        )
    }

    fun setSubtitleTrack(option: TrackOption) {
        if (option.id == "off" || option.mediaTrackGroup == null || option.trackIndex < 0) {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .build()

            _uiState.value = _uiState.value.copy(
                subtitleTracks = _uiState.value.subtitleTracks.map {
                    it.copy(isSelected = (it.id == "off"))
                }
            )
        } else {
            val override = TrackSelectionOverride(option.mediaTrackGroup, option.trackIndex)
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .addOverride(override)
                .build()

            _uiState.value = _uiState.value.copy(
                subtitleTracks = _uiState.value.subtitleTracks.map {
                    it.copy(isSelected = (it.id == option.id))
                }
            )
        }
    }

    fun setVideoQuality(option: VideoQualityOption) {
        val parametersBuilder = player.trackSelectionParameters.buildUpon()
        if (option.maxHeight == Int.MAX_VALUE) {
            parametersBuilder.setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
        } else {
            // Constrain ExoPlayer video track height to selected quality (e.g. 1080p, 720p, 480p, 2160p)
            parametersBuilder.setMaxVideoSize(option.maxHeight * 16 / 9, option.maxHeight)
        }
        player.trackSelectionParameters = parametersBuilder.build()

        _uiState.value = _uiState.value.copy(
            availableVideoQualities = _uiState.value.availableVideoQualities.map {
                it.copy(isSelected = (it.id == option.id))
            }
        )
    }

    fun setSubtitleStyle(style: SubtitleStyleConfig) {
        _uiState.value = _uiState.value.copy(subtitleStyle = style)
    }

    fun retryPlayback() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        loadStream()
    }

    fun syncProgress() {
        val positionSec = (player.currentPosition / 1000).toInt()
        val durationSec = (player.duration.takeIf { it > 0 } ?: 0L).div(1000).toInt()
        viewModelScope.launch {
            repository.updateProgress(titleId, episodeId, positionSec, durationSec)
        }
    }

    override fun onCleared() {
        syncProgress()
        player.stop()
        player.clearMediaItems()
        player.removeListener(playerListener)
        player.release()
        super.onCleared()
    }
}

