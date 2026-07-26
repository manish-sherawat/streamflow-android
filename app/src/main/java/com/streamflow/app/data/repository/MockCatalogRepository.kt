package com.streamflow.app.data.repository

import com.streamflow.app.data.model.CastMember
import com.streamflow.app.data.model.Episode
import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.model.TitleType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

// Public test HLS streams so playback is exercisable end-to-end without a real CDN.
private const val TEST_STREAM_1 = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
private const val TEST_STREAM_2 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"

@Singleton
class MockCatalogRepository @Inject constructor() : CatalogRepository {

    private val watchlist = mutableSetOf<String>()
    private val progress = mutableMapOf<String, Pair<Int, Int>>() // titleId -> (positionSec, durationSec)

    private val castPool = listOf(
        CastMember("c1", "Landry Jones", "https://i.pravatar.cc/150?img=12"),
        CastMember("c2", "Grace Palma", "https://i.pravatar.cc/150?img=32"),
        CastMember("c3", "Denham Cole", "https://i.pravatar.cc/150?img=14"),
        CastMember("c4", "Mara Voss", "https://i.pravatar.cc/150?img=45")
    )

    private val episodesForDogMan = (1..6).map { n ->
        Episode(
            id = "ep$n",
            title = if (n == 1) "Sleep of the Just" else "Episode $n",
            seasonNumber = 1,
            episodeNumber = n,
            durationSec = 45 * 60,
            thumbUrl = "https://picsum.photos/seed/dogman-ep$n/400/225",
            hlsPath = TEST_STREAM_1,
            watchedPositionSec = if (n == 1) 20 * 60 else 0
        )
    }

    private val animeTitles = listOf(
        Title(
            id = "a1", name = "Solo Leveling: Arise", type = TitleType.SERIES,
            synopsis = "In a world where hunters must battle deadly monsters, the weakest hunter receives a mysterious quest line that allows him to level up endlessly.",
            posterUrl = "https://picsum.photos/seed/sololeveling/400/600",
            backdropUrl = "https://picsum.photos/seed/sololeveling-bg/1200/800",
            imdbRating = 8.9, releaseYear = 2025,
            genres = listOf("Anime", "Action", "Fantasy"),
            is4kHdr = true,
            hlsManifestPath = TEST_STREAM_1
        ),
        Title(
            id = "a2", name = "Demon Blade: Infinity", type = TitleType.SERIES,
            synopsis = "A young swordmaster embarks on a perilous mission through an infinite labyrinth to save his sister and defeat ancient evil.",
            posterUrl = "https://picsum.photos/seed/demonblade/400/600",
            backdropUrl = "https://picsum.photos/seed/demonblade-bg/1200/800",
            imdbRating = 9.1, releaseYear = 2024,
            genres = listOf("Anime", "Action", "Supernatural"),
            is4kHdr = true,
            hlsManifestPath = TEST_STREAM_2
        ),
        Title(
            id = "a3", name = "Attack on Titan: Final", type = TitleType.SERIES,
            synopsis = "Humanity's last stand reaches its dramatic climax as secrets of the Titans and world history are finally revealed.",
            posterUrl = "https://picsum.photos/seed/aotfinal/400/600",
            backdropUrl = "https://picsum.photos/seed/aotfinal-bg/1200/800",
            imdbRating = 9.2, releaseYear = 2024,
            genres = listOf("Anime", "Sci-Fi", "Drama"),
            is4kHdr = true,
            hlsManifestPath = TEST_STREAM_1
        ),
        Title(
            id = "a4", name = "Jujutsu Cursed Realm", type = TitleType.SERIES,
            synopsis = "Sorcerers defend Tokyo against deadly cursed spirits threatening to engulf the real world in total chaos.",
            posterUrl = "https://picsum.photos/seed/jujutsu/400/600",
            backdropUrl = "https://picsum.photos/seed/jujutsu-bg/1200/800",
            imdbRating = 8.8, releaseYear = 2025,
            genres = listOf("Anime", "Fantasy", "Action"),
            hlsManifestPath = TEST_STREAM_2
        )
    )

