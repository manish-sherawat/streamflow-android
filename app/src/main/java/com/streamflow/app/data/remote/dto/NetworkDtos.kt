package com.streamflow.app.data.remote.dto

import com.streamflow.app.data.model.CastMember
import com.streamflow.app.data.model.Episode
import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.model.TitleType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CastMemberDto(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("role") val role: String = ""
) {
    fun toDomain(): CastMember = CastMember(
        id = id,
        name = name,
        photoUrl = photoUrl,
        role = role
    )
}

@Serializable
data class EpisodeDto(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("season_number") val seasonNumber: Int = 1,
    @SerialName("seasonNumber") val altSeasonNumber: Int? = null,
    @SerialName("episode_number") val episodeNumber: Int = 1,
    @SerialName("episodeNumber") val altEpisodeNumber: Int? = null,
    @SerialName("duration_sec") val durationSec: Int = 0,
    @SerialName("durationSec") val altDurationSec: Int? = null,
    @SerialName("thumb_url") val thumbUrl: String = "",
    @SerialName("thumbUrl") val altThumbUrl: String? = null,
    @SerialName("hls_path") val hlsPath: String = "",
    @SerialName("hlsPath") val altHlsPath: String? = null,
    @SerialName("download_url") val downloadUrl: String = "",
    @SerialName("downloadUrl") val altDownloadUrl: String? = null,
    @SerialName("watched_position_sec") val watchedPositionSec: Int = 0
) {
    fun toDomain(): Episode = Episode(
        id = id,
        title = title,
        seasonNumber = altSeasonNumber ?: seasonNumber,
        episodeNumber = altEpisodeNumber ?: episodeNumber,
        durationSec = altDurationSec ?: durationSec,
        thumbUrl = (altThumbUrl ?: thumbUrl).ifEmpty { "" },
        hlsPath = (altHlsPath ?: hlsPath).ifEmpty { "" },
        downloadUrl = (altDownloadUrl ?: downloadUrl).ifEmpty { "" },
        watchedPositionSec = watchedPositionSec
    )
}

@Serializable
data class StreamLinkDto(
    @SerialName("id") val id: String = "",
    @SerialName("server_name") val serverName: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("quality") val quality: String = "1080p",
    @SerialName("is_hls") val isHls: Boolean = true
)

@Serializable
data class TitleDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String = "MOVIE", // MOVIE or SERIES
    @SerialName("synopsis") val synopsis: String = "",
    @SerialName("poster_url") val posterUrl: String = "",
    @SerialName("backdrop_url") val backdropUrl: String = "",
    @SerialName("imdb_rating") val imdbRating: Double = 0.0,
    @SerialName("release_year") val releaseYear: Int = 2024,
    @SerialName("genres") val genres: List<String> = emptyList(),
    @SerialName("cast") val cast: List<CastMemberDto> = emptyList(),
    @SerialName("has_subtitles") val hasSubtitles: Boolean = true,
    @SerialName("is_4k_hdr") val is4kHdr: Boolean = true,
    @SerialName("hls_manifest_path") val hlsManifestPath: String = "",
    @SerialName("episodes") val episodes: List<EpisodeDto> = emptyList(),
    @SerialName("stream_links") val streamLinks: List<StreamLinkDto> = emptyList(),
    @SerialName("watched_position_sec") val watchedPositionSec: Int = 0,
    @SerialName("duration_sec") val durationSec: Int = 0
) {
    fun toDomain(): Title {
        val resolvedHlsPath = hlsManifestPath.ifEmpty {
            streamLinks.firstOrNull()?.url ?: ""
        }
        return Title(
            id = id,
            name = name,
            type = if (type.equals("SERIES", ignoreCase = true)) TitleType.SERIES else TitleType.MOVIE,
            synopsis = synopsis,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            imdbRating = imdbRating,
            releaseYear = releaseYear,
            genres = genres,
            cast = cast.map { it.toDomain() },
            hasSubtitles = hasSubtitles,
            is4kHdr = is4kHdr,
            hlsManifestPath = resolvedHlsPath,
            episodes = episodes.map { it.toDomain() },
            watchedPositionSec = watchedPositionSec,
            durationSec = durationSec
        )
    }
}

@Serializable
data class RailDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("titles") val titles: List<TitleDto> = emptyList()
) {
    fun toDomain(): Rail = Rail(
        id = id,
        title = title,
        titles = titles.map { it.toDomain() }
    )
}

@Serializable
data class CatalogResponseDto(
    @SerialName("featured") val featured: List<TitleDto> = emptyList(),
    @SerialName("rails") val rails: List<RailDto> = emptyList()
)

@Serializable
data class WatchlistRequestDto(
    @SerialName("title_id") val titleId: String,
    @SerialName("action") val action: String // "add" or "remove"
)
