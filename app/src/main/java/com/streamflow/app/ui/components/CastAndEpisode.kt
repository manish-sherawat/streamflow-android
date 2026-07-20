package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.streamflow.app.data.model.Episode
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.Sizes
import com.streamflow.app.ui.theme.StreamFlowType

@Composable
fun CastAvatar(
    photoUrl: String,
    name: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(72.dp)
    ) {
        AsyncImage(
            model = photoUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(Sizes.castAvatar)
                .clip(CircleShape)
        )
        Text(
            text = name,
            style = StreamFlowType.caption,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun EpisodeCard(
    episode: Episode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.width(200.dp).clickable(onClick = onClick)) {
        Box {
            AsyncImage(
                model = episode.thumbUrl,
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            if (episode.progressFraction > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                        .padding(horizontal = 6.dp)
                        .background(Divider)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(episode.progressFraction)
                            .height(3.dp)
                            .background(AccentPrimary)
                    )
                }
            }
        }
        Text(
            text = "S${episode.seasonNumber}: E${episode.episodeNumber} \u201c${episode.title}\u201d",
            style = StreamFlowType.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
