/**
 * PERFORMANS TAKİP PRO - GOOGLE APPS SCRIPT API
 * 
 * Bu kod, Android uygulamanızın Google Sheets ile tamamen ücretsiz
 * (Google Cloud servisine veya kredi kartına ihtiyaç duymadan) haberleşmesini sağlar.
 * 
 * KURULUM ADIMLARI:
 * 1. Google Drive'da yeni bir Google E-Tablo oluşturun.
 * 2. Üst menüden Uzantılar > Apps Script'e tıklayın.
 * 3. Açılan kod ekranındaki her şeyi silip, bu dosyadaki tüm kodları yapıştırın.
 * 4. Üstteki "Yeni Dağıtım" (Deploy -> New Deployment) tuşuna basın.
 * 5. Tür olarak "Web Uygulaması"nı (Web App) seçin.
 * 6. "Erişim Düzeyi"ni (Who has access) "Herkes" (Anyone) olarak seçin. (ÖNEMLİ!)
 * 7. Dağıt'a basın ve size verilen "Web Uygulaması URL'sini" kopyalayın.
 *    (Örn: https://script.google.com/macros/s/AKfycb.../exec)
 * 8. Bu URL'yi Android uygulamanızın Ayarlar kısmına yapıştırın.
 */

// Tablo İsimleri (Kendi tablolarınızın alt sekmelerindeki isimlerle birebir aynı olmalı)
var SPREADSHEET_ID = SpreadsheetApp.getActiveSpreadsheet().getId();
var SHEET_PERSONEL = "PERSONEL";
var SHEET_ISLEMLER = "Islemler";
var SHEET_BOLUMLER = "Bolumler";
var SHEET_KAYITLAR = "KAYITLAR";

/**
 * GET İsteklerini Karşılar (Veri Okuma)
 * Android uygulamasından gelen ?action=... parametresine göre çalışır.
 */
function doGet(e) {
  var action = e.parameter.action;
  
  if (action == 'getEmployees') {
    return okResponse(getEmployees());
  } 
  else if (action == 'getWorkTypes') {
    return okResponse(getWorkTypes());
  }
  else if (action == 'getDepartments') {
    return okResponse(getDepartments());
  }
  else if (action == 'getRecords') {
    var days = e.parameter.days || 33;
    var employee = e.parameter.employeeName || null;
    return okResponse(getRecords(days, employee));
  }
  
  return errorResponse("Geçersiz action parametresi: " + action);
}

/**
 * POST İsteklerini Karşılar (Veri Yazma, Güncelleme, Silme)
 */
function doPost(e) {
  try {
    var body = JSON.parse(e.postData.contents);
    var action = body.action;
    
    if (action == 'saveRecord') {
      return okResponse(saveRecord(body.data));
    }
    else if (action == 'updateRecord') {
      return okResponse(updateRecord(body.data));
    }
    else if (action == 'deleteRecord') {
      return okResponse(deleteRecord(body.kayitId));
    }
    
    return errorResponse("Geçersiz action parametresi: " + action);
  } catch (err) {
    return errorResponse("POST Hatası: " + err.toString());
  }
}

// ==========================================
// YARDIMCI VERİ OKUMA FONKSİYONLARI
// ==========================================

function getEmployees() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_PERSONEL);
  var data = sheet.getDataRange().getValues();
  var employees = [];
  
  // İlk satır başlık kabul edilir, 1. satırdan (index 1) başlanıyor.
  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    var durum = row[0] ? row[0].toString().toUpperCase().trim() : "";
    
    // "ACTIVE" (İngilizce) ve "AKTİF"/"AKTIF" (Türkçe) kabul et
    if (durum === "ACTIVE" || durum.indexOf("AKT") === 0) {
      employees.push({
        durum: row[0].toString(),
        personelId: parseInt(row[1]) || 0,
        adSoyad: row[2] ? row[2].toString() : "",
        bolumAdi: row[3] ? row[3].toString() : "",
        departman: row[4] ? row[4].toString() : "",
        gorevi: row[5] ? row[5].toString() : ""
      });
    }
  }
  return employees;
}

function getWorkTypes() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_ISLEMLER);
  var data = sheet.getDataRange().getValues();
  var workTypes = [];
  
  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    if (row[1]) {
      workTypes.push({
        islemId: parseInt(row[0]) || 0,
        islemAdi: row[1].toString(),
        birim: row[2] ? row[2].toString() : ""
      });
    }
  }
  return workTypes;
}

