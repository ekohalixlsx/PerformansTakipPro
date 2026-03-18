package com.ekomak.performanstakippro.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.screens.dashboard.DashboardScreen
import com.ekomak.performanstakippro.ui.screens.entry.EntryScreen
import com.ekomak.performanstakippro.ui.screens.history.HistoryScreen
import com.ekomak.performanstakippro.ui.screens.settings.AboutScreen
import com.ekomak.performanstakippro.ui.screens.settings.SettingsScreen
import com.ekomak.performanstakippro.ui.theme.*
import kotlinx.coroutines.launch

data class TabItem(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val tabItems = listOf(
    TabItem(R.string.nav_entry, Icons.Filled.EditNote, Icons.Outlined.EditNote),
    TabItem(R.string.nav_history, Icons.Filled.History, Icons.Outlined.History),
    TabItem(R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
    TabItem(R.string.nav_dashboard, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppNavigation() {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // About ekranı için state
    var showAboutScreen by remember { mutableStateOf(false) }

    // SharedPreferences
    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

    // Bildirim izni — tek seferlik
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
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

    // ==================== İLK KULLANIM HOŞGELDİN DİALOGU ====================
    val employees by viewModel.employees.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showWelcomeDialog by remember {
        mutableStateOf(!prefs.getBoolean("first_launch_done", false))
    }

    if (showWelcomeDialog && employees.isNotEmpty()) {
        var searchQuery by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = { /* Kullanıcı seçim yapmadan kapatamaz */ },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(24.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(
                            Color(0xFF1A1F36), Color(0xFF0F1425)
                        )))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Hoş Geldiniz!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Lütfen listeden kendinizi seçin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Personel ara…", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = Accent.copy(alpha = 0.7f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            cursorColor = Accent,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Employee list
                    val filtered = employees.filter {
                        searchQuery.isEmpty() ||
                        it.adSoyad.contains(searchQuery, ignoreCase = true) ||
                        it.personelId.toString().contains(searchQuery)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        items(filtered) { emp ->
                            Surface(
                                onClick = {
                                    viewModel.setSelectedEmployee(emp)
                                    prefs.edit().putBoolean("first_launch_done", true).apply()
                                    showWelcomeDialog = false
                                },
                                color = Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Accent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emp.initials, color = Accent,
                                            fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(emp.adSoyad,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White, fontWeight = FontWeight.Medium)
                                        Text(emp.bolumAdi,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.5f))
                                    }
                                    Text(emp.personelId.toString(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                        color = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // HorizontalPager state
    val pagerState = rememberPagerState(initialPage = 0) { tabItems.size }

    // About ekranı açıksa üzerine göster
    if (showAboutScreen) {
        AboutScreen(
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
                tabItems.forEachIndexed { index, tab ->
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
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = stringResource(tab.titleRes),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(tab.titleRes),
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            key = { it }
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (page) {
                    0 -> EntryScreen(viewModel)
                    1 -> HistoryScreen(viewModel)
                    2 -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAbout = { showAboutScreen = true }
                    )
                    3 -> DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}
