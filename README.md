# Performans Takip Pro

Şirket ve kurum personellerinin günlük iş ve performans kayıtlarını hızlı, kolay ve düzenli şekilde yönetmek için geliştirilmiş modern bir Android uygulamasıdır.

## 📱 Uygulama Hakkında

Performans Takip Pro, üretim ve hizmet sektöründe çalışan personelin günlük yaptığı işleri kayıt altına alır, geçmişe dönük takip sağlar ve yöneticilere performans analizleri sunar. Veriler güvenli şekilde Google Sheets üzerinde saklanır.

## 🚀 Özellikler

### Kayıt Ekranı
- Tarihe göre çalışma kaydı oluşturma
- Personel seçimi (aranabilir liste, otomatik departman eşleşmesi)
- İş türü ve miktar girişi (birim otomatik gelir)
- Tek tuşla hızlı kayıt

### Geçmiş Ekranı
- Son 33 günlük kayıtları listeleme
- Personele göre filtreleme ve tarih sıralaması
- Kayıt düzenleme ve silme
- Günlük gruplandırılmış kompakt kart görünümü

### Dashboard (Yönetici Paneli)
- Admin girişi gerektirir (güvenli erişim)
- Günlük / Haftalık / Aylık performans özetleri
- Personel bazlı detaylı analiz
- PDF rapor oluşturma ve e-posta ile gönderme
- Şirket adı PDF raporlara otomatik eklenir

### Ayarlar
- Varsayılan personel ve iş türü seçimi
- Şirket adı düzenleme
- Günlük hatırlatıcı bildirimi (saat ayarlanabilir)
- Bağlantı durumu kontrolü
- Hakkında sayfası ve geliştirici iletişimi

### Ek Özellikler
- **İlk kullanım hoşgeldin ekranı:** Uygulama ilk açıldığında kullanıcı kendini personel listesinden seçer
- **Yatay kaydırma (Swipe):** Ekranlar arasında parmakla kaydırarak geçiş
- **Karanlık mod** uyumlu modern tasarım
- **WorkManager** ile günlük hatırlatıcı bildirimi
- **Şifre değiştirme:** Dashboard üzerinden admin şifresi değiştirilebilir
- **Otomatik Kayıt Numarası:** Her yeni kayıda otomatik sıra numarası verilir (tablo silinse bile devam eder)

## 📋 Kullanım Kılavuzu

### Kurulum
1. APK dosyasını telefonunuza yükleyin
2. İlk açılışta hoşgeldin ekranında kendinizi personel listesinden seçin
3. Bu seçim Ayarlar ekranına kaydedilir

### Günlük Kullanım
1. **Kayıt ekranından** tarihi, personelinizi, iş türünü ve miktarını seçip kaydedin
2. **Geçmiş ekranından** eski kayıtlarınızı görüntüleyin, düzenleyin veya silin
3. **Ayarlar ekranından** varsayılan tercihlerinizi değiştirin

### Yönetici Erişimi
1. Dashboard ekranına gidin
2. "Giriş Yap" butonuna tıklayın
3. Yönetici bilgilerinizi girin
4. Performans analizlerini inceleyin, PDF rapor oluşturup e-posta ile gönderin

## 📦 Google Sheets Bağlantısı

Uygulama, verilerini Google Sheets üzerinde saklar. Kurulum için:
1. Google Drive'da yeni bir E-Tablo oluşturun
2. Gerekli sayfa sekmelerini ekleyin (PERSONEL, KAYITLAR, Islemler, Bolumler, KULLANICILAR)
3. Apps Script kodunu yapıştırıp Web Uygulaması olarak dağıtın
4. URL'yi uygulamadaki Ayarlar bölümüne girin

> Detaylı kurulum için `Proje Bilgileri/Google_Sheets_Kurulum_Rehberi.md` dosyasına bakın.

## 👨‍💻 Geliştirici

**İlyas YEŞİL**
📧 ilyasyesil.develop@gmail.com

---
*Tüm hakları saklıdır. İzinsiz kopyalanamaz, dağıtılamaz.*