    private val allTitles = listOf(
        Title(
            id = "t1", name = "Dog Man", type = TitleType.SERIES,
            synopsis = "A half-dog, half-man police officer fights crime with unconventional methods in a city that never quite knows what to make of him.",
            posterUrl = "https://picsum.photos/seed/dogman/400/600",
            backdropUrl = "https://picsum.photos/seed/dogman-bg/1200/800",
            imdbRating = 7.1, releaseYear = 2025,
            genres = listOf("Drama", "Comedy"),
            cast = castPool,
            is4kHdr = true,
            hlsManifestPath = TEST_STREAM_1,
            episodes = episodesForDogMan
        ),
        Title(
            id = "t2", name = "Wet Miter", type = TitleType.MOVIE,
            synopsis = "A detective haunted by his past returns to his hometown to solve a murder that mirrors an unsolved case from his childhood.",
            posterUrl = "https://picsum.photos/seed/wetmiter/400/600",
            backdropUrl = "https://picsum.photos/seed/wetmiter-bg/1200/800",
            imdbRating = 6.8, releaseYear = 2024,
            genres = listOf("Thriller"),
            cast = castPool.shuffled(),
            hlsManifestPath = TEST_STREAM_2
        ),
        Title(
            id = "t3", name = "Stone Hate", type = TitleType.MOVIE,
            synopsis = "Two rival cartel families collide in a neon-soaked border town, where loyalty is currency and betrayal is fatal.",
            posterUrl = "https://picsum.photos/seed/stonehate/400/600",
            backdropUrl = "https://picsum.photos/seed/stonehate-bg/1200/800",
            imdbRating = 7.4, releaseYear = 2023,
            genres = listOf("Action", "Crime"),
            cast = castPool.shuffled(),
            hlsManifestPath = TEST_STREAM_1
        ),
        Title(
            id = "t4", name = "Sonet Might", type = TitleType.MOVIE,
            synopsis = "A washed-up boxer gets one last shot at redemption when he's asked to train the daughter he never knew he had.",
            posterUrl = "https://picsum.photos/seed/sonetmight/400/600",
            backdropUrl = "https://picsum.photos/seed/sonetmight-bg/1200/800",
            imdbRating = 6.9, releaseYear = 2022,
            genres = listOf("Drama", "Sport"),
            hlsManifestPath = TEST_STREAM_2
        ),
        Title(
            id = "t5", name = "Guind Word", type = TitleType.MOVIE,
            synopsis = "A war correspondent embeds with a rebel unit and begins to question which side of the story she's really telling.",
            posterUrl = "https://picsum.photos/seed/guindword/400/600",
            backdropUrl = "https://picsum.photos/seed/guindword-bg/1200/800",
            imdbRating = 7.7, releaseYear = 2025,
            genres = listOf("War", "Drama"),
            hlsManifestPath = TEST_STREAM_1
        ),
        Title(
            id = "t6", name = "Hot Ivy", type = TitleType.SERIES,
            synopsis = "A rookie undercover agent infiltrates a high-society heist ring — and starts to enjoy it more than she should.",
            posterUrl = "https://picsum.photos/seed/hotivy/400/600",
            backdropUrl = "https://picsum.photos/seed/hotivy-bg/1200/800",
            imdbRating = 7.0, releaseYear = 2024,
            genres = listOf("Crime", "Comedy"),
            is4kHdr = true,
            hlsManifestPath = TEST_STREAM_2,
            episodes = episodesForDogMan.map { it.copy(id = "hi-${it.id}") }
        )
    ) + animeTitles

    init {
        progress["t1"] = 20 * 60 to 45 * 60
    }

    override fun getHomeRails(): Flow<List<Rail>> = flow {
        delay(200) // simulate network
        val webSeriesTitles = allTitles.filter { it.type == TitleType.SERIES && !it.genres.contains("Anime") }
        emit(
            listOf(
                Rail("continue-watching", "Continue Watching", getContinueWatching()),
                Rail("web-series", "Web Series & TV Shows", webSeriesTitles),
                Rail("trending", "Trending Now", allTitles),
                Rail("popular", "Popular on StreamFlow", allTitles.shuffled()),
                Rail("anime-universe", "Anime World", animeTitles)
            )
        )
    }

    override suspend fun getTitle(id: String, forceRefresh: Boolean): Title? {
        delay(150)
        val base = allTitles.find { it.id == id } ?: return null
        val (pos, dur) = progress[id] ?: (0 to (base.episodes.firstOrNull()?.durationSec ?: 0))
        return base.copy(watchedPositionSec = pos, durationSec = if (dur > 0) dur else base.durationSec)
    }

    override suspend fun getSimilarTitles(id: String): List<Title> {
        delay(150)
        return allTitles.filterNot { it.id == id }.shuffled().take(6)
    }

    override suspend fun search(query: String): List<Title> {
        delay(150)
        if (query.isBlank()) return emptyList()
        return allTitles.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.genres.any { g -> g.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun getContinueWatching(): List<Title> =
        allTitles.filter { progress.containsKey(it.id) }

    override suspend fun addToWatchlist(titleId: String) {
        watchlist.add(titleId)
    }

    override suspend fun removeFromWatchlist(titleId: String) {
        watchlist.remove(titleId)
    }

    override suspend fun getWatchlist(): List<Title> =
        allTitles.filter { watchlist.contains(it.id) }

    override suspend fun updateProgress(titleId: String, episodeId: String?, positionSec: Int, durationSec: Int) {
        progress[titleId] = positionSec to durationSec
    }

    override suspend fun getPlaybackUrl(titleId: String, episodeId: String?): String {
        delay(100) // simulates POST /api/v1/playback/token round trip
        val title = allTitles.find { it.id == titleId } ?: return TEST_STREAM_1
        if (episodeId != null) {
            return title.episodes.find { it.id == episodeId }?.hlsPath ?: title.hlsManifestPath
        }
        return title.hlsManifestPath
    }

    override suspend fun submitReport(titleId: String, titleName: String, issueType: String, details: String): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }
}
