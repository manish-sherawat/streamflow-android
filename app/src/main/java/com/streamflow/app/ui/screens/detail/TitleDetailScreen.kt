package com.streamflow.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.streamflow.app.ui.theme.GlassBorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamflow.app.data.model.Episode
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.model.TitleType
import com.streamflow.app.ui.components.DetailShimmerSkeleton
import com.streamflow.app.ui.components.EpisodeCard
import com.streamflow.app.ui.components.GlassPill
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.AccentStar
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun TitleDetailScreen(
    onBack: () -> Unit,
    onPlay: (titleId: String, episodeId: String?) -> Unit,
    onTitleClick: (Title) -> Unit,
    viewModel: TitleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        when (val state = uiState) {
            is TitleDetailUiState.Loading ->
                DetailShimmerSkeleton()
            is TitleDetailUiState.NotFound ->
                Text(
                    "Title not found",
                    style = StreamFlowType.body,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            is TitleDetailUiState.Success ->
                DetailContent(
                    state = state,
                    onBack = onBack,
                    onPlay = onPlay,
                    onToggleWatchlist = viewModel::toggleWatchlist,
                    onTitleClick = onTitleClick
                )
        }
    }
}

@Composable
private fun DetailContent(
    state: TitleDetailUiState.Success,
    onBack: () -> Unit,
    onPlay: (titleId: String, episodeId: String?) -> Unit,
    onToggleWatchlist: () -> Unit,
    onTitleClick: (Title) -> Unit
) {
    val title = state.title
    val isSeries = title.type == TitleType.SERIES

    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        item { Backdrop(title = title, onBack = onBack) }

        item {
            Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
                // Title
                Text(
                    text = title.name,
                    style = StreamFlowType.displayTitle.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary
                )
                Spacer(Modifier.height(Spacing.xs))

                // Metadata chips row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (title.imdbRating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = AccentStar,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = " ${title.imdbRating}",
                                style = StreamFlowType.pillLabel,
                                color = AccentStar,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    GlassPill(label = "${title.releaseYear}")
                    if (title.hasSubtitles) GlassPill(label = "CC")
                    if (title.is4kHdr) GlassPill(label = "4K")
                }
                Spacer(Modifier.height(Spacing.md))

                // Action row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PrimaryButton(
                        label = "Play Now",
                        icon = Icons.Filled.PlayArrow,
                        onClick = { onPlay(title.id, title.episodes.firstOrNull()?.id) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        label = if (state.isInWatchlist) "Saved" else "My List",
                        icon = if (state.isInWatchlist) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        onClick = onToggleWatchlist
                    )
                }
                Spacer(Modifier.height(Spacing.md))

                // Expandable Synopsis
                var isSynopsisExpanded by remember { mutableStateOf(false) }

                Column {
                    Text(
                        text = title.synopsis,
                        style = StreamFlowType.body,
                        color = TextSecondary,
                        maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (title.synopsis.length > 120) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isSynopsisExpanded) "Show Less ▲" else "Read More ▼",
                            style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                            color = AccentPrimary,
                            modifier = Modifier.clickable { isSynopsisExpanded = !isSynopsisExpanded }
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
            }
        }

        // Movie Details Area
        item { SectionHeader("Movie Details") }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md)
                    .clip(Radius.card)
                    .background(BgCard)
                    .border(1.dp, GlassBorder, Radius.card)
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                DetailRow(
                    label = "Genre",
                    value = if (title.genres.isNotEmpty()) title.genres.joinToString(", ") else "Entertainment"
                )
                DetailRow(
                    label = "Quality",
                    value = if (title.is4kHdr) "4K Ultra HD • HDR10 • 2160p" else "Full HD 1080p • 60fps"
                )
                DetailRow(
                    label = "Audio & Language",
                    value = if (title.hasSubtitles) "English (5.1 Surround), Subtitles Available" else "English (Stereo)"
                )
                DetailRow(
                    label = "Release Year",
                    value = "${title.releaseYear}"
                )
                DetailRow(
                    label = "Category",
                    value = if (isSeries) "TV Series • Multi-Season" else "Feature Film"
                )
            }
            Spacer(Modifier.height(Spacing.lg))
        }

        // Episodes Section with Season Selector & View Mode Toggle
        if (isSeries && title.episodes.isNotEmpty()) {
            item {
                var selectedSeason by remember { mutableStateOf("Season 1") }
                var isListView by remember { mutableStateOf(false) }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Episodes",
                            style = StreamFlowType.sectionHeader,
                            color = TextPrimary
                        )

                        // Layout View Toggle Button
                        Box(
                            modifier = Modifier
                                .clip(Radius.chip)
                                .background(BgCard)
                                .clickable { isListView = !isListView }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isListView) " Grid View" else " List View",
                                style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                                color = AccentStar
                            )
                        }
                    }

                    // Season Chips Selector
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        items(listOf("Season 1", "Season 2", "Bonus Content")) { season ->
                            val isSelected = season == selectedSeason
                            Text(
                                text = season,
                                style = StreamFlowType.pillLabel.copy(
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier
                                    .clip(Radius.chip)
                                    .background(if (isSelected) AccentPrimary else BgCard)
                                    .clickable { selectedSeason = season }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xs))

                    if (!isListView) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            items(title.episodes, key = { it.id }) { ep: Episode ->
                                EpisodeCard(episode = ep, onClick = { onPlay(title.id, ep.id) })
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            title.episodes.forEach { ep ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(Radius.card)
                                        .background(BgCard)
                                        .clickable { onPlay(title.id, ep.id) }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(80.dp, 48.dp).clip(Radius.chip)) {
                                        AsyncImage(
                                            model = ep.thumbUrl,
                                            contentDescription = ep.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "E${ep.episodeNumber}. ${ep.title}",
                                            style = StreamFlowType.body.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${ep.durationSec / 60} mins • S${ep.seasonNumber}",
                                            style = StreamFlowType.caption,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.lg))
                }
            }
        }

        // More like this
        if (state.similar.isNotEmpty()) {
            item { SectionHeader("More Like This") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(state.similar, key = { it.id }) { similar ->
                        PosterCard(
                            posterUrl = similar.posterUrl,
                            titleLabel = similar.name,
                            ratingLabel = if (similar.imdbRating > 0) "${similar.imdbRating}" else null,
                            onClick = { onTitleClick(similar) }
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
private fun Backdrop(title: Title, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        AsyncImage(
            model = title.backdropUrl,
            contentDescription = title.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Clean bottom-to-black gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f  to Color.Black.copy(alpha = 0.3f),
                            0.55f to Color.Transparent,
                            1.0f  to BgBase
                        )
                    )
                )
        )

        // Back button — clean circle on scrim with status bars safe space
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(Spacing.md)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = StreamFlowType.sectionHeader,
        color = TextPrimary,
        modifier = Modifier.padding(
            horizontal = Spacing.md,
            vertical = Spacing.xs
        )
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = StreamFlowType.caption.copy(fontSize = 13.sp),
            color = TextSecondary,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            style = StreamFlowType.body.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = TextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
