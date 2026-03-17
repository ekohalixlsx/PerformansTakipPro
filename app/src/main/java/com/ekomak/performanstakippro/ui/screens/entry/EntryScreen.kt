package com.ekomak.performanstakippro.ui.screens.entry

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekomak.performanstakippro.R
import com.ekomak.performanstakippro.data.model.Employee
import com.ekomak.performanstakippro.data.model.WorkType
import com.ekomak.performanstakippro.ui.MainViewModel
import com.ekomak.performanstakippro.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(viewModel: MainViewModel) {
    val employees by viewModel.employees.collectAsState()
    val workTypes by viewModel.workTypes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val savedEmployee by viewModel.selectedEmployee.collectAsState()
    val savedWorkType by viewModel.defaultWorkType.collectAsState()

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedEmployee by remember { mutableStateOf<com.ekomak.performanstakippro.data.model.Employee?>(null) }
    var selectedWorkType by remember { mutableStateOf<com.ekomak.performanstakippro.data.model.WorkType?>(null) }
    var initialized by remember { mutableStateOf(false) }

    // İlk açılışta Ayarlar'daki seçimleri kullan, sonra bağımsız
    LaunchedEffect(savedEmployee, savedWorkType) {
        if (!initialized && (savedEmployee != null || savedWorkType != null)) {
            if (selectedEmployee == null) selectedEmployee = savedEmployee
            if (selectedWorkType == null) selectedWorkType = savedWorkType
            initialized = true
        }
    }
    var quantity by remember { mutableStateOf("") }
    var showEmployeeSheet by remember { mutableStateOf(false) }
    var showWorkTypeSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var employeeSearch by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
    val saveDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))
    val scrollState = rememberScrollState()

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Calendar.getInstance().apply { timeInMillis = it }
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Employee Bottom Sheet — LazyColumn ile scroll edilebilir
    if (showEmployeeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEmployeeSheet = false; employeeSearch = "" },
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.entry_employee).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = employeeSearch,
                    onValueChange = { employeeSearch = it },
                    placeholder = { Text(stringResource(R.string.entry_search_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val filtered = employees.filter {
                employeeSearch.isEmpty() ||
                it.adSoyad.contains(employeeSearch, ignoreCase = true) ||
                it.personelId.toString().contains(employeeSearch)
            }

            if (filtered.isEmpty()) {
                Text(
                    text = if (isLoading) stringResource(R.string.loading) else "Personel bulunamadı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 16.dp)
            ) {
                items(filtered) { emp ->
                    Surface(
                        onClick = {
                            selectedEmployee = emp
                            showEmployeeSheet = false
                            employeeSearch = ""
                        },
                        color = if (selectedEmployee?.personelId == emp.personelId)
                            Accent.copy(alpha = 0.08f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emp.initials, color = Accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(emp.adSoyad, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
                                Text(emp.bolumAdi, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Text(
                                emp.personelId.toString(),
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMono),
                                color = TextSecondary
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Work Type Bottom Sheet — LazyColumn ile scroll edilebilir
    if (showWorkTypeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWorkTypeSheet = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.entry_work_type).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 16.dp)
            ) {
                items(workTypes) { wt ->
                    Surface(
                        onClick = {
                            selectedWorkType = wt
                            showWorkTypeSheet = false
                        },
                        color = if (selectedWorkType?.islemId == wt.islemId)
                            Accent.copy(alpha = 0.08f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Construction, null, tint = Accent, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(wt.islemAdi, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                            Surface(color = Accent.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                Text(wt.birim, style = MaterialTheme.typography.labelSmall, color = Accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                    .padding(top = 48.dp, bottom = 10.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextOnPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.entry_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextOnPrimary.copy(alpha = 0.7f)
                        )
                    }
                    // App icon — daha büyük
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Date Field
                    Text(stringResource(R.string.entry_date).uppercase(),
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.sp)
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CalendarMonth, null, tint = Accent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(dateFormat.format(selectedDate.time), style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary)
                        }
                    }

                    HorizontalDivider(color = Divider, thickness = 0.5.dp)

                    // Employee Selection
                    Text(stringResource(R.string.entry_employee).uppercase(),
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedCard(
                            onClick = { showEmployeeSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (showEmployeeSheet) Accent else Border),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedEmployee?.adSoyad ?: stringResource(R.string.select),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedEmployee == null) TextSecondary else TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary)
                            }
                        }
                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Border),
                            colors = CardDefaults.outlinedCardColors(containerColor = SurfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(stringResource(R.string.entry_employee_id), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    text = selectedEmployee?.personelId?.toString() ?: "—",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
                                    color = TextPrimary, fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Department (auto)
                    Text(stringResource(R.string.entry_department).uppercase(),
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.sp)
                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Border),
                        colors = CardDefaults.outlinedCardColors(containerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedEmployee?.bolumAdi ?: "—",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedEmployee == null) TextSecondary else TextPrimary
                            )
                            Surface(color = Accent.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                Text(stringResource(R.string.entry_department_auto),
                                    style = MaterialTheme.typography.labelSmall, color = Accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = Divider, thickness = 0.5.dp)

                    // Work Type Selection
                    Text(stringResource(R.string.entry_work_type).uppercase(),
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.sp)
                    OutlinedCard(
                        onClick = { showWorkTypeSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (showWorkTypeSheet) Accent else Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedWorkType?.islemAdi ?: stringResource(R.string.select),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedWorkType == null) TextSecondary else TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary)
                        }
                    }

                    // Quantity Input
                    Text(stringResource(R.string.entry_quantity).uppercase(),
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { newValue ->
                            val cleaned = newValue.replace(".", ",")
                            if (cleaned.isEmpty() || cleaned.matches(Regex("^\\d*,?\\d*$"))) {
                                quantity = cleaned
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 26.sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent, unfocusedBorderColor = Border,
                            cursorColor = Accent, focusedContainerColor = Accent.copy(alpha = 0.03f)
                        )
                    )

                    // Unit (auto)
                    Text(stringResource(R.string.entry_unit).uppercase(),
                        style = MaterialTheme.typography.labelMedium, color = TextSecondary, letterSpacing = 1.sp)
                    OutlinedCard(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Border),
                        colors = CardDefaults.outlinedCardColors(containerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedWorkType?.birim ?: "—",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedWorkType == null) TextSecondary else TextPrimary
                            )
                            Surface(color = Accent.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                                Text(stringResource(R.string.entry_unit_auto),
                                    style = MaterialTheme.typography.labelSmall, color = Accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    val emp = selectedEmployee
                    val wt = selectedWorkType
                    val miktarStr = quantity.replace(",", ".")
                    val miktarVal = miktarStr.toDoubleOrNull()

                    if (emp == null || wt == null || miktarVal == null || miktarVal <= 0) {
                        saveError = "Lütfen tüm alanları doldurunuz"
                        return@Button
                    }

                    isSaving = true
                    viewModel.saveRecord(
                        tarih = saveDateFormat.format(selectedDate.time),
                        employee = emp,
                        workType = wt,
                        miktar = miktarVal,
                        onSuccess = {
                            isSaving = false
                            saveSuccess = true
                            quantity = ""
                        },
                        onError = { msg ->
                            isSaving = false
                            saveError = msg
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = TextOnAccent),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = TextOnAccent, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.entry_save), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Success Snackbar
        AnimatedVisibility(
            visible = saveSuccess,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        ) {
            LaunchedEffect(saveSuccess) {
                if (saveSuccess) { kotlinx.coroutines.delay(2000); saveSuccess = false }
            }
            Surface(color = Success, shape = RoundedCornerShape(12.dp), shadowElevation = 8.dp,
                modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.entry_save_success), color = Color.White,
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Error Snackbar
        AnimatedVisibility(
            visible = saveError != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        ) {
            LaunchedEffect(saveError) {
                if (saveError != null) { kotlinx.coroutines.delay(3000); saveError = null }
            }
            Surface(color = Danger, shape = RoundedCornerShape(12.dp), shadowElevation = 8.dp,
                modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Error, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(saveError ?: "", color = Color.White,
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
