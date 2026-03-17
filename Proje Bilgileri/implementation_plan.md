# Google Sheets Integration (Free Tier via Apps Script)

## The Problem
Google Cloud Console requires a credit card to set up a project and generate Service Account credentials for the Google Sheets API, even for the free tier.

## The Solution: Google Apps Script Web App
We will replace the direct `google-api-services-sheets` library usage with a custom REST API built on **Google Apps Script**. This approach is 100% free, requires no credit card, and leverages the user's personal Google account.

### How it works:
1. **Google Apps Script:** The user creates a script attached to their Google Sheet.
2. **Web App Deployment:** The script is deployed as a Web App accessible via a secure URL (`https://script.google.com/macros/s/.../exec`).
3. **Android App:** The Android app sends standard HTTP GET/POST requests to this Web App URL, parsing JSON responses.

### Implementation Steps

#### 1. Apps Script Code (Server-side)
We will provide a `Code.gs` script containing:
- `doGet(e)`: Handles requests for Employee list, Work Types, Daily Records, and Dashboard statistics.
- `doPost(e)`: Handles inserting new performance records, editing, and deleting.
- Security: Uses simple token validation or relies on the obscure, unguessable Web App URL for security in this internal app context.

#### 2. Android App Changes (Client-side)
- **Remove Google API Client dependencies** from [build.gradle.kts](file:///c:/projects/SmartQRKotlin/build.gradle.kts) to reduce app size.
- **Add Retrofit/OkHttp** for standard HTTP networking (or just use OkHttp/HttpURLConnection).
- **Rewrite [SheetsService.kt](file:///c:/projects/PerformansTakipPro/app/src/main/java/com/ekomak/performanstakippro/data/remote/SheetsService.kt)** to use Retrofit/HTTP calls instead of the Google API Client.
- **Update Settings:** Remove JSON upload; add a single text field for the "Apps Script Web App URL".

### Migration Plan (Component Updates)

#### [MODIFY] app/build.gradle.kts
- Remove: `com.google.api-client:google-api-client-android`
- Remove: `com.google.apis:google-api-services-sheets`
- Remove: `com.google.auth:google-auth-library-oauth2-http`
- Add: `com.squareup.okhttp3:okhttp` (or Retrofit)

#### [MODIFY] app/src/main/java/com/ekomak/performanstakippro/data/remote/SheetsService.kt
- Completely rewrite to execute HTTP requests to the provided `scriptUrl`.

#### [MODIFY] app/src/main/java/com/ekomak/performanstakippro/ui/screens/settings/SettingsScreen.kt
- Remove the complex Admin/JSON setup.
- Add a simple field: "Web App URL" (Script URL) for connection.

#### [NEW] Proje Bilgileri/Google_Apps_Script.js
- Deliver the JavaScript code that the user needs to paste into their Google Sheet's Apps Script editor.