function getDepartments() {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_BOLUMLER);
  var data = sheet.getDataRange().getValues();
  var departments = [];
  
  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    if (row[1]) {
      departments.push({
        bolumId: parseInt(row[0]) || 0,
        bolumAdi: row[1].toString()
      });
    }
  }
  return departments;
}

function getRecords(days, employee) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_KAYITLAR);
  var data = sheet.getDataRange().getValues();
  var records = [];
  
  // Bugünden geriye filtreleme için tarih hesaplaması (yaklaşık)
  var cutoffDate = new Date();
  cutoffDate.setDate(cutoffDate.getDate() - parseInt(days));

  for (var i = 1; i < data.length; i++) {
    var row = data[i];
    if (row[0]) {
      // Tarih formatı "dd/MM/yyyy" varsayılıyor. Basit kontrol yapıyoruz.
      // E-Tablo tarihlerini Javascript Date objesine çevirme:
      var rowDateStr = row[1].toString();
      var parts = rowDateStr.split('/');
      var rowDate = null;
      if(parts.length == 3) {
         rowDate = new Date(parts[2], parts[1]-1, parts[0]);
      }
      
      // employee filtresi
      if (employee && employee !== "") {
        if (row[3] !== employee) continue;
      }
      
      // tarih filtresi
      if (rowDate && rowDate < cutoffDate) continue;

      records.push({
        kayitId: row[0].toString(),
        tarih: row[1].toString(),
        personelId: parseInt(row[2]) || 0,
        adSoyad: row[3].toString(),
        bolumAdi: row[4] ? row[4].toString() : "",
        islemAdi: row[5] ? row[5].toString() : "",
        miktar: parseFloat(row[6].toString().replace(",", ".")) || 0.0,
        birim: row[7] ? row[7].toString() : "",
        created: row[8] ? row[8].toString() : ""
      });
    }
  }
  
  // En yeniden en eskiye sıralama
  records.sort(function(a, b) {
    if (a.created > b.created) return -1;
    if (a.created < b.created) return 1;
    return 0;
  });
  
  return records;
}

// ==========================================
// YARDIMCI VERİ YAZMA FONKSİYONLARI
// ==========================================

function saveRecord(data) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_KAYITLAR);
  
  // Miktarı Türkçe Excel formatında virgülle kaydetmek istiyorsanız:
  var miktarStr = data.miktar.toString().replace(".", ",");
  
  sheet.appendRow([
    data.kayitId,
    data.tarih,
    data.personelId,
    data.adSoyad,
    data.bolumAdi,
    data.islemAdi,
    miktarStr,
    data.birim,
    data.created
  ]);
  
  return { success: true };
}

function updateRecord(data) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_KAYITLAR);
  var values = sheet.getDataRange().getValues();
  
  for (var i = 1; i < values.length; i++) {
    if (values[i][0] == data.kayitId) {
       var rowIndex = i + 1; // 1-based index
       var miktarStr = data.miktar.toString().replace(".", ",");
       
       sheet.getRange(rowIndex, 1, 1, 9).setValues([[
          data.kayitId,
          data.tarih,
          data.personelId,
          data.adSoyad,
          data.bolumAdi,
          data.islemAdi,
          miktarStr,
          data.birim,
          data.created
       ]]);
       return { success: true };
    }
  }
  throw new Error("Kayıt bulunamadı.");
}

function deleteRecord(kayitId) {
  var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(SHEET_KAYITLAR);
  var values = sheet.getDataRange().getValues();
  
  for (var i = 1; i < values.length; i++) {
    if (values[i][0] == kayitId) {
       var rowIndex = i + 1; // 1-based index
       sheet.deleteRow(rowIndex);
       return { success: true };
    }
  }
  throw new Error("Silinecek kayıt bulunamadı.");
}

// ==========================================
// YANIT (RESPONSE) FORMATLAYICILARI
// ==========================================

function okResponse(data) {
  return ContentService.createTextOutput(JSON.stringify({
    status: 'success',
    data: data
  })).setMimeType(ContentService.MimeType.JSON);
}

function errorResponse(message) {
  return ContentService.createTextOutput(JSON.stringify({
    status: 'error',
    message: message
  })).setMimeType(ContentService.MimeType.JSON);
}
