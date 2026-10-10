# Navira – Travel Tracker Android

**Navira** adalah aplikasi pencatat perjalanan realtime bergaya *cartoon sticker* untuk Gen Z. Aplikasi ini mencatat koordinat titik awal secara otomatis begitu pelacakan diaktifkan, memplot rute di atas peta Leaflet, menghitung jarak tempuh realtime, kompas, serta menyediakan fitur unggulan **Pulang ke Start** agar penjelajah tidak tersesat kembali ke titik awal.

---

## Fitur Utama

- **Realtime GPS Tracking Engine:**
  - 2 Mode: **Jalan-jalan** (interval adaptif 4-10 dtk) dan **Olahraga** (interval 1.5 dtk, kalori MET dinamis, pace).
  - Filter Kalman ringan + pembersihan lonjakan/teleportasi.
  - Deteksi gap sinyal > 30 detik & auto-pause saat diam (< 0.5 m/s).
  - Foreground Service dengan sticky notification, tombol kontrol Jeda & Berhenti (tahan/long-press).
- **Peta Leaflet Offline Bundle:**
  - Leaflet 1.9.4 dibundle lokal di `assets/map/` (tanpa ketergantungan CDN).
  - 2 Gaya: **Real Life** (citra satelit Esri) dan **Putih Polos** (grid minimalis tanpa tile untuk hemat kuota dan baterai).
  - Marker Start, marker lokasi sekarang dengan lingkaran akurasi dan panah arah (heading).
- **Pulang ke Start:**
  - Panah penunjuk arah garis lurus dari posisi sekarang ke titik start.
  - Perhitungan jarak garis lurus dan estimasi waktu jalan kaki.
  - Sorot rute awal di peta untuk kembali.
- **Kompas Realtime:**
  - Sensor `TYPE_ROTATION_VECTOR` dengan fallback accelerometer + magnetometer.
  - Low-pass filter (30 fps), derajat & huruf arah mata angin, koreksi utara sejati (deklinasi).
  - Animasi kalibrasi angka 8 saat akurasi sensor rendah.
- **Riwayat, Replay & Ekspor GPX:**
  - Riwayat dengan kartu pratinjau rute berbasis Canvas (sangat ringan).
  - Replay rute interaktif dengan slider.
  - Ekspor GPX 1.1 standar via FileProvider.
- **Autentikasi & Akun Demo:**
  - Mode Tamu, Akun Demo (lokal, batas 5 trip, UID acak), dan Member (Google / Form Username).
  - Migrasi otomatis jejak lokal demo ke akun cloud saat upgrade ke member.
  - Rate limiting eksponensial pada kegagalan login.
- **Design System Cartoon & Animasi Bintang:**
  - Maskot *Jejak* digambar dengan vector/Compose Canvas dengan 4 state: IDLE, WALKING, CELEBRATING, SLEEPY.
  - Latar langit malam *StarSky* dengan paralaks 3 layer dan bintang jatuh.
  - 1 km = 1 Bintang Perjalanan di koleksi user.
- **Optimalisasi POCO F4 & HP Kentang:**
  - Mode Hemat otomatis saat Battery Saver aktif.
  - RAM saat tracking < 200 MB, cold start cepat, target AAB < 25 MB.

---

## Prasyarat Lingkungan

- Android SDK (compileSdk 35, minSdk 26).
- Java 17 (OpenJDK / Temurin).
- Gradle 8.10.2 (sudah termasuk wrapper `./gradlew.bat`).
- Perangkat pengujian: POCO F4 atau perangkat Android lainnya dengan USB debugging aktif.

---

## Cara Build dan Menjalankan

### 1. Build Debug APK
```bash
.\gradlew.bat assembleDebug
```
Output APK berada di `app/build/outputs/apk/debug/app-debug.apk`.

### 2. Jalankan Unit Test
```bash
.\gradlew.bat testDebugUnitTest
```

### 3. Install ke Perangkat via ADB
Pastikan USB Debugging aktif di POCO F4 Anda:
```bash
adb devices
.\gradlew.bat installDebug
```

### 4. Build Release Android App Bundle (AAB)
```bash
.\gradlew.bat bundleRelease
```
Output AAB siap upload ke Google Play berada di `app/build/outputs/bundle/release/app-release.aab`.

---

## Struktur Proyek

```
NaviraProject/
├── app/
│   ├── src/main/
│   │   ├── java/com/navira/app/
│   │   │   ├── data/
│   │   │   │   ├── export/      # GPX 1.1 Exporter
│   │   │   │   ├── local/       # Room Database, DAOs, Entities, DataStore
│   │   │   │   ├── location/    # Kalman Filter, TrackingService, RDP Simplifier, Calories
│   │   │   │   └── sensor/      # Compass Sensor Manager
│   │   │   ├── di/              # Hilt DI Modules
│   │   │   ├── ui/
│   │   │   │   ├── components/  # CartoonCard, CartoonButton, MascotJejak, StarSky, CanvasRouteThumbnail
│   │   │   │   ├── navigation/  # NavRoute, BottomTab, NaviraNavGraph
│   │   │   │   ├── screens/     # Dashboard, TrackingLive, ReturnToStart, Compass, History, Detail, Summary, Auth, Profile
│   │   │   │   └── theme/       # Color tokens, Typography, Shapes, NaviraTheme
│   │   │   └── NaviraApp.kt
│   │   ├── assets/map/          # Leaflet.js, leaflet.css, map.html (Offline Bundle)
│   │   └── res/                 # Vector drawables, strings (Indonesian), colors, themes
│   └── build.gradle.kts
├── docs/
│   ├── PRIVACY_POLICY.md
│   ├── TERMS.md
│   └── PLAY_STORE_CHECKLIST.md
├── firestore.rules
├── SETUP_FIREBASE.md
├── DECISIONS.md
└── README.md
```
