package com.ekomak.performanstakippro.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.screens.dashboard.DashboardScreen
import com.ekomak.performanstakippro.ui.screens.entry.EntryScreen
import com.ekomak.performanstakippro.ui.screens.history.HistoryScreen
import com.ekomak.performanstakippro.ui.screens.settings.SettingsScreen
import com.ekomak.performanstakippro.ui.theme.*

sealed class Screen(
    val route: String,
    val titleRes: Int? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Entry : Screen("entry", R.string.nav_entry, Icons.Filled.EditNote, Icons.Outlined.EditNote)
    data object History : Screen("history", R.string.nav_history, Icons.Filled.History, Icons.Outlined.History)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    data object Dashboard : Screen("dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    data object About : Screen("about")
}

val bottomNavItems = listOf(
    Screen.Entry,
    Screen.History,
    Screen.Settings,
    Screen.Dashboard,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val viewModel: MainViewModel = viewModel()

    Scaffold(
        bottomBar = {
            val showBottomBar = currentDestination?.route != Screen.About.route
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Primary,
                    contentColor = TextOnPrimary,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .shadow(24.dp)
                        .navigationBarsPadding()
                        .height(64.dp)
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Box(contentAlignment = Alignment.Center) {
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Accent.copy(alpha = 0.15f))
                                        )
                                    }
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon!! else screen.unselectedIcon!!,
                                        contentDescription = stringResource(screen.titleRes!!),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(screen.titleRes!!),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent,
                                selectedTextColor = Accent,
                                unselectedIconColor = TextOnPrimary.copy(alpha = 0.6f),
                                unselectedTextColor = TextOnPrimary.copy(alpha = 0.6f),
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Entry.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Entry.route) { EntryScreen(viewModel) }
            composable(Screen.History.route) { HistoryScreen(viewModel) }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                ) 
            }
            composable(Screen.Dashboard.route) { DashboardScreen(viewModel) }
            composable(Screen.About.route) {
                com.ekomak.performanstakippro.ui.screens.settings.AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
