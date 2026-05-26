package me.june8th.speakez.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.june8th.speakez.ui.navigation.screen.LoginScreen
import me.june8th.speakez.ui.onboarding.OnboardingScreen

@Composable
fun SpeakEZNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val onboardingComplete = remember {
        context.getSharedPreferences("SpeakEZ_Prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("onboarding_complete", false)
    }

    NavHost(
        navController = navController,
        startDestination = if (onboardingComplete) AppRoute.Main else AppRoute.Onboarding,
        modifier = modifier,
    ) {
        composable(AppRoute.Onboarding) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(AppRoute.Main) {
                        popUpTo(AppRoute.Onboarding) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoute.Login) {
            LoginScreen(
                onAvatarSelected = {
                    navController.navigate(AppRoute.Main) {
                        popUpTo(AppRoute.Login) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoute.Main) {
            MainShell()
        }
    }
}


