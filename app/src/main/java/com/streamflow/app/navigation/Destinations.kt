package com.streamflow.app.navigation

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Search : Destination("search")
    data object Watchlist : Destination("watchlist")
    data object Profile : Destination("profile")

    data object TitleDetail : Destination("title/{titleId}") {
        fun createRoute(titleId: String) = "title/$titleId"
    }

    data object Player : Destination("player/{titleId}?episodeId={episodeId}") {
        fun createRoute(titleId: String, episodeId: String? = null) =
            "player/$titleId?episodeId=${episodeId ?: ""}"
    }
}

// Tabs shown in the FloatingBottomNav
val bottomNavDestinations = listOf(
    Destination.Home,
    Destination.Search,
    Destination.Watchlist,
    Destination.Profile
)
