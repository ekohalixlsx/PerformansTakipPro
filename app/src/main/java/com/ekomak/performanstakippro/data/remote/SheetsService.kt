package com.ekomak.performanstakippro.data.remote

import com.ekomak.performanstakippro.data.model.*
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*

class SheetsService(
    private val serviceAccountJson: String,
    private val spreadsheetId: String
) {
    private val sheetsService: Sheets by lazy {
        val credentials = GoogleCredentials
            .fromStream(ByteArrayInputStream(serviceAccountJson.toByteArray()))
            .createScoped(listOf(SheetsScopes.SPREADSHEETS))

        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        Sheets.Builder(httpTransport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName("PerformansTakipPro")
            .build()
    }

    suspend fun getEmployees(): Result<List<Employee>> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "PERSONEL!A2:F")
                .execute()

            val employees = response.getValues()?.mapNotNull { row ->
                try {
                    Employee(
                        durum = row.getOrNull(0)?.toString() ?: "Aktif",
                        personelId = row.getOrNull(1)?.toString()?.toIntOrNull() ?: return@mapNotNull null,
                        adSoyad = row.getOrNull(2)?.toString() ?: return@mapNotNull null,
                        bolumAdi = row.getOrNull(3)?.toString() ?: "",
                        departman = row.getOrNull(4)?.toString() ?: "",
                        gorevi = row.getOrNull(5)?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }?.filter { it.isActive } ?: emptyList()

            Result.success(employees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkTypes(): Result<List<WorkType>> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "Islemler!A2:C")
                .execute()

            val workTypes = response.getValues()?.mapNotNull { row ->
                try {
                    WorkType(
                        islemId = row.getOrNull(0)?.toString()?.toIntOrNull() ?: return@mapNotNull null,
                        islemAdi = row.getOrNull(1)?.toString() ?: return@mapNotNull null,
                        birim = row.getOrNull(2)?.toString() ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            Result.success(workTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecords(
        employeeName: String? = null,
        days: Int = 33
    ): Result<List<PerformanceRecord>> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "KAYITLAR!A2:I")
                .execute()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val cutoffDate = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -days)
            }.time

            val records = response.getValues()?.mapNotNull { row ->
                try {
                    val record = PerformanceRecord(
                        kayitId = row.getOrNull(0)?.toString() ?: return@mapNotNull null,
                        tarih = row.getOrNull(1)?.toString() ?: return@mapNotNull null,
                        personelId = row.getOrNull(2)?.toString()?.toIntOrNull() ?: 0,
                        adSoyad = row.getOrNull(3)?.toString() ?: "",
                        bolumAdi = row.getOrNull(4)?.toString() ?: "",
                        islemAdi = row.getOrNull(5)?.toString() ?: "",
                        miktar = row.getOrNull(6)?.toString()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0,
                        birim = row.getOrNull(7)?.toString() ?: "",
                        created = row.getOrNull(8)?.toString() ?: ""
                    )

                    // Filter by date
                    val recordDate = try { dateFormat.parse(record.tarih) } catch (e: Exception) { null }
                    if (recordDate != null && recordDate.before(cutoffDate)) return@mapNotNull null

                    // Filter by employee name
                    if (employeeName != null && record.adSoyad != employeeName) return@mapNotNull null

                    record
                } catch (e: Exception) {
                    null
                }
            }?.sortedByDescending { it.created } ?: emptyList()

            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveRecord(record: PerformanceRecord): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val values = listOf(
                listOf(
                    record.kayitId,
                    record.tarih,
                    record.personelId.toString(),
                    record.adSoyad,
                    record.bolumAdi,
                    record.islemAdi,
                    record.miktar.toString().replace(".", ","),
                    record.birim,
                    record.created
                )
            )

            val body = com.google.api.services.sheets.v4.model.ValueRange()
                .setValues(values)

            sheetsService.spreadsheets().values()
                .append(spreadsheetId, "KAYITLAR!A:I", body)
                .setValueInputOption("USER_ENTERED")
                .execute()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(kayitId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "KAYITLAR!A:A")
                .execute()

            val rowIndex = response.getValues()?.indexOfFirst {
                it.getOrNull(0)?.toString() == kayitId
            } ?: -1

            if (rowIndex < 0) return@withContext Result.failure(Exception("Record not found"))

            // Clear the row content
            val range = "KAYITLAR!A${rowIndex + 1}:I${rowIndex + 1}"
            val clearBody = com.google.api.services.sheets.v4.model.ClearValuesRequest()
            sheetsService.spreadsheets().values()
                .clear(spreadsheetId, range, clearBody)
                .execute()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecord(record: PerformanceRecord): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "KAYITLAR!A:A")
                .execute()

            val rowIndex = response.getValues()?.indexOfFirst {
                it.getOrNull(0)?.toString() == record.kayitId
            } ?: -1

            if (rowIndex < 0) return@withContext Result.failure(Exception("Record not found"))

            val range = "KAYITLAR!A${rowIndex + 1}:I${rowIndex + 1}"
            val values = listOf(
                listOf(
                    record.kayitId,
                    record.tarih,
                    record.personelId.toString(),
                    record.adSoyad,
                    record.bolumAdi,
                    record.islemAdi,
                    record.miktar.toString().replace(".", ","),
                    record.birim,
                    record.created
                )
            )

            val body = com.google.api.services.sheets.v4.model.ValueRange()
                .setValues(values)

            sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDepartments(): Result<List<Department>> = withContext(Dispatchers.IO) {
        try {
            val response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "Bolumler!A2:B")
                .execute()

            val departments = response.getValues()?.mapNotNull { row ->
                try {
                    Department(
                        bolumId = row.getOrNull(0)?.toString()?.toIntOrNull() ?: return@mapNotNull null,
                        bolumAdi = row.getOrNull(1)?.toString() ?: return@mapNotNull null
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            Result.success(departments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
