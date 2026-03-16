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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.data.model.PerformanceRecord
import com.ekomak.performanstakippro.ui.theme.*

@Composable
fun HistoryScreen() {
    // Sample data for UI demonstration
    val sampleRecords = remember {
        listOf(
            PerformanceRecord("0001", "16/03/2026", 1234, "ALİ CAN", "A BÖLÜMÜ", "İŞLEM 1", 120.50, "m²", "20260316111825"),
            PerformanceRecord("0002", "16/03/2026", 1234, "ALİ CAN", "A BÖLÜMÜ", "İŞLEM 2", 85.0, "Kg", "20260316142030"),
            PerformanceRecord("0003", "15/03/2026", 1234, "ALİ CAN", "A BÖLÜMÜ", "İŞLEM 1", 125.00, "m²", "20260315163000"),
            PerformanceRecord("0004", "14/03/2026", 1234, "ALİ CAN", "A BÖLÜMÜ", "İŞLEM 1", 110.75, "m²", "20260314170200"),
        )
    }

    // Group records by category (today, yesterday, date)
    val groupedRecords = remember(sampleRecords) {
        sampleRecords.groupBy { record ->
            when (record.tarih) {
                "16/03/2026" -> "BUGÜN — 16 MART"
                "15/03/2026" -> "DÜN — 15 MART"
                else -> record.tarih
            }
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
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Primary, PrimaryLight)
                        )
                    )
                    .padding(top = 48.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
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
                            text = "ALİ CAN · ${stringResource(R.string.history_subtitle)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnPrimary.copy(alpha = 0.7f)
                        )
                    }

                    // Filter icon
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Outlined.PersonSearch,
                            contentDescription = null,
                            tint = TextOnPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Records List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedRecords.forEach { (dateGroup, records) ->
                    // Date Header
                    item {
                        Text(
                            text = dateGroup,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // Record Cards
                    itemsIndexed(records) { index, record ->
                        RecordCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordCard(record: PerformanceRecord) {
    var showActions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.adSoyad,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = record.bolumAdi,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    // Time from created timestamp
                    val time = if (record.created.length >= 12) {
                        "${record.created.substring(8, 10)}:${record.created.substring(10, 12)}"
                    } else ""
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Work type chip
                Surface(
                    color = Primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = record.islemAdi,
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Quantity
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = record.formattedMiktar,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMono
                        ),
                        color = Accent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = record.birim,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Action buttons
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { /* TODO: Edit */ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.history_edit),
                        color = Accent,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                TextButton(
                    onClick = { /* TODO: Delete */ },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = Danger,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.history_delete),
                        color = Danger,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
