package com.ekomak.performanstakippro.ui.screens.settings

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("tr") }
    var notificationHour by remember { mutableIntStateOf(16) }
    var notificationMinute by remember { mutableIntStateOf(45) }
    var showEmployeeSheet by remember { mutableStateOf(false) }
    var showWorkTypeSheet by remember { mutableStateOf(false) }

    // Employee selection bottom sheet
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
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                employees.forEach { emp ->
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Work Type selection bottom sheet
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
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                workTypes.forEach { wt ->
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                    .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineLarge,
                        color = TextOnPrimary, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.settings_subtitle), style = MaterialTheme.typography.bodyMedium,
                        color = TextOnPrimary.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

                SettingsItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = if (selectedLanguage == "tr") stringResource(R.string.settings_language_tr)
                              else stringResource(R.string.settings_language_en),
                    onClick = { selectedLanguage = if (selectedLanguage == "tr") "en" else "tr" },
                    showChevron = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notifications Section
            SectionTitle(stringResource(R.string.settings_notifications).uppercase())

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
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
                    SectionLabel(stringResource(R.string.settings_notification_time).uppercase())
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Connection Status (simplified)
            SectionTitle(stringResource(R.string.connection_status).uppercase())

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
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
                            color = if (isConnected) Success else Danger, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // Developer Credit
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.developer_credit), style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text("v${com.ekomak.performanstakippro.BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary.copy(alpha = 0.4f), fontFamily = JetBrainsMono)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = TextSecondary,
        letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = TextSecondary,
        letterSpacing = 1.sp, modifier = Modifier.padding(start = 52.dp, top = 6.dp))
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
