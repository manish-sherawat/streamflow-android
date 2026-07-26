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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Spacing
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextMuted
import com.streamflow.app.ui.theme.TextPrimary
import com.streamflow.app.ui.theme.TextSecondary

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTitleClick: (Title) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val watchlistIds by viewModel.watchlistIds.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val smartResumeTitle by viewModel.smartResumeTitle.collectAsState()

    val pullToRefreshState = androidx.compose.material3.pulltorefresh.rememberPullToRefreshState()

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) viewModel.refreshHome()
    }
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) pullToRefreshState.startRefresh() else pullToRefreshState.endRefresh()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.md)
                )
            }
            is HomeUiState.Success -> {
                HomeContent(
                    rails = state.rails,
                    watchlistIds = watchlistIds,
                    onTitleClick = onTitleClick,
                    onToggleWatchlist = viewModel::toggleWatchlist
                )
            }
        }

        androidx.compose.material3.pulltorefresh.PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = BgCard,
            contentColor = AccentPrimary
        )

        // ── Update Dialog ─────────────────────────────────────────────────────
        val updateState by viewModel.updateState.collectAsState()
        val uriHandler = LocalUriHandler.current
        updateState?.let { update ->
            if (update.hasUpdate) {
                Dialog(onDismissRequest = { viewModel.dismissUpdateModal() }) {
                    Surface(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = BgElevated,
                        tonalElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Icon(
                                Icons.Filled.SystemUpdate,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Update Available (${update.latestVersion})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = update.changelog,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SecondaryButton(
                                    label = "Later",
                                    onClick = { viewModel.dismissUpdateModal() },
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryButton(
                                    label = "Update",
                                    icon = Icons.Filled.SystemUpdate,
                                    onClick = {
                                        uriHandler.openUri(update.downloadUrl)
                                        viewModel.dismissUpdateModal()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Smart Resume Banner ───────────────────────────────────────────────
        smartResumeTitle?.let { title ->
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = BgElevated,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
                    .clickable { onTitleClick(title) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Resume Watching",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = title.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    FilledTonalIconButton(
                        onClick = { viewModel.dismissSmartResume() },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = BgCard,
                            contentColor = TextSecondary
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    rails: List<Rail>,
    watchlistIds: Set<String>,
    onTitleClick: (Title) -> Unit,
    onToggleWatchlist: (String) -> Unit
) {
    val heroTitles = remember(rails) {
        val featuredRail = rails.find { it.id == "featured" || it.title.contains("Featured", ignoreCase = true) }
        val items = (featuredRail?.titles ?: rails.flatMap { it.titles })
        items.distinctBy { it.id }.take(5)
    }

    var selectedCategory by remember { mutableStateOf("All") }
    var seeAllRail by remember { mutableStateOf<Rail?>(null) }
    val categories = listOf("All", "Movies", "TV Shows", "Web Series", "Anime", "Trending")

    val activeRails = remember(rails) {
        rails.filterNot {
            it.id == "featured" ||
                it.title.contains("Featured Highlights", ignoreCase = true) ||
                it.title.contains("Feature Highlights", ignoreCase = true)
        }
    }

    val categoryTitles = remember(selectedCategory, rails) {
        if (selectedCategory == "All") emptyList()
        else {
            val matchedRails = activeRails.filter { rail ->
                rail.title.contains(selectedCategory, ignoreCase = true) ||
                    rail.titles.any { t -> t.genres.any { g -> g.contains(selectedCategory, ignoreCase = true) } }
            }.ifEmpty { activeRails }
            matchedRails.flatMap { it.titles }.distinctBy { it.id }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBrandBar()
            CategoryChipsRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            if (selectedCategory == "All") {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 110.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (heroTitles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            HeroCarousel(
                                heroTitles = heroTitles,
                                watchlistIds = watchlistIds,
                                onTitleClick = onTitleClick,
                                onToggleWatchlist = onToggleWatchlist
                            )
                        }
                    }

                    items(activeRails, key = { it.id }, contentType = { "rail" }) { rail ->
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        if (rail.id == "continue-watching") {
                            ContinueWatchingSection(rail = rail, onTitleClick = onTitleClick)
                        } else {
                            val isAnimeRail = rail.id == "anime-universe" ||
                                rail.id.contains("anime", ignoreCase = true) ||
                                rail.title.contains("anime", ignoreCase = true)
                            if (isAnimeRail) {
                                AnimeSection(rail = rail, onTitleClick = onTitleClick, onSeeAllClick = { seeAllRail = rail })
                            } else {
                                RailSection(rail = rail, onTitleClick = onTitleClick, onSeeAllClick = { seeAllRail = rail })
                            }
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    ) {
                        Text(
                            text = selectedCategory,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = Radius.chip,
                            color = AccentPrimary.copy(alpha = 0.14f),
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Text(
                                text = "${categoryTitles.size} titles",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val isTrending = selectedCategory.equals("Trending", ignoreCase = true)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(
                            start = Spacing.md, end = Spacing.md,
                            bottom = 120.dp, top = Spacing.xs
                        ),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            categoryTitles,
                            key = { index, title -> "cat_grid_${title.id}_$index" }
                        ) { index, title ->
                            PosterCard(
                                posterUrl = title.posterUrl,
                                titleLabel = title.name,
                                ratingLabel = if (title.imdbRating > 0) "${title.imdbRating}" else null,
                                rankBadge = if (isTrending) index + 1 else null,
                                onClick = { onTitleClick(title) },
                                width = 110.dp,
                                height = 163.dp
                            )
                        }
                    }
                }
            }
        }

        seeAllRail?.let { rail ->
            SeeAllWindowDialog(
                rail = rail,
                onTitleClick = onTitleClick,
                onDismiss = { seeAllRail = null }
            )
        }
    }
}

// ── Hero Carousel ──────────────────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HeroCarousel(
    heroTitles: List<Title>,
    watchlistIds: Set<String>,
    onTitleClick: (Title) -> Unit,
    onToggleWatchlist: (String) -> Unit
) {
    if (heroTitles.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { heroTitles.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000)
            if (heroTitles.isNotEmpty()) {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % heroTitles.size)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val title = heroTitles[page]
            HeroBanner(
                title = title,
                isBookmarked = watchlistIds.contains(title.id),
                onClick = { onTitleClick(title) },
                onToggleWatchlist = { onToggleWatchlist(title.id) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Animated pill indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(heroTitles.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by androidx.compose.animation.core.animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 5.dp,
                    animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 300f),
                    label = "indicatorWidth_$index"
                )
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(width)
                        .clip(Radius.pill)
                        .background(
                            if (isSelected) AccentPrimary else TextMuted.copy(alpha = 0.45f)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
    }
}

// ── Top Brand Bar ──────────────────────────────────────────────────────────────
@Composable
private fun TopBrandBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBase)
            .border(width = 0.5.dp, color = GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "STREAMFLOW",
                    style = StreamFlowType.brandTitle,
                    color = TextPrimary
                )
            }
        }
    }
}

// ── Category Chips Row ─────────────────────────────────────────────────────────
@Composable
private fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat == selectedCategory
            val bgColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) AccentPrimary else BgCard,
                animationSpec = androidx.compose.animation.core.tween(220),
                label = "chipBg_$cat"
            )
            val textColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) Color.White else TextSecondary,
                animationSpec = androidx.compose.animation.core.tween(220),
                label = "chipText_$cat"
            )
            Text(
                text = cat,
                style = StreamFlowType.pillLabel.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = textColor,
                modifier = Modifier
                    .clip(Radius.pill)
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 0.dp else 0.5.dp,
                        color = GlassBorder,
                        shape = Radius.pill
                    )
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

// ── Hero Banner ────────────────────────────────────────────────────────────────
@Composable
private fun HeroBanner(
    title: Title,
    isBookmarked: Boolean,
    onClick: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .padding(horizontal = Spacing.lg)
                .clip(RoundedCornerShape(20.dp))
        ) {
            AsyncImage(
                model = title.posterUrl,
                contentDescription = title.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Cinematic vignette — no colored ambient glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f  to Color.Black.copy(alpha = 0.30f),
                                0.38f to Color.Transparent,
                                0.60f to BgBase.copy(alpha = 0.55f),
                                0.85f to BgBase.copy(alpha = 0.92f),
                                1.0f  to BgBase
                            )
                        )
                    )
            )

            // Trending badge
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
                            Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "#1 SPOTLIGHT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White
                        )
                    }
                }
                if (title.is4kHdr) GlassPill(label = "4K HDR")
            }

            // Bottom content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Spacing.md)
            ) {
                Text(
                    text = title.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    if (title.imdbRating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), Radius.chip)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = AccentStar, modifier = Modifier.size(11.dp))
                            Text(
                                text = " ${title.imdbRating}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentStar
                            )
                        }
                    }
                    GlassPill(label = "${title.releaseYear}")
                    if (title.genres.isNotEmpty()) GlassPill(label = title.genres.first())
                    GlassPill(label = if (title.type.name == "SERIES") "Series" else "Movie")
                }

                Spacer(Modifier.height(Spacing.sm))

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
                        label = if (isBookmarked) "✓ Saved" else "+ My List",
                        icon = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        onClick = onToggleWatchlist,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
        }
    }
}

