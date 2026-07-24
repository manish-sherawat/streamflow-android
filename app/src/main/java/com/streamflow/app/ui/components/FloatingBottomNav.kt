package com.streamflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflow.app.navigation.Destination

private data class NavItem(
    val destination: Destination,
    val icon: ImageVector,
    val iconSelected: ImageVector,
    val label: String
)

private val navItems = listOf(
    NavItem(Destination.Home,      Icons.Outlined.Home,          Icons.Filled.Home,     "Home"),
    NavItem(Destination.Search,    Icons.Outlined.Search,         Icons.Filled.Search,   "Search"),
    NavItem(Destination.Watchlist, Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark, "My List"),
    NavItem(Destination.Profile,   Icons.Outlined.Person,         Icons.Filled.Person,   "Profile")
)

/**
 * StreamFlow Material 3 Expressive Floating Navigation Bar.
 * See design.md §3 & §5. Uses surfaceContainerHigh and extraLarge shape.
 */
@Composable
fun FloatingBottomNav(
    currentRoute: String?,
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(com.streamflow.app.ui.theme.BgElevated.copy(alpha = 0.95f))
            .border(1.dp, com.streamflow.app.ui.theme.GlassBorder, MaterialTheme.shapes.extraLarge)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.destination.route

                val iconColor by animateColorAsState(
                    targetValue = if (selected) com.streamflow.app.ui.theme.AccentPrimary else com.streamflow.app.ui.theme.TextSecondary,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "navIconColor_${item.label}"
                )

                val labelColor by animateColorAsState(
                    targetValue = if (selected) com.streamflow.app.ui.theme.AccentPrimary else com.streamflow.app.ui.theme.TextSecondary,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "navLabelColor_${item.label}"
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    label = "navIconScale_${item.label}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.destination) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 30.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(com.streamflow.app.ui.theme.AccentPrimary.copy(alpha = 0.15f))
                            )
                        }

                        Icon(
                            imageVector = if (selected) item.iconSelected else item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(iconScale)
                        )
                    }

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = labelColor,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}
