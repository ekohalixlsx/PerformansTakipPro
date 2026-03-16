# Performans Takip Pro — Kotlin Implementation Plan

## Proje Özeti

İşletme personelinin günlük performans verilerini kaydedip yöneticinin analiz edebileceği bir Android mobil uygulama. Backend olarak **Google Sheets API** kullanılır. Kotlin + Jetpack Compose ile native Android uygulaması olarak geliştirilecek.

> [!IMPORTANT]
> Ayrıca **Flutter referans planı** da bir HTML dosyası olarak hazırlanacak. Bu dosya, gelecekte Flutter versiyonunu geliştirmek isteyen AI araçlarına doğrudan verilebilecek detayda olacak.

## Tasarım Hakkında Görüşüm

Mevcut UI tasarımı **oldukça başarılı** — derin lacivert (#1A1F36) zemin üzerine teal vurgusu modern ve profesyonel görünüyor. Ancak birkaç iyileştirme önerim var:

1. **Dashboard kartlarında** daha zengin gradient'ler ve subtle glassmorphism efektleri
2. **Miktar giriş alanının** daha belirgin ve premium hissi (arka plan vurgusu, animasyonlu focus state)
3. **Geçmiş ekranındaki** kayıt kartlarına hafif giriş animasyonları
4. **Bottom navigation bar'a** seçili ikon için glow efekti
5. **Kaydet butonuna** ripple + success animasyonu

Bunları uygulama sırasında UI'a entegre edeceğim.

---

## Proposed Changes

### 1. Proje Oluşturma

#### [NEW] Android Projesi
- Android Studio project yapısı oluşturma (Jetpack Compose template)
- `com.ekomak.performanstakippro` package name
- Min SDK: 26 (Android 8.0), Target SDK: 35
- Kotlin DSL (build.gradle.kts)

#### Bağımlılıklar:
```kotlin
// Core
implementation("androidx.core:core-ktx")
implementation("androidx.lifecycle:lifecycle-runtime-ktx")
implementation("androidx.activity:activity-compose")

// Compose
implementation(platform("androidx.compose:compose-bom"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Navigation
implementation("androidx.navigation:navigation-compose")

// Google Sheets API
implementation("com.google.api-client:google-api-client-android")
implementation("com.google.apis:google-api-services-sheets")
implementation("com.google.auth:google-auth-library-oauth2-http")

// Charts
implementation("com.patrykandpatrick.vico:compose-m3")

// DataStore (SharedPreferences yerine)
implementation("androidx.datastore:datastore-preferences")

// WorkManager (bildirimler için)
implementation("androidx.work:work-runtime-ktx")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")

// Font
implementation("androidx.compose.ui:ui-text-google-fonts")
```

---

### 2. Tema ve Tasarım Sistemi

#### [NEW] `ui/theme/`
- **Color.kt** — Renk paleti (#1A1F36, #00C2A8, #F5A623, vb.)
- **Type.kt** — Plus Jakarta Sans + Inter tipografi
- **Theme.kt** — Material3 tema konfigürasyonu
- **Shape.kt** — Border radius tanımları

---

### 3. Navigasyon

#### [NEW] `navigation/AppNavigation.kt`
- Bottom navigation: Kayıt, Geçmiş, Ayarlar, Dashboard
- Phosphor Icons veya Material Icons
- Animasyonlu geçişler

---

### 4. Google Sheets API Servisi

#### [NEW] `data/remote/SheetsService.kt`
- Service Account JSON ile kimlik doğrulama
- `getEmployees()` — PERSONEL sayfasından Aktif personeller
- `getWorkTypes()` — Islemler sayfasından iş türleri
- `getRecords()` — KAYITLAR sayfasından kayıtlar (filtreli)
- `saveRecord()` — KAYITLAR sayfasına yeni satır ekleme
- `updateRecord()` — Mevcut kaydı güncelleme
- `deleteRecord()` — Kaydı silme

#### [NEW] `data/local/PreferencesManager.kt`
- DataStore ile son seçilen personel, iş türü, dil, şirket adı saklama

#### [NEW] `data/repository/PerformanceRepository.kt`
- SheetsService + PreferencesManager birleştiren repository

---

### 5. Ekranlar

#### [NEW] `ui/screens/entry/EntryScreen.kt` — Kayıt Formu
- Tarih seçici (bugün varsayılan)
- Personel dropdown (aranabilir, son seçimi hatırlar)
- Personel ID (otomatik, readonly)
- Bölüm (otomatik, readonly)
- İş Türü dropdown (aranabilir, son seçimi hatırlar)
- Miktar girişi (büyük font, sayısal klavye, ondalık destekli)
- Birim (otomatik, readonly)
- Kaydet butonu (doğrulama + animasyonlu feedback)

#### [NEW] `ui/screens/history/HistoryScreen.kt` — Geçmiş
- Son 33 gün kayıtları
- Ayarlardaki personele göre otomatik filtre
- Tarih grupları (bugün, dün, tarih)
- Her kayıtta düzenle/sil
- Swipe-to-delete desteği

#### [NEW] `ui/screens/settings/SettingsScreen.kt` — Ayarlar
- Personel seçimi (kayıtlı)
- Varsayılan iş türü
- Bildirimler açma/kapama
- Bildirim saati (16:45 varsayılan)
- Dil seçimi (TR/EN)
- Şirket adı
- Yönetici girişi (Google Sign-In)
- Uygulama sürümü

#### [NEW] `ui/screens/dashboard/DashboardScreen.kt` — Dashboard
- Tab layout: Günlük / Haftalık / Aylık
- Personel seçimi dropdown
- İstatistik kartları (Toplam, Ortalama, En Yüksek)
- Vico kütüphanesi ile grafikler
- PDF dışa aktarma butonu

---

### 6. Bildirim Sistemi

#### [NEW] `notifications/NotificationWorker.kt`
- WorkManager ile günlük tekrarlayan bildirim
- Ayarlardan belirlenen saatte tetiklenir
- "Performans kaydı girmeyi unutmayın!" mesajı

---

### 7. Çoklu Dil (i18n)

#### [NEW] `res/values/strings.xml` — Türkçe
#### [NEW] `res/values-en/strings.xml` — İngilizce
- Tüm UI metinleri her iki dilde

---

### 8. Flutter Referans Planı

#### [NEW] `Proje Bilgileri/Flutter_Plan.html`
- Detaylı Flutter implementasyon dokümanı
- Ekran tasarımlarının HTML/CSS mockup'ları
- AI araçlarına doğrudan verilebilecek prompt
- Tüm teknik detaylar ve veri şemaları

---

## Verification Plan

### Build Doğrulaması
```bash
cd c:\projects\PerformansTakipPro
.\gradlew assembleDebug
```
- Projenin hatasız derlenmesi

### UI Doğrulaması
- Emülatörde veya cihazda uygulamayı çalıştırma
- Tüm ekranların doğru render edilmesi
- Bottom navigation geçişleri
- Tema ve renklerin doğruluğu

### Manuel Test (Kullanıcı)
1. APK'yı bir Android cihaza/emülatöre kurun
2. Uygulama açıldığında Kayıt ekranı görünmeli
3. Personel ve İş Türü dropdown'ları Google Sheets'ten veri çekmeli
4. Kayıt girilip kaydedilmeli, Geçmiş ekranında görünmeli
5. Ayarlardan dil değiştirilip UI'ın güncellenmesi
6. Dashboard'da grafiklerin doğru çalışması