// ── Rail Section ──────────────────────────────────────────────────────────────
@Composable
private fun RailSection(
    rail: Rail,
    onTitleClick: (Title) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    val isTrending = rail.id == "trending" || rail.title.contains("Trending", ignoreCase = true)

    Column {
        SectionHeader(
            title = if (isTrending) "🔥 Trending Now" else rail.title,
            onSeeAll = onSeeAllClick
        )
        Spacer(Modifier.height(Spacing.xs))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            itemsIndexed(rail.titles, key = { _, t -> t.id }, contentType = { _, _ -> "poster" }) { index, title ->
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

// ── Anime Section ─────────────────────────────────────────────────────────────
@Composable
private fun AnimeSection(
    rail: Rail,
    onTitleClick: (Title) -> Unit,
    onSeeAllClick: () -> Unit = {}
) {
    Column {
        SectionHeader(title = "⚡ Anime World", onSeeAll = onSeeAllClick)
        Spacer(Modifier.height(Spacing.xs))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            itemsIndexed(rail.titles, key = { _, t -> "anime_${t.id}" }, contentType = { _, _ -> "poster" }) { _, title ->
                PosterCard(
                    posterUrl = title.posterUrl,
                    titleLabel = title.name,
                    ratingLabel = if (title.imdbRating > 0) "${title.imdbRating}" else null,
                    onClick = { onTitleClick(title) }
                )
            }
        }
    }
}

// ── Section Header ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
            .padding(top = Spacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (onSeeAll != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(Radius.chip)
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentPrimary
                )
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ── Continue Watching Section ─────────────────────────────────────────────────
@Composable
private fun ContinueWatchingSection(
    rail: Rail,
    onTitleClick: (Title) -> Unit
) {
    Column {
        SectionHeader(title = "Continue Watching", onSeeAll = null)
        Spacer(Modifier.height(Spacing.xs))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(rail.titles, key = { "cw_${it.id}" }) { title ->
                ContinueWatchingCard(title = title, onClick = { onTitleClick(title) })
            }
        }
    }
}

// ── Continue Watching Card ─────────────────────────────────────────────────────
@Composable
private fun ContinueWatchingCard(title: Title, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = title.backdropUrl.ifBlank { title.posterUrl },
            contentDescription = title.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.82f))
                    )
                )
        )

        // Play icon
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(34.dp)
                .clip(CircleShape)
                .background(AccentPrimary.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.PlayArrow, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(20.dp)
            )
        }

        // Title + progress
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Text(
                text = title.name,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(Radius.pill)
                    .background(Color.White.copy(alpha = 0.22f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(3.dp)
                        .clip(Radius.pill)
                        .background(AccentPrimary)
                )
            }
        }
    }
}

// ── See All Dialog ─────────────────────────────────────────────────────────────
@Composable
private fun SeeAllWindowDialog(
    rail: Rail,
    onTitleClick: (Title) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgBase)
                .statusBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BgElevated)
                        .border(1.dp, GlassBorder)
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm + 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = rail.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = Radius.chip,
                            color = AccentPrimary.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${rail.titles.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    FilledTonalIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = BgCard,
                            contentColor = TextSecondary
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                val isTrending = rail.id == "trending" || rail.title.contains("Trending", ignoreCase = true)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, bottom = 32.dp, top = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        rail.titles,
                        key = { index, t -> "${rail.id}_seeall_${t.id}_$index" }
                    ) { index, title ->
                        PosterCard(
                            posterUrl = title.posterUrl,
                            titleLabel = title.name,
                            ratingLabel = if (title.imdbRating > 0) "${title.imdbRating}" else null,
                            rankBadge = if (isTrending) index + 1 else null,
                            onClick = { onDismiss(); onTitleClick(title) },
                            width = 110.dp,
                            height = 163.dp
                        )
                    }
                }
            }
        }
    }
}
