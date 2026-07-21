package com.streamflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamflow.app.navigation.Destination
import com.streamflow.app.ui.theme.AccentPrimary
import com.streamflow.app.ui.theme.BgBase
import com.streamflow.app.ui.theme.Divider
import com.streamflow.app.ui.theme.Sizes
import com.streamflow.app.ui.theme.StreamFlowType
import com.streamflow.app.ui.theme.TextMuted
import com.streamflow.app.ui.theme.TextPrimary

private data class NavItem(val destination: Destination, val icon: ImageVector, val label: String)

private val navItems = listOf(
    NavItem(Destination.Home,      Icons.Filled.Home,     "Home"),
    NavItem(Destination.Search,    Icons.Filled.Search,   "Search"),
    NavItem(Destination.Watchlist, Icons.Filled.Bookmark, "My List"),
    NavItem(Destination.Profile,   Icons.Filled.Person,   "Profile")
)

/**
 * Modern glassmorphic bottom navigation bar with safe bottom insets support.
 */
@Composable
fun FloatingBottomNav(
    currentRoute: String?,
    onNavigate: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgBase)
            .navigationBarsPadding()
    ) {
        // Hairline top divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Divider)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(BgBase)
        ) {
            navItems.forEach { item ->
                val selected = currentRoute == item.destination.route
                val activeColor by animateColorAsState(
                    targetValue = if (selected) AccentPrimary else TextMuted,
                    animationSpec = tween(durationMillis = 220),
                    label = "navIconColor_${item.label}"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clickable(
                            interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
                            indication = null
                        ) { onNavigate(item.destination) }
                        .padding(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = activeColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = item.label,
                        style = StreamFlowType.caption.copy(
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) TextPrimary else TextMuted
                    )
                    // Active indicator dot
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(if (selected) 4.dp else 0.dp)
                            .clip(CircleShape)
                            .background(AccentPrimary)
                    )
                }
            }
        }
    }
}

