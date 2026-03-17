package com.ekomak.performanstakippro.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.screens.dashboard.DashboardScreen
import com.ekomak.performanstakippro.ui.screens.entry.EntryScreen
import com.ekomak.performanstakippro.ui.screens.history.HistoryScreen
import com.ekomak.performanstakippro.ui.screens.settings.SettingsScreen
import com.ekomak.performanstakippro.ui.theme.*
import kotlinx.coroutines.launch

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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppNavigation() {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { bottomNavItems.size }

    var showAboutScreen by remember { mutableStateOf(false) }

    // Bildirim izni — tek seferlik
    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // İzin sonucu ne olursa olsun, bir daha sorma
        prefs.edit().putBoolean("notification_permission_asked", true).apply()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyAsked = prefs.getBoolean("notification_permission_asked", false)
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!alreadyAsked && !hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // About ekranı ayrı gösterilir (pager dışında)
    if (showAboutScreen) {
        com.ekomak.performanstakippro.ui.screens.settings.AboutScreen(
            viewModel = viewModel,
            onNavigateBack = { showAboutScreen = false }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Primary,
                contentColor = TextOnPrimary,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .shadow(24.dp)
                    .navigationBarsPadding()
                    .height(64.dp)
            ) {
                bottomNavItems.forEachIndexed { index, screen ->
                    val selected = pagerState.currentPage == index

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
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
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
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> EntryScreen(viewModel)
                1 -> HistoryScreen(viewModel)
                2 -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAbout = { showAboutScreen = true }
                )
                3 -> DashboardScreen(viewModel)
            }
        }
    }
}
