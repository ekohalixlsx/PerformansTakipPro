# Performans Takip Pro — v1.2.0 Planı

## Tamamlanan Fazlar ✅
- Faz 1: Google Sheets entegrasyonu (MainViewModel, tüm ekranlar)
- Faz 2: Scroll düzeltme, tarih formatı, kompakt kartlar, düzenle dialogu
- Faz 3: Admin girişi (Dashboard'a taşındı)
- Faz 4: Versiyon 1.1.0, APK adlandırma
- Faz 5: **v1.2.0 Final Polish** (HorizontalPager, Flickering fix, Swipe Nav) ✅

## Görevler (v1.2.0)

### 1. Dashboard Admin Erişim Kontrolü ✅
- [x] Dashboard ekranına giriş için admin şifresi iste (EKO / EKO2026)
- [x] Admin giriş dialogunu SettingsScreen'den kaldır, Dashboard'a taşı
- [x] Giriş yapılmadıysa Dashboard yerine şifre ekranı göster
- [x] Beni hatırla özelliği

### 2. Ana Ekran (EntryScreen) Kompaktlık ✅
- [x] Miktar alanı font veya yükseklik ayarla (sayı sığmıyor)
- [x] Form öğeleri arası boşluk azalt (Tarih/Personel/Bölüm/İşTürü)
- [x] Alt menü yüksekliği azalt (HorizontalPager kullanıldı)

### 3. Geçmiş Ekranı Filtreleme ✅
- [x] Ayarlarda seçili personele göre kayıtları filtrele

### 4. Şirket Adı Düzenleme ✅
- [x] Şirket adını değiştirebilir dialogu ekle
- [x] SharedPreferences'a kaydet

### 5. Karanlık Mod Düzeltmeleri ✅
- [x] Hakkında ekranı "İletişime Geç" buton rengi düzelt

### 6. Dil Seçimi ✅
- [x] Kaldırıldı (şimdilik gerek yok)

### 7. PDF Rapor ve Mail ✅
- [x] Dashboard'da seçili personelin raporunu PDF olarak oluştur
- [x] KULLANICILAR tablosundan personel seçimi
- [x] PDF'i mail olarak gönder (İzin sorunu yok)
- [x] PDF rapora şirket adını ekle (v1.2.0)
