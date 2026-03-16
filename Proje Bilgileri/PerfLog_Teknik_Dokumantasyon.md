# PerfLog — Performans Takip Mobil Uygulaması
## Tam Teknik Plan & AI Üretim Dokümantasyonu

> **Önerilen Uygulama Adı: Performans Takip Pro  

---

## 📋 İçindekiler

1. [Proje Özeti](#1-proje-özeti)
2. [Sistem Mimarisi](#2-sistem-mimarisi)
3. [Google Sheets Veritabanı Yapısı](#3-google-sheets-veritabanı-yapısı)
4. [Uygulama Ekranları & Kullanıcı Akışı](#4-uygulama-ekranları--kullanıcı-akışı)
5. [UI/UX Tasarım Rehberi](#5-uiux-tasarım-rehberi)
6. [Bildirim Sistemi](#6-bildirim-sistemi)
7. [Yönetici Dashboard Önerisi](#7-yönetici-dashboard-önerisi)
8. [AI Üretim Promptu (AppSheet / Cursor / Lovable için)](#8-ai-üretim-promptu)
9. [GitHub Otomasyonu — Antigravity AI İçin](#9-github-otomasyonu--antigravity-ai-için)
10. [Geliştirme Notları](#10-geliştirme-notları)
11. [Gelecek Versiyon Modülleri](#11-gelecek-versiyon-modülleri)

---

## 1. Proje Özeti

### Amaç
Bir işletmede çalışan personelin **gün sonunda yaptıkları iş miktarını mobil uygulama üzerinden girmesi** ve yöneticinin bu verileri **Google Sheets üzerinden kolayca takip etmesi**.

### Temel Prensipler

| Prensip | Açıklama |
|---|---|
| Hız | Çalışanlar 3 dokunuşta kayıt girebilmeli |
| Sadelik | Minimum arayüz, maksimum kullanılabilirlik |
| Yönetilebilirlik | Yönetici Excel/Sheets gibi veriyi görebilmeli |
| Ölçeklenebilirlik | Yeni bölüm/işlem/personel eklemek kolay olmalı |

### Teknoloji Kararı
Proje **AppSheet** (Google'ın no-code platformu) üzerine inşa edilecektir. Neden AppSheet?

- Google Sheets ile native entegrasyon (ek API yoktur)
- Mobil uygulama otomatik olarak üretilir (Android + iOS)
- Offline çalışma desteği
- Push bildirim desteği
- Yönetici dashboard yerleşik olarak gelir
- Ücretsiz başlangıç katmanı mevcuttur.

---

## 2. Sistem Mimarisi

```
┌─────────────────────────────────────────────────────────┐
│                    KULLANICI KATMANI                    │
│  ┌─────────────┐    ┌─────────────┐   ┌──────────────┐ │
│  │ Personel    │    │ Personel    │   │  Yönetici    │ │
│  │ (Android)   │    │ (iOS)       │   │  (Tablet/Web)│ │
│  └──────┬──────┘    └──────┬──────┘   └──────┬───────┘ │
└─────────┼─────────────────┼─────────────────┼─────────┘
          │                 │                 │
          └─────────────────┼─────────────────┘
                            │
                    ┌───────▼───────┐
                    │   APPSHEET    │
                    │  (No-Code)    │
                    │               │
                    │ • Mobil App   │
                    │ • Forms       │
                    │ • Dashboard   │
                    │ • PDF Rapor   │
                    │ • Bildirimler │
                    └───────┬───────┘
                            │  Google Sheets API (native)
                    ┌───────▼───────────────────────────┐
                    │     GOOGLE SHEETS VERİTABANI      │
                    │  Performans_EKOMAKHALI            │
                    │                                   │
                    │  📄 PERSONEL    📄 Bolumler       │
                    │  📄 Islemler    📄 KAYITLAR		|
                    │  📄 KULLANICILAR					|
                    └───────────────────────────────────┘
```

### Veri Akışı

```
Personel Açar App
        │
        ▼
Personel Seçilir (cached)
        │
        ▼
Tarih Seçilir (bugün varsayılan)
        │
        ▼
İş Türü Seçilir (cached)
        │
        ▼
Miktar Girilir
        │
        ▼
[KAYDET] → AppSheet → Google Sheets → KAYITLAR Tablosu
        │
        ▼
Yönetici Dashboard'da Gerçek Zamanlı Görünür
```

---

## 3. Google Sheets Veritabanı Yapısı

**Dosya Adı:** `Performans_EKOMAKHALI`
Tablolardaki kayıtlar temsilidir, gerçek kayıt verileri değildir. Gerçek veriler Googlesheet deki tablolardan çekilecek.

### 3.1 PERSONEL Sayfası

**Sayfa Adı:** `PERSONEL`  
**Tablo Adı:** `PersonelListesi`  
**Kullanım:** Personel dropdown listesi bu tablodan çekilir. AppSheet'te `FILTER()` ile Durum="Aktif" olanlar gösterilir.

| Sütun | Tip | Zorunlu | Açıklama |
|---|---|---|---|
| Durum | Enum | ✓ | "Aktif" / "Pasif" — Pasif personel formda görünmez |
| personel_id | Number | ✓ | Benzersiz personel numarası |
| adSoyad | Text | ✓ | Tam ad — büyük harf |
| bolumAdi | Ref → Bolumler | ✓ | Bölüm adı — otomatik referans |
| Departman | Text | — | İsteğe bağlı ek bilgi |
| Gorevi | Text | — | Görev tanımı |

**Örnek Veri:**

| Durum | personel_id | adSoyad | bolumAdi | Departman | Gorevi |
|---|---|---|---|---|---|
| Aktif | 1234 | ALİ CAN | A BÖLÜMÜ | X DEPARTMANI | 1 GÖREVİ |
| Aktif | 1235 | VELİ HAN | A BÖLÜMÜ | Y DEPARTMANI | 3 GÖREVİ |
| Aktif | 1236 | AYŞE SU | B BÖLÜMÜ | Z DEPARTMANI | 5 GÖREVİ |
| Pasif | 1237 | CAN KAYA | C BÖLÜMÜ | X DEPARTMANI | 2 GÖREVİ |

---

### 3.2 Bolumler Sayfası

**Sayfa Adı:** `Bolumler`  
**Tablo Adı:** `Bolumler`  
**Kullanım:** PERSONEL tablosundaki bolumAdi alanına referans verir. Yeni bölüm eklemek için sadece bu tabloya satır eklenir.

| Sütun | Tip | Zorunlu | Açıklama |
|---|---|---|---|
| bolum_id | Number | ✓ | Benzersiz bölüm ID |
| bolumAdi | Text | ✓ | Bölüm adı |

**Örnek Veri:**

| bolum_id | bolumAdi |
|---|---|
| 1 | A BÖLÜMÜ |
| 2 | B BÖLÜMÜ |
| 3 | C BÖLÜMÜ |

---

### 3.3 Islemler Sayfası

**Sayfa Adı:** `Islemler`  
	**Tablo Adı:** `Islemler`  
**Kullanım:** İş türü dropdown bu tablodan gelir. Seçilen işlemin `birim` alanı otomatik olarak kayıt formuna yansır.

| Sütun | Tip | Zorunlu | Açıklama |
|---|---|---|---|
| islem_id | Number | ✓ | Benzersiz işlem ID |
| islemAdi | Text | ✓ | İşlem adı |
| birim | Text | ✓ | Ölçü birimi (m2, Kg, Adet vb.) |

**Örnek Veri:**

| islem_id | islemAdi | birim |
|---|---|---|
| 1 | İŞLEM 1 | m2 |
| 2 | İŞLEM 2 | Kg |
| 3 | İŞLEM 3 | Adet |

---

### 3.4 KAYITLAR Sayfası

**Sayfa Adı:** `KAYITLAR`  
	**Tablo Adı:** `KAYITLAR`  
**Kullanım:** Tüm performans verileri bu tabloda tutulur. Yönetici dashboard bu tabloyu okur.

| Sütun | Tip | Zorunlu | Açıklama |
|---|---|---|---|
| kayit_id | Text | ✓ | Otomatik üretilen benzersiz ID |
| tarih | Date | ✓ | Kayıt tarihi (DD/MM/YYYY) |
| personel_id | Ref → PERSONEL | ✓ | Personel referansı |
| adSoyad | Text | otomatik | PERSONEL'den çekilir |
| bolumAdi | Text | otomatik | PERSONEL'den çekilir |
| islemAdi | Ref → Islemler | ✓ | İşlem türü |
| miktar | Decimal | ✓ | Yapılan iş miktarı |
| birim | Text | otomatik | Islemler tablosundan çekilir |
| created | Text | otomatik | Sistem zamanı — YYYYMMDDHHMMSS |

**Örnek Veri:**

| kayit_id | tarih | personel_id | adSoyad | bolumAdi | islemAdi | miktar | birim | created |
|---|---|---|---|---|---|---|---|---|
| 0001 | 16/03/2026 | 1234 | ALİ CAN | A BÖLÜMÜ | İŞLEM 1 | 120,50 | m2 | 20260316111825 |
| 0002 | 16/03/2026 | 1235 | VELİ HAN | A BÖLÜMÜ | İŞLEM 3 | 60 | Adet | 20260316112032 |
| 0003 | 16/03/2026 | 1236 | AYŞE SU | B BÖLÜMÜ | İŞLEM 2 | 30,50 | kg | 20260316112153 |



### 3.5 KULLANICILAR Sayfası

**Sayfa Adı:** `KULLANICILAR`  
	**Tablo Adı:** `KULLANICILAR`  

| email                   | rol      | adSoyad     |
| ----------------------- | -------- | ----------- |
| bekir.atay@eko-mak.com  | YÖNETİCİ | BEKİR ATAY  |
| ilyas.yesil@eko-mak.com | IT       | İLYAS YEŞİL |
|                         |          |             |

### 3.6 Kayıt ID Sistemi

```
Format: YYYYMMDDHHMMSS
Örnek:  20260316111825
        │    │ │ │ │ │
        │    │ │ │ │ └── Saniye: 25
        │    │ │ │ └──── Dakika: 18
        │    │ │ └────── Saat: 11
        │    │ └──────── Gün: 16
        │    └────────── Ay: 03
        └─────────────── Yıl: 2026
```

**AppSheet'te üretim formülü:**
```
TEXT(NOW(), "YYYYMMDDHHmmss")
```

Bu format:
- Benzersiz ID garantiler (saniye bazında)
- Kronolojik sıralamanın korunmasını sağlar
- İnsan tarafından okunabilir

---

## 4. Uygulama Ekranları & Kullanıcı Akışı

Çift Dil desteği yapalım Türkçe/İngilizce. Ayarlar ekranına dil seçim butonu koyalım. Tasarım ekranına koymayı unuttum sen ayarlarsın. 

### 4.1 Ekran Haritası

```
┌─────────────────────────────────────┐
│         UYGULAMA ANA AKIŞI          │
│                                     │
│  İlk Açılış ──► Ayarlar (Kurulum)  │
│                      │              │
│                       ▼             │
│  ┌─────────────┐  ┌──────────────┐ │
│  │  ANA EKRAN  │  │   GEÇMİŞ    │ │
│  │ (Kayıt Gir) │  │  / ARŞİV    │ │
│  └─────────────┘  └──────────────┘ │
│         │                │          │
│         └────────────────┘          │
│                  │                  │
│          ┌───────▼───────┐          │
│          │    AYARLAR    │          │
│          │  / ADMIN      │          │
│          └───────────────┘          │
└─────────────────────────────────────┘
```

### 4.2 Ana Ekran — Performans Kayıt Formu

**Alanlar ve Davranışlar:**

| Alan | Tip | Varsayılan | Davranış |
|---|---|---|---|
| Tarih | DatePicker | Bugün | Değiştirilebilir |
| Personel | Dropdown | Son seçilen | PERSONEL tablosundan, Aktif filtreli, aranabilir. Hatırlanır, seçim değiştirilebilir |
| Personel ID | Text (readonly) | Otomatik | Personel seçimine göre doldurulur |
| Bölüm | Text (readonly) | Otomatik | PERSONEL'den PERSONEL.bolumAdi çekilir |
| İş Türü | Dropdown | Son seçilen | Islemler tablosundan, aranabilir. Hatırlanır, seçim değiştirilebilir |
| Miktar | Decimal | Boş | Sayısal klavye açılır. Ondaliklı veri girilebilir. |
| Birim | Text (readonly) | Otomatik | Seçilen işlemin birimi |
| Kaydet | Button | — | Formu doğrular ve KAYITLAR'a yazar |

**AppSheet Formül Örnekleri:**

```
// Bölümü personelden çek
=LOOKUP([personel_id], PersonelListesi[personel_id], PersonelListesi[bolumAdi])

// Birimi işlemden çek
=LOOKUP([islemAdi], Islemler[islemAdi], Islemler[birim])

// kayit_id oluştur
=TEXT(NOW(), "YYYYMMDDHHmmss")

// created zamanı
=TEXT(NOW(), "YYYYMMDDHHmmss")
```

---

### 4.3 Geçmiş / Arşiv Ekranı

- Son **33 gün** listelenir
- Filtre: Ayarlar'da seçili olan personel ismine göre otomatik. İlk kullanımda ana ekrandaki seçim varsayılan olarak atanır. Sonraki kullanımlarda sadece ayarlardan değiştirilebilir. İş Türü içinde aynısı geçerlidir.
- Her kayıt: Tarih, Personel Adı, İşlem, Miktar + Birim
- Her kayıtta **Düzenle** ve **Sil** aksiyonları
- Tarih gruplarına göre bölünür (bugün, dün, vb.)

**AppSheet Filtre Formülü:**
```
AND(
  [tarih] >= TODAY() - 33,
  [adSoyad] = USERSETTINGS("seciliPersonel")
)
```

---

### 4.4 Ayarlar / Admin Paneli

**Personel Bölümü (Tüm Kullanıcılar):**
- Personel seçimi ve kaydetme (`USERSETTINGS` ile saklanır)
- Seçilen personel Geçmiş ekranında otomatik filtre olarak kullanılır
- Ana ekranda başka biri adına da kayıt girilebilir ama buradaki seçili olanı değiştirmez. Ayarlar ekranındaki personel seçimi geçmişte görüntülenecek personel verilerini filtrelemek için kullanılır.
- Şirket adı değiştirilebilir. Varsayılan “EKO-MAK HALI”
- Dil seçim butonu.

**Yönetici Giriş Bölümü:**

Yöneticinin uygulamada özel dashboard görmesi için AppSheet'te **Security Filters** ve **Roles** özelliği kullanılır:

```
Yaklaşım:
1. AppSheet'te kullanıcı tablosu oluşturulur (KULLANICILAR sayfası)
2. Yönetici email'i bu tabloya "YONETICI" rolüyle eklenir
3. AppSheet Security: USERROLE() = "YONETICI" koşuluyla özel view'lar açılır
4. Yönetici kendi Google hesabıyla giriş yapar (şifre Google tarafından yönetilir)
```

**KULLANICILAR Sayfası (Sheets'e Ekle):**

| email | rol | adSoyad |
|---|---|---|
| yonetici@sirket.com | YONETICI | AHMET YÖNETİCİ |
| personel@sirket.com | PERSONEL | ALİ CAN |

**AppSheet Rol Formülü:**
```
USERROLE() = "YONETICI"
```

Bu formül doğruysa Dashboard, grafikler ve tam rapor erişimi açılır.

---

## 5. UI/UX Tasarım Rehberi

### Renk Paleti

```
Ana Renk (Primary)    : #1A1F36  — Derin Lacivert
Vurgu (Accent)        : #00C2A8  — Canlı Teal
İkincil Vurgu         : #F5A623  — Amber (uyarı/bilgi)
Arka Plan             : #F4F6FA  — Açık Gri
Kart Arka Plan        : #FFFFFF  — Beyaz
Metin Birincil        : #1A1F36  — Koyu
Metin İkincil         : #6B7280  — Gri
Başarı               : #10B981  — Yeşil
Hata                  : #EF4444  — Kırmızı
```

### Tipografi

```
Başlık Fontu   : Plus Jakarta Sans (veya Outfit)
Gövde Fontu    : Inter
Monospace      : JetBrains Mono (ID'ler için)

Boyutlar:
  H1: 24px / Bold
  H2: 20px / SemiBold
  H3: 16px / SemiBold
  Body: 14px / Regular
  Caption: 12px / Regular
  Micro: 11px / Medium
```

### Komponent Rehberi

```
Dropdown        : Arama kutulu, 48px yükseklik, teal border focus
Input (Sayısal) : 56px yükseklik, büyük font, sağ hizalı
Button (Primary): 52px yükseklik, teal bg, tam genişlik, 12px radius
Kart            : 12px radius, 1px border, 16px padding, hafif gölge
İkon            : Phosphor Icons seti
```

### Hız için Tasarım Prensipleri

1. **Formda maksimum 2 dokunuş** — Personel + İş türü hatırlanır
2. **Sayısal klavye** — Miktar alanı açılınca direkt numpad gelir
3. **Tek sütun layout** — Kaydırma yok, her şey ekranda
4. **Büyük dokunma alanları** — Minimum 48×48px hedef
5. **Anında feedback** — Kayıt başarılı olunca toast mesajı + titreşim

---

## 6. Bildirim Sistemi

AppSheet'in yerleşik bildirim sistemi kullanılır.

### Günlük Hatırlatıcı

```
Tür          : Push Notification
Saat         : 16:45 (her gün)
Mesaj        : "📋 Performans kaydı girmeyi unutmayın!"
Kime         : Aktif tüm personel
Koşul        : Bugün kayıt girmemişse (isteğe bağlı gelişmiş kural)
```

**AppSheet Automation Konfigürasyonu:**
```
Trigger    : Scheduled — Daily at 16:45
Condition  : ISBLANK(SELECT(KAYITLAR[kayit_id], 
               AND([tarih] = TODAY(), [personel_id] = CONTEXT("User"))))
Action     : Send a notification
Message    : "Performans kaydı girmeyi unutmayın!"
```

---

## 7. Yönetici Dashboard Önerisi

### Yöntem: AppSheet Dashboard View

AppSheet'in **Dashboard** özelliği ile tek ekranda birden fazla view birleştirilebilir.

### Dashboard Bileşenleri

**Kart 1: Günlük Üretim Özeti**
- Bugünkü toplam kayıt sayısı
- Bölüm bazında toplam miktar
- Grafik tipi: Bar Chart

**Kart 2: Personel Performansı**
- Personel bazında günlük toplam miktar
- AppSheet Formülü: `SUMIF(KAYITLAR[miktar], KAYITLAR[adSoyad] = [adSoyad] AND KAYITLAR[tarih] = TODAY())`
- Grafik tipi: Horizontal Bar

**Kart 3: Aylık Trend**
- Son 30 günlük üretim trendi
- Grafik tipi: Line Chart

**Kart 4: Bölüm Karşılaştırma**
- Bölüm bazında karşılaştırma
- Grafik tipi: Pie / Donut

### PDF Rapor

AppSheet'in **Reports** özelliği:
```
Template : Google Docs şablonu
İçerik   : Seçilen tarih aralığı özeti, personel listesi, grafikler
Tetikleyici: Manuel (Rapor Al butonu) veya haftalık otomatik email
```

### Alternatif: Looker Studio Entegrasyonu

Google Sheets'e bağlı ücretsiz Looker Studio dashboard:
- Google hesabıyla giriş
- Gelişmiş grafik seçenekleri
- Paylaşılabilir link
- Tamamen ücretsiz
- Tarayıcıda açılır, uygulama gerekmez

---

## 8. AI Üretim Promptu

> Bu promptu AppSheet AI Builder, Cursor, Lovable veya benzeri AI araçlarına verin.

---

### 8.1 AppSheet AI Builder Promptu

```
Create a mobile performance tracking app called "PerfLog" using Google Sheets as the database.

DATABASE (Google Sheets file: Performans_EKOMAKHALI):

SHEET 1 - PERSONEL (Table: PersonelListesi):
- Durum (Text): "Aktif" or "Pasif"
- personel_id (Number): unique employee ID
- adSoyad (Text): full name
- bolumAdi (Text): department reference
- Departman (Text): subdivision
- Gorevi (Text): role title

SHEET 2 - Bolumler (Table: Bolumler):
- bolum_id (Number): unique ID
- bolumAdi (Text): department name

SHEET 3 - Islemler (Table: Islemler):
- islem_id (Number): unique ID
- islemAdi (Text): work type name
- birim (Text): measurement unit (m2, Kg, Adet, etc.)

SHEET 4 - KAYITLAR (Table: KAYITLAR):
- kayit_id (Text): auto-generated timestamp ID format YYYYMMDDHHMMSS
- tarih (Date): record date, default today
- personel_id (Ref to PERSONEL): employee reference
- adSoyad (Text): auto-populated from PERSONEL
- bolumAdi (Text): auto-populated from PERSONEL
- islemAdi (Ref to Islemler): work type reference
- miktar (Decimal): quantity entered by employee
- birim (Text): auto-populated from Islemler
- created (Text): system timestamp YYYYMMDDHHMMSS

APP REQUIREMENTS:

1. MAIN SCREEN - Performance Entry Form:
   - Date picker (default: today)
   - Employee dropdown (only "Aktif" employees, searchable, remembers last selection)
   - Employee ID display (auto-filled, readonly)
   - Department display (auto-filled from employee, readonly)
   - Work type dropdown (from Islemler table, remembers last selection)
   - Unit display (auto-filled from work type, readonly)
   - Quantity input (numeric keyboard, decimal allowed, large font)
   - SAVE button (full width, teal color, triggers validation + save)

2. HISTORY SCREEN:
   - Show last 33 days of records
   - Filter by currently selected employee (from Settings)
   - Group by date (today, yesterday, date)
   - Each record shows: date, employee name, work type, quantity + unit
   - Edit and Delete actions on each record

3. SETTINGS / ADMIN PANEL:
   - Employee selector (saves to user settings, used as filter in History)
   - Admin section: visible only to users with YONETICI role
   - Admin dashboard: daily production charts, employee performance, department comparison

4. NOTIFICATIONS:
   - Daily push notification at 16:45
   - Message: "Performans kaydı girmeyi unutmayın!"

5. DESIGN:
   - Primary color: #1A1F36 (deep navy)
   - Accent color: #00C2A8 (teal)
   - Font: Plus Jakarta Sans
   - Modern, clean, fast data entry focused
   - Large touch targets (minimum 48px)
   - Numeric keypad opens automatically for quantity field

6. BUSINESS RULES:
   - kayit_id = TEXT(NOW(), "YYYYMMDDHHmmss")
   - bolumAdi auto-fills from selected employee's department
   - birim auto-fills from selected work type
   - Anyone can add a record for any employee (for shared devices)
   - Employee selection is remembered in USERSETTINGS

Generate the complete AppSheet application configuration.
```

---

### 8.2 Cursor / Lovable / v0 React Native Promptu

```
Build a React Native (Expo) mobile app called PerfLog for employee performance tracking.

TECH STACK:
- React Native + Expo
- Google Sheets API v4 (via service account)
- AsyncStorage for local persistence
- React Navigation (bottom tabs)
- React Native Paper or NativeBase UI

SCREENS:
1. HomeScreen - Performance entry form
2. HistoryScreen - Last 33 days records with edit/delete
3. SettingsScreen - Employee selection + admin panel

GOOGLE SHEETS INTEGRATION:
- Read PERSONEL sheet for employee list
- Read Islemler sheet for work types
- Write to KAYITLAR sheet on save
- Use service account JSON key for auth

FORM BEHAVIOR:
- Last selected employee stored in AsyncStorage
- Last selected work type stored in AsyncStorage
- Department auto-fills from employee data (local lookup)
- Unit auto-fills from work type data (local lookup)
- kayit_id = new Date().toISOString().replace(/[-T:.Z]/g,'').slice(0,14)

DESIGN TOKENS:
const theme = {
  primary: '#1A1F36',
  accent: '#00C2A8',
  accent2: '#F5A623',
  bg: '#F4F6FA',
  card: '#FFFFFF',
  text: '#1A1F36',
  textMuted: '#6B7280',
  success: '#10B981',
  danger: '#EF4444',
  radius: 12,
  fontFamily: 'PlusJakartaSans',
}

Implement complete navigation, form validation, error handling, and loading states.
```

---

## 9. GitHub Otomasyonu — Antigravity AI İçin

Bu bölüm Antigravity (veya benzeri AI coding tool) kullanılarak projenin **tamamen otomatik** olarak GitHub'da kodlanması, derlenmesi ve deploy edilmesi için adım adım talimatları içerir.

---

### 9.1 Ön Koşullar

Antigravity başlamadan önce şunların hazır olması gerekir:

```
✅ GitHub hesabı (Personal Access Token ile)
✅ Google Cloud Console projesi (Sheets API aktif)
✅ Service Account JSON dosyası (Sheets erişimi için)
✅ Expo hesabı (EAS Build için) — isteğe bağlı
```

---

### 9.2 Antigravity İçin Komple Otomasyon Promptu

```
=== PERFLOG — TAM OTOMATİK GITHUB & BUILD KURULUMU ===

Aşağıdaki adımları sırasıyla ve tamamen otomatik olarak gerçekleştir.
Her adımı tamamladıktan sonra bir sonrakine geç. Hata alırsan düzelt ve devam et.

--- ADIM 1: GITHUB REPO OLUŞTUR ---

GitHub API kullanarak:
1. Yeni bir public/private repo oluştur:
   - Repo adı: "perflog-app"
   - Açıklama: "PerfLog - Employee Performance Tracking Mobile App"
   - .gitignore: Node
   - License: MIT
   - Ana branch: main

2. Aşağıdaki branch koruma kurallarını ekle:
   - main branch korumalı olsun
   - develop branch oluştur (geliştirme için)

GitHub REST API:
POST https://api.github.com/user/repos
Authorization: token {GITHUB_TOKEN}
{
  "name": "perflog-app",
  "description": "PerfLog - Employee Performance Tracking Mobile App",
  "private": false,
  "auto_init": true,
  "gitignore_template": "Node",
  "license_template": "mit"
}

--- ADIM 2: PROJE YAPISI OLUŞTUR ---

Repo'ya aşağıdaki dosya ve klasör yapısını oluştur:

perflog-app/
├── .github/
│   └── workflows/
│       ├── ci.yml           # Test ve lint
│       └── build.yml        # EAS Build tetikleyici
├── src/
│   ├── screens/
│   │   ├── HomeScreen.tsx
│   │   ├── HistoryScreen.tsx
│   │   └── SettingsScreen.tsx
│   ├── components/
│   │   ├── RecordCard.tsx
│   │   ├── EmployeeDropdown.tsx
│   │   ├── WorkTypeDropdown.tsx
│   │   └── SaveButton.tsx
│   ├── services/
│   │   ├── sheetsService.ts  # Google Sheets API
│   │   └── storageService.ts # AsyncStorage
│   ├── hooks/
│   │   ├── useEmployees.ts
│   │   ├── useWorkTypes.ts
│   │   └── useRecords.ts
│   ├── types/
│   │   └── index.ts
│   ├── theme/
│   │   └── index.ts
│   └── navigation/
│       └── AppNavigator.tsx
├── assets/
│   ├── icon.png
│   └── splash.png
├── app.json
├── app.config.ts
├── package.json
├── tsconfig.json
├── babel.config.js
├── .env.example
└── README.md

--- ADIM 3: TEMEL DOSYALARI YAZ ---

Her dosyayı tam olarak yaz. Aşağıdaki konfigürasyonları kullan:

### package.json
{
  "name": "perflog-app",
  "version": "1.0.0",
  "main": "node_modules/expo/AppEntry.js",
  "scripts": {
    "start": "expo start",
    "android": "expo start --android",
    "ios": "expo start --ios",
    "test": "jest",
    "lint": "eslint src --ext .ts,.tsx",
    "build:android": "eas build --platform android",
    "build:ios": "eas build --platform ios"
  },
  "dependencies": {
    "expo": "~51.0.0",
    "expo-status-bar": "~1.12.1",
    "react": "18.2.0",
    "react-native": "0.74.1",
    "@react-navigation/native": "^6.1.17",
    "@react-navigation/bottom-tabs": "^6.5.20",
    "react-native-safe-area-context": "4.10.1",
    "react-native-screens": "3.31.1",
    "@react-native-async-storage/async-storage": "1.23.1",
    "expo-notifications": "~0.28.5",
    "expo-font": "~12.0.5",
    "date-fns": "^3.6.0",
    "axios": "^1.7.2"
  },
  "devDependencies": {
    "@babel/core": "^7.24.0",
    "@types/react": "~18.2.79",
    "typescript": "^5.3.3",
    "@typescript-eslint/eslint-plugin": "^7.7.0",
    "eslint": "^8.57.0",
    "jest": "^29.2.1"
  }
}

### src/theme/index.ts
export const theme = {
  colors: {
    primary: '#1A1F36',
    accent: '#00C2A8',
    accent2: '#F5A623',
    background: '#F4F6FA',
    card: '#FFFFFF',
    text: '#1A1F36',
    textMuted: '#6B7280',
    success: '#10B981',
    danger: '#EF4444',
    border: '#E5E7EB',
  },
  spacing: { xs: 4, sm: 8, md: 16, lg: 24, xl: 32 },
  borderRadius: { sm: 8, md: 12, lg: 16, xl: 24 },
  typography: {
    h1: { fontSize: 24, fontWeight: '700' as const },
    h2: { fontSize: 20, fontWeight: '600' as const },
    h3: { fontSize: 16, fontWeight: '600' as const },
    body: { fontSize: 14, fontWeight: '400' as const },
    caption: { fontSize: 12, fontWeight: '400' as const },
  }
}

### src/types/index.ts
export interface Employee {
  personel_id: number
  adSoyad: string
  bolumAdi: string
  Departman: string
  Gorevi: string
  Durum: 'Aktif' | 'Pasif'
}

export interface WorkType {
  islem_id: number
  islemAdi: string
  birim: string
}

export interface PerformanceRecord {
  kayit_id: string
  tarih: string
  personel_id: number
  adSoyad: string
  bolumAdi: string
  islemAdi: string
  miktar: number
  birim: string
  created: string
}

### src/services/sheetsService.ts
Implement Google Sheets API v4 service with:
- getEmployees(): fetch PERSONEL sheet, filter Aktif only
- getWorkTypes(): fetch Islemler sheet
- getRecords(personelId, days): fetch last N days records for employee
- saveRecord(record): append to KAYITLAR sheet
- updateRecord(kayit_id, data): update existing row
- deleteRecord(kayit_id): delete row by kayit_id

Use service account JWT authentication.
Sheets ID: from environment variable SHEETS_ID
Service account credentials: from environment variable GOOGLE_SERVICE_ACCOUNT_JSON

--- ADIM 4: CI/CD WORKFLOWS YAZ ---

### .github/workflows/ci.yml
name: CI
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      - run: npm ci
      - run: npm run lint
      - run: npm test

### .github/workflows/build.yml
name: EAS Build
on:
  push:
    branches: [main]
    tags: ['v*']
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: npm install -g eas-cli
      - run: npm ci
      - uses: expo/expo-github-action@v8
        with:
          expo-version: latest
          eas-version: latest
          token: ${{ secrets.EXPO_TOKEN }}
      - run: eas build --platform android --non-interactive
        env:
          EXPO_TOKEN: ${{ secrets.EXPO_TOKEN }}

--- ADIM 5: README YAZ ---

Tam bir README.md oluştur:
- Proje açıklaması (Türkçe ve İngilizce)
- Kurulum adımları
- Environment variables açıklaması
- Google Sheets yapısı
- AppSheet alternatifleri
- Screenshots bölümü (placeholder)
- Katkıda bulunma rehberi

--- ADIM 6: GitHub SECRETS TANIMLA ---

Repo'ya aşağıdaki secrets'ları ekle (değerler dışarıdan verilecek):
- GOOGLE_SHEETS_ID
- GOOGLE_SERVICE_ACCOUNT_JSON
- EXPO_TOKEN

--- ADIM 7: İLK COMMIT ---

Tüm dosyaları develop branch'e commit et:
git commit -m "feat: initial PerfLog app structure

- Add project structure with React Native + Expo
- Add Google Sheets service integration
- Add all three screens (Home, History, Settings)
- Add CI/CD workflows
- Add theme and type definitions"

--- ADIM 8: PULL REQUEST OLUŞTUR ---

develop → main pull request oluştur:
- Başlık: "feat: Initial PerfLog app release v1.0.0"
- Açıklama: Değişiklik listesi
- Label: "enhancement"

--- TAMAMLANDI ---
Repo URL'ini ve kurulum adımlarını özetle.
```

---

### 9.3 Environment Variables

`.env.example` dosyası içeriği:

```env
# Google Sheets
GOOGLE_SHEETS_ID=your_spreadsheet_id_here
GOOGLE_SERVICE_ACCOUNT_JSON={"type":"service_account",...}

# App Config
APP_NAME=PerfLog
APP_VERSION=1.0.0
APP_ENV=development

# Expo (EAS Build için)
EXPO_TOKEN=your_expo_token_here
```

---

## 10. Geliştirme Notları

### AppSheet Kurulum Adımları (Manuel)

```
1. appsheet.com → New App → Start with existing data
2. Google Sheets dosyasını seç: Performans_EKOMAKHALI
3. Tüm 4 sayfayı tablo olarak ekle
4. Table Relationships:
   - KAYITLAR.personel_id → PERSONEL.personel_id
   - KAYITLAR.islemAdi → Islemler.islemAdi
   - PERSONEL.bolumAdi → Bolumler.bolumAdi
5. Virtual Column ekle: kayit_id = TEXT(NOW(), "YYYYMMDDHHmmss")
6. Form View oluştur: KAYITLAR için
7. Deck/Table View oluştur: Geçmiş için
8. Dashboard View oluştur: Yönetici için
9. Security → Require Sign-In → Google
10. Automation → New Automation → Schedule → 16:45 → Notification
```

### Önemli Kısıtlamalar

| Kısıtlama | Açıklama | Çözüm |
|---|---|---|
| AppSheet ücretsiz sınır | 10 kullanıcıya kadar ücretsiz | Starter plan ~$5/kullanıcı/ay |
| Sheets yazma hızı | Saniyede 1 yazma | Sıra bekleme yok, bağımsız cihazlar |
| Offline sync | AppSheet offline destekler | Bağlantı gelince otomatik sync |
| PDF boyutu | Büyük raporlar yavaş | Tarih aralığını sınırla |

### Hata Senaryoları

```
1. İnternet bağlantısı yok   → Yerel kayıt, online gelince sync
2. Personel bulunamadı       → "Ayarlar > Personel Seç" yönlendirmesi
3. Sheets yazma hatası       → Toast hata mesajı + yeniden dene butonu
4. Aynı kayıt tekrarı        → kayit_id benzersizliği sayesinde önlenir
```

---

## 11. Gelecek Versiyon Modülleri

### v1.1 — Hızlı Kazanımlar

- [ ] **Çoklu kayıt girişi**: Bir günde birden fazla işlem girebilme
- [ ] **Fotoğraf eki**: Kayda fotoğraf ekleme (QC kanıtı için)
- [ ] **Toplu onay**: Yöneticinin kayıtları onaylaması

### v1.2 — Analitik Genişletme

- [ ] **Kişisel grafik**: Personelin kendi performans trendi
- [ ] **Hedef takibi**: Günlük/aylık hedef ve gerçekleşme
- [ ] **Puan sistemi**: Gamification ile motivasyon artışı

### v2.0 — Platform Genişlemesi

- [ ] **Çoklu lokasyon**: Farklı fabrika/şantiye desteği
- [ ] **QR kod**: QR ile hızlı personel girişi
- [ ] **Barkod okuyucu**: Malzeme miktarı için barkod
- [ ] **WhatsApp entegrasyonu**: Günlük özet raporu WhatsApp'a
- [ ] **Maaş hesaplama**: Performansa bağlı prim hesabı

### v3.0 — AI Özellikleri

- [ ] **Anomali tespiti**: Olağandışı miktar girişlerinde uyarı
- [ ] **Tahminleme**: Gelecek hafta üretim tahmini
- [ ] **Akıllı hatırlatıcı**: Kişiye özel bildirim zamanı

---

## Ekler

### AppSheet Fiyatlandırma (2026)

| Plan | Kullanıcı | Fiyat | Özellikler |
|---|---|---|---|
| Free | 10 | $0 | Temel özellikler |
| Starter | Sınırsız | $5/kullanıcı/ay | PDF, Bildirimler |
| Core | Sınırsız | $10/kullanıcı/ay | Gelişmiş güvenlik |
| Enterprise | Özel | Özel | SSO, Audit Log |

### Yararlı Linkler

- AppSheet Dokümantasyon: https://support.google.com/appsheet
- Google Sheets API: https://developers.google.com/sheets
- Expo Dokümantasyon: https://docs.expo.dev
- Looker Studio: https://lookerstudio.google.com

---

*Son Güncelleme: 16 Mart 2026*  
*Belge Sürümü: v1.0*  
*Hazırlayan: PerfLog Teknik Ekibi*
