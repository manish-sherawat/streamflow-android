package com.streamflow.app.ui.screens.search

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.streamflow.app.ui.components.SearchFilterSheet
import com.streamflow.app.ui.components.StreamFlowSpinner
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.BgInput
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextMuted
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary
import com.streamflow.app.ui.theme.TextTertiary

import androidx.compose.foundation.layout.statusBarsPadding

@Composable
fun SearchScreen(
    onTitleClick: (Title) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        SearchFilterSheet(
            currentFilter = uiState.filter,
            onApplyFilter = viewModel::setFilter,
            onDismiss = { showFilterSheet = false }
        )
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
                .padding(horizontal = Spacing.md)
                .padding(top = Spacing.sm, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Search",
                style = StreamFlowType.displayTitle,
                color = TextPrimary
            )

            // Filter trigger button
            Box(
                modifier = Modifier
                    .clip(Radius.chip)
                    .background(BgCard)
                    .border(0.5.dp, GlassBorder, Radius.chip)
                    .clickable { showFilterSheet = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filters",
                        tint = AccentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Filter",
                        style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
            }
        }

        // ── Search Bar ───────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = {
                Text(
                    text = "Titles, genres, actors, anime…",
                    style = StreamFlowType.body,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.onQueryChange("") }
                    )
                }
            },
            singleLine = true,
            shape = Radius.input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor   = BgInput,
                unfocusedContainerColor = BgInput,
                focusedBorderColor      = AccentPrimary.copy(alpha = 0.8f),
                unfocusedBorderColor    = GlassBorder,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        // ── Genre Filter Chips ───────────────────────────────────────────────
        var selectedSort by remember { mutableStateOf("All") }
        val sortOptions = listOf("All", "Top Rated", "Anime", "Action", "Web Series", "Sci-Fi", "Drama", "4K UHD")

        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(sortOptions) { option ->
                val isSelected = option == selectedSort
                Text(
                    text = option,
                    style = StreamFlowType.pillLabel.copy(
                        color = if (isSelected) BgBase else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    modifier = Modifier
                        .clip(Radius.chip)
                        .background(
                            if (isSelected) TextPrimary else Color.Transparent
                        )
                        .border(0.5.dp, if (isSelected) Color.Transparent else GlassBorder, Radius.chip)
                        .clickable {
                            selectedSort = option
                            if (option != "All" && option != "Top Rated" && option != "Latest" && option != "4K UHD") {
                                viewModel.onQueryChange(option)
                            }
                        }
                        .padding(horizontal = 13.dp, vertical = 6.dp)
                )
            }
        }

        // ── Recent Searches (Shown when query is empty) ─────────────────────
        var recentSearches by remember { mutableStateOf(listOf("Solo Leveling", "Dog Man", "Cyberpunk", "Hot Ivy")) }

        if (uiState.query.isEmpty() && recentSearches.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.xs))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Text(
                        text = "Clear",
                        style = StreamFlowType.caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = AccentPrimary,
                        modifier = Modifier.clickable { recentSearches = emptyList() }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(recentSearches) { recent ->
                        Box(
                            modifier = Modifier
                                .clip(Radius.chip)
                                .background(BgCard)
                                .border(0.5.dp, GlassBorder, Radius.chip)
                                .clickable { viewModel.onQueryChange(recent) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🕒 $recent",
                                style = StreamFlowType.caption.copy(fontSize = 11.sp),
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // ── Autocomplete Suggestions ─────────────────────────────────────────
        if (uiState.query.length >= 2 && uiState.results.isNotEmpty()) {
            val suggestions = remember(uiState.query, uiState.results) {
                uiState.results.map { it.name }.take(4)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    .clip(Radius.input)
                    .background(BgCard)
                    .padding(vertical = Spacing.xs)
            ) {
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onQueryChange(suggestion) }
                            .padding(horizontal = Spacing.md, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = suggestion,
                            style = StreamFlowType.body,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        // ── Results / State ──────────────────────────────────────────────────
        when {
            uiState.isSearching -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    StreamFlowSpinner(size = 32.dp)
                }
            }
            uiState.results.isNotEmpty() -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(
                        start = Spacing.md,
                        end = Spacing.md,
                        bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    items(uiState.results, key = { it.id }) { title ->
                        PosterCard(
                            posterUrl = title.posterUrl,
                            titleLabel = title.name,
                            ratingLabel = if (title.imdbRating > 0) "${title.imdbRating}" else null,
                            onClick = { onTitleClick(title) },
                            width = 110.dp,
                            height = 163.dp
                        )
                    }
                }
            }
            uiState.query.isNotBlank() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results for \"${uiState.query}\"",
                        style = StreamFlowType.body,
                        color = TextSecondary
                    )
                }
            }
            else -> {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = "Search movies, shows, and more",
                            style = StreamFlowType.body,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
