package me.june8th.speakez.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.june8th.speakez.R
import me.june8th.speakez.ui.navigation.screen.HomeScreen
import me.june8th.speakez.ui.navigation.screen.QuickPhrasesScreen
import me.june8th.speakez.ui.navigation.screen.SettingsScreen

@Composable
fun MainShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTitle = remember(currentRoute) {
        when (currentRoute) {
            MainRoute.Home -> R.string.home_title
            MainRoute.QuickPhrases -> R.string.quick_phrases_title
            MainRoute.Settings -> R.string.settings_title
            else -> R.string.app_name
        }
    }

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
                mainNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
        )
    }
}

@Composable
private fun MainNavHost(
    navController: androidx.navigation.NavHostController,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = MainRoute.Home,
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        composable(MainRoute.Home) {
            HomeScreen()
        }
        composable(MainRoute.QuickPhrases) {
            QuickPhrasesScreen()
        }
        composable(MainRoute.Settings) {
            SettingsScreen()
        }
    }
}



