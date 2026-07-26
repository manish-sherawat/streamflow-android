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

private val fallbackTitles = listOf(
    Title(
        id = "cmrxewwru0000v0jkc2793vh5",
        name = "Elite Force (Season 1)",
        type = TitleType.SERIES,
        synopsis = "A high-stakes tactical team embarks on critical covert operations.",
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/ymjYmuHG4Af93fcoF0NKwx9Rovb.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w1000_and_h563_face/tMpfa73LmKpeZ3Fix1QmFGIUrKI.jpg",
        imdbRating = 8.5,
        releaseYear = 2026,
        genres = listOf("Action", "Crime", "Drama"),
        is4kHdr = true,
        hlsManifestPath = ""
    ),
    Title(
        id = "cmrvgqd650000l204g4bmaabw",
        name = "The Gentlemen (Season 1)",
        type = TitleType.SERIES,
        synopsis = "Eddie Horniman inherits his father's estate, only to discover it's part of a cannabis empire.",
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/z3JRz5ad27V62UJuvDfHo8ueOhZ.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w1000_and_h563_face/iJMSvEENvkgIEImhBSKmGCev15w.jpg",
        imdbRating = 8.8,
        releaseYear = 2024,
        genres = listOf("Crime", "Action", "Comedy"),
        is4kHdr = true,
        hlsManifestPath = ""
    ),
    Title(
        id = "cmrvfb3nn0000jo04h35h5fdu",
        name = "Disclosure Day (2026)",
        type = TitleType.MOVIE,
        synopsis = "A dramatic thriller unraveling secrets that could shatter global trust.",
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/259wnijEJoJLPuZuscxDTqwnypw.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w1000_and_h563_face/gVczdWWAkBCuwEV1v9cg7ELfdhT.jpg",
        imdbRating = 7.8,
        releaseYear = 2026,
        genres = listOf("Action", "Sci-Fi", "Thriller"),
        is4kHdr = true,
        hlsManifestPath = ""
    )
)

private val fallbackAnimeTitles = listOf(
    Title(
        id = "cmruhfdx7000hjr040mabl4nx",
        name = "India's Got Latent (Season 2)",
        type = TitleType.SERIES,
        synopsis = "Unfiltered talent showcase featuring raw performances and hilarious comedy.",
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/8jcdd5HqW4nhF2upVGFS0KJ6hdY.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w1000_and_h563_face/3qS74RxSO4K9TaxSSHPHc1vCGFW.jpg",
        imdbRating = 9.1,
        releaseYear = 2026,
        genres = listOf("Comedy", "Reality"),
        is4kHdr = true,
        hlsManifestPath = ""
    ),
    Title(
        id = "cmrxdzadx000al504bojsuzaj",
        name = "Pritam and Pedro (Season 1)",
        type = TitleType.SERIES,
        synopsis = "An unorthodox duo teams up for wild adventures and mysteries.",
        posterUrl = "https://media.themoviedb.org/t/p/w440_and_h660_face/lC0MtWrnC0SZICFHazagIfVKy0x.jpg",
        backdropUrl = "https://media.themoviedb.org/t/p/w1000_and_h563_face/eAXSlqAJwpdsZ59G10lao9OJ4SG.jpg",
        imdbRating = 8.9,
        releaseYear = 2026,
        genres = listOf("Comedy", "Crime", "Drama"),
        is4kHdr = true,
        hlsManifestPath = ""
    )
)

