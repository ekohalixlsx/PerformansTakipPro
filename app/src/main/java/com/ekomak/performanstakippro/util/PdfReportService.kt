package com.ekomak.performanstakippro.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.ekomak.performanstakippro.data.model.Employee
import com.ekomak.performanstakippro.data.model.PerformanceRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * PDF rapor oluşturma ve email gönderme servisi.
 * Android PdfDocument API kullanır.
 */
class PdfReportService(private val context: Context) {

    private val pageWidth = 595  // A4
    private val pageHeight = 842

    /**
     * Aylık performans raporu oluşturur.
     * İş türü bazında aylık toplamları bar chart ile gösterir.
     */
    fun generateMonthlyReport(
        employee: Employee,
        records: List<PerformanceRecord>
    ): File? {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(26, 31, 54) // Primary
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.rgb(107, 114, 128) // TextSecondary
                textSize = 12f
                isAntiAlias = true
            }
            val bodyPaint = Paint().apply {
                color = Color.rgb(26, 31, 54)
                textSize = 11f
                isAntiAlias = true
            }
            val boldPaint = Paint().apply {
                color = Color.rgb(26, 31, 54)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val accentPaint = Paint().apply {
                color = Color.rgb(0, 194, 168) // Accent
                isAntiAlias = true
            }
            val headerBgPaint = Paint().apply {
                color = Color.rgb(26, 31, 54)
            }
            val linePaint = Paint().apply {
                color = Color.rgb(229, 231, 235)
                strokeWidth = 1f
            }

            var y = 0f

            // Header background
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 100f, headerBgPaint)

            // Header text
            val headerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val headerSubPaint = Paint().apply {
                color = Color.rgb(200, 200, 200)
                textSize = 11f
                isAntiAlias = true
            }
            canvas.drawText("PERFORMANS TAKİP PRO — AYLIK RAPOR", 30f, 40f, headerPaint)

            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr"))
            canvas.drawText("Rapor Tarihi: ${dateFormat.format(Date())}", 30f, 60f, headerSubPaint)

            // Accent line
            canvas.drawRect(0f, 100f, pageWidth.toFloat(), 104f, accentPaint)

            y = 130f

            // Employee Info
            canvas.drawText("Personel: ${employee.adSoyad} (ID: ${employee.personelId})", 30f, y, boldPaint)
            y += 18f
            canvas.drawText("Bölüm: ${employee.bolumAdi}", 30f, y, bodyPaint)
            y += 18f

            // Divider
            canvas.drawLine(30f, y, pageWidth - 30f, y, linePaint)
            y += 20f

            // Group records by work type
            val byWorkType = records.groupBy { it.islemAdi }

            if (byWorkType.isEmpty()) {
                canvas.drawText("Bu dönemde kayıt bulunmuyor.", 30f, y, subtitlePaint)
            } else {
                // Genel istatistikler
                val totalMiktar = records.sumOf { it.miktar }
                val avgMiktar = if (records.isNotEmpty()) totalMiktar / records.size else 0.0
                canvas.drawText("ÖZET", 30f, y, titlePaint.apply { textSize = 14f })
                y += 20f
                canvas.drawText("Toplam: ${formatVal(totalMiktar)}", 30f, y, bodyPaint)
                y += 16f
                canvas.drawText("Ortalama: ${formatVal(avgMiktar)} / kayıt", 30f, y, bodyPaint)
                y += 16f
                canvas.drawText("Kayıt Sayısı: ${records.size}", 30f, y, bodyPaint)
                y += 30f

                canvas.drawLine(30f, y, pageWidth - 30f, y, linePaint)
                y += 20f

                // For each work type, group by month and draw bar chart
                for ((workType, workRecords) in byWorkType) {
                    if (y > pageHeight - 100) break // Sayfa sınırı

                    canvas.drawText(workType.uppercase(), 30f, y, boldPaint.apply { textSize = 13f })
                    y += 8f

                    val unit = workRecords.firstOrNull()?.birim ?: ""

                    // Group by month
                    val byMonth = workRecords
                        .mapNotNull { rec ->
                            val date = DateUtils.parseToDate(rec.tarih)
                            if (date != null) Pair(date, rec) else null
                        }
                        .groupBy { pair ->
                            val cal = Calendar.getInstance().apply { time = pair.first }
                            SimpleDateFormat("MMMM", Locale("tr")).format(pair.first).uppercase()
                        }
                        .mapValues { it.value.sumOf { pair -> pair.second.miktar } }

                    val maxVal = byMonth.values.maxOrNull() ?: 1.0
                    val barMaxWidth = 300f

                    for ((month, value) in byMonth) {
                        y += 20f
                        if (y > pageHeight - 50) break

                        // Month label
                        canvas.drawText(month, 40f, y, bodyPaint)

                        // Bar
                        val barWidth = ((value / maxVal) * barMaxWidth).toFloat().coerceAtLeast(10f)
                        val barRect = RectF(150f, y - 10f, 150f + barWidth, y + 2f)
                        accentPaint.color = Color.rgb(0, 194, 168)
                        canvas.drawRoundRect(barRect, 4f, 4f, accentPaint)

                        // Value
                        canvas.drawText("${formatVal(value)} $unit", 160f + barWidth, y, bodyPaint)
                    }

                    y += 30f
                }
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.rgb(156, 163, 175)
                textSize = 9f
                isAntiAlias = true
            }
            canvas.drawText("Performans Takip © 2026 | Otomatik oluşturulmuş rapor",
                30f, (pageHeight - 20).toFloat(), footerPaint)

            document.finishPage(page)

            // Save to cache
            val fileName = "Rapor_${employee.adSoyad.replace(" ", "_")}_${
                SimpleDateFormat("yyyyMM", Locale("tr")).format(Date())
            }.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * PDF dosyasını email ile paylaşır.
     */
    fun sendEmail(pdfFile: File, recipientEmail: String, employeeName: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, "Performans Raporu - $employeeName")
                putExtra(Intent.EXTRA_TEXT, "$employeeName için aylık performans raporu ekte yer almaktadır.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Rapor Gönder"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatVal(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.1f", value).replace(".", ",")
        }
    }
}
