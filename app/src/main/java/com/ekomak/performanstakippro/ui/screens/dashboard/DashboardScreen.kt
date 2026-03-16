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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.dashboard_daily),
        stringResource(R.string.dashboard_weekly),
        stringResource(R.string.dashboard_monthly)
    )

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
                                text = stringResource(R.string.dashboard_title),
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextOnPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ALİ CAN · ${tabs[selectedTab]}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnPrimary.copy(alpha = 0.7f)
                            )
                        }

                        // PDF and Employee selector
                        Row {
                            IconButton(onClick = { }) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = stringResource(R.string.dashboard_pdf),
                                    tint = TextOnPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Employee dropdown
                    Surface(
                        color = TextOnPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ALİ CAN",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextOnPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = TextOnPrimary.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = index }
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) TextOnAccent else TextOnPrimary.copy(alpha = 0.6f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Dashboard Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = stringResource(R.string.dashboard_total),
                            value = "2.5K",
                            unit = "m²",
                            gradientColors = GradientTeal,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.dashboard_average),
                            value = "614,81",
                            unit = "m²/hafta",
                            gradientColors = GradientAmber,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = stringResource(R.string.dashboard_highest),
                            value = "709,25",
                            unit = "m²",
                            gradientColors = GradientPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Performance items based on tab
                when (selectedTab) {
                    0 -> { // Daily
                        val dailyData = listOf(
                            Triple("10 Mar", "Pazartesi", "98,5 m²"),
                            Triple("11 Mar", "Salı", "112 m²"),
                            Triple("12 Mar", "Çarşamba", "105,25 m²"),
                            Triple("13 Mar", "Perşembe", "88 m²"),
                            Triple("14 Mar", "Cuma", "120,5 m²"),
                            Triple("15 Mar", "Cumartesi", "60 m²"),
                        )
                        items(dailyData.size) { index ->
                            val (date, day, amount) = dailyData[index]
                            PerformanceRow(
                                date = date,
                                subtitle = day,
                                value = amount,
                                progress = (index + 1).toFloat() / dailyData.size
                            )
                        }
                    }
                    1 -> { // Weekly
                        val weeklyData = listOf(
                            Triple("Hafta 8", "17–23 Şub", "560 m²"),
                            Triple("Hafta 9", "24 Şub–2 Mar", "610 m²"),
                            Triple("Hafta 10", "3–9 Mar", "580 m²"),
                            Triple("Hafta 11", "10–16 Mar", "709,25 m²"),
                        )
                        items(weeklyData.size) { index ->
                            val (week, range, amount) = weeklyData[index]
                            PerformanceRow(
                                date = week,
                                subtitle = range,
                                value = amount,
                                progress = (index + 1).toFloat() / weeklyData.size
                            )
                        }
                    }
                    2 -> { // Monthly
                        val monthlyData = listOf(
                            Triple("Ocak", "2026", "2.6K m²"),
                            Triple("Şubat", "2026", "2.4K m²"),
                            Triple("Mart", "2026", "1.9K m²"),
                        )
                        items(monthlyData.size) { index ->
                            val (month, year, amount) = monthlyData[index]
                            PerformanceRow(
                                date = month,
                                subtitle = year,
                                value = amount,
                                progress = (index + 1).toFloat() / monthlyData.size
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(gradientColors)
                )
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = JetBrainsMono
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun PerformanceRow(
    date: String,
    subtitle: String,
    value: String,
    progress: Float
) {
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.3f)) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(GradientTeal)
                        )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Value
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono
                ),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(0.25f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}
