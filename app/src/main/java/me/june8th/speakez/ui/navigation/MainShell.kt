package me.june8th.speakez.ui.navigation

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.ui.auth.SessionViewModel
import me.june8th.speakez.ui.navigation.screen.AccountScreen
import me.june8th.speakez.ui.navigation.screen.GuardianHomeScreen
import me.june8th.speakez.ui.navigation.screen.HomeScreen
import me.june8th.speakez.ui.navigation.screen.QuickPhrasesScreen
import me.june8th.speakez.ui.navigation.screen.SettingsScreen

@Composable
fun MainShell(
    onLoginRequested: () -> Unit,
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val profile by sessionViewModel.profileState.collectAsStateWithLifecycle()
    val isGuardian = profile?.accountType == AccountType.GUARDIAN
    val navItems = remember(profile?.accountType) { mainNavItemsFor(profile?.accountType) }
    val startDestination = if (isGuardian) MainRoute.GuardianHome else MainRoute.Home

    val context = LocalContext.current
    val homeViewModel: me.june8th.speakez.ui.home.HomeViewModel = hiltViewModel(
        viewModelStoreOwner = context as androidx.lifecycle.ViewModelStoreOwner
    )
    val isEditMode by homeViewModel.isEditMode.collectAsState()

    LaunchedEffect(isGuardian, currentRoute) {
        if (currentRoute == null) return@LaunchedEffect
        if (navItems.none { it.route == currentRoute }) {
            homeViewModel.setEditMode(false)
            navController.navigate(startDestination) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    val currentTitle = remember(currentRoute, isGuardian) {
        when (currentRoute) {
            MainRoute.Home -> R.string.home_title
            MainRoute.GuardianHome -> R.string.nav_home
            MainRoute.QuickPhrases -> R.string.quick_phrases_title
            MainRoute.EditRecommendation -> R.string.edit_recommendation_title
            MainRoute.Settings -> R.string.settings_title
            MainRoute.Account -> if (isGuardian) R.string.nav_monitoring else R.string.nav_account
            else -> R.string.app_name
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLandscape || drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "SpeakEZ Menu",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                navItems.forEach { item ->
                    val selected = if (item.route == MainRoute.EditRecommendation) {
                        currentRoute == MainRoute.Home && isEditMode
                    } else if (item.route == MainRoute.Home) {
                        currentRoute == MainRoute.Home && !isEditMode
                    } else {
                        currentRoute == item.route
                    }
                    NavigationDrawerItem(
                        label = { Text(text = stringResource(item.labelRes)) },
                        selected = selected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.route == MainRoute.EditRecommendation) {
                                homeViewModel.setEditMode(true)
                                homeViewModel.selectCategory("RECOMMENDATION")
                                homeViewModel.updateSearchQuery("")
                                navController.navigate(MainRoute.Home) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                homeViewModel.setEditMode(false)
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = stringResource(item.labelRes),
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        if (isLandscape) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing
            ) { innerPadding ->
                        MainNavHost(
                            navController = navController,
                            contentPadding = innerPadding,
                            startDestination = startDestination,
                            isGuardian = isGuardian,
                            onLoginRequested = onLoginRequested,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = stringResource(currentTitle),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                    )
                },
                bottomBar = {
                    NavigationBar {
                        navItems.forEach { item ->
                            val selected = if (item.route == MainRoute.EditRecommendation) {
                                currentRoute == MainRoute.Home && isEditMode
                            } else if (item.route == MainRoute.Home) {
                                currentRoute == MainRoute.Home && !isEditMode
                            } else {
                                currentRoute == item.route
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (item.route == MainRoute.EditRecommendation) {
                                        homeViewModel.setEditMode(true)
                                        homeViewModel.selectCategory("RECOMMENDATION")
                                        homeViewModel.updateSearchQuery("")
                                        navController.navigate(MainRoute.Home) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else {
                                        homeViewModel.setEditMode(false)
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = stringResource(item.labelRes),
                                    )
                                },
                                label = { Text(text = stringResource(item.labelRes)) },
                            )
                        }
                    }
                },
            ) { innerPadding ->
                MainNavHost(
                    navController = navController,
                    contentPadding = innerPadding,
                    startDestination = startDestination,
                    isGuardian = isGuardian,
                    onLoginRequested = onLoginRequested,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MainNavHost(
    navController: androidx.navigation.NavHostController,
    contentPadding: PaddingValues,
    startDestination: String,
    isGuardian: Boolean,
    onLoginRequested: () -> Unit,
    onMenuClick: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        composable(MainRoute.Home) {
            HomeScreen(
                onMenuClick = onMenuClick,
                onQuickPhrasesClick = {
                    navController.navigate(MainRoute.QuickPhrases) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(MainRoute.GuardianHome) {
            GuardianHomeScreen(onMenuClick = onMenuClick)
        }
        composable(MainRoute.QuickPhrases) {
            QuickPhrasesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MainRoute.Settings) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                isGuardian = isGuardian,
            )
        }
        composable(MainRoute.Account) {
            AccountScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginRequested = onLoginRequested,
            )
        }
        composable(MainRoute.EditRecommendation) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Màn hình Chỉnh sửa Đề xuất (Tính năng đang khảo sát phương án)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
