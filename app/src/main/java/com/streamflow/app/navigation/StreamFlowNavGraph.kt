package com.streamflow.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamflow.app.ui.components.FloatingBottomNav
import com.streamflow.app.ui.screens.auth.AuthScreen
import com.streamflow.app.ui.screens.detail.TitleDetailScreen
import com.streamflow.app.ui.screens.home.HomeScreen
import com.streamflow.app.ui.screens.player.PlayerScreen
import com.streamflow.app.ui.screens.profile.ProfileScreen
import com.streamflow.app.ui.screens.profile.WatchlistScreen
import com.streamflow.app.ui.screens.search.SearchScreen

private val topLevelRoutes = bottomNavDestinations.map { it.route }.toSet()

@Composable
fun StreamFlowNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = Destination.Home.route) {
            composable(Destination.Auth.route) {
                AuthScreen(onAuthSuccess = {
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.Auth.route) { inclusive = true }
                    }
                })
            }
            composable(Destination.Home.route) {
                HomeScreen(onTitleClick = { title ->
                    navController.navigate(Destination.TitleDetail.createRoute(title.id))
                })
            }
            composable(Destination.Search.route) {
                SearchScreen(onTitleClick = { title ->
                    navController.navigate(Destination.TitleDetail.createRoute(title.id))
                })
            }
            composable(Destination.Watchlist.route) {
                WatchlistScreen(onTitleClick = { title ->
                    navController.navigate(Destination.TitleDetail.createRoute(title.id))
                })
            }
            composable(Destination.Profile.route) {
                ProfileScreen(
                    onTitleClick = { title ->
                        navController.navigate(Destination.TitleDetail.createRoute(title.id))
                    },
                    onOpenAuth = {
                        navController.navigate(Destination.Auth.route)
                    },
                    onSignOut = {
                        navController.navigate(Destination.Auth.route) {
                            popUpTo(Destination.Home.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Destination.TitleDetail.route,
                arguments = listOf(navArgument("titleId") { })
            ) {
                TitleDetailScreen(
                    onBack = { navController.popBackStack() },
                    onPlay = { titleId, episodeId ->
                        navController.navigate(Destination.Player.createRoute(titleId, episodeId))
                    },
                    onTitleClick = { title ->
                        navController.navigate(Destination.TitleDetail.createRoute(title.id))
                    }
                )
            }
            composable(
                route = Destination.Player.route,
                arguments = listOf(
                    navArgument("titleId") { },
                    navArgument("episodeId") { nullable = true; defaultValue = null }
                )
            ) {
                PlayerScreen(onBack = { navController.popBackStack() })
            }
        }

        // Floating bottom nav only shows on the four top-level tabs — hidden on Auth/Detail/Player
        if (currentRoute in topLevelRoutes) {
            FloatingBottomNav(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(Destination.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    }
}
