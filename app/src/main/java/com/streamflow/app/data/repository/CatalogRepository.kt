package com.streamflow.app.data.repository

import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun getHomeRails(): Flow<List<Rail>>
    suspend fun getTitle(id: String): Title?
    suspend fun getSimilarTitles(id: String): List<Title>
    suspend fun search(query: String): List<Title>
    suspend fun getContinueWatching(): List<Title>
    suspend fun addToWatchlist(titleId: String)
    suspend fun removeFromWatchlist(titleId: String)
    suspend fun getWatchlist(): List<Title>
    suspend fun updateProgress(titleId: String, episodeId: String?, positionSec: Int, durationSec: Int)
    suspend fun getPlaybackUrl(titleId: String, episodeId: String? = null): String
    suspend fun submitReport(titleId: String, titleName: String, issueType: String, details: String): Result<Unit>
}
