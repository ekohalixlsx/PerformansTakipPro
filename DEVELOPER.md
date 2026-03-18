# Performans Takip Pro — Geliştirici Rehberi

Bu belge, projenin teknik yapısını, mimarisini ve geliştirme sürecini detaylı olarak açıklar.

## 🏗️ Teknik Mimari

### Platform & Teknoloji
| Öğe | Değer |
|-----|-------|
| Platform | Android (Native) |
| Dil | Kotlin |
| UI Framework | Jetpack Compose + Material 3 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| Build System | Gradle 8.9 (Kotlin DSL) |
| JDK | 24 |

### Proje Yapısı
```
app/src/main/java/com/ekomak/performanstakippro/
├── data/
│   ├── model/          # Veri sınıfları (Employee, WorkType, PerformanceRecord, etc.)
│   └── remote/         # API servisleri
│       ├── SheetsService.kt    # Google Sheets ile haberleşme (OkHttp)
│       └── AppConfig.kt        # Script URL ve sabit ayarlar
├── navigation/
│   └── AppNavigation.kt        # HorizontalPager tabanlı navigasyon
├── ui/
│   ├── MainViewModel.kt        # Merkezi ViewModel (tüm ekranlar)
│   ├── screens/
│   │   ├── entry/EntryScreen.kt        # Kayıt formu
│   │   ├── history/HistoryScreen.kt    # Geçmiş kayıtlar
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt       # Ayarlar
│   │   │   └── AboutScreen.kt         # Hakkında
│   │   └── dashboard/DashboardScreen.kt # Yönetici paneli
│   └── theme/
│       ├── Color.kt     # Renk paleti (Deep Navy / Teal)
│       ├── Theme.kt     # Material 3 tema konfigürasyonu
│       └── Type.kt      # Tipografi (Inter, JetBrains Mono)
├── util/
│   ├── PdfReportService.kt     # PDF rapor oluşturma & e-posta
│   ├── DateUtils.kt            # Tarih yardımcıları
│   └── NotificationHelper.kt   # WorkManager bildirimleri
└── PerformansTakipProApplication.kt
```

## 🔧 Bağımlılıklar (Dependencies)

### Core
- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`
- `androidx.activity:activity-compose`

### Compose
- `compose-bom` (platform)
- `material3`, `foundation`, `ui`, `ui-tooling`

### Navigasyon
- `foundation.pager.HorizontalPager` (NavHost yerine, v1.2.0'dan itibaren)

### Ağ İletişimi
- `com.squareup.okhttp3:okhttp` — HTTP istekleri
- `org.jetbrains.kotlinx:kotlinx-serialization-json` — JSON parse

### Arkaplan İşler
- `androidx.work:work-runtime-ktx` — Günlük hatırlatıcı bildirimleri

### Diğer
- `com.google.android.material:material` — Material Components
- `com.google.accompanist:accompanist-systemuicontroller` — Status bar renkleri

## 🗄️ Veri Mimarisi

### Backend: Google Apps Script
Uygulama, doğrudan Google Cloud API kullanmaz. Bunun yerine, kullanıcının Google Sheets'ine bağlı bir **Apps Script Web App** üzerinden REST API sağlar.

**Avantajları:**
- 100% ücretsiz (kredi kartı gerektirmez)
- Google Cloud projesi oluşturmaya gerek yok
- Kullanıcının kendi Google hesabıyla çalışır

**API Endpoint'leri:**
| Yöntem | Action | Açıklama |
|--------|--------|----------|
| GET | `getEmployees` | PERSONEL tablosundan aktif personelleri getirir |
| GET | `getWorkTypes` | Islemler tablosundan iş türlerini getirir |
| GET | `getDepartments` | Bolumler tablosundan bölümleri getirir |
| GET | `getRecords` | KAYITLAR tablosundan kayıtları getirir (gün filtresi) |
| GET | `getUsers` | KULLANICILAR tablosundan kullanıcıları getirir |
| POST | `saveRecord` | Yeni kayıt ekler (otomatik kayit_id) |
| POST | `updateRecord` | Mevcut kaydı günceller |
| POST | `deleteRecord` | Kaydı siler |

### Yerel Depolama: SharedPreferences
Kullanıcı ayarları cihazda saklanır:
- `selected_employee_id` — Seçili personel
- `default_work_type_id` — Varsayılan iş türü
- `admin_username` / `admin_password` — Admin bilgileri
- `admin_remember_me` — Beni hatırla
- `company_name` — Şirket adı
- `first_launch_done` — İlk kullanım tamamlandı mı
- `notification_permission_asked` — Bildirim izni soruldu mu

### kayit_id Otomatik Numara Sistemi
`PropertiesService.getScriptProperties()` kullanılarak kalıcı bir sayaç tutulur. Tablo tamamen silinse bile sayaç korunur ve sıradaki numaradan devam eder. Format: `00001`, `00002`, ...

## 🎨 Tasarım Sistemi

### Renk Paleti
- **Primary:** `#1B2838` (Deep Navy)
- **PrimaryDark:** `#101822`
- **Accent:** `#26C6DA` (Teal/Cyan)
- **Background:** `#F4F6F9`
- **CardBackground:** `#FFFFFF`
- **Danger:** `#EF5350`
- **Success:** `#66BB6A`
- **Warning:** `#FFA726`

