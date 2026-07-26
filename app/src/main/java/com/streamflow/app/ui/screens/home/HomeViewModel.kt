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

import kotlinx.coroutines.Dispatchers
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val rails: List<Rail>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

data class SilentUpdateState(
    val hasUpdate: Boolean = false,
    val latestVersion: String = "",
    val changelog: String = "",
    val downloadUrl: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _watchlistIds = MutableStateFlow<Set<String>>(emptySet())
    val watchlistIds: StateFlow<Set<String>> = _watchlistIds.asStateFlow()

    private val _updateState = MutableStateFlow<SilentUpdateState?>(null)
    val updateState: StateFlow<SilentUpdateState?> = _updateState.asStateFlow()

    private val _smartResumeTitle = MutableStateFlow<com.streamflow.app.data.model.Title?>(null)
    val smartResumeTitle: StateFlow<com.streamflow.app.data.model.Title?> = _smartResumeTitle.asStateFlow()

    fun dismissSmartResume() {
        _smartResumeTitle.value = null
    }

    init {
        loadHome()
        refreshWatchlist()
        checkSilentUpdate()
    }

    fun checkSilentUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/manish-sherawat/streamflow-android/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                if (connection.responseCode == 200) {
                    val text = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    val tagName = json.optString("tag_name", "")
                    val changelog = json.optString("body", "Bug fixes and performance improvements.")
                    val assets = json.optJSONArray("assets")
                    var apkUrl = ""
                    if (assets != null && assets.length() > 0) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            if (asset.optString("name", "").endsWith(".apk")) {
                                apkUrl = asset.optString("browser_download_url", "")
                                break
                            }
                        }
                    }
                    val latestClean = tagName.removePrefix("v").trim()
                    val currentClean = com.streamflow.app.BuildConfig.VERSION_NAME.removePrefix("v").trim()
                    if (isVersionNewer(latestClean, currentClean)) {
                        _updateState.value = SilentUpdateState(
                            hasUpdate = true,
                            latestVersion = tagName,
                            changelog = changelog,
                            downloadUrl = apkUrl.ifEmpty { "https://github.com/manish-sherawat/streamflow-android/releases" }
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors during silent background check
            }
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        for (i in 0 until minOf(latestParts.size, currentParts.size)) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }
        return latestParts.size > currentParts.size
    }

    fun dismissUpdateModal() {
        _updateState.value = null
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
                            _smartResumeTitle.value = continueWatching.firstOrNull()
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
