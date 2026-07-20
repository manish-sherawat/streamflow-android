package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.streamflow.app.data.model.Title
import com.streamflow.app.data.model.TitleType
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@Composable
fun AdminAddTitleDialog(
    onAddTitle: (Title) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TitleType.MOVIE) }
    var posterUrl by remember { mutableStateOf("") }
    var backdropUrl by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("8.5") }
    var releaseYear by remember { mutableStateOf("2024") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(Radius.card)
                .background(BgElevated)
                .border(1.dp, GlassBorder, Radius.card)
                .padding(Spacing.lg)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Admin: Add New Movie / Series", style = StreamFlowType.sheetHeader, color = TextPrimary)
            Text(
                "Save new streaming content directly to live Firestore database",
                style = StreamFlowType.caption,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Title Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )
            Spacer(modifier = Modifier.height(Spacing.xs))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = type == TitleType.MOVIE,
                    onClick = { type = TitleType.MOVIE },
                    colors = RadioButtonDefaults.colors(selectedColor = AccentPrimary)
                )
                Text("Movie", style = StreamFlowType.body, color = TextPrimary)

                RadioButton(
                    selected = type == TitleType.SERIES,
                    onClick = { type = TitleType.SERIES },
                    colors = RadioButtonDefaults.colors(selectedColor = AccentPrimary)
                )
                Text("Series", style = StreamFlowType.body, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(Spacing.xs))

            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("HLS Stream URL (.m3u8)") },
                placeholder = { Text("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )
            Spacer(modifier = Modifier.height(Spacing.xs))

            OutlinedTextField(
                value = posterUrl,
                onValueChange = { posterUrl = it },
                label = { Text("Poster Image URL") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )
            Spacer(modifier = Modifier.height(Spacing.xs))

            OutlinedTextField(
                value = backdropUrl,
                onValueChange = { backdropUrl = it },
                label = { Text("Backdrop Hero Image URL") },
                modifier = Modifier.fillMaxWidth(),
                colors = customTextFieldColors()
            )
            Spacer(modifier = Modifier.height(Spacing.xs))

            Row {
                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it },
                    label = { Text("IMDb Rating") },
                    modifier = Modifier.weight(1f),
                    colors = customTextFieldColors()
                )
                Spacer(modifier = Modifier.padding(horizontal = Spacing.xs))
                OutlinedTextField(
                    value = releaseYear,
                    onValueChange = { releaseYear = it },
                    label = { Text("Release Year") },
                    modifier = Modifier.weight(1f),
                    colors = customTextFieldColors()
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))

            OutlinedTextField(
                value = synopsis,
                onValueChange = { synopsis = it },
                label = { Text("Synopsis / Storyline") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            PrimaryButton(
                label = "Publish to Firestore",
                onClick = {
                    if (name.isNotBlank()) {
                        val newTitle = Title(
                            id = "title_${System.currentTimeMillis()}",
                            name = name,
                            type = type,
                            synopsis = synopsis.ifBlank { "Action-packed streaming title." },
                            posterUrl = posterUrl.ifBlank { "https://picsum.photos/seed/${name.hashCode()}/300/450" },
                            backdropUrl = backdropUrl.ifBlank { "https://picsum.photos/seed/${name.hashCode()}_bg/1280/720" },
                            imdbRating = rating.toDoubleOrNull() ?: 8.5,
                            releaseYear = releaseYear.toIntOrNull() ?: 2024,
                            genres = listOf("Action", "Sci-Fi"),
                            hlsManifestPath = videoUrl.ifBlank { "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8" }
                        )
                        onAddTitle(newTitle)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            SecondaryButton(
                label = "Cancel",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentPrimary,
    unfocusedBorderColor = GlassBorder,
    focusedLabelColor = AccentPrimary,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
