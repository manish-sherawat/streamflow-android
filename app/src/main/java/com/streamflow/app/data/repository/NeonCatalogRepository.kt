package com.streamflow.app.data.repository

import com.streamflow.app.data.model.CastMember
import com.streamflow.app.data.model.Episode
import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.model.TitleType
import com.streamflow.app.data.remote.api.StreamFlowApiService
import com.streamflow.app.data.remote.dto.WatchlistRequestDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TEST_STREAM = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

private val fallbackTitles = listOf(
    Title(
        id = "t1", name = "Dog Man", type = TitleType.SERIES,
        synopsis = "A half-dog, half-man police officer fights crime with unconventional methods.",
        posterUrl = "https://picsum.photos/seed/dogman/400/600",
        backdropUrl = "https://picsum.photos/seed/dogman-bg/1200/800",
        imdbRating = 8.5, releaseYear = 2025,
        genres = listOf("Action", "Comedy"),
        hlsManifestPath = TEST_STREAM
    ),
    Title(
        id = "t2", name = "Wet Miter", type = TitleType.MOVIE,
        synopsis = "A detective returns to his hometown to solve a mysterious cold case.",
        posterUrl = "https://picsum.photos/seed/wetmiter/400/600",
        backdropUrl = "https://picsum.photos/seed/wetmiter-bg/1200/800",
        imdbRating = 7.8, releaseYear = 2024,
        genres = listOf("Drama", "Sci-Fi"),
        hlsManifestPath = TEST_STREAM
    ),
    Title(
        id = "t3", name = "Cyber Nexus", type = TitleType.MOVIE,
        synopsis = "In a futuristic metropolis, a hacker uncovers a conspiracy that threatens humanity.",
        posterUrl = "https://picsum.photos/seed/cybernexus/400/600",
        backdropUrl = "https://picsum.photos/seed/cybernexus-bg/1200/800",
        imdbRating = 9.1, releaseYear = 2025,
        genres = listOf("Sci-Fi", "Action"),
        hlsManifestPath = TEST_STREAM
    )
)

private val fallbackRails = listOf(
    Rail("trending", "Trending Now", fallbackTitles),
    Rail("popular", "Popular Movies & Shows", fallbackTitles.shuffled()),
    Rail("top-rated", "Top Rated", fallbackTitles)
)

@Singleton
class NeonCatalogRepository @Inject constructor(
    private val apiService: StreamFlowApiService
) : CatalogRepository {

    @Volatile
    private var cachedRails: List<Rail>? = null

    @Volatile
    private var cachedTitlesMap: MutableMap<String, Title> = mutableMapOf()

    override fun getHomeRails(): Flow<List<Rail>> = flow {
        // 1. Emit cached rails immediately for 0ms fast loading
        cachedRails?.let { emit(it) }

        // 2. Fetch fresh content from API
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
            if (rails.isNotEmpty()) {
                cachedRails = rails
                // Cache title lookup
                rails.flatMap { it.titles }.forEach { cachedTitlesMap[it.id] = it }
                emit(rails)
            } else if (cachedRails == null) {
                cachedRails = fallbackRails
                emit(fallbackRails)
            }
        } catch (e: Exception) {
            if (cachedRails == null) {
                cachedRails = fallbackRails
                emit(fallbackRails)
            }
        }
    }

    override suspend fun getTitle(id: String): Title? {
        cachedTitlesMap[id]?.let { return it }
        return try {
            val dto = apiService.getTitleById(id)
            val title = dto.toDomain()
            cachedTitlesMap[title.id] = title
            title
        } catch (e: Exception) {
            fallbackTitles.find { it.id == id }
        }
    }

    override suspend fun getSimilarTitles(id: String): List<Title> {
        return try {
            val title = getTitle(id) ?: return fallbackTitles.filterNot { it.id == id }
            val genre = title.genres.firstOrNull() ?: ""
            if (genre.isNotEmpty()) {
                val results = search(genre).filter { it.id != id }
                if (results.isNotEmpty()) results else fallbackTitles.filterNot { it.id == id }
            } else {
                fallbackTitles.filterNot { it.id == id }
            }
        } catch (e: Exception) {
            fallbackTitles.filterNot { it.id == id }
        }
    }

    override suspend fun search(query: String): List<Title> {
        if (query.isBlank()) return emptyList()
        return try {
            val dtos = apiService.searchTitles(query)
            val results = dtos.map { it.toDomain() }
            if (results.isNotEmpty()) {
                results.forEach { cachedTitlesMap[it.id] = it }
                results
            } else {
                fallbackTitles.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.genres.any { g -> g.contains(query, ignoreCase = true) }
                }
            }
        } catch (e: Exception) {
            fallbackTitles.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.genres.any { g -> g.contains(query, ignoreCase = true) }
            }
        }
    }

    override suspend fun getContinueWatching(): List<Title> {
        return cachedRails?.find { it.id == "continue-watching" }?.titles ?: emptyList()
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
        // No-op
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
                TEST_STREAM
            }
        } catch (e: Exception) {
            TEST_STREAM
        }
    }
}
