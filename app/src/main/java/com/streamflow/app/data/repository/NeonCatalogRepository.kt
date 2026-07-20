package com.streamflow.app.data.repository

import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.remote.api.StreamFlowApiService
import com.streamflow.app.data.remote.dto.WatchlistRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeonCatalogRepository @Inject constructor(
    private val apiService: StreamFlowApiService
) : CatalogRepository {

    override fun getHomeRails(): Flow<List<Rail>> = flow {
        try {
            val response = apiService.getCatalog()
            val rails = response.rails.map { it.toDomain() }.toMutableList()
            if (response.featured.isNotEmpty()) {
                rails.add(
                    0,
                    Rail(
                        id = "featured",
                        title = "Featured Highlights",
                        titles = response.featured.map { it.toDomain() }
                    )
                )
            }
            emit(rails)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getTitle(id: String): Title? {
        return try {
            val dto = apiService.getTitleById(id)
            dto.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getSimilarTitles(id: String): List<Title> {
        return try {
            val title = getTitle(id) ?: return emptyList()
            val genre = title.genres.firstOrNull() ?: ""
            if (genre.isNotEmpty()) {
                search(genre).filter { it.id != id }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun search(query: String): List<Title> {
        return try {
            val dtos = apiService.searchTitles(query)
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getContinueWatching(): List<Title> {
        return emptyList()
    }

    override suspend fun addToWatchlist(titleId: String) {
        try {
            apiService.syncWatchlist(WatchlistRequestDto(titleId = titleId, action = "add"))
        } catch (e: Exception) {
            // Ignored
        }
    }

    override suspend fun removeFromWatchlist(titleId: String) {
        try {
            apiService.syncWatchlist(WatchlistRequestDto(titleId = titleId, action = "remove"))
        } catch (e: Exception) {
            // Ignored
        }
    }

    override suspend fun getWatchlist(): List<Title> {
        return try {
            val dtos = apiService.getUserWatchlist()
            dtos.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateProgress(titleId: String, episodeId: String?, positionSec: Int, durationSec: Int) {
        // No-op or sync with API in future
    }

    override suspend fun getPlaybackUrl(titleId: String, episodeId: String?): String {
        return try {
            val title = getTitle(titleId)
            if (title != null && title.hlsManifestPath.isNotEmpty()) {
                if (episodeId != null) {
                    val ep = title.episodes.find { it.id == episodeId }
                    if (ep != null && ep.hlsPath.isNotEmpty()) {
                        return ep.hlsPath
                    }
                }
                title.hlsManifestPath
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
