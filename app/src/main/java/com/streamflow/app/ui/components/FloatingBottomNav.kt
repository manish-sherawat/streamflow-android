package com.streamflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.streamflow.app.navigation.Destination
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgElevated
import com.streamflow.app.ui.theme.GlassBorder
import com.streamflow.app.ui.theme.GlassFill
import com.streamflow.app.ui.theme.Radius
import com.streamflow.app.ui.theme.Sizes
import com.streamflow.app.ui.theme.TextTertiary

private data class NavItem(val destination: Destination, val icon: ImageVector, val label: String)

private val navItems = listOf(
    NavItem(Destination.Home, Icons.Filled.Home, "Home"),
    NavItem(Destination.Search, Icons.Filled.Search, "Search"),
    NavItem(Destination.Watchlist, Icons.Filled.Bookmark, "My List"),
    NavItem(Destination.Profile, Icons.Filled.Person, "Profile")
)

/**
 * Floating glass pill bottom nav with glowing active state indicator capsule.
 */
@Composable
fun FloatingBottomNav(
    currentRoute: String?,
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.bottomNavHeight)
            .clip(Radius.pill)
            .background(BgElevated.copy(alpha = 0.95f))
            .border(1.dp, GlassBorder, Radius.pill)
            .padding(horizontal = 12.dp)
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.destination.route
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (selected) AccentPrimary.copy(alpha = 0.18f) else GlassFill)
                    .border(1.dp, if (selected) AccentPrimary else GlassBorder, CircleShape)
                    .clickable { onNavigate(item.destination) }
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (selected) AccentPrimary else TextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