### Tipografi
- **Inter** — Ana metin fontu
- **JetBrains Mono** — Sayısal veriler ve kodlar

### Navigasyon
`HorizontalPager` kullanılır (NavHost yerine). Bu yaklaşım:
- Ekranlar arasında yatay kaydırma (swipe) desteği sağlar
- Geçiş animasyonlarında "flicker" sorununu ortadan kaldırır
- Bottom navigation ile senkronize çalışır

### Premium Dialog Tasarımı
Admin giriş ve hoşgeldin dialogları koyu gradient tema kullanır:
- Arka plan: `#1A1F36` → `#0F1425` gradient
- Accent vurguları: Teal/Cyan tonları
- Giriş butonu: Teal → Mor horizontal gradient

## 📄 PDF Rapor
Android `PdfDocument` API kullanılarak oluşturulur. İçerik:
- Şirket adı (header & footer)
- Personel bilgileri
- Aylık performans özeti
- `FileProvider` ile e-posta paylaşımı

## 🔐 Admin Sistemi
- Varsayılan: `eko / eko2026`
- SharedPreferences'ta saklanır
- "Beni Hatırla" ile oturum korunur
- Dashboard üzerinden şifre değiştirilebilir

## 🔔 Bildirim Sistemi
- `WorkManager` ile günlük hatırlatıcı
- Kullanıcının belirlediği saatte tetiklenir
- Android 13+ için `POST_NOTIFICATIONS` izni istenir

## 📦 Build & Release

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (imzalı)
./gradlew assembleRelease

# APK konumları
app/build/outputs/apk/debug/PerformansTakipPro-v{version}-debug.apk
app/build/outputs/apk/release/PerformansTakipPro-v{version}-release.apk
```

APK adlandırma `build.gradle.kts` içindeki `applicationVariants` bloğu ile otomatik yapılır.

## 📝 Sürüm Geçmişi

| Sürüm | Tarih | Değişiklikler |
|-------|-------|---------------|
| 1.0.0 | — | İlk sürüm |
| 1.1.0 | — | Google Sheets entegrasyonu, admin girişi, APK isimlendirme |
| 1.2.0 | 2026-03 | HorizontalPager, swipe navigasyon, flicker fix, PDF şirket adı |
| 1.2.1 | 2026-03 | İlk kullanım dialogu, premium admin UI, şifre değiştirme, kayit_id otomatik, README güncelleme |

---

**Geliştirici:** İlyas YEŞİL
📧 ilyasyesil.develop@gmail.com
