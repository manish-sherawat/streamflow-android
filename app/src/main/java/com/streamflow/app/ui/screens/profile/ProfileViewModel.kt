package com.streamflow.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.repository.AuthRepository
import com.streamflow.app.data.repository.CatalogRepository
import com.streamflow.app.data.repository.FirestoreCatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

data class AppUpdateState(
    val isChecking: Boolean = false,
    val hasUpdate: Boolean = false,
    val isUpToDate: Boolean = false,
    val latestVersion: String = "",
    val changelog: String = "",
    val downloadUrl: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    val userSession = authRepository.userSession
    val isAuthenticated = authRepository.isAuthenticated
    val isDataSaverEnabled = authRepository.isDataSaverEnabled
    val isAutoPlayNextEnabled = authRepository.isAutoPlayNextEnabled

    private val _updateState = MutableStateFlow(AppUpdateState())
    val updateState: StateFlow<AppUpdateState> = _updateState.asStateFlow()

    fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            _updateState.value = AppUpdateState(isChecking = true)
            try {
                val url = URL("https://api.github.com/repos/manish-sherawat/streamflow-android/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val tagName = json.optString("tag_name", "v1.0.0")
                    val body = json.optString("body", "Bug fixes and performance improvements.")
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
                        _updateState.value = AppUpdateState(
                            hasUpdate = true,
                            latestVersion = tagName,
                            changelog = body,
                            downloadUrl = apkUrl.ifEmpty { "https://github.com/manish-sherawat/streamflow-android/releases" }
                        )
                    } else {
                        _updateState.value = AppUpdateState(isUpToDate = true, latestVersion = tagName)
                    }
                } else {
                    _updateState.value = AppUpdateState(isUpToDate = true, latestVersion = "v${com.streamflow.app.BuildConfig.VERSION_NAME}")
                }
            } catch (e: Exception) {
                _updateState.value = AppUpdateState(isUpToDate = true, latestVersion = "v${com.streamflow.app.BuildConfig.VERSION_NAME}")
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

    fun dismissUpdateState() {
        _updateState.value = AppUpdateState()
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            authRepository.switchProfile(profileId)
        }
    }

    fun toggleDataSaver(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.toggleDataSaver(enabled)
        }
    }

    fun toggleAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch {
            authRepository.toggleAutoPlayNext(enabled)
        }
    }

    fun addTitle(title: Title) {
        viewModelScope.launch {
            (catalogRepository as? FirestoreCatalogRepository)?.addTitle(title)
        }
    }

    fun deleteTitle(titleId: String) {
        viewModelScope.launch {
            (catalogRepository as? FirestoreCatalogRepository)?.deleteTitle(titleId)
        }
    }

    fun seedCatalog() {
        viewModelScope.launch {
            (catalogRepository as? FirestoreCatalogRepository)?.seedInitialCatalog()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
