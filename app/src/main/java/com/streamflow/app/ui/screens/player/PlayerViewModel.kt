package com.streamflow.app.ui.screens.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.streamflow.app.data.repository.AuthRepository
import com.streamflow.app.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val playbackSpeed: Float = 1.0f,
    val audioTrack: String = "English (Original 5.1)",
    val subtitleTrack: String = "Off"
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

    val player: ExoPlayer = ExoPlayer.Builder(application)
        .setTrackSelector(trackSelector)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(15_000, 50_000, 2_500, 5_000)
                .build()
        )
        .build()

    init {
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
            runCatching {
                repository.getPlaybackUrl(titleId, episodeId)
            }.onSuccess { url ->
                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message)
            }
        }
    }

    private fun startProgressSyncLoop() {
        viewModelScope.launch {
            while (true) {
                delay(10_000)
                if (player.playbackState == Player.STATE_READY && player.isPlaying) {
                    syncProgress()
                }
            }
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setAudioTrack(track: String) {
        _uiState.value = _uiState.value.copy(audioTrack = track)
    }

    fun setSubtitleTrack(track: String) {
        _uiState.value = _uiState.value.copy(subtitleTrack = track)
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
        player.release()
        super.onCleared()
    }
}
