package com.ekomak.performanstakippro.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ekomak.performanstakippro.data.model.*
import com.ekomak.performanstakippro.data.remote.AppConfig
import com.ekomak.performanstakippro.data.remote.SheetsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Uygulamanın ana ViewModel'i.
 * Tüm ekranlar bu ViewModel üzerinden Google Sheets verilerine erişir.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val sheetsService = SheetsService(AppConfig.SHEETS_SCRIPT_URL)

    // ==================== STATE ====================

    // Employees
    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    // Work Types
    private val _workTypes = MutableStateFlow<List<WorkType>>(emptyList())
    val workTypes: StateFlow<List<WorkType>> = _workTypes.asStateFlow()

    // Departments
    private val _departments = MutableStateFlow<List<Department>>(emptyList())
    val departments: StateFlow<List<Department>> = _departments.asStateFlow()

    // App Users (KULLANICILAR)
    private val _appUsers = MutableStateFlow<List<AppUser>>(emptyList())
    val appUsers: StateFlow<List<AppUser>> = _appUsers.asStateFlow()

    val developerEmail: String get() = _appUsers.value
        .firstOrNull { it.rol.uppercase() == "DEVELOPER" }?.email ?: ""

    // Records
    private val _records = MutableStateFlow<List<PerformanceRecord>>(emptyList())
    val records: StateFlow<List<PerformanceRecord>> = _records.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingRecords = MutableStateFlow(false)
    val isLoadingRecords: StateFlow<Boolean> = _isLoadingRecords.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Connection status
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // User Settings
    private val _selectedEmployee = MutableStateFlow<Employee?>(null)
    val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

    private val _defaultWorkType = MutableStateFlow<WorkType?>(null)
    val defaultWorkType: StateFlow<WorkType?> = _defaultWorkType.asStateFlow()

    // Admin
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    init {
        // Uygulama açılışında verileri yükle
        loadAllData()
        // Beni hatırla kontrolü
        val rememberMe = prefs.getBoolean("admin_remember_me", false)
        if (rememberMe) {
            _isAdminLoggedIn.value = true
        }
        // Kaydedilmiş personel ID'sini yükle
        val savedEmployeeId = prefs.getInt("selected_employee_id", -1)
        if (savedEmployeeId != -1) {
            // employees yüklendikten sonra eşleştirilecek
        }
    }

    // ==================== VERİ YÜKLEME ====================

    fun loadAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Paralel yükleme
                val employeesJob = launch { loadEmployees() }
                val workTypesJob = launch { loadWorkTypes() }
                val departmentsJob = launch { loadDepartments() }
                val usersJob = launch { loadUsers() }

                employeesJob.join()
                workTypesJob.join()
                departmentsJob.join()
                usersJob.join()

                _isConnected.value = true

                // Kaydedilmiş personeli eşleştir
                val savedEmployeeId = prefs.getInt("selected_employee_id", -1)
                if (savedEmployeeId != -1) {
                    _selectedEmployee.value = _employees.value.find { it.personelId == savedEmployeeId }
                }

                // Kaydedilmiş iş türünü eşleştir
                val savedWorkTypeId = prefs.getInt("default_work_type_id", -1)
                if (savedWorkTypeId != -1) {
                    _defaultWorkType.value = _workTypes.value.find { it.islemId == savedWorkTypeId }
                }

                // Kayıtları yükle
                loadRecords()

            } catch (e: Exception) {
                _error.value = e.message ?: "Bağlantı hatası"
                _isConnected.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadEmployees() {
        sheetsService.getEmployees().fold(
            onSuccess = { _employees.value = it },
            onFailure = { _error.value = "Personel listesi yüklenemedi: ${it.message}" }
        )
    }

    private suspend fun loadWorkTypes() {
        sheetsService.getWorkTypes().fold(
            onSuccess = { _workTypes.value = it },
            onFailure = { _error.value = "İş türleri yüklenemedi: ${it.message}" }
        )
    }

    private suspend fun loadDepartments() {
        sheetsService.getDepartments().fold(
            onSuccess = { _departments.value = it },
            onFailure = { _error.value = "Bölümler yüklenemedi: ${it.message}" }
        )
    }

    private suspend fun loadUsers() {
        sheetsService.getUsers().fold(
            onSuccess = { _appUsers.value = it },
            onFailure = { /* Kullanıcılar opsiyonel, hata gösterme */ }
        )
    }

    fun loadRecords(employeeName: String? = null) {
        viewModelScope.launch {
            _isLoadingRecords.value = true
            sheetsService.getRecords(employeeName = employeeName).fold(
                onSuccess = { _records.value = it },
                onFailure = { _error.value = "Kayıtlar yüklenemedi: ${it.message}" }
            )
            _isLoadingRecords.value = false
        }
    }

    // ==================== VERİ YAZMA ====================

    fun saveRecord(
        tarih: String,
        employee: Employee,
        workType: WorkType,
        miktar: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val timestamp = java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault())
                .format(java.util.Date())

            val record = PerformanceRecord(
                kayitId = "",
                tarih = tarih,
                personelId = employee.personelId,
                adSoyad = employee.adSoyad,
                bolumAdi = employee.bolumAdi,
                islemAdi = workType.islemAdi,
                miktar = miktar,
                birim = workType.birim,
                created = timestamp
            )

            sheetsService.saveRecord(record).fold(
                onSuccess = {
                    loadRecords() // Kayıtları yenile
                    onSuccess()
                },
                onFailure = { onError(it.message ?: "Kayıt eklenemedi") }
            )
        }
    }

    fun deleteRecord(kayitId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            sheetsService.deleteRecord(kayitId).fold(
                onSuccess = {
                    loadRecords()
                    onSuccess()
                },
                onFailure = { onError(it.message ?: "Kayıt silinemedi") }
            )
        }
    }

    fun updateRecord(record: PerformanceRecord, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            sheetsService.updateRecord(record).fold(
                onSuccess = {
                    loadRecords()
                    onSuccess()
                },
                onFailure = { onError(it.message ?: "Kayıt güncellenemedi") }
            )
        }
    }

    // ==================== KULLANICI AYARLARI ====================

    fun setSelectedEmployee(employee: Employee?) {
        _selectedEmployee.value = employee
        prefs.edit().putInt("selected_employee_id", employee?.personelId ?: -1).apply()
    }

    fun setDefaultWorkType(workType: WorkType?) {
        _defaultWorkType.value = workType
        prefs.edit().putInt("default_work_type_id", workType?.islemId ?: -1).apply()
    }

    fun clearError() {
        _error.value = null
    }

    // ==================== ADMİN ====================

    fun adminLogin(username: String, password: String): Boolean {
        val savedUsername = prefs.getString("admin_username", "eko") ?: "eko"
        val savedPassword = prefs.getString("admin_password", "eko2026") ?: "eko2026"

        return if (username == savedUsername && password == savedPassword) {
            _isAdminLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun adminLogout() {
        _isAdminLoggedIn.value = false
        prefs.edit().putBoolean("admin_remember_me", false).apply()
    }

    fun setAdminRememberMe(remember: Boolean) {
        prefs.edit().putBoolean("admin_remember_me", remember).apply()
    }

    fun changeAdminCredentials(newUsername: String, newPassword: String) {
        prefs.edit()
            .putString("admin_username", newUsername)
            .putString("admin_password", newPassword)
            .apply()
    }
}
