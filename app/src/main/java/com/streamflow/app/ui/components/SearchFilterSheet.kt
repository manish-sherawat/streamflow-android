package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.streamflow.app.ui.screens.search.SearchFilter
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun SearchFilterSheet(
    currentFilter: SearchFilter,
    onApplyFilter: (SearchFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }

    val contentTypes = listOf("All", "Movies", "TV Shows", "Anime")
    val ratingOptions = listOf(0f to "Any Rating", 7.0f to "⭐ 7.0+", 8.0f to "⭐ 8.0+")
    val genres = listOf("All", "Action", "Sci-Fi", "Drama", "Anime", "Romance", "Comedy", "Thriller")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.card)
                .background(BgElevated)
                .border(1.dp, GlassBorder, Radius.card)
                .padding(Spacing.lg)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Search Filters",
                        style = StreamFlowType.sheetHeader.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BgCard)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // 1. Content Type Filter
            Text(
                text = "CONTENT TYPE",
                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Spacer(Modifier.height(Spacing.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(contentTypes) { type ->
                    val isSelected = tempFilter.contentType == type
                    Text(
                        text = type,
                        style = StreamFlowType.pillLabel,
                        color = if (isSelected) Color.White else TextPrimary,
                        modifier = Modifier
                            .clip(Radius.pill)
                            .background(if (isSelected) AccentPrimary else BgCard)
                            .border(1.dp, if (isSelected) AccentPrimary else GlassBorder, Radius.pill)
                            .clickable { tempFilter = tempFilter.copy(contentType = type) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // 2. Minimum Rating Filter
            Text(
                text = "IMDB RATING THRESHOLD",
                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Spacer(Modifier.height(Spacing.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(ratingOptions) { (ratingVal, label) ->
                    val isSelected = tempFilter.minRating == ratingVal
                    Text(
                        text = label,
                        style = StreamFlowType.pillLabel,
                        color = if (isSelected) Color.White else TextPrimary,
                        modifier = Modifier
                            .clip(Radius.pill)
                            .background(if (isSelected) AccentPrimary else BgCard)
                            .border(1.dp, if (isSelected) AccentPrimary else GlassBorder, Radius.pill)
                            .clickable { tempFilter = tempFilter.copy(minRating = ratingVal) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // 3. Genre Filter
            Text(
                text = "GENRE",
                style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Spacer(Modifier.height(Spacing.xs))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(genres) { g ->
                    val isSelected = tempFilter.selectedGenre == g
                    Text(
                        text = g,
                        style = StreamFlowType.pillLabel,
                        color = if (isSelected) Color.White else TextPrimary,
                        modifier = Modifier
                            .clip(Radius.pill)
                            .background(if (isSelected) AccentPrimary else BgCard)
                            .border(1.dp, if (isSelected) AccentPrimary else GlassBorder, Radius.pill)
                            .clickable { tempFilter = tempFilter.copy(selectedGenre = g) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Action buttons: Reset & Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                SecondaryButton(
                    label = "Reset",
                    onClick = {
                        tempFilter = SearchFilter()
                    },
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    label = "Apply Filters",
                    onClick = {
                        onApplyFilter(tempFilter)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}
