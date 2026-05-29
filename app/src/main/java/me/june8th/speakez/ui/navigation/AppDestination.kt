package me.june8th.speakez.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import me.june8th.speakez.R

object AppRoute {
    const val Login = "login"
    const val Onboarding = "onboarding"
    const val Main = "main"
}

object MainRoute {
    const val Home = "home"
    const val QuickPhrases = "quick_phrases"
    const val EditRecommendation = "edit_recommendation"
    const val Settings = "settings"
    const val Account = "account"
}

data class MainNavItem(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
)

val mainNavItems = listOf(
    MainNavItem(MainRoute.Home, R.string.nav_home, Icons.Filled.Home),
    MainNavItem(MainRoute.QuickPhrases, R.string.nav_quick_phrases, Icons.Filled.Bolt),
    MainNavItem(MainRoute.EditRecommendation, R.string.nav_edit_recommendation, Icons.Filled.Edit),
    MainNavItem(MainRoute.Settings, R.string.nav_settings, Icons.Filled.Settings),
    MainNavItem(MainRoute.Account, R.string.nav_account, Icons.Filled.AccountCircle),
)

