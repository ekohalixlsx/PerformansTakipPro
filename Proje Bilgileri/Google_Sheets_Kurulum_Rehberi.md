# Google Sheets Bağlantı Kurulum Rehberi (Adım Adım)

Bu rehber, Performans Takip Pro uygulamasını Google E-Tablonuza bağlamak için yapmanız gereken **tek seferlik** kurulumu anlatır. Kredi kartı veya ödeme **gerekmez**.

---

## Adım 1: Google E-Tablo Oluşturma

1. **Google Drive**'a gidin: [drive.google.com](https://drive.google.com)
2. Sol üstten **"+ Yeni"** → **"Google E-Tablolar"** → **"Boş E-Tablo"** seçin
3. Tabloya isim verin: **"Performans Takip Pro"**
4. Alt sekmelere (sayfaları) aşağıdaki isimleri verin (tam olarak bu şekilde yazın!):

### Sayfa 1: `PERSONEL`
| Durum | PersonelID | Ad Soyad | Bölüm Adı | Departman | Görevi |
|-------|-----------|----------|-----------|-----------|--------|
| Aktif | 1001 | ALİ CAN | A BÖLÜMÜ | ÜRETİM | OPERATÖR |
| Aktif | 1002 | MEHMET YILMAZ | B BÖLÜMÜ | ÜRETİM | OPERATÖR |

### Sayfa 2: `Islemler`
| İşlem ID | İşlem Adı | Birim |
|----------|-----------|-------|
| 1 | DOKUMA | m² |
| 2 | OVERLOK | Kg |
| 3 | PAKETLEME | Adet |

### Sayfa 3: `Bolumler`
| Bölüm ID | Bölüm Adı |
|----------|-----------|
| 1 | A BÖLÜMÜ |
| 2 | B BÖLÜMÜ |

### Sayfa 4: `KAYITLAR`
| Kayıt ID | Tarih | Personel ID | Ad Soyad | Bölüm Adı | İşlem Adı | Miktar | Birim | Oluşturulma |
|----------|-------|-------------|----------|-----------|-----------|--------|-------|------------|

*(Bu sayfayı boş bırakın, sadece başlık satırını ekleyin. Uygulama buraya yazar.)*

---

## Adım 2: Apps Script Açma

1. E-Tablonuz açıkken üst menüden: **Uzantılar → Apps Script** tıklayın
2. Yeni bir sekme açılacak ve bir kod editörü göreceksiniz
3. Orada varsayılan olarak `function myFunction() {}` yazıyordur

---

## Adım 3: Kodu Yapıştırma

1. Editördeki **tüm mevcut kodu silin** (Ctrl+A → Delete)
2. Projenizdeki şu dosyayı açın:
   - **Konum:** `c:\projects\PerformansTakipPro\Proje Bilgileri\Google_Apps_Script.js`
3. Dosyanın **tüm içeriğini** kopyalayın (Ctrl+A → Ctrl+C)
4. Apps Script editörüne yapıştırın (Ctrl+V)
5. Sol üstteki 💾 **disket simgesine** basarak kaydedin (veya Ctrl+S)

---

## Adım 4: Web Uygulaması Olarak Yayınlama

1. Sağ üstteki **"Dağıt"** (Deploy) butonuna tıklayın
2. **"Yeni dağıtım"** (New deployment) seçin
3. Sol taraftaki ⚙️ **dişli çarka** tıklayıp **"Web uygulaması"** (Web app) seçin
4. Açılan pencerede:
   - **Açıklama:** `Performans API v1` (isteğe bağlı)
   - **Şu kullanıcı olarak çalıştır:** `Ben` (Me) ← değiştirmeyin
   - **Kim erişebilir:** ⚠️ **"Herkes"** (Anyone) seçin ← **ÖNEMLİ!**
5. **"Dağıt"** (Deploy) butonuna basın

---

## Adım 5: İzin Verme

1. Google sizden **izin isteyecek**. "Erişimi incele" (Review Permissions) tıklayın
2. Google hesabınızı seçin
3. **"Bu uygulama doğrulanmamış"** uyarısı çıkabilir:
   - Sol alttaki **"Gelişmiş"** (Advanced) tıklayın  
   - **"Performans API v1'e git (güvenli değil)"** tıklayın
   - (Bu uyarı normaldir, kendi betiğiniz olduğu için güvenlidir)
4. **"İzin ver"** (Allow) tıklayın

---

## Adım 6: URL'yi Kopyalama

1. Başarılı dağıtım sonrası bir **Web uygulaması URL'si** görünecek:
   ```
   https://script.google.com/macros/s/AKfycbx...uzun_bir_kod.../exec
   ```
2. Bu URL'yi kopyalayın
3. **Bu URL'yi bana (sohbete) yapıştırın** → Ben uygulamaya gömeceğim

---

## ÖNEMLİ NOTLAR

- ❌ Kredi kartı **gerekmez**
- ❌ Google Cloud projesi **gerekmez**  
- ✅ Sadece normal Google hesabı yeterli
- ✅ URL tamamen size özeldir, başkası bilemez
- ✅ E-Tablonuzdaki veriler her zaman sizin kontrolünüzde
- ⚠️ Sayfa isimlerini (`PERSONEL`, `Islemler`, `Bolumler`, `KAYITLAR`) **tam olarak** bu şekilde yazın!
