package com.ekomak.performanstakippro.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.data.model.PerformanceRecord
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.theme.*
import com.ekomak.performanstakippro.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val records by viewModel.records.collectAsState()
    val isLoading by viewModel.isLoadingRecords.collectAsState()
    val selectedEmployee by viewModel.selectedEmployee.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<PerformanceRecord?>(null) }
    var showEditDialog by remember { mutableStateOf<PerformanceRecord?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale("tr")).format(Date())
    val yesterdayStr = SimpleDateFormat("dd/MM/yyyy", Locale("tr")).format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    )
    val todayDisplay = SimpleDateFormat("d MMMM", Locale("tr")).format(Date()).uppercase()
    val yesterdayDisplay = SimpleDateFormat("d MMMM", Locale("tr")).format(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    ).uppercase()

    // Normalize tarih ve grupla
    val groupedRecords = remember(records) {
        records.groupBy { record ->
            val normalDate = DateUtils.normalizeDate(record.tarih)
            when (normalDate) {
                todayStr -> "BUGÜN — $todayDisplay"
                yesterdayStr -> "DÜN — $yesterdayDisplay"
                else -> {
                    val displayDate = DateUtils.formatForDisplay(normalDate)
                    displayDate ?: normalDate
                }
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
                }) { Text(stringResource(R.string.history_yes), color = Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.history_no))
                }
            }
        )
    }

    // Düzenleme dialogu
    showEditDialog?.let { record ->
        EditRecordDialog(
            record = record,
            onDismiss = { showEditDialog = null },
            onSave = { updatedRecord ->
                viewModel.updateRecord(
                    record = updatedRecord,
                    onSuccess = { showEditDialog = null },
                    onError = { showEditDialog = null }
                )
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                    .padding(top = 48.dp, bottom = 14.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.history_title),
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextOnPrimary, fontWeight = FontWeight.Bold)
                        Text("${selectedEmployee?.adSoyad ?: ""} · ${stringResource(R.string.history_subtitle)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnPrimary.copy(alpha = 0.7f))
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
                        Icon(Icons.Outlined.Inbox, null, tint = TextSecondary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(stringResource(R.string.history_empty), color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mini Bar Chart
                    item {
                        MiniBarChart(records = records.take(15))
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    groupedRecords.forEach { (dateGroup, dateRecords) ->
                        item {
                            Text(dateGroup, style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary, letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp))
                        }
                        items(dateRecords) { record ->
                            RecordCard(
                                record = record,
                                onDelete = { showDeleteDialog = record },
                                onEdit = { showEditDialog = record }
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
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .height(32.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            records.reversed().forEach { record ->
                val fraction = (record.miktar / maxVal).toFloat().coerceIn(0.1f, 1f)
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                        .background(Brush.verticalGradient(GradientTeal))
                )
            }
        }
    }
}

@Composable
fun RecordCard(record: PerformanceRecord, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol: İsim, bölüm, iş türü
            Column(modifier = Modifier.weight(1f)) {
                Text(record.adSoyad, style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("${record.bolumAdi} · ${record.islemAdi}",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            // Sağ: Miktar + butonlar
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(record.formattedMiktar,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                        color = Accent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(record.birim, style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary, modifier = Modifier.padding(bottom = 1.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Edit, null, tint = Accent, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, null, tint = Danger, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecordDialog(
    record: PerformanceRecord,
    onDismiss: () -> Unit,
    onSave: (PerformanceRecord) -> Unit
) {
    var editMiktar by remember { mutableStateOf(record.formattedMiktar) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_edit)) },
        text = {
            Column {
                Text("${record.adSoyad} · ${record.islemAdi}",
                    style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = editMiktar,
                    onValueChange = { newVal ->
                        val cleaned = newVal.replace(".", ",")
                        if (cleaned.isEmpty() || cleaned.matches(Regex("^\\d*,?\\d*$"))) {
                            editMiktar = cleaned
                        }
                    },
                    label = { Text(stringResource(R.string.entry_quantity)) },
                    suffix = { Text(record.birim) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newMiktar = editMiktar.replace(",", ".").toDoubleOrNull()
                if (newMiktar != null && newMiktar > 0) {
                    onSave(record.copy(miktar = newMiktar))
                }
            }) { Text(stringResource(R.string.entry_save), color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
