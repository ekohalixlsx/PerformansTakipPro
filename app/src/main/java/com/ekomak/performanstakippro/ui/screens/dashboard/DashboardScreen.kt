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
import com.ekomak.performanstakippro.data.model.Employee
import com.ekomak.performanstakippro.data.model.PerformanceRecord
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    val isLoading by viewModel.isLoadingRecords.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showEmployeeDropdown by remember { mutableStateOf(false) }

    val tabs = listOf(
        stringResource(R.string.dashboard_daily),
        stringResource(R.string.dashboard_weekly),
        stringResource(R.string.dashboard_monthly)
    )

    // Seçili personele göre kayıtları filtrele
    val filteredRecords = remember(records, selectedEmployee) {
        if (selectedEmployee != null) {
            records.filter { it.personelId == selectedEmployee!!.personelId }
        } else records
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
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                            Text(
                                stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextOnPrimary, fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${selectedEmployee?.adSoyad ?: stringResource(R.string.all_employees)} · ${tabs[selectedTab]}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnPrimary.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { /* TODO: PDF */ }) {
                            Icon(Icons.Outlined.PictureAsPdf, stringResource(R.string.dashboard_pdf),
                                tint = TextOnPrimary.copy(alpha = 0.8f))
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clickable { showEmployeeDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedEmployee?.adSoyad ?: stringResource(R.string.all_employees),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextOnPrimary, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Filled.ArrowDropDown, null, tint = TextOnPrimary.copy(alpha = 0.6f))
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = showEmployeeDropdown,
                            onDismissRequest = { showEmployeeDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.all_employees)) },
                                onClick = {
                                    viewModel.setSelectedEmployee(null)
                                    showEmployeeDropdown = false
                                }
                            )
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text("${emp.adSoyad} (${emp.personelId})") },
                                    onClick = {
                                        viewModel.setSelectedEmployee(emp)
                                        showEmployeeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                    textAlign = TextAlign.Center
                                )
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stats Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(stringResource(R.string.dashboard_total),
                                formatLargeNumber(totalMiktar), mainUnit,
                                GradientTeal, Modifier.weight(1f))
                            StatCard(stringResource(R.string.dashboard_average),
                                String.format("%.1f", avgMiktar).replace(".", ","),
                                "$mainUnit/gün", GradientAmber, Modifier.weight(1f))
                            StatCard(stringResource(R.string.dashboard_highest),
                                String.format("%.1f", maxMiktar).replace(".", ","),
                                mainUnit, GradientPurple, Modifier.weight(1f))
                        }
                    }

                    // Data rows
                    if (groupedData.isEmpty()) {
                        item {
                            Text(stringResource(R.string.history_empty), color = TextSecondary,
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                textAlign = TextAlign.Center)
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
    val format = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))
    val displayFormat = SimpleDateFormat("d MMM", Locale("tr"))
    val dayFormat = SimpleDateFormat("EEEE", Locale("tr"))

    return records.groupBy { it.tarih }
        .map { (date, recs) ->
            val parsedDate = try { format.parse(date) } catch (_: Exception) { null }
            val display = if (parsedDate != null) displayFormat.format(parsedDate) else date
            val day = if (parsedDate != null) dayFormat.format(parsedDate).replaceFirstChar { it.uppercase() } else ""
            DashboardGroup(display, recs.sumOf { it.miktar }, day)
        }
        .sortedByDescending { it.label }
        .take(14)
}

fun groupByWeek(records: List<PerformanceRecord>): List<DashboardGroup> {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))
    val cal = Calendar.getInstance()

    return records.mapNotNull { rec ->
        val date = try { format.parse(rec.tarih) } catch (_: Exception) { null }
        if (date != null) { cal.time = date; Pair(cal.get(Calendar.WEEK_OF_YEAR), rec) } else null
    }
        .groupBy { it.first }
        .map { (week, pairs) ->
            DashboardGroup("Hafta $week", pairs.sumOf { it.second.miktar }, "${pairs.size} kayıt")
        }
        .sortedByDescending { it.label }
        .take(8)
}

fun groupByMonth(records: List<PerformanceRecord>): List<DashboardGroup> {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))
    val monthFormat = SimpleDateFormat("MMMM", Locale("tr"))
    val yearFormat = SimpleDateFormat("yyyy", Locale("tr"))

    return records.mapNotNull { rec ->
        val date = try { format.parse(rec.tarih) } catch (_: Exception) { null }
        if (date != null) Pair(SimpleDateFormat("yyyy-MM", Locale("tr")).format(date), rec) else null
    }
        .groupBy { it.first }
        .map { (_, pairs) ->
            val date = format.parse(pairs.first().second.tarih)!!
            val month = monthFormat.format(date).replaceFirstChar { it.uppercase() }
            val year = yearFormat.format(date)
            DashboardGroup(month, pairs.sumOf { it.second.miktar }, year)
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
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(12.dp)
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f), letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(3.dp))
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
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.3f)) {
                Text(date, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Box(
                modifier = Modifier.weight(0.45f).height(6.dp)
                    .clip(RoundedCornerShape(3.dp)).background(Border)
            ) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(GradientTeal)))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                color = TextPrimary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.25f), textAlign = TextAlign.End)
        }
    }
}
