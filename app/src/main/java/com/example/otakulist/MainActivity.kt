package com.example.otakulist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.otakulist.ui.browse.BrowseScreen
import com.example.otakulist.ui.browse.BrowseViewModel
import com.example.otakulist.ui.components.NavDestination
import com.example.otakulist.ui.components.OtakuBottomBar
import com.example.otakulist.ui.detail.DetailScreen
import com.example.otakulist.ui.detail.DetailViewModel
import com.example.otakulist.ui.settings.SettingsScreen
import com.example.otakulist.ui.settings.SettingsViewModel
import com.example.otakulist.ui.stats.StatsScreen
import com.example.otakulist.ui.stats.StatsViewModel
import com.example.otakulist.ui.theme.OtakuListTheme
import com.example.otakulist.ui.watchlist.WatchlistScreen
import com.example.otakulist.ui.watchlist.WatchlistViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        val app = application as OtakuApplication
        val repository = app.repository

        setContent {
            OtakuListTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: NavDestination.WATCHLIST.route

                val isBottomBarVisible = currentRoute in NavDestination.entries.map { it.route }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (isBottomBarVisible) {
                            OtakuBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { destination ->
                                    if (currentRoute != destination.route) {
                                        navController.navigate(destination.route) {
                                            popUpTo(NavDestination.WATCHLIST.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavDestination.WATCHLIST.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(NavDestination.WATCHLIST.route) {
                            val watchlistViewModel: WatchlistViewModel = viewModel(
                                factory = WatchlistViewModel.Factory(repository)
                            )
                            WatchlistScreen(
                                viewModel = watchlistViewModel,
                                onAnimeClick = { animeId ->
                                    navController.navigate("detail/$animeId")
                                },
                                onNavigateToBrowse = {
                                    navController.navigate(NavDestination.BROWSE.route) {
                                        popUpTo(NavDestination.WATCHLIST.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        composable(NavDestination.BROWSE.route) {
                            val browseViewModel: BrowseViewModel = viewModel(
                                factory = BrowseViewModel.Factory(repository)
                            )
                            BrowseScreen(
                                viewModel = browseViewModel,
                                onAnimeClick = { animeId ->
                                    navController.navigate("detail/$animeId")
                                }
                            )
                        }

                        composable(NavDestination.STATS.route) {
                            val statsViewModel: StatsViewModel = viewModel(
                                factory = StatsViewModel.Factory(repository)
                            )
                            StatsScreen(viewModel = statsViewModel)
                        }

                        composable(NavDestination.SETTINGS.route) {
                            val settingsViewModel: SettingsViewModel = viewModel(
                                factory = SettingsViewModel.Factory(repository)
                            )
                            SettingsScreen(viewModel = settingsViewModel)
                        }

                        composable(
                            route = "detail/{animeId}",
                            arguments = listOf(navArgument("animeId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                            val detailViewModel: DetailViewModel = viewModel(
                                key = "detail_$animeId",
                                factory = DetailViewModel.Factory(animeId, repository)
                            )
                            DetailScreen(
                                viewModel = detailViewModel,
                                onBackClick = { navController.popBackStack() },
                                onAnimeClick = { targetId ->
                                    navController.navigate("detail/$targetId")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}