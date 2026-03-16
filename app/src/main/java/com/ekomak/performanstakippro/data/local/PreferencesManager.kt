package com.ekomak.performanstakippro.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "perflog_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        val SELECTED_EMPLOYEE_ID = intPreferencesKey("selected_employee_id")
        val SELECTED_EMPLOYEE_NAME = stringPreferencesKey("selected_employee_name")
        val DEFAULT_WORK_TYPE_ID = intPreferencesKey("default_work_type_id")
        val DEFAULT_WORK_TYPE_NAME = stringPreferencesKey("default_work_type_name")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val LAST_ENTRY_EMPLOYEE_ID = intPreferencesKey("last_entry_employee_id")
        val LAST_ENTRY_WORK_TYPE_ID = intPreferencesKey("last_entry_work_type_id")
        val SHEETS_ID = stringPreferencesKey("sheets_id")
    }

    val selectedEmployeeId: Flow<Int?> = context.dataStore.data.map { it[SELECTED_EMPLOYEE_ID] }
    val selectedEmployeeName: Flow<String?> = context.dataStore.data.map { it[SELECTED_EMPLOYEE_NAME] }
    val defaultWorkTypeId: Flow<Int?> = context.dataStore.data.map { it[DEFAULT_WORK_TYPE_ID] }
    val defaultWorkTypeName: Flow<String?> = context.dataStore.data.map { it[DEFAULT_WORK_TYPE_NAME] }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val notificationHour: Flow<Int> = context.dataStore.data.map { it[NOTIFICATION_HOUR] ?: 16 }
    val notificationMinute: Flow<Int> = context.dataStore.data.map { it[NOTIFICATION_MINUTE] ?: 45 }
    val companyName: Flow<String> = context.dataStore.data.map { it[COMPANY_NAME] ?: "EKOMAKHALI" }
    val appLanguage: Flow<String> = context.dataStore.data.map { it[APP_LANGUAGE] ?: "tr" }
    val lastEntryEmployeeId: Flow<Int?> = context.dataStore.data.map { it[LAST_ENTRY_EMPLOYEE_ID] }
    val lastEntryWorkTypeId: Flow<Int?> = context.dataStore.data.map { it[LAST_ENTRY_WORK_TYPE_ID] }
    val sheetsId: Flow<String?> = context.dataStore.data.map { it[SHEETS_ID] }

    suspend fun setSelectedEmployee(id: Int, name: String) {
        context.dataStore.edit {
            it[SELECTED_EMPLOYEE_ID] = id
            it[SELECTED_EMPLOYEE_NAME] = name
        }
    }

    suspend fun setDefaultWorkType(id: Int, name: String) {
        context.dataStore.edit {
            it[DEFAULT_WORK_TYPE_ID] = id
            it[DEFAULT_WORK_TYPE_NAME] = name
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[NOTIFICATION_HOUR] = hour
            it[NOTIFICATION_MINUTE] = minute
        }
    }

    suspend fun setCompanyName(name: String) {
        context.dataStore.edit { it[COMPANY_NAME] = name }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { it[APP_LANGUAGE] = language }
    }

    suspend fun setLastEntryEmployee(id: Int) {
        context.dataStore.edit { it[LAST_ENTRY_EMPLOYEE_ID] = id }
    }

    suspend fun setLastEntryWorkType(id: Int) {
        context.dataStore.edit { it[LAST_ENTRY_WORK_TYPE_ID] = id }
    }

    suspend fun setSheetsId(id: String) {
        context.dataStore.edit { it[SHEETS_ID] = id }
    }
}
