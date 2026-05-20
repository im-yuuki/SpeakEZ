package me.june8th.speakez.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import me.june8th.speakez.ui.navigation.screen.LoginScreen
import me.june8th.speakez.ui.navigation.MainShell

@Composable
fun SpeakEZNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login,
        modifier = modifier,
    ) {
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


