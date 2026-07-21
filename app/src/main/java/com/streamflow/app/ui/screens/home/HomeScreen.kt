package com.streamflow.app.ui.screens.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.streamflow.app.data.model.Rail
import com.streamflow.app.data.model.Title
import com.streamflow.app.ui.components.GlassPill
import com.streamflow.app.ui.components.PosterCard
import com.streamflow.app.ui.components.PrimaryButton
import com.streamflow.app.ui.components.SecondaryButton
import com.streamflow.app.ui.components.HeroShimmerSkeleton
import com.streamflow.app.ui.components.RailShimmerSkeleton
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.AccentStar
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary
import com.streamflow.app.ui.theme.TextMuted

@Composable
fun HomeScreen(
    onTitleClick: (Title) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgBase)) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Column(modifier = Modifier.statusBarsPadding().padding(top = 16.dp)) {
                    HeroShimmerSkeleton()
                    Spacer(modifier = Modifier.height(Spacing.md))
                    repeat(2) { RailShimmerSkeleton() }
                }
            }
            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    style = StreamFlowType.body,
                    color = TextSecondary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(Spacing.md)
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
    val heroTitles = remember(rails) {
        rails.flatMap { it.titles }.distinctBy { it.id }.take(5)
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Movies", "TV Shows", "Web Series", "Anime", "Trending")

    val filteredRails = remember(selectedCategory, rails) {
        if (selectedCategory == "All") rails
        else rails.filter { rail ->
            rail.title.contains(selectedCategory, ignoreCase = true) ||
                    rail.titles.any { t -> t.genres.any { g -> g.contains(selectedCategory, ignoreCase = true) } }
        }.ifEmpty { rails }
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Top Header with status bars safe space
        item {
            TopBrandBar()
        }

        // Category chips
        item {
            CategoryChipsRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelect = { selectedCategory = it }
            )
        }

        // Redesigned Hero Carousel
        if (heroTitles.isNotEmpty() && selectedCategory == "All") {
            item {
                Spacer(modifier = Modifier.height(Spacing.xs))
                HeroCarousel(heroTitles = heroTitles, onTitleClick = onTitleClick)
            }
        }

        // Content Rails (Web Series, Trending, Popular, Anime World at bottom)
        items(filteredRails, key = { it.id }, contentType = { "rail" }) { rail ->
            Spacer(modifier = Modifier.height(Spacing.lg))
            val isAnimeRail = rail.id == "anime-universe" || rail.id.contains("anime", ignoreCase = true) || rail.title.contains("anime", ignoreCase = true)
            if (isAnimeRail) {
                AnimeSection(rail = rail, onTitleClick = onTitleClick, onSeeAllClick = { selectedCategory = "Anime" })
            } else {
                val targetCategory = when {
                    rail.id == "trending" || rail.title.contains("Trending", ignoreCase = true) -> "Trending"
                    rail.id == "bollywood" || rail.title.contains("Bollywood", ignoreCase = true) -> "Movies"
                    rail.id == "hollywood" || rail.title.contains("Hollywood", ignoreCase = true) -> "Movies"
                    rail.id == "web-series" || rail.title.contains("Series", ignoreCase = true) -> "TV Shows"
                    else -> "All"
                }
                RailSection(rail = rail, onTitleClick = onTitleClick, onSeeAllClick = { selectedCategory = targetCategory })
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HeroCarousel(
    heroTitles: List<Title>,
    onTitleClick: (Title) -> Unit
) {
    if (heroTitles.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { heroTitles.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            if (heroTitles.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % heroTitles.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val title = heroTitles[page]
            HeroBanner(title = title, onClick = { onTitleClick(title) })
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // Animated dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(heroTitles.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(if (isSelected) 24.dp else 6.dp)
                        .clip(Radius.pill)
                        .background(
                            if (isSelected) AccentPrimary
                            else GlassBorder
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
    }
}

@Composable
private fun TopBrandBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentPrimary, Color(0xFF8A2BE2))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "STREAMFLOW",
                style = StreamFlowType.brandTitle,
                color = TextPrimary
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            // VIP premium badge with gradient border
            Box(
                modifier = Modifier
                    .clip(Radius.chip)
                    .background(Color(0xFF1E1C28))
                    .border(1.dp, Brush.horizontalGradient(listOf(AccentStar, Color(0xFFFF8C00))), Radius.chip)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "✦ VIP PRO",
                    style = StreamFlowType.caption.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = AccentStar
                )
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        items(categories) { cat ->
            val isSelected = cat == selectedCategory
            Text(
                text = cat,
                style = StreamFlowType.pillLabel.copy(
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier
                    .clip(Radius.chip)
                    .background(
                        if (isSelected) Brush.horizontalGradient(
                            listOf(AccentPrimary, Color(0xFF6B21A8))
                        ) else Brush.linearGradient(
                            listOf(BgCard, BgCard)
                        )
                    )
                    .border(0.5.dp, if (isSelected) AccentPrimary.copy(alpha = 0.5f) else GlassBorder, Radius.chip)
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }
    }
}

@Composable
private fun HeroBanner(title: Title, onClick: () -> Unit) {
    var isBookmarked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(410.dp)
            .padding(horizontal = Spacing.md)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) {
        // Backdrop Image
        AsyncImage(
            model = title.backdropUrl,
            contentDescription = title.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Multi-stage cinematic ambient gradient vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f  to Color.Black.copy(alpha = 0.40f),
                            0.35f to Color.Transparent,
                            0.60f to BgBase.copy(alpha = 0.65f),
                            0.88f to BgBase.copy(alpha = 0.95f),
                            1.0f  to BgBase
                        )
                    )
                )
        )

        // Top Trending / Highlight Tag
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(Radius.chip)
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF4500), Color(0xFFFF8C00))))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "#1 SPOTLIGHT",
                        style = StreamFlowType.caption.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = Color.White
                    )
                }
            }

            if (title.is4kHdr) {
                GlassPill(label = "ULTRA 4K HDR", isHighlighted = false)
            }
        }

        // Content overlay bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.md)
        ) {
            Text(
                text = title.name,
                style = StreamFlowType.displayTitle.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))

            // Metadata info row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                if (title.imdbRating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), Radius.chip)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = AccentStar,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = " ${title.imdbRating}",
                            style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                            color = AccentStar
                        )
                    }
                }
                GlassPill(label = "${title.releaseYear}")
                if (title.genres.isNotEmpty()) GlassPill(label = title.genres.first())
                GlassPill(label = if (title.type.name == "SERIES") "Web Series" else "Movie")
            }

            Spacer(Modifier.height(8.dp))

            // Synopsis preview string
            Text(
                text = title.synopsis,
                style = StreamFlowType.caption.copy(
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = TextSecondary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(Spacing.md))

            // CTA Button Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryButton(
                    label = "Watch Now",
                    icon = Icons.Filled.PlayArrow,
                    onClick = onClick,
                    modifier = Modifier.weight(1.2f)
                )
                SecondaryButton(
                    label = if (isBookmarked) "Saved" else "My List",
                    icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    onClick = { isBookmarked = !isBookmarked },
                    modifier = Modifier.weight(0.8f)
                )
            }
        }
    }
}

