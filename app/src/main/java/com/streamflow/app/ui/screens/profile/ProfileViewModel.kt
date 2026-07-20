package com.streamflow.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.repository.AuthRepository
import com.streamflow.app.data.repository.CatalogRepository
import com.streamflow.app.data.repository.DownloadRepository
import com.streamflow.app.data.repository.FirestoreCatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val downloadRepository: DownloadRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    val userSession = authRepository.userSession
    val isAuthenticated = authRepository.isAuthenticated
    val isDataSaverEnabled = authRepository.isDataSaverEnabled

    val downloadedTitles: StateFlow<List<Title>> = downloadRepository.downloadedTitles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun removeDownload(titleId: String) {
        viewModelScope.launch {
            downloadRepository.removeDownload(titleId)
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
