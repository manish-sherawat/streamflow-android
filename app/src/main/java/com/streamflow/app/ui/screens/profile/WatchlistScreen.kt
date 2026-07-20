package com.streamflow.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.streamflow.app.data.model.Title
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType

@Composable
fun WatchlistScreen(
    onTitleClick: (Title) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val titles by viewModel.watchlist.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        if (titles.isEmpty()) {
            Text(
                "Your list is empty. Tap the bookmark icon on any title to save it here.",
                style = StreamFlowType.body,
                modifier = Modifier.align(Alignment.Center).padding(Spacing.lg)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(top = 56.dp, start = Spacing.md, end = Spacing.md, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                items(titles, key = { it.id }) { title ->
                    PosterCard(
                        posterUrl = title.posterUrl,
                        titleLabel = title.name,
                        onClick = { onTitleClick(title) },
                        width = 108.dp,
                        height = 160.dp
                    )
                }
            }
        }
    }
}
