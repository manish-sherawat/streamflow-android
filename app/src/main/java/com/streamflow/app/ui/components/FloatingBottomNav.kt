package com.streamflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflow.app.navigation.Destination
import com.streamflow.app.ui.theme.AccentGlow
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgCard
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextMuted
import com.streamflow.app.ui.theme.TextPrimary

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
 * Premium floating bottom navigation bar with glassmorphic surface.
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Floating nav pill with deep elevated surface
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BgElevated,
                            BgCard
                        )
                    )
                )
                .padding(horizontal = 8.dp)
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.destination.route

                val iconColor by animateColorAsState(
                    targetValue = if (selected) AccentPrimary else TextMuted,
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                    label = "navIconColor_${item.label}"
                )

                val labelColor by animateColorAsState(
                    targetValue = if (selected) TextPrimary else TextMuted,
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                    label = "navLabelColor_${item.label}"
                )

                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.15f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "navIconScale_${item.label}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.destination) }
                ) {
                    // Active indicator capsule behind the icon
                    Box(contentAlignment = Alignment.Center) {
                        // Glow background for active item
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentGlow)
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
                        style = StreamFlowType.caption.copy(
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = labelColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
