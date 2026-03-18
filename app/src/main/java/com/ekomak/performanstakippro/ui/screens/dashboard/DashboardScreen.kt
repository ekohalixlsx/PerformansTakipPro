package com.ekomak.performanstakippro.ui.screens.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.data.model.PerformanceRecord
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.theme.*
import com.ekomak.performanstakippro.util.DateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    val isLoading by viewModel.isLoadingRecords.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val appUsers by viewModel.appUsers.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var showEmployeeDropdown by remember { mutableStateOf(false) }
    var showAdminLoginDialog by remember { mutableStateOf(false) }
    var adminLoginError by remember { mutableStateOf(false) }
    var showPdfEmailDialog by remember { mutableStateOf(false) }
    var pdfGenerating by remember { mutableStateOf(false) }

    val tabs = listOf(
        stringResource(R.string.dashboard_daily),
        stringResource(R.string.dashboard_weekly),
        stringResource(R.string.dashboard_monthly)
    )

    // Admin giriş yapılmamışsa kullanıcı butona tıklayınca dialog açılacak

    // İlk personeli otomatik seç (Tüm Personel yok)
    LaunchedEffect(employees) {
        if (selectedEmployee == null && employees.isNotEmpty()) {
            viewModel.setSelectedEmployee(employees.first())
        }
    }

    // Admin Login Dialog
    if (showAdminLoginDialog && !isAdminLoggedIn) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var rememberMe by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAdminLoginDialog = false },
            icon = { Icon(Icons.Outlined.Lock, null, tint = Accent) },
            title = { Text("Yönetici Girişi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Dashboard'u görüntülemek için\nyönetici girişi gereklidir.",
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
                TextButton(onClick = {
                    showAdminLoginDialog = false
                }) { Text("İptal", color = TextSecondary) }
            }
        )
    }

    // Admin giriş yapılmamışsa giriş ekranı göster
    if (!isAdminLoggedIn) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Lock, null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Yönetici Paneli", color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Dashboard'u görüntülemek için\nyönetici girişi gereklidir.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showAdminLoginDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = TextOnAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Lock, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Giriş Yap", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        return
    }

    // === ADMIN GİRİŞ YAPILDI — DASHBOARD İÇERİĞİ ===

    // Seçili personele göre kayıtları filtrele
    val filteredRecords = remember(records, selectedEmployee) {
        if (selectedEmployee != null) {
            records.filter { it.personelId == selectedEmployee!!.personelId }
        } else records
    }

    // PDF Email Dialog
    if (showPdfEmailDialog && selectedEmployee != null) {
        var selectedUserIndex by remember { mutableIntStateOf(0) }
        val userList = appUsers.filter { it.email.isNotEmpty() }

        AlertDialog(
            onDismissRequest = { showPdfEmailDialog = false },
            icon = { Icon(Icons.Outlined.PictureAsPdf, null, tint = Accent) },
            title = { Text("PDF Rapor Gönder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${selectedEmployee!!.adSoyad} için aylık rapor oluşturulup gönderilecek.",
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    if (userList.isNotEmpty()) {
                        Text("Alıcı:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        userList.forEachIndexed { index, user ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { selectedUserIndex = index }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedUserIndex == index,
                                    onClick = { selectedUserIndex = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = Accent)
                                )
                                Text("${user.adSoyad} (${user.email})",
                                    style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                    } else {
                        Text("Email alıcısı bulunamadı", color = Danger,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (userList.isNotEmpty()) {
                            val recipient = userList[selectedUserIndex]
                            showPdfEmailDialog = false
                            // PDF oluştur ve email gönder
                            val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            val companyName = prefs.getString("company_name", "") ?: ""
                            val pdfService = com.ekomak.performanstakippro.util.PdfReportService(context)
                            val pdfFile = pdfService.generateMonthlyReport(
                                employee = selectedEmployee!!,
                                records = filteredRecords,
                                companyName = companyName
                            )
                            if (pdfFile != null) {
                                pdfService.sendEmail(
                                    pdfFile = pdfFile,
                                    recipientEmail = recipient.email,
                                    employeeName = selectedEmployee!!.adSoyad
                                )
                            }
                        }
                    },
                    enabled = userList.isNotEmpty()
                ) { Text("Gönder", color = if (userList.isNotEmpty()) Accent else TextSecondary) }
            },
            dismissButton = {
                TextButton(onClick = { showPdfEmailDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // İstatistik hesapla
    val totalMiktar = filteredRecords.sumOf { it.miktar }
    val avgMiktar = if (filteredRecords.isNotEmpty()) totalMiktar / filteredRecords.size else 0.0
    val maxMiktar = filteredRecords.maxOfOrNull { it.miktar } ?: 0.0
    val mainUnit = filteredRecords.groupBy { it.birim }
        .maxByOrNull { it.value.size }?.key ?: "m²"

    // Günlük/Haftalık/Aylık gruplama
    val groupedData = remember(filteredRecords, selectedTab) {
        when (selectedTab) {
            0 -> groupByDay(filteredRecords)
            1 -> groupByWeek(filteredRecords)
            2 -> groupByMonth(filteredRecords)
            else -> emptyList()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                    .padding(top = 48.dp, bottom = 8.dp, start = 20.dp, end = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextOnPrimary, fontWeight = FontWeight.Bold)
                            Text("${selectedEmployee?.adSoyad ?: stringResource(R.string.all_employees)} · ${tabs[selectedTab]}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnPrimary.copy(alpha = 0.7f))
                        }
                        Row {
                            IconButton(onClick = { showPdfEmailDialog = true }) {
                                Icon(Icons.Outlined.PictureAsPdf, stringResource(R.string.dashboard_pdf),
                                    tint = TextOnPrimary.copy(alpha = 0.8f))
                            }
                            IconButton(onClick = { viewModel.adminLogout() }) {
                                Icon(Icons.Outlined.Logout, "Çıkış",
                                    tint = TextOnPrimary.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Employee dropdown
                    ExposedDropdownMenuBox(
                        expanded = showEmployeeDropdown,
                        onExpandedChange = { showEmployeeDropdown = it }
                    ) {
                        Surface(
                            color = TextOnPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                                .menuAnchor()
                                .clickable { showEmployeeDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedEmployee?.adSoyad ?: stringResource(R.string.all_employees),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextOnPrimary, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.ArrowDropDown, null, tint = TextOnPrimary.copy(alpha = 0.6f))
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = showEmployeeDropdown,
                            onDismissRequest = { showEmployeeDropdown = false }
                        ) {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text("${emp.adSoyad} (${emp.personelId})") },
                                    onClick = { viewModel.setSelectedEmployee(emp); showEmployeeDropdown = false }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab selector
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TextOnPrimary.copy(alpha = 0.08f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTab == index
                            Surface(
                                color = if (isSelected) Accent else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).clickable { selectedTab = index }
                            ) {
                                Text(tab, style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) TextOnAccent else TextOnPrimary.copy(alpha = 0.6f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stats Cards
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatCard(stringResource(R.string.dashboard_total),
                                formatLargeNumber(totalMiktar), mainUnit, GradientTeal, Modifier.weight(1f))
                            StatCard(stringResource(R.string.dashboard_average),
                                String.format("%.1f", avgMiktar).replace(".", ","),
                                "$mainUnit/gün", GradientAmber, Modifier.weight(1f))
                            StatCard(stringResource(R.string.dashboard_highest),
                                String.format("%.1f", maxMiktar).replace(".", ","),
                                mainUnit, GradientPurple, Modifier.weight(1f))
                        }
                    }

                    if (groupedData.isEmpty()) {
                        item {
                            Text(stringResource(R.string.history_empty), color = TextSecondary,
                                modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = TextAlign.Center)
                        }
                    } else {
                        val maxGroupVal = groupedData.maxOfOrNull { it.value } ?: 1.0
                        items(groupedData.size) { index ->
                            val item = groupedData[index]
                            PerformanceRow(
                                date = item.label,
                                subtitle = item.subtitle,
                                value = "${formatLargeNumber(item.value)} $mainUnit",
                                progress = (item.value / maxGroupVal).toFloat().coerceIn(0.05f, 1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== HELPER FUNCTIONS ====================

data class DashboardGroup(val label: String, val value: Double, val subtitle: String)

fun groupByDay(records: List<PerformanceRecord>): List<DashboardGroup> {
    return records.mapNotNull { rec ->
        val date = DateUtils.parseToDate(rec.tarih)
        if (date != null) Pair(date, rec) else null
    }
        .groupBy { DateUtils.formatDashboardDay(it.first) }
        .map { (dateStr, pairs) ->
            val dayOfWeek = DateUtils.getDayOfWeek(pairs.first().first)
            DashboardGroup(dateStr, pairs.sumOf { it.second.miktar }, dayOfWeek)
        }
        .sortedByDescending { it.label }
        .take(14)
}

fun groupByWeek(records: List<PerformanceRecord>): List<DashboardGroup> {
    return records.mapNotNull { rec ->
        val date = DateUtils.parseToDate(rec.tarih)
        if (date != null) Pair(date, rec) else null
    }
        .groupBy { DateUtils.formatDashboardWeek(it.first) }
        .map { (weekStr, pairs) ->
            DashboardGroup(weekStr, pairs.sumOf { it.second.miktar }, "${pairs.size} kayıt")
        }
        .sortedByDescending { it.label }
        .take(8)
}

fun groupByMonth(records: List<PerformanceRecord>): List<DashboardGroup> {
    return records.mapNotNull { rec ->
        val date = DateUtils.parseToDate(rec.tarih)
        if (date != null) Pair(date, rec) else null
    }
        .groupBy { DateUtils.formatDashboardMonth(it.first) }
        .map { (monthStr, pairs) ->
            DashboardGroup(monthStr, pairs.sumOf { it.second.miktar }, "${pairs.size} kayıt")
        }
        .sortedByDescending { it.label }
        .take(6)
}

fun formatLargeNumber(value: Double): String {
    return when {
        value >= 1000 -> String.format("%.1fK", value / 1000).replace(".", ",")
        value == value.toLong().toDouble() -> value.toLong().toString()
        else -> String.format("%.1f", value).replace(".", ",")
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, gradientColors: List<Color>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxWidth()
            .background(Brush.linearGradient(gradientColors)).padding(10.dp)) {
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f), letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontFamily = JetBrainsMono),
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(unit, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun PerformanceRow(date: String, subtitle: String, value: String, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.3f)) {
                Text(date, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Box(modifier = Modifier.weight(0.4f).height(6.dp)
                .clip(RoundedCornerShape(3.dp)).background(Border)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(GradientTeal)))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                color = TextPrimary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.3f), textAlign = TextAlign.End)
        }
    }
}
