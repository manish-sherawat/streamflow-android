package com.streamflow.app.ui.screens.profile

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

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _watchlist = MutableStateFlow<List<Title>>(emptyList())
    val watchlist: StateFlow<List<Title>> = _watchlist.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _watchlist.value = repository.getWatchlist()
        }
    }
}
