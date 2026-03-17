# Performans Takip Pro

Bu uygulama, şirket personellerinin günlük iş ve performans kayıtlarını hızlı, güvenli ve düzenli bir şekilde yönetmek için geliştirilmiş modern bir Android (Kotlin / Jetpack Compose) uygulamasıdır. 

Uygulamanın en büyük özelliklerinden biri, veri tabanı olarak **Google E-Tablolar'ı (Google Sheets)** kullanmasıdır. Bu sayede hiçbir sunucu maliyeti, veritabanı kurulumu veya Google Cloud hesabı gerektirmeden, **tamamen ücretsiz** bir şekilde veriler bulutta tutulur.

## 📱 Özellikler

- **Personel Özelinde Kayıt:** Personel seçimi, otomatik departman eşleşmesi.
- **İşlem Modülleri:** Yapılacak işlemler ve miktarlarının kolayca seçilmesi ve kaydedilmesi.
- **Geçmiş Ekranı:** Son 33 günlük kayıtları listeleme, düzenleme ve silme.
- **Gelişmiş Dashboard:** Günlük, haftalık, aylık performansı grafiksel olarak izleme ve analiz etme.
- **PDF Dışa Aktarım:** Performans raporlarını PDF formatında dışa aktarma (planlanan özellik).
- **Akıllı Hatırlatıcı:** Geliştirilmiş **WorkManager** entegrasyonu sayesinde, her gün belirlediğiniz saatte "Bugün için performans kayıtlarını girdiniz mi?" hatırlatması.
- **Çoklu Dil (i18n):** Uygulama üzerinden anlık Türkçe ve İngilizce dil değişimi.
- **Dark Mode Uyumluluğu:** Modern, göz yormayan karanlık mod desteği.
- **Tek Tıkla Senkronizasyon:** Ayarlar içinden sadece tek bir URL yapıştırarak tüm firmaya Google E-Tablo entegrasyonu sağlama.

## 🛠️ Kurulum ve Google Sheets Entegrasyonu

Uygulamanın çalışması için kendi Google E-Tablonuzu bağlamanız gerekir.

1. **E-Tablo Oluşturun:** Drive'da yeni bir tablo oluşturun (`PERSONEL`, `Islemler`, `Bolumler`, `KAYITLAR` isimli sekmeler olmalı).
2. **Apps Script:** `Proje Bilgileri/Google_Apps_Script.js` içindeki kodu kopyalayın ve E-Tablonuzun **Uzantılar -> Apps Script** alanına yapıştırıp "Web Uygulaması" (Herkes erişebilir) olarak dağıtın.
3. **Uygulamaya Bağlayın:** Çıkan URL'yi, uygulamanızın Ayarlar kısmındaki veya `AppConfig.kt` içindeki ilgili alana ekleyin.

## 🎨 Tasarım Sistemi

Uygulama, Jetpack Compose ile **Material 3** standartlarına uygun, fakat tamamen özelleştirilmiş, modern "Deep Navy" ve "Teal" kontrastı kullanılarak tasarlanmıştır. Akıcı sayfa geçişleri, yumuşak gölgeler (shadows) ve pürüzsüz micro-animasyonlar barındırır.

## 👩‍💻 Geliştirici ve Hakkında

Uygulama içerisindeki **Ayarlar > Hakkında** sayfasından kullanım kılavuzuna ulaşabilirsiniz. 

**Geliştirici:** İlyas YEŞİL
**İletişim:** ilyasyesil.develop@gmail.com

---
*Tüm hakları saklıdır. İzinsiz kopyalanamaz, dağıtılamaz.*
