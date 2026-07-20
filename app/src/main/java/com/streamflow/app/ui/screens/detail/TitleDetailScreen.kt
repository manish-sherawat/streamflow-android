package com.streamflow.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.streamflow.app.data.model.Episode
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.model.TitleType
import com.streamflow.app.ui.components.CastAvatar
import com.streamflow.app.ui.components.EpisodeCard
import com.streamflow.app.ui.components.GlassPill
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType

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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is TitleDetailUiState.NotFound ->
                Text("Title not found", style = StreamFlowType.body, modifier = Modifier.align(Alignment.Center))
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

    LazyColumn(contentPadding = PaddingValues(bottom = Spacing.xl)) {
        item { Backdrop(title = title, onBack = onBack) }

        item {
            Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)) {
                Text(text = title.name.uppercase(), style = StreamFlowType.displayTitle)
                Spacer(Modifier.height(Spacing.sm))

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    GlassPill(label = "IMDb ${title.imdbRating}")
                    if (title.hasSubtitles) GlassPill(label = "Subtitles")
                    if (title.is4kHdr) GlassPill(label = "4K Ultra HD")
                }
                Spacer(Modifier.height(Spacing.sm))

                SecondaryButton(
                    label = if (state.isInWatchlist) "In My List" else "Add to My List",
                    icon = if (state.isInWatchlist) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    onClick = onToggleWatchlist
                )
                Spacer(Modifier.height(Spacing.md))

                Text(text = title.synopsis, style = StreamFlowType.body)
                Spacer(Modifier.height(Spacing.lg))

                PrimaryButton(
                    label = "Play",
                    icon = Icons.Filled.PlayArrow,
                    onClick = { onPlay(title.id, title.episodes.firstOrNull()?.id) }
                )
            }
        }

        if (title.cast.isNotEmpty()) {
            item { SectionHeader("Actors") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(title.cast, key = { it.id }) { cast ->
                        CastAvatar(photoUrl = cast.photoUrl, name = cast.name)
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
            }
        }

        if (isSeries && title.episodes.isNotEmpty()) {
            item { SectionHeader("Other Episodes") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(title.episodes, key = { it.id }) { ep: Episode ->
                        EpisodeCard(episode = ep, onClick = { onPlay(title.id, ep.id) })
                    }
                }
                Spacer(Modifier.height(Spacing.lg))
            }
        }

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
                            onClick = { onTitleClick(similar) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Backdrop(title: Title, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
        AsyncImage(
            model = title.backdropUrl,
            contentDescription = title.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, BgBase), startY = 300f))
        )
        Box(
            modifier = Modifier
                .padding(Spacing.md)
                .size(40.dp)
                .background(BgElevated, CircleShape)
                .border(1.dp, GlassBorder, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = StreamFlowType.sectionHeader,
        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
    )
}
