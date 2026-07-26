package com.streamflow.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.model.TitleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCatalogRepository @Inject constructor() : CatalogRepository {

    private val firestore: FirebaseFirestore? by lazy {
        runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }
    private val titlesCollection by lazy {
        firestore?.collection("titles")
    }

    private val sampleTitles = listOf(
        Title(
            id = "t1",
            name = "Cyberpunk Neo 2099",
            type = TitleType.MOVIE,
            posterUrl = "https://picsum.photos/seed/cyberpunk/300/450",
            backdropUrl = "https://picsum.photos/seed/cyberpunk_bg/1280/720",
            synopsis = "In a high-tech dystopian future, a rogue hacker uncovers a conspiracy that threatens humanity's digital consciousness.",
            imdbRating = 8.8,
            releaseYear = 2024,
            genres = listOf("Sci-Fi", "Action", "Cyberpunk"),
            hlsManifestPath = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        ),
        Title(
            id = "t2",
            name = "Starlight Horizon",
            type = TitleType.SERIES,
            posterUrl = "https://picsum.photos/seed/starlight/300/450",
            backdropUrl = "https://picsum.photos/seed/starlight_bg/1280/720",
            synopsis = "A team of space explorers ventures into uncharted galaxy territory searching for a new habitable world.",
            imdbRating = 9.1,
            releaseYear = 2023,
            genres = listOf("Sci-Fi", "Drama", "Adventure"),
            hlsManifestPath = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        ),
        Title(
            id = "t3",
            name = "Shadow Protocol",
            type = TitleType.MOVIE,
            posterUrl = "https://picsum.photos/seed/shadow/300/450",
            backdropUrl = "https://picsum.photos/seed/shadow_bg/1280/720",
            synopsis = "An elite undercover operative must race against time to stop an international syndicate from activating an orbital weapon.",
            imdbRating = 8.4,
            releaseYear = 2024,
            genres = listOf("Action", "Thriller"),
            hlsManifestPath = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        ),
        Title(
            id = "t4",
            name = "Neon Dreams",
            type = TitleType.SERIES,
            posterUrl = "https://picsum.photos/seed/neon/300/450",
            backdropUrl = "https://picsum.photos/seed/neon_bg/1280/720",
            synopsis = "A futuristic noir mystery tracking a detective through the illuminated subculture of a sprawling megalopolis.",
            imdbRating = 8.7,
            releaseYear = 2022,
            genres = listOf("Crime", "Mystery", "Sci-Fi"),
            hlsManifestPath = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        ),
        Title(
            id = "t5",
            name = "Quantum Break: Origins",
            type = TitleType.MOVIE,
            posterUrl = "https://picsum.photos/seed/quantum/300/450",
            backdropUrl = "https://picsum.photos/seed/quantum_bg/1280/720",
            synopsis = "When a particle accelerator experiment tears the fabric of time, survivors gain unpredictable temporal abilities.",
            imdbRating = 8.5,
            releaseYear = 2024,
            genres = listOf("Sci-Fi", "Action"),
            hlsManifestPath = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        )
    )

    private val watchlistIds = MutableStateFlow<Set<String>>(emptySet())
    private val continueWatchingList = MutableStateFlow<List<Title>>(emptyList())

    private fun buildDefaultRails(titles: List<Title>): List<Rail> = listOf(
        Rail(id = "trending", title = "Trending Now", titles = titles.take(3)),
        Rail(id = "action", title = "Action & Sci-Fi", titles = titles.filter { it.genres.contains("Action") || it.genres.contains("Sci-Fi") }),
        Rail(id = "popular", title = "Popular Movies & Shows", titles = titles)
    )

    override fun getHomeRails(): Flow<List<Rail>> = callbackFlow {
        val col = titlesCollection
        if (col == null) {
            trySend(buildDefaultRails(sampleTitles))
            close()
            return@callbackFlow
        }

        val listener = try {
            col.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fallback to sample titles on network/permission error
                    trySend(buildDefaultRails(sampleTitles))
                    return@addSnapshotListener
                }

                val titlesList = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        Title(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            type = if (doc.getString("type") == "SERIES") TitleType.SERIES else TitleType.MOVIE,
                            posterUrl = doc.getString("posterUrl") ?: "",
                            backdropUrl = doc.getString("backdropUrl") ?: "",
                            synopsis = doc.getString("synopsis") ?: "",
                            imdbRating = doc.getDouble("imdbRating") ?: 8.5,
                            releaseYear = (doc.getLong("releaseYear") ?: 2024L).toInt(),
                            genres = doc.get("genres") as? List<String> ?: listOf("Drama"),
                            hlsManifestPath = doc.getString("hlsManifestPath") ?: "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                        )
                    }.getOrNull()
                } ?: emptyList()

                val finalTitles = if (titlesList.isEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        runCatching { seedInitialCatalog() }
                    }
                    sampleTitles
                } else titlesList

                trySend(buildDefaultRails(finalTitles))
            }
        } catch (e: Exception) {
            trySend(buildDefaultRails(sampleTitles))
            null
        }

        awaitClose { listener?.remove() }
    }

    suspend fun seedInitialCatalog() {
        val col = titlesCollection ?: return
        runCatching {
            sampleTitles.forEach { title ->
                val data = mapOf(
                    "name" to title.name,
                    "type" to title.type.name,
                    "posterUrl" to title.posterUrl,
                    "backdropUrl" to title.backdropUrl,
                    "synopsis" to title.synopsis,
                    "imdbRating" to title.imdbRating,
                    "releaseYear" to title.releaseYear,
                    "genres" to title.genres,
                    "hlsManifestPath" to title.hlsManifestPath
                )
                col.document(title.id).set(data).await()
            }
        }
    }

    suspend fun addTitle(title: Title): Result<Unit> {
        val col = titlesCollection ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return runCatching {
            val docId = title.id.ifBlank { "title_${System.currentTimeMillis()}" }
            val data = mapOf(
                "name" to title.name,
                "type" to title.type.name,
                "posterUrl" to title.posterUrl.ifBlank { "https://picsum.photos/seed/${docId}/300/450" },
                "backdropUrl" to title.backdropUrl.ifBlank { "https://picsum.photos/seed/${docId}_bg/1280/720" },
                "synopsis" to title.synopsis,
                "imdbRating" to title.imdbRating,
                "releaseYear" to title.releaseYear,
                "genres" to title.genres.ifEmpty { listOf("Action", "Sci-Fi") },
                "hlsManifestPath" to title.hlsManifestPath.ifBlank { "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" }
            )
            col.document(docId).set(data).await()
            Unit
        }
    }

    suspend fun deleteTitle(titleId: String): Result<Unit> {
        val col = titlesCollection ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return runCatching {
            col.document(titleId).delete().await()
            Unit
        }
    }

    override suspend fun getTitle(id: String, forceRefresh: Boolean): Title? {
        val col = titlesCollection ?: return sampleTitles.find { it.id == id }
        return runCatching {
            val doc = col.document(id).get().await()
            if (!doc.exists()) sampleTitles.find { it.id == id }
            else Title(
                id = doc.id,
                name = doc.getString("name") ?: "",
                type = if (doc.getString("type") == "SERIES") TitleType.SERIES else TitleType.MOVIE,
                posterUrl = doc.getString("posterUrl") ?: "",
                backdropUrl = doc.getString("backdropUrl") ?: "",
                synopsis = doc.getString("synopsis") ?: "",
                imdbRating = doc.getDouble("imdbRating") ?: 8.5,
                releaseYear = (doc.getLong("releaseYear") ?: 2024L).toInt(),
                genres = doc.get("genres") as? List<String> ?: listOf("Drama"),
                hlsManifestPath = doc.getString("hlsManifestPath") ?: "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
            )
        }.getOrElse { sampleTitles.find { it.id == id } }
    }

    override suspend fun getSimilarTitles(id: String): List<Title> {
        val col = titlesCollection ?: return sampleTitles.filter { it.id != id }.take(4)
        return runCatching {
            val allDocs = col.get().await()
            allDocs.documents.mapNotNull { doc ->
                if (doc.id == id) null
                else Title(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    type = if (doc.getString("type") == "SERIES") TitleType.SERIES else TitleType.MOVIE,
                    posterUrl = doc.getString("posterUrl") ?: "",
                    backdropUrl = doc.getString("backdropUrl") ?: "",
                    synopsis = doc.getString("synopsis") ?: "",
                    imdbRating = doc.getDouble("imdbRating") ?: 8.5,
                    releaseYear = (doc.getLong("releaseYear") ?: 2024L).toInt(),
                    genres = doc.get("genres") as? List<String> ?: listOf("Drama"),
                    hlsManifestPath = doc.getString("hlsManifestPath") ?: "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                )
            }.take(4)
        }.getOrElse { sampleTitles.filter { it.id != id }.take(4) }
    }

    override suspend fun search(query: String): List<Title> {
        if (query.isBlank()) return emptyList()
        val col = titlesCollection ?: return sampleTitles.filter { it.name.contains(query, ignoreCase = true) }
        return runCatching {
            val allDocs = col.get().await()
            allDocs.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: ""
                if (name.contains(query, ignoreCase = true)) {
                    Title(
                        id = doc.id,
                        name = name,
                        type = if (doc.getString("type") == "SERIES") TitleType.SERIES else TitleType.MOVIE,
                        posterUrl = doc.getString("posterUrl") ?: "",
                        backdropUrl = doc.getString("backdropUrl") ?: "",
                        synopsis = doc.getString("synopsis") ?: "",
                        imdbRating = doc.getDouble("imdbRating") ?: 8.5,
                        releaseYear = (doc.getLong("releaseYear") ?: 2024L).toInt(),
                        genres = doc.get("genres") as? List<String> ?: listOf("Drama"),
                        hlsManifestPath = doc.getString("hlsManifestPath") ?: "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                    )
                } else null
            }
        }.getOrElse { sampleTitles.filter { it.name.contains(query, ignoreCase = true) } }
    }

    override suspend fun getContinueWatching(): List<Title> = continueWatchingList.value

    override suspend fun addToWatchlist(titleId: String) {
        watchlistIds.value = watchlistIds.value + titleId
    }

    override suspend fun removeFromWatchlist(titleId: String) {
        watchlistIds.value = watchlistIds.value - titleId
    }

    override suspend fun getWatchlist(): List<Title> {
        val ids = watchlistIds.value
        return ids.mapNotNull { getTitle(it) }
    }

    override suspend fun updateProgress(titleId: String, episodeId: String?, positionSec: Int, durationSec: Int) {
        val title = getTitle(titleId) ?: return
        if (continueWatchingList.value.none { it.id == titleId }) {
            continueWatchingList.value = continueWatchingList.value + title
        }
    }

    override suspend fun getPlaybackUrl(titleId: String, episodeId: String?): String {
        val title = getTitle(titleId)
        return title?.hlsManifestPath?.takeIf { it.isNotBlank() }
            ?: "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
    }

    override suspend fun submitReport(titleId: String, titleName: String, issueType: String, details: String): Result<Unit> = runCatching {
        val report = mapOf(
            "titleId" to titleId,
            "titleName" to titleName,
            "issueType" to issueType,
            "details" to details,
            "timestamp" to System.currentTimeMillis()
        )
        firestore?.collection("reports")?.add(report)?.await()
        Unit
    }
}
