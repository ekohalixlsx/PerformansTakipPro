package com.ekomak.performanstakippro.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Google Sheets'ten gelen tarih formatlarını işler.
 * Sheets bazen JS Date string döndürür:
 * "Tue Mar 17 2026 00:00:00 GMT+0300 (Türkiye Standart Saati)"
 * Bu utility bunu "dd/MM/yyyy" formatına çevirir.
 */
object DateUtils {

    private val jsDateFormat = SimpleDateFormat("EEE MMM dd yyyy", Locale.ENGLISH)
    private val normalFormat = SimpleDateFormat("dd/MM/yyyy", Locale("tr"))
    private val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("tr"))
    private val dashboardDayFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))
    private val weekFormat = SimpleDateFormat("yyyy", Locale("tr"))
    private val monthYearFormat = SimpleDateFormat("yyyy/MMMM", Locale("tr"))

    /**
     * Herhangi formattaki tarihi dd/MM/yyyy formatına normalize eder.
     */
    fun normalizeDate(rawDate: String): String {
        // Zaten dd/MM/yyyy formatındaysa direkt dön
        if (rawDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
            return rawDate
        }

        // JS Date format: "Tue Mar 17 2026 00:00:00 GMT+0300 (Türkiye Standart Saati)"
        try {
            // Sadece ilk 4 kelimeyi al: "Tue Mar 17 2026"
            val parts = rawDate.split(" ")
            if (parts.size >= 4) {
                val dateStr = "${parts[0]} ${parts[1]} ${parts[2]} ${parts[3]}"
                val parsed = jsDateFormat.parse(dateStr)
                if (parsed != null) {
                    return normalFormat.format(parsed)
                }
            }
        } catch (_: Exception) {}

        // dd.MM.yyyy formatı
        try {
            val dotFormat = SimpleDateFormat("dd.MM.yyyy", Locale("tr"))
            val parsed = dotFormat.parse(rawDate)
            if (parsed != null) return normalFormat.format(parsed)
        } catch (_: Exception) {}

        // yyyy-MM-dd formatı
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale("tr"))
            val parsed = isoFormat.parse(rawDate)
            if (parsed != null) return normalFormat.format(parsed)
        } catch (_: Exception) {}

        return rawDate
    }

    /**
     * dd/MM/yyyy formatındaki tarihi ekran gösterim formatına çevirir.
     * Örn: "17/03/2026" → "17 MART 2026"
     */
    fun formatForDisplay(normalizedDate: String): String? {
        return try {
            val parsed = normalFormat.parse(normalizedDate) ?: return null
            displayFormat.format(parsed).uppercase()
        } catch (_: Exception) { null }
    }

    /**
     * Tarih stringini Date objesine çevirir.
     */
    fun parseToDate(rawDate: String): Date? {
        val normalized = normalizeDate(rawDate)
        return try {
            normalFormat.parse(normalized)
        } catch (_: Exception) { null }
    }

    /**
     * Dashboard günlük görünüm: "17.03.2026"
     */
    fun formatDashboardDay(date: Date): String {
        return dashboardDayFormat.format(date)
    }

    /**
     * Dashboard haftalık görünüm: "2026/12" (yıl/hafta no)
     */
    fun formatDashboardWeek(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return "$year/$week"
    }

    /**
     * Dashboard aylık görünüm: "2026/Mart"
     */
    fun formatDashboardMonth(date: Date): String {
        return monthYearFormat.format(date).replaceFirstChar { it.uppercase() }
    }

    /**
     * Haftanın günü: "Pazartesi"
     */
    fun getDayOfWeek(date: Date): String {
        val format = SimpleDateFormat("EEEE", Locale("tr"))
        return format.format(date).replaceFirstChar { it.uppercase() }
    }
}
