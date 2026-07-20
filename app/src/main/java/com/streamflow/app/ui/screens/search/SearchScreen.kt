package com.streamflow.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import com.streamflow.app.data.model.Title
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.GlassFill
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextTertiary

@Composable
fun SearchScreen(
    onTitleClick: (Title) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(top = 56.dp)) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Search titles, genres, actors\u2026", style = StreamFlowType.body) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GlassFill,
                    unfocusedContainerColor = GlassFill,
                    focusedBorderColor = GlassBorder,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md)
            )

            when {
                uiState.isSearching -> CircularProgressIndicator(
                    modifier = Modifier.padding(Spacing.lg).align(Alignment.CenterHorizontally)
                )
                uiState.results.isNotEmpty() -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        items(uiState.results, key = { it.id }) { title ->
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
                uiState.query.isNotBlank() -> Text(
                    "No results for \u201c${uiState.query}\u201d",
                    style = StreamFlowType.body,
                    modifier = Modifier.padding(Spacing.md)
                )
            }
        }
    }
}
