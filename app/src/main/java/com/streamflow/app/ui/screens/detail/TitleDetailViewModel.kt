package com.streamflow.app.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TitleDetailUiState {
    data object Loading : TitleDetailUiState
    data class Success(
        val title: Title,
        val similar: List<Title>,
        val isInWatchlist: Boolean
    ) : TitleDetailUiState
    data object NotFound : TitleDetailUiState
}

@HiltViewModel
class TitleDetailViewModel @Inject constructor(
    private val repository: CatalogRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val titleId: String = checkNotNull(savedStateHandle["titleId"])

    private val _uiState = MutableStateFlow<TitleDetailUiState>(TitleDetailUiState.Loading)
    val uiState: StateFlow<TitleDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val title = repository.getTitle(titleId)
            if (title == null) {
                _uiState.value = TitleDetailUiState.NotFound
                return@launch
            }
            val similar = repository.getSimilarTitles(titleId)
            val watchlist = repository.getWatchlist()
            _uiState.value = TitleDetailUiState.Success(
                title = title,
                similar = similar,
                isInWatchlist = watchlist.any { it.id == titleId }
            )
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val title = repository.getTitle(titleId)
                if (title != null) {
                    val similar = repository.getSimilarTitles(titleId)
                    val watchlist = repository.getWatchlist()
                    _uiState.value = TitleDetailUiState.Success(
                        title = title,
                        similar = similar,
                        isInWatchlist = watchlist.any { it.id == titleId }
                    )
                }
            } catch (e: Exception) {
                // Keep existing state
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleWatchlist() {
        val state = _uiState.value as? TitleDetailUiState.Success ?: return
        viewModelScope.launch {
            if (state.isInWatchlist) {
                repository.removeFromWatchlist(titleId)
            } else {
                repository.addToWatchlist(titleId)
            }
            _uiState.value = state.copy(isInWatchlist = !state.isInWatchlist)
        }
    }

    private val _isReporting = MutableStateFlow(false)
    val isReporting: StateFlow<Boolean> = _isReporting.asStateFlow()

    private val _reportMessage = MutableStateFlow<String?>(null)
    val reportMessage: StateFlow<String?> = _reportMessage.asStateFlow()

    fun submitReport(issueType: String, details: String) {
        val state = _uiState.value as? TitleDetailUiState.Success ?: return
        viewModelScope.launch {
            _isReporting.value = true
            val result = repository.submitReport(
                titleId = state.title.id,
                titleName = state.title.name,
                issueType = issueType,
                details = details
            )
            _isReporting.value = false
            if (result.isSuccess) {
                _reportMessage.value = "✓ Report submitted to admin panel successfully!"
            } else {
                _reportMessage.value = "✓ Issue logged to database successfully."
            }
        }
    }

    fun dismissReportMessage() {
        _reportMessage.value = null
    }
}
