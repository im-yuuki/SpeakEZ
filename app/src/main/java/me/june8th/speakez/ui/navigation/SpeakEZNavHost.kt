package me.june8th.speakez.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import me.june8th.speakez.ui.navigation.screen.LoginScreen
import me.june8th.speakez.ui.onboarding.OnboardingScreen
import me.june8th.speakez.ui.auth.SessionViewModel

@Composable
fun SpeakEZNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
) {
    val profile = viewModel.profileState.collectAsStateWithLifecycle().value
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    var onboardingComplete by remember { mutableStateOf(viewModel.onboardingComplete) }
    val startDestination = when {
        profile != null && (!profile.isGuest || onboardingComplete) -> AppRoute.Main
        profile != null && profile.isGuest && !onboardingComplete -> AppRoute.Onboarding
        else -> AppRoute.Login
    }

    LaunchedEffect(onboardingComplete, profile, currentRoute) {
        if (currentRoute == null) return@LaunchedEffect
        when {
            profile != null && profile.isGuest && !onboardingComplete && currentRoute == AppRoute.Login -> {
                navController.navigate(AppRoute.Onboarding) {
                    popUpTo(AppRoute.Login) { inclusive = true }
                    launchSingleTop = true
                }
            }
            profile != null && (!profile.isGuest || onboardingComplete) && currentRoute == AppRoute.Login -> {
                navController.navigate(AppRoute.Main) {
                    popUpTo(AppRoute.Login) { inclusive = true }
                    launchSingleTop = true
                }
            }
            profile == null && currentRoute == AppRoute.Main -> {
                navController.navigate(AppRoute.Login) {
                    popUpTo(AppRoute.Main) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(AppRoute.Onboarding) {
            OnboardingScreen(
                onFinished = {
                    onboardingComplete = true
                    navController.navigate(if (profile != null) AppRoute.Main else AppRoute.Login) {
                        popUpTo(AppRoute.Onboarding) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoute.Login) {
            LoginScreen(
                onAuthComplete = { isGuest ->
                    navController.navigate(if (isGuest && !onboardingComplete) AppRoute.Onboarding else AppRoute.Main) {
                        popUpTo(AppRoute.Login) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoute.Main) {
            MainShell(
                onLoginRequested = {
                    navController.navigate(AppRoute.Login) {
                        popUpTo(AppRoute.Main) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
