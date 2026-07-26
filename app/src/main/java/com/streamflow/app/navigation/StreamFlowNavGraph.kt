package com.streamflow.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.streamflow.app.ui.components.FloatingBottomNav
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

    val showBottomNav = currentRoute in topLevelRoutes

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(animationSpec = tween(220)) + slideInHorizontally(initialOffsetX = { 200 }, animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) + slideOutHorizontally(targetOffsetX = { -200 }, animationSpec = tween(220)) },
            popEnterTransition = { fadeIn(animationSpec = tween(220)) + slideInHorizontally(initialOffsetX = { -200 }, animationSpec = tween(220)) },
            popExitTransition = { fadeOut(animationSpec = tween(220)) + slideOutHorizontally(targetOffsetX = { 200 }, animationSpec = tween(220)) }
        ) {
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
                    onOpenAuth = {},
                    onSignOut = {}
                )
            }
            composable(
                route = Destination.TitleDetail.route,
                arguments = listOf(androidx.navigation.navArgument("titleId") { }),
                deepLinks = listOf(
                    androidx.navigation.navDeepLink { uriPattern = "streamflow://title/{titleId}" },
                    androidx.navigation.navDeepLink { uriPattern = "https://streamflow.app/title/{titleId}" }
                )
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

        if (showBottomNav) {
            FloatingBottomNav(
                currentRoute = currentRoute,
                onNavigate = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(Destination.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
