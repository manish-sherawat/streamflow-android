package com.streamflow.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchFilter(
    val contentType: String = "All",
    val minRating: Float = 0f,
    val selectedGenre: String = "All"
)

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter(),
    val results: List<Title> = emptyList(),
    val rawResults: List<Title> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        // 300ms debounce per PRD.md Flow C — avoids a network/DB hit on every keystroke.
        queryFlow
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _uiState.value = _uiState.value.copy(rawResults = emptyList(), results = emptyList(), isSearching = false)
                    return@onEach
                }
                _uiState.value = _uiState.value.copy(isSearching = true)
                val results = repository.search(query)
                val filtered = applyFilter(results, _uiState.value.filter)
                _uiState.value = _uiState.value.copy(rawResults = results, results = filtered, isSearching = false)
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        viewModelScope.launch { queryFlow.value = query }
    }

    fun setFilter(filter: SearchFilter) {
        val filtered = applyFilter(_uiState.value.rawResults, filter)
        _uiState.value = _uiState.value.copy(filter = filter, results = filtered)
    }

    private fun applyFilter(list: List<Title>, filter: SearchFilter): List<Title> {
        return list.filter { title ->
            val matchesType = when (filter.contentType) {
                "Movies" -> title.type.name == "MOVIE"
                "TV Shows" -> title.type.name == "SERIES"
                "Anime" -> title.genres.any { it.equals("Anime", ignoreCase = true) }
                else -> true
            }
            val matchesRating = title.imdbRating >= filter.minRating
            val matchesGenre = filter.selectedGenre == "All" || title.genres.any { it.equals(filter.selectedGenre, ignoreCase = true) }

            matchesType && matchesRating && matchesGenre
        }
    }
}
