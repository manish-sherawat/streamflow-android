package com.streamflow.app.data.repository

import com.streamflow.app.data.model.Title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface DownloadRepository {
    val downloadedTitles: StateFlow<List<Title>>
    suspend fun downloadTitle(title: Title)
    suspend fun removeDownload(titleId: String)
    fun isDownloaded(titleId: String): Boolean
}

@Singleton
class MockDownloadRepository @Inject constructor() : DownloadRepository {
    private val _downloadedTitles = MutableStateFlow<List<Title>>(emptyList())
    override val downloadedTitles: StateFlow<List<Title>> = _downloadedTitles.asStateFlow()

    override suspend fun downloadTitle(title: Title) {
        if (_downloadedTitles.value.none { it.id == title.id }) {
            _downloadedTitles.value = _downloadedTitles.value + title
        }
    }

    override suspend fun removeDownload(titleId: String) {
        _downloadedTitles.value = _downloadedTitles.value.filterNot { it.id == titleId }
    }

    override fun isDownloaded(titleId: String): Boolean {
        return _downloadedTitles.value.any { it.id == titleId }
    }
}
