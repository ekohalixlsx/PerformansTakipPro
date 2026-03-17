package com.ekomak.performanstakippro.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsState()
    val isLoading by viewModel.isLoadingRecords.collectAsState()
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<PerformanceRecord?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    // Bugünün ve dünün tarihini hesapla
    val today = SimpleDateFormat("dd/MM/yyyy", Locale("tr")).format(Date())
    val yesterday = SimpleDateFormat("dd/MM/yyyy", Locale("tr")).format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    )
    val todayDisplay = SimpleDateFormat("d MMMM", Locale("tr")).format(Date()).uppercase()
    val yesterdayDisplay = SimpleDateFormat("d MMMM", Locale("tr")).format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    ).uppercase()

    // Gruplama
    val groupedRecords = remember(records) {
        records.groupBy { record ->
            when (record.tarih) {
                today -> "BUGÜN — $todayDisplay"
                yesterday -> "DÜN — $yesterdayDisplay"
                else -> record.tarih
            }
        }
    }

    // Silme onay dialogu
    showDeleteDialog?.let { record ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.history_delete)) },
            text = { Text(stringResource(R.string.history_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord(
                        kayitId = record.kayitId,
                        onSuccess = { showDeleteDialog = null },
                        onError = { deleteError = it; showDeleteDialog = null }
                    )
                }) {
                    Text(stringResource(R.string.history_yes), color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.history_no))
                }
            }
        )
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
                    .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.history_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextOnPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedEmployee?.adSoyad ?: ""} · ${stringResource(R.string.history_subtitle)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnPrimary.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { viewModel.loadRecords() }) {
                        Icon(Icons.Outlined.Refresh, null, tint = TextOnPrimary.copy(alpha = 0.8f))
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
            } else if (records.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Inbox, null, tint = TextSecondary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.history_empty), color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mini Bar Chart (son kayıtların miktarları)
                    item {
                        MiniBarChart(records = records.take(15))
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    groupedRecords.forEach { (dateGroup, dateRecords) ->
                        item {
                            Text(
                                text = dateGroup,
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 2.dp)
                            )
                        }
                        itemsIndexed(dateRecords) { _, record ->
                            RecordCard(
                                record = record,
                                onDelete = { showDeleteDialog = record },
                                onEdit = { /* TODO: Edit dialog */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniBarChart(records: List<PerformanceRecord>) {
    if (records.isEmpty()) return

    val maxVal = records.maxOfOrNull { it.miktar } ?: 1.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            records.reversed().forEach { record ->
                val fraction = (record.miktar / maxVal).toFloat().coerceIn(0.1f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(Brush.verticalGradient(GradientTeal))
                )
            }
        }
    }
}

@Composable
fun RecordCard(
    record: PerformanceRecord,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.adSoyad, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(record.bolumAdi, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                val time = if (record.created.length >= 12) {
                    "${record.created.substring(8, 10)}:${record.created.substring(10, 12)}"
                } else ""
                Text(time, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Surface(color = Primary.copy(alpha = 0.08f), shape = RoundedCornerShape(6.dp)) {
                    Text(record.islemAdi, style = MaterialTheme.typography.labelMedium, color = Primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(record.formattedMiktar,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMono),
                        color = Accent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(record.birim, style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                    Icon(Icons.Outlined.Edit, null, tint = Accent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.history_edit), color = Accent, style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)) {
                    Icon(Icons.Outlined.DeleteOutline, null, tint = Danger, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.history_delete), color = Danger, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
