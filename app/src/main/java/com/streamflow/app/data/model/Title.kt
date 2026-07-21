package com.streamflow.app.data.model

enum class TitleType { MOVIE, SERIES }

data class CastMember(
    val id: String,
    val name: String,
    val photoUrl: String,
    val role: String = ""
)

data class Episode(
    val id: String,
    val title: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val durationSec: Int,
    val thumbUrl: String,
    val hlsPath: String,
    val downloadUrl: String = "",
    val watchedPositionSec: Int = 0
) {
    val progressFraction: Float
        get() = if (durationSec == 0) 0f else (watchedPositionSec.toFloat() / durationSec).coerceIn(0f, 1f)
}

data class Title(
    val id: String,
    val name: String,
    val type: TitleType,
    val synopsis: String,
    val posterUrl: String,
    val backdropUrl: String,
    val imdbRating: Double,
    val releaseYear: Int,
    val genres: List<String> = emptyList(),
    val cast: List<CastMember> = emptyList(),
    val hasSubtitles: Boolean = true,
    val is4kHdr: Boolean = true,
    val hlsManifestPath: String = "",
    val episodes: List<Episode> = emptyList(), // populated for SERIES
    val watchedPositionSec: Int = 0,
    val durationSec: Int = 0
)

data class Rail(
    val id: String,
    val title: String,
    val titles: List<Title>
)
