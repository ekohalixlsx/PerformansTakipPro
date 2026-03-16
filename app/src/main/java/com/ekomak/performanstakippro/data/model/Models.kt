package com.ekomak.performanstakippro.data.model

data class Employee(
    val personelId: Int,
    val adSoyad: String,
    val bolumAdi: String,
    val departman: String = "",
    val gorevi: String = "",
    val durum: String = "Aktif"
) {
    val isActive: Boolean get() = durum == "Aktif"
    val displayName: String get() = adSoyad
    val initials: String get() = adSoyad.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
}

data class WorkType(
    val islemId: Int,
    val islemAdi: String,
    val birim: String
) {
    val displayName: String get() = islemAdi
}

data class PerformanceRecord(
    val kayitId: String,
    val tarih: String,
    val personelId: Int,
    val adSoyad: String,
    val bolumAdi: String,
    val islemAdi: String,
    val miktar: Double,
    val birim: String,
    val created: String
) {
    val formattedMiktar: String get() {
        return if (miktar == miktar.toLong().toDouble()) {
            miktar.toLong().toString()
        } else {
            String.format("%.2f", miktar).replace(".", ",")
        }
    }
}

data class Department(
    val bolumId: Int,
    val bolumAdi: String
)

data class AppUser(
    val email: String,
    val rol: String,
    val adSoyad: String
) {
    val isAdmin: Boolean get() = rol.uppercase() == "YÖNETİCİ" || rol.uppercase() == "IT"
}
