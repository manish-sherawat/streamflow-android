package com.streamflow.app.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamflow.app.data.model.Title
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextMuted
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun WatchlistScreen(
    onTitleClick: (Title) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val titles by viewModel.watchlist.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My List",
                style = StreamFlowType.displayTitle,
                color = TextPrimary
            )

            if (titles.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(Radius.chip)
                        .background(BgCard)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${titles.size} Saved",
                        style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                        color = AccentPrimary
                    )
                }
            }
        }

        var selectedSort by remember { mutableStateOf("Date Added") }
        val sortedTitles = remember(titles, selectedSort) {
            when (selectedSort) {
                "Highest Rating" -> titles.sortedByDescending { it.imdbRating }
                "Name A-Z" -> titles.sortedBy { it.name }
                else -> titles
            }
        }

        if (titles.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(listOf("Date Added", "Highest Rating", "Name A-Z")) { sortOpt ->
                    val isSelected = sortOpt == selectedSort
                    Text(
                        text = sortOpt,
                        style = StreamFlowType.caption.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier
                            .clip(Radius.chip)
                            .background(if (isSelected) AccentPrimary else BgCard)
                            .clickable { selectedSort = sortOpt }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (titles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(BgCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkBorder,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        text = "Your Watchlist is Empty",
                        style = StreamFlowType.sectionHeader,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = "Tap the bookmark icon on any movie, show, or anime to save it here for later.",
                        style = StreamFlowType.body,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, top = Spacing.sm, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(sortedTitles, key = { it.id }) { title ->
                    PosterCard(
                        posterUrl = title.posterUrl,
                        titleLabel = title.name,
                        ratingLabel = if (title.imdbRating > 0) "${title.imdbRating}" else null,
                        onClick = { onTitleClick(title) },
                        width = 108.dp,
                        height = 160.dp
                    )
                }
            }
        }
    }
}