private val fallbackRails = listOf(
    Rail("trending", "Trending Now", fallbackTitles),
    Rail("popular", "Popular Movies & Shows", fallbackTitles),
    Rail("top-rated", "Top Rated", fallbackTitles),
    Rail("anime-universe", "Trending Highlights", fallbackAnimeTitles)
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
        // Fetch fresh live catalog from Vega API in stable chronological sequence
        try {
            val response = apiService.getCatalog()
            val rails = response.rails.map { railDto ->
                railDto.toDomain()
            }.toMutableList()

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
                cachedTitlesMap.clear()
                rails.flatMap { it.titles }.forEach { cachedTitlesMap[it.id] = it }
                emit(rails)
            }
        } catch (e: Exception) {
            android.util.Log.e("NeonCatalogRepository", "Failed to fetch live catalog from API", e)
            emit(fallbackRails)
        }
    }

    override suspend fun getTitle(id: String, forceRefresh: Boolean): Title? {
        if (!forceRefresh) {
            cachedTitlesMap[id]?.let { return it }
        }
        return try {
            val dto = apiService.getTitleById(id)
            val title = dto.toDomain()
            cachedTitlesMap[title.id] = title
            title
        } catch (e: Exception) {
            android.util.Log.w("NeonCatalogRepository", "Failed to fetch live title $id from API, falling back to cache", e)
            cachedTitlesMap[id]
                ?: fallbackTitles.find { it.id == id }
                ?: fallbackAnimeTitles.find { it.id == id }
                ?: cachedRails?.flatMap { it.titles }?.find { it.id == id }
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
                (fallbackTitles + fallbackAnimeTitles).filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.genres.any { g -> g.contains(query, ignoreCase = true) }
                }
            }
        } catch (e: Exception) {
            (fallbackTitles + fallbackAnimeTitles).filter {
                it.name.contains(query, ignoreCase = true) ||
                it.genres.any { g -> g.contains(query, ignoreCase = true) }
            }
        }
    }

    override suspend fun getContinueWatching(): List<Title> {
        return cachedRails?.find { it.id == "continue-watching" }?.titles ?: emptyList()
    }

    private val watchlistTitleIds = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()

    override suspend fun addToWatchlist(titleId: String) {
        watchlistTitleIds.add(titleId)
        getTitle(titleId)?.let { cachedTitlesMap[it.id] = it }
        try {
            apiService.syncWatchlist(WatchlistRequestDto(titleId = titleId, action = "add"))
        } catch (e: Exception) {
            // Ignored
        }
    }

    override suspend fun removeFromWatchlist(titleId: String) {
        watchlistTitleIds.remove(titleId)
        try {
            apiService.syncWatchlist(WatchlistRequestDto(titleId = titleId, action = "remove"))
        } catch (e: Exception) {
            // Ignored
        }
    }

    override suspend fun getWatchlist(): List<Title> {
        val localTitles = watchlistTitleIds.mapNotNull { getTitle(it) }.toMutableList()
        try {
            val dtos = apiService.getUserWatchlist()
            val remoteTitles = dtos.map { it.toDomain() }
            remoteTitles.forEach { t ->
                watchlistTitleIds.add(t.id)
                cachedTitlesMap[t.id] = t
                if (localTitles.none { it.id == t.id }) {
                    localTitles.add(t)
                }
            }
        } catch (e: Exception) {
            // Ignored
        }
        return localTitles
    }

    override suspend fun updateProgress(titleId: String, episodeId: String?, positionSec: Int, durationSec: Int) {
        // No-op
    }

    override suspend fun getPlaybackUrl(titleId: String, episodeId: String?): String {
        return try {
            // 1. Fetch live DTO directly from API to get the latest updated link
            val dto = apiService.getTitleById(titleId)
            val freshTitle = dto.toDomain()
            cachedTitlesMap[freshTitle.id] = freshTitle

            if (episodeId != null) {
                val ep = dto.episodes.find { it.id == episodeId }
                if (ep != null) {
                    val epPath = ep.hlsPath.ifEmpty { ep.altHlsPath ?: "" }
                    if (epPath.isNotEmpty()) return epPath
                }
            }

            val linkUrl = dto.streamLinks.firstOrNull()?.url ?: ""
            if (linkUrl.isNotEmpty()) return linkUrl

            if (dto.hlsManifestPath.isNotEmpty()) {
                return dto.hlsManifestPath
            }

            freshTitle.hlsManifestPath
        } catch (e: Exception) {
            android.util.Log.e("NeonCatalogRepository", "Failed to fetch live playback URL for title $titleId, falling back to cache", e)
            val cachedTitle = getTitle(titleId, forceRefresh = false)
            if (cachedTitle != null) {
                if (episodeId != null) {
                    val ep = cachedTitle.episodes.find { it.id == episodeId }
                    if (ep != null && ep.hlsPath.isNotEmpty()) {
                        return ep.hlsPath
                    }
                }
                if (cachedTitle.hlsManifestPath.isNotEmpty()) {
                    return cachedTitle.hlsManifestPath
                }
            }
            ""
        }
    }

    override suspend fun submitReport(
        titleId: String,
        titleName: String,
        issueType: String,
        details: String
    ): Result<Unit> = runCatching {
        val request = com.streamflow.app.data.remote.dto.ReportRequestDto(
            titleId = titleId,
            altTitleId = titleId,
            titleName = titleName,
            altTitleName = titleName,
            altTitle = titleName,
            issueType = issueType,
            altIssueType = issueType,
            reason = issueType,
            type = issueType,
            details = details,
            message = details,
            description = details,
            status = "open",
            deviceInfo = "Android App v${com.streamflow.app.BuildConfig.VERSION_NAME}"
        )
        var sent = false
        try {
            apiService.submitReportV1(request)
            sent = true
        } catch (e: Exception) {
            android.util.Log.w("NeonCatalogRepository", "v1/reports endpoint failed, trying /reports", e)
        }

        if (!sent) {
            try {
                apiService.submitReportRoot(request)
                sent = true
            } catch (e: Exception) {
                android.util.Log.w("NeonCatalogRepository", "reports endpoint failed, trying /v1/titles/{id}/report", e)
            }
        }

        if (!sent) {
            try {
                apiService.reportTitleV1(titleId, request)
                sent = true
            } catch (e: Exception) {
                android.util.Log.w("NeonCatalogRepository", "v1/titles/{id}/report failed, trying /titles/{id}/report", e)
            }
        }

        if (!sent) {
            apiService.reportTitleRoot(titleId, request)
        }
        Unit
    }
}