/**
 * Feature Highlights Section on Home Screen
 */
@Composable
private fun FeatureHighlightsSection(rail: Rail, onTitleClick: (Title) -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp, 16.dp)
                        .clip(Radius.pill)
                        .background(AccentPrimary)
                )
                Text(
                    text = "FEATURE HIGHLIGHTS",
                    style = StreamFlowType.sectionHeader.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextPrimary
                )
            }
            Text(
                text = "Explore All",
                style = StreamFlowType.caption,
                color = AccentPrimary
            )
        }
        Spacer(Modifier.height(Spacing.sm))

        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(rail.titles, key = { "fh_${it.id}" }) { title ->
                FeatureHighlightCard(title = title, onClick = { onTitleClick(title) })
            }
        }
    }
}

@Composable
private fun FeatureHighlightCard(title: Title, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(165.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = title.backdropUrl,
            contentDescription = title.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.2f),
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Top tag badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(Radius.chip)
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = if (title.genres.contains("Anime")) "✨ ANIME SPOTLIGHT" else "⭐ FEATURED PICK",
                style = StreamFlowType.caption.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentStar
                )
            )
        }

        // Bottom title info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = title.name,
                style = StreamFlowType.body.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (title.imdbRating > 0) {
                    Text(
                        text = "★ ${title.imdbRating}",
                        style = StreamFlowType.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = AccentStar
                    )
                }
                Text(
                    text = "• ${title.genres.firstOrNull() ?: "StreamFlow"}",
                    style = StreamFlowType.caption.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
            }
        }

        // Play Button Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(AccentPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Anime Section (Placed right after Web Series)
 */
@Composable
private fun AnimeSection(
    rail: Rail,
    onTitleClick: (Title) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp, 16.dp)
                        .clip(Radius.pill)
                        .background(Color(0xFFFF007F)) // Anime Vibrant Pink/Magenta
                )
                Text(
                    text = "ANIME WORLD ⚡",
                    style = StreamFlowType.sectionHeader.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextPrimary
                )
            }
            Text(
                text = "See all",
                style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFF007F),
                modifier = Modifier
                    .clip(Radius.chip)
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(rail.titles, key = { "anime_${it.id}" }, contentType = { "poster" }) { title ->
                PosterCard(
                    posterUrl = title.posterUrl,
                    titleLabel = title.name,
                    ratingLabel = if (title.imdbRating > 0) "★ ${title.imdbRating}" else null,
                    onClick = { onTitleClick(title) }
                )
            }
        }
    }
}

@Composable
private fun RailSection(
    rail: Rail,
    onTitleClick: (Title) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    val isTrending = rail.id == "trending" || rail.title.contains("Trending", ignoreCase = true)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 0.dp)
        ) {
            Text(
                text = if (isTrending) "🔥 TOP 10 TRENDING TODAY" else rail.title,
                style = StreamFlowType.sectionHeader,
                color = TextPrimary
            )
            Text(
                text = "See all",
                style = StreamFlowType.caption.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary,
                modifier = Modifier
                    .clip(Radius.chip)
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(Spacing.xs))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            itemsIndexed(rail.titles, key = { _, title -> title.id }, contentType = { _, _ -> "poster" }) { index, title ->
                PosterCard(
                    posterUrl = title.posterUrl,
                    titleLabel = title.name,
                    ratingLabel = if (title.imdbRating > 0) "${title.imdbRating}" else null,
                    rankBadge = if (isTrending) index + 1 else null,
                    onClick = { onTitleClick(title) }
                )
            }
        }
    }
}

