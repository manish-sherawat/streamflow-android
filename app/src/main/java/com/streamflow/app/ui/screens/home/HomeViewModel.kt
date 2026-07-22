package com.streamflow.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val rails: List<Rail>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _watchlistIds = MutableStateFlow<Set<String>>(emptySet())
    val watchlistIds: StateFlow<Set<String>> = _watchlistIds.asStateFlow()

    init {
        loadHome()
        refreshWatchlist()
    }

    fun refreshWatchlist() {
        viewModelScope.launch {
            val list = repository.getWatchlist()
            _watchlistIds.value = list.map { it.id }.toSet()
        }
    }

    fun toggleWatchlist(titleId: String) {
        viewModelScope.launch {
            val current = _watchlistIds.value
            if (current.contains(titleId)) {
                repository.removeFromWatchlist(titleId)
                _watchlistIds.value = current - titleId
            } else {
                repository.addToWatchlist(titleId)
                _watchlistIds.value = current + titleId
            }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshHome() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val continueWatching = runCatching { repository.getContinueWatching() }.getOrDefault(emptyList())
                repository.getHomeRails()
                    .catch { }
                    .collect { rails ->
                        val combinedRails = mutableListOf<Rail>()
                        if (continueWatching.isNotEmpty()) {
                            combinedRails.add(Rail(id = "continue-watching", title = "CONTINUE WATCHING", titles = continueWatching))
                        }
                        combinedRails.addAll(rails.filter { it.titles.isNotEmpty() })
                        _uiState.value = HomeUiState.Success(combinedRails)
                    }
                refreshWatchlist()
            } catch (e: Exception) {
                // Keep existing state
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val continueWatching = runCatching { repository.getContinueWatching() }.getOrDefault(emptyList())
                repository.getHomeRails()
                    .catch { e ->
                        _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load content")
                    }
                    .collect { rails ->
                        val combinedRails = mutableListOf<Rail>()
                        if (continueWatching.isNotEmpty()) {
                            combinedRails.add(Rail(id = "continue-watching", title = "CONTINUE WATCHING", titles = continueWatching))
                        }
                        combinedRails.addAll(rails.filter { it.titles.isNotEmpty() })
                        _uiState.value = HomeUiState.Success(combinedRails)
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Something went wrong")
            }
        }
    }
}
