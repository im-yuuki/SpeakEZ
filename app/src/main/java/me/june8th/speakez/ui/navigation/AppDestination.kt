package me.june8th.speakez.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import me.june8th.speakez.R

object AppRoute {
    const val Login = "login"
    const val Main = "main"
}

object MainRoute {
    const val Home = "home"
    const val QuickPhrases = "quick_phrases"
    const val Settings = "settings"
}

data class MainNavItem(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

val mainNavItems = listOf(
    MainNavItem(MainRoute.Home, R.string.nav_home, Icons.Filled.Home),
    MainNavItem(MainRoute.QuickPhrases, R.string.nav_quick_phrases, Icons.Filled.MoreHoriz),
    MainNavItem(MainRoute.Settings, R.string.nav_settings, Icons.Filled.Settings),
)


