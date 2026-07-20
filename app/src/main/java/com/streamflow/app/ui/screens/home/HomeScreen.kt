package com.streamflow.app.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import com.streamflow.app.ui.components.GlassPill
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.RailShimmerSkeleton
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary

@Composable
fun HomeScreen(
    onTitleClick: (Title) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Column(modifier = Modifier.padding(top = 56.dp)) {
                    repeat(3) {
                        RailShimmerSkeleton()
                    }
                }
            }
            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    style = StreamFlowType.body,
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.md)
                )
            }
            is HomeUiState.Success -> {
                HomeContent(rails = state.rails, onTitleClick = onTitleClick)
            }
        }
    }
}

@Composable
private fun HomeContent(rails: List<Rail>, onTitleClick: (Title) -> Unit) {
    val heroTitle = rails.firstOrNull { it.id == "trending" }?.titles?.firstOrNull()
        ?: rails.firstOrNull()?.titles?.firstOrNull()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        if (heroTitle != null) {
            item { HeroBanner(title = heroTitle, onClick = { onTitleClick(heroTitle) }) }
        }
        items(rails, key = { it.id }) { rail ->
            RailSection(rail = rail, onTitleClick = onTitleClick)
        }
    }
}

@Composable
private fun HeroBanner(title: Title, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(460.dp)
    ) {
        AsyncImage(
            model = title.backdropUrl,
            contentDescription = title.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Multi-stage Vignette & Scrim Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BgBase.copy(alpha = 0.4f),
                            BgBase
                        ),
                        startY = 150f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.md)
        ) {
            // Badges row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = AccentPrimary, modifier = Modifier.height(14.dp))
                    Text(
                        text = " ${title.imdbRating}",
                        style = StreamFlowType.pillLabel,
                        color = AccentPrimary
                    )
                }
                GlassPill(label = "${title.releaseYear}")
                GlassPill(label = "4K HDR")
                if (title.genres.isNotEmpty()) {
                    GlassPill(label = title.genres.first())
                }
            }

            Spacer(Modifier.height(Spacing.xs))
            Text(text = title.name.uppercase(), style = StreamFlowType.displayTitle, color = TextPrimary)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = title.synopsis,
                style = StreamFlowType.body,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(0.95f)
            )

            Spacer(Modifier.height(Spacing.md))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryButton(
                    label = "Watch Now",
                    icon = Icons.Filled.PlayArrow,
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    label = "My List",
                    icon = Icons.Filled.Add,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun RailSection(rail: Rail, onTitleClick: (Title) -> Unit) {
    Column {
        Text(
            text = rail.title,
            style = StreamFlowType.sectionHeader,
            modifier = Modifier.padding(horizontal = Spacing.md)
        )
        Spacer(Modifier.height(Spacing.xs))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(rail.titles, key = { it.id }) { title ->
                PosterCard(
                    posterUrl = title.posterUrl,
                    titleLabel = title.name,
                    onClick = { onTitleClick(title) }
                )
            }
        }
    }
}
