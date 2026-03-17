package com.ekomak.performanstakippro.ui.screens.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.theme.*
import androidx.compose.ui.text.input.PasswordVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToAbout: () -> Unit = {}
) {
    val context = LocalContext.current
    val employees by viewModel.employees.collectAsState()
    val workTypes by viewModel.workTypes.collectAsState()
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    val defaultWorkType by viewModel.defaultWorkType.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    var companyName by remember { mutableStateOf("") }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminLoginError by remember { mutableStateOf(false) }
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    var showCompanyNameDialog by remember { mutableStateOf(false) }
    var showEmployeeSheet by remember { mutableStateOf(false) }
    var showWorkTypeSheet by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationHour by remember { mutableIntStateOf(16) }
    var notificationMinute by remember { mutableIntStateOf(45) }

    // SharedPreferences'ten şirket adını yükle
    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    LaunchedEffect(Unit) {
        companyName = prefs.getString("company_name", "-") ?: "-"
    }

    // Employee selection bottom sheet — LazyColumn ile scroll
    if (showEmployeeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEmployeeSheet = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    stringResource(R.string.settings_selected_employee).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary, letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).padding(horizontal = 16.dp)
            ) {
                items(employees) { emp ->
                    Surface(
                        onClick = {
                            viewModel.setSelectedEmployee(emp)
                            showEmployeeSheet = false
                        },
                        color = if (selectedEmployee?.personelId == emp.personelId)
                            Accent.copy(alpha = 0.08f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Person, null, tint = Accent, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("${emp.adSoyad} (${emp.personelId})",
                                style = MaterialTheme.typography.bodyLarge, color = TextPrimary,
                                modifier = Modifier.weight(1f))
                            if (selectedEmployee?.personelId == emp.personelId) {
                                Icon(Icons.Filled.Check, null, tint = Accent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Work Type selection bottom sheet — LazyColumn ile scroll
    if (showWorkTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWorkTypeSheet = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    stringResource(R.string.settings_default_work_type).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary, letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).padding(horizontal = 16.dp)
            ) {
                items(workTypes) { wt ->
                    Surface(
                        onClick = {
                            viewModel.setDefaultWorkType(wt)
                            showWorkTypeSheet = false
                        },
                        color = if (defaultWorkType?.islemId == wt.islemId)
                            Accent.copy(alpha = 0.08f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Construction, null, tint = Accent, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(wt.islemAdi, style = MaterialTheme.typography.bodyLarge, color = TextPrimary,
                                modifier = Modifier.weight(1f))
                            Text(wt.birim, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            if (defaultWorkType?.islemId == wt.islemId) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Filled.Check, null, tint = Accent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                    .padding(top = 48.dp, bottom = 14.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge,
                        color = TextOnPrimary, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.bodyMedium,
                        color = TextOnPrimary.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Settings Section
            SectionTitle(stringResource(R.string.settings_user).uppercase())

            SettingsCard {
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    title = stringResource(R.string.settings_selected_employee),
                    subtitle = selectedEmployee?.let { "${it.adSoyad} (${it.personelId})" } ?: "Seçilmedi",
                    onClick = { showEmployeeSheet = true },
                    showChevron = true
                )
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                SettingsItem(
                    icon = Icons.Outlined.Work,
                    title = stringResource(R.string.settings_default_work_type),
                    subtitle = defaultWorkType?.islemAdi ?: "Seçilmedi",
                    onClick = { showWorkTypeSheet = true },
                    showChevron = true
                )
                HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                // Şirket adı
                SettingsItem(
                    icon = Icons.Outlined.Business,
                    title = stringResource(R.string.settings_company),
                    subtitle = companyName.ifEmpty { "-" },
                    onClick = { showCompanyNameDialog = true },
                    showChevron = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notifications Section
            SectionTitle(stringResource(R.string.settings_notifications).uppercase())

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Notifications, null, tint = Accent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(stringResource(R.string.settings_notifications), style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White, checkedTrackColor = Accent,
                            uncheckedThumbColor = Color.White, uncheckedTrackColor = Border
                        )
                    )
                }

                if (notificationsEnabled) {
                    HorizontalDivider(color = Divider, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                TimePickerDialog(context, { _, h, m ->
                                    notificationHour = h
                                    notificationMinute = m
                                }, notificationHour, notificationMinute, true).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Schedule, null, tint = Accent, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(stringResource(R.string.settings_daily_reminder), style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary, modifier = Modifier.weight(1f))
                        Surface(color = Accent.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                String.format("%02d:%02d", notificationHour, notificationMinute),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                color = Accent, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Admin Panel kaldırıldı — Dashboard'a taşındı

            Spacer(modifier = Modifier.height(10.dp))

            // Connection Status
            SectionTitle(stringResource(R.string.connection_status).uppercase())

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Link, null, tint = Accent, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(stringResource(R.string.db_connection), style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                            .background(if (isConnected) Success else Danger))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isConnected) stringResource(R.string.connected) else stringResource(R.string.not_connected),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isConnected) Success else Danger, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // About
            SettingsCard {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about_title),
                    subtitle = stringResource(R.string.about_app_info),
                    onClick = onNavigateToAbout,
                    showChevron = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Developer Credit
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.developer_credit), style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("v${com.ekomak.performanstakippro.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.4f), fontFamily = JetBrainsMono)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Admin Login Dialog
        if (showAdminLoginDialog) {
            var username by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var rememberMe by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showAdminLoginDialog = false; adminLoginError = false },
                icon = { Icon(Icons.Outlined.AdminPanelSettings, null, tint = Accent) },
                title = { Text(stringResource(R.string.settings_admin_login)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it; adminLoginError = false },
                            label = { Text("Kullanıcı adı") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            isError = adminLoginError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; adminLoginError = false },
                            label = { Text("Şifre") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            isError = adminLoginError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (adminLoginError) {
                            Text("Kullanıcı adı veya şifre hatalı",
                                style = MaterialTheme.typography.bodySmall, color = Danger)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Accent))
                            Text("Beni hatırla", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (viewModel.adminLogin(username, password)) {
                            if (rememberMe) viewModel.setAdminRememberMe(true)
                            showAdminLoginDialog = false
                            adminLoginError = false
                        } else {
                            adminLoginError = true
                        }
                    }) { Text("Giriş", color = Accent) }
                },
                dismissButton = {
                    TextButton(onClick = { showAdminLoginDialog = false; adminLoginError = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Company Name Edit Dialog
        if (showCompanyNameDialog) {
            var editedName by remember { mutableStateOf(companyName) }
            AlertDialog(
                onDismissRequest = { showCompanyNameDialog = false },
                icon = { Icon(Icons.Outlined.Business, null, tint = Accent) },
                title = { Text(stringResource(R.string.settings_company)) },
                text = {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Şirket adı") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        companyName = editedName
                        prefs.edit().putString("company_name", editedName).apply()
                        showCompanyNameDialog = false
                    }) { Text("Kaydet", color = Accent) }
                },
                dismissButton = {
                    TextButton(onClick = { showCompanyNameDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = TextSecondary,
        letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp))
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = TextSecondary,
        letterSpacing = 1.sp, modifier = Modifier.padding(start = 52.dp, top = 4.dp))
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        content = content
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, showChevron: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
        if (showChevron) {
            Icon(Icons.Filled.ChevronRight, null, tint = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        }
    }
}
