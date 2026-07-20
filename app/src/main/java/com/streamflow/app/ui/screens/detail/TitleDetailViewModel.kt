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
}
