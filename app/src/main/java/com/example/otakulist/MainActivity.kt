package com.example.otakulist

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.otakulist.ui.theme.parseHexColor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
        val app = application as OtakuApplication
        val repository = app.repository
        val settingsManager = repository.settingsManager

        setContent {
            val themeMode by settingsManager.themeMode.collectAsState()
            val accentColorHex by settingsManager.accentColorHex.collectAsState()
            val accentColor = remember(accentColorHex) {
                parseHexColor(accentColorHex)
            }

            OtakuListTheme(
                themeMode = themeMode,
                accentColor = accentColor
            ) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "main",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("main") {
                        val pagerState = rememberPagerState(
                            initialPage = 0,
                            pageCount = { NavDestination.entries.size }
                        )
                        val coroutineScope = rememberCoroutineScope()
                        val context = LocalContext.current
                        var lastBackPressTime by remember { mutableLongStateOf(0L) }
                        var exitToast by remember { mutableStateOf<Toast?>(null) }

                        BackHandler {
                            if (pagerState.currentPage != 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            } else {
                                val now = SystemClock.uptimeMillis()
                                if (now - lastBackPressTime < 2000L) {
                                    exitToast?.cancel()
                                    (context as? Activity)?.finish()
                                } else {
                                    lastBackPressTime = now
                                    exitToast?.cancel()
                                    exitToast = Toast.makeText(
                                        context,
                                        context.getString(R.string.press_again_to_exit),
                                        Toast.LENGTH_SHORT
                                    ).apply {
                                        show()
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = pagerState,
                                beyondViewportPageCount = 1,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                when (NavDestination.entries[page]) {
                                    NavDestination.WATCHLIST -> {
                                        val watchlistViewModel: WatchlistViewModel = viewModel(
                                            factory = WatchlistViewModel.Factory(repository)
                                        )
                                        WatchlistScreen(
                                            viewModel = watchlistViewModel,
                                            onAnimeClick = { animeId, isAdult ->
                                                navController.navigateSafely("detail/$animeId?isAdult=$isAdult")
                                            },
                                            onNavigateToBrowse = {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(NavDestination.BROWSE.ordinal)
                                                }
                                            }
                                        )
                                    }
                                    NavDestination.BROWSE -> {
                                        val browseViewModel: BrowseViewModel = viewModel(
                                            factory = BrowseViewModel.Factory(repository)
                                        )
                                        BrowseScreen(
                                            viewModel = browseViewModel,
                                            onAnimeClick = { animeId, isAdult ->
                                                navController.navigateSafely("detail/$animeId?isAdult=$isAdult")
                                            }
                                        )
                                    }
                                    NavDestination.STATS -> {
                                        val statsViewModel: StatsViewModel = viewModel(
                                            factory = StatsViewModel.Factory(repository)
                                        )
                                        StatsScreen(viewModel = statsViewModel)
                                    }
                                    NavDestination.SETTINGS -> {
                                        val settingsViewModel: SettingsViewModel = viewModel(
                                            factory = SettingsViewModel.Factory(repository)
                                        )
                                        SettingsScreen(viewModel = settingsViewModel)
                                    }
                                }
                            }

                            OtakuBottomBar(
                                pagerPosition = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                                onNavigate = { destination ->
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(destination.ordinal)
                                    }
                                },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }

                    composable(
                        route = "detail/{animeId}?isAdult={isAdult}",
                        arguments = listOf(
                            navArgument("animeId") { type = NavType.IntType },
                            navArgument("isAdult") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        ),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(280)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(280)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(280)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(280)
                            )
                        }
                    ) { backStackEntry ->
                        val animeId = backStackEntry.arguments?.getInt("animeId") ?: 0
                        val isAdult = backStackEntry.arguments?.getBoolean("isAdult") ?: false
                        val detailViewModel: DetailViewModel = viewModel(
                            key = "detail_${animeId}_$isAdult",
                            factory = DetailViewModel.Factory(animeId, isAdult, repository)
                        )
                        DetailScreen(
                            viewModel = detailViewModel,
                            onBackClick = { navController.popBackStackSafely() },
                            onAnimeClick = { targetId, targetIsAdult ->
                                if (targetId != animeId || targetIsAdult != isAdult) {
                                    navController.navigateSafely("detail/$targetId?isAdult=$targetIsAdult") {
                                        popUpTo("detail/{animeId}?isAdult={isAdult}") {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private var lastNavigationEventTime = 0L

/**
 * Safely navigates to a route preventing multiple rapid clicks / double navigation bugs.
 * Ensures:
 * 1. Minimum debounce threshold (500ms) between navigation actions across the app.
 * 2. Current backstack entry is in Lifecycle.State.RESUMED before navigating.
 * 3. launchSingleTop = true is enabled so duplicate instances are not pushed.
 */
fun NavController.navigateSafely(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
    val now = SystemClock.uptimeMillis()
    if (now - lastNavigationEventTime < 500L) {
        return
    }
    val currentEntry = currentBackStackEntry
    if (currentEntry == null || currentEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        lastNavigationEventTime = now
        navigate(route) {
            launchSingleTop = true
            builder()
        }
    }
}

fun NavController.popBackStackSafely(): Boolean {
    val now = SystemClock.uptimeMillis()
    if (now - lastNavigationEventTime < 400L) {
        return false
    }
    val currentEntry = currentBackStackEntry
    if (currentEntry == null || currentEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
        lastNavigationEventTime = now
        return popBackStack()
    }
    return false
}