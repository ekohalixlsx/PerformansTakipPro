package com.ekomak.performanstakippro.data.remote

import com.ekomak.performanstakippro.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Google Apps Script Web App ile haberleşen servis.
 *
 * Tüm veri okuma/yazma işlemleri, kullanıcının Google Sheets'ine
 * bağlı Apps Script Web URL'si üzerinden yapılır.
 * Kredi kartı veya Google Cloud hesabı gerektirmez.
 */
class SheetsService(private val scriptUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    // ==========================================
    // VERİ OKUMA (GET)
    // ==========================================

    suspend fun getEmployees(): Result<List<Employee>> = withContext(Dispatchers.IO) {
        try {
            val response = doGet("getEmployees")
            val data = response.jsonArray

            val employees = data.map { item ->
                val obj = item.jsonObject
                Employee(
                    durum = obj.str("durum"),
                    personelId = obj.int("personelId"),
                    adSoyad = obj.str("adSoyad"),
                    bolumAdi = obj.str("bolumAdi"),
                    departman = obj.str("departman"),
                    gorevi = obj.str("gorevi")
                )
            }
            Result.success(employees)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkTypes(): Result<List<WorkType>> = withContext(Dispatchers.IO) {
        try {
            val response = doGet("getWorkTypes")
            val data = response.jsonArray

            val workTypes = data.map { item ->
                val obj = item.jsonObject
                WorkType(
                    islemId = obj.int("islemId"),
                    islemAdi = obj.str("islemAdi"),
                    birim = obj.str("birim")
                )
            }
            Result.success(workTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDepartments(): Result<List<Department>> = withContext(Dispatchers.IO) {
        try {
            val response = doGet("getDepartments")
            val data = response.jsonArray

            val departments = data.map { item ->
                val obj = item.jsonObject
                Department(
                    bolumId = obj.int("bolumId"),
                    bolumAdi = obj.str("bolumAdi")
                )
            }
            Result.success(departments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<AppUser>> = withContext(Dispatchers.IO) {
        try {
            val response = doGet("getUsers")
            val data = response.jsonArray

            val users = data.map { item ->
                val obj = item.jsonObject
                AppUser(
                    email = obj.str("email"),
                    rol = obj.str("rol"),
                    adSoyad = obj.str("adSoyad")
                )
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecords(
        employeeName: String? = null,
        days: Int = 33
    ): Result<List<PerformanceRecord>> = withContext(Dispatchers.IO) {
        try {
            val params = mutableMapOf("days" to days.toString())
            if (!employeeName.isNullOrEmpty()) {
                params["employeeName"] = employeeName
            }
            val response = doGet("getRecords", params)
            val data = response.jsonArray

            val records = data.map { item ->
                val obj = item.jsonObject
                PerformanceRecord(
                    kayitId = obj.str("kayitId"),
                    tarih = obj.str("tarih"),
                    personelId = obj.int("personelId"),
                    adSoyad = obj.str("adSoyad"),
                    bolumAdi = obj.str("bolumAdi"),
                    islemAdi = obj.str("islemAdi"),
                    miktar = obj.double("miktar"),
                    birim = obj.str("birim"),
                    created = obj.str("created")
                )
            }
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // VERİ YAZMA (POST)
    // ==========================================

    suspend fun saveRecord(record: PerformanceRecord): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = buildJsonObject {
                put("action", "saveRecord")
                put("data", buildJsonObject {
                    put("kayitId", record.kayitId)
                    put("tarih", record.tarih)
                    put("personelId", record.personelId)
                    put("adSoyad", record.adSoyad)
                    put("bolumAdi", record.bolumAdi)
                    put("islemAdi", record.islemAdi)
                    put("miktar", record.miktar)
                    put("birim", record.birim)
                    put("created", record.created)
                })
            }
            doPost(body)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecord(record: PerformanceRecord): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = buildJsonObject {
                put("action", "updateRecord")
                put("data", buildJsonObject {
                    put("kayitId", record.kayitId)
                    put("tarih", record.tarih)
                    put("personelId", record.personelId)
                    put("adSoyad", record.adSoyad)
                    put("bolumAdi", record.bolumAdi)
                    put("islemAdi", record.islemAdi)
                    put("miktar", record.miktar)
                    put("birim", record.birim)
                    put("created", record.created)
                })
            }
            doPost(body)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(kayitId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val body = buildJsonObject {
                put("action", "deleteRecord")
                put("kayitId", kayitId)
            }
            doPost(body)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // HTTP YARDIMCI FONKSİYONLAR
    // ==========================================

    private fun doGet(action: String, extraParams: Map<String, String> = emptyMap()): JsonElement {
        val urlBuilder = StringBuilder(scriptUrl)
        urlBuilder.append("?action=$action")
        extraParams.forEach { (key, value) ->
            urlBuilder.append("&$key=${java.net.URLEncoder.encode(value, "UTF-8")}")
        }

        val request = Request.Builder()
            .url(urlBuilder.toString())
            .get()
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Sunucudan boş yanıt geldi")

        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        val status = jsonResponse["status"]?.jsonPrimitive?.content

        if (status != "success") {
            val message = jsonResponse["message"]?.jsonPrimitive?.content ?: "Bilinmeyen hata"
            throw Exception(message)
        }

        return jsonResponse["data"] ?: throw Exception("Veri bulunamadı")
    }

    private fun doPost(body: JsonObject) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = body.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(scriptUrl)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw Exception("Sunucudan boş yanıt geldi")

        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        val status = jsonResponse["status"]?.jsonPrimitive?.content

        if (status != "success") {
            val message = jsonResponse["message"]?.jsonPrimitive?.content ?: "Bilinmeyen hata"
            throw Exception(message)
        }
    }

    // JSON extension helpers
    private fun JsonObject.str(key: String): String =
        this[key]?.jsonPrimitive?.content ?: ""

    private fun JsonObject.int(key: String): Int =
        this[key]?.jsonPrimitive?.intOrNull ?: 0

    private fun JsonObject.double(key: String): Double =
        this[key]?.jsonPrimitive?.doubleOrNull ?: 0.0
}
