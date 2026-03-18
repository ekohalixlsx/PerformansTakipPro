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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var showPasswordChangeDialog by remember { mutableStateOf(false) }

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

    // Premium Admin Login Dialog
    if (showAdminLoginDialog && !isAdminLoggedIn) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var rememberMe by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { showAdminLoginDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .shadow(24.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.verticalGradient(listOf(
                            Color(0xFF1A1F36), Color(0xFF0F1425)
                        )))
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shield Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.radialGradient(listOf(
                                Accent.copy(alpha = 0.25f), Color.Transparent
                            ))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Shield, null,
                            tint = Accent, modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Admin Girişi",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Lütfen bilgilerinizi girin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f))

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; adminLoginError = false },
                        placeholder = { Text("Kullanıcı Adı", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Outlined.Person, null, tint = Accent.copy(alpha = 0.7f)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        isError = adminLoginError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            cursorColor = Accent,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            errorBorderColor = Danger
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; adminLoginError = false },
                        placeholder = { Text("Şifre", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Accent.copy(alpha = 0.7f)) },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        isError = adminLoginError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            cursorColor = Accent,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                            errorBorderColor = Danger
                        )
                    )

                    if (adminLoginError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kullanıcı adı veya şifre hatalı",
                            style = MaterialTheme.typography.bodySmall, color = Danger)
                    }

                    // Remember Me
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Accent,
                                uncheckedColor = Color.White.copy(alpha = 0.4f),
                                checkmarkColor = Color.White
                            ))
                        Text("Beni hatırla", style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Login Button — gradient
                    Button(
                        onClick = {
                            if (viewModel.adminLogin(username, password)) {
                                if (rememberMe) viewModel.setAdminRememberMe(true)
                                showAdminLoginDialog = false
                                adminLoginError = false
                            } else {
                                adminLoginError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(
                                    Accent, Color(0xFF8B5CF6)
                                )), shape = RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Giriş Yap", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { showAdminLoginDialog = false }) {
                        Text("İptal", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }

    // Password Change Dialog
    if (showPasswordChangeDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showPasswordChangeDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(20.dp),
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
                    Icon(Icons.Outlined.Key, null, tint = Accent, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Şifre Değiştir", style = MaterialTheme.typography.titleLarge,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent, unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        cursorColor = Accent,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    )

                    OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it; passwordError = null },
                        placeholder = { Text("Mevcut Şifre", color = Color.White.copy(alpha = 0.4f)) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; passwordError = null },
                        placeholder = { Text("Yeni Şifre", color = Color.White.copy(alpha = 0.4f)) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; passwordError = null },
                        placeholder = { Text("Yeni Şifre (Tekrar)", color = Color.White.copy(alpha = 0.4f)) },
                        singleLine = true, shape = RoundedCornerShape(12.dp),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(), colors = fieldColors)

                    if (passwordError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(passwordError!!, style = MaterialTheme.typography.bodySmall, color = Danger)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { showPasswordChangeDialog = false },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) { Text("İptal") }
                        Button(
                            onClick = {
                                val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                val savedPassword = prefs.getString("admin_password", "eko2026") ?: "eko2026"
                                when {
                                    currentPassword != savedPassword -> passwordError = "Mevcut şifre hatalı"
                                    newPassword.length < 4 -> passwordError = "Şifre en az 4 karakter olmalı"
                                    newPassword != confirmPassword -> passwordError = "Şifreler eşleşmiyor"
                                    else -> {
                                        viewModel.changeAdminCredentials(
                                            prefs.getString("admin_username", "eko") ?: "eko",
                                            newPassword
                                        )
                                        showPasswordChangeDialog = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent)
                        ) { Text("Kaydet", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }

    // Admin giriş yapılmamışsa giriş ekranı göster
    if (!isAdminLoggedIn) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Primary, PrimaryDark))
            ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Shield, null, tint = Accent, modifier = Modifier.size(44.dp))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Yönetici Paneli", color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Dashboard'u görüntülemek için\nyönetici girişi gereklidir.",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { showAdminLoginDialog = true },
                    modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(
                                Accent, Color(0xFF8B5CF6)
                            )), shape = RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Lock, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Giriş Yap", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium, color = Color.White)
                        }
                    }
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
                            IconButton(onClick = { showPasswordChangeDialog = true }) {
                                Icon(Icons.Outlined.Key, "Şifre Değiştir",
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
