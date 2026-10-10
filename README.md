# Zevira — Modern Travel & Real-Time Activity Tracker for Android

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2F%20MVVM-FF6F00?style=for-the-badge)]()
[![Backend](https://img.shields.io/badge/Backend-Node.js%20%7C%20Express%20%7C%20MySQL-00758F?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com)
[![License](https://img.shields.io/badge/License-Proprietary-red?style=for-the-badge)]()

**Zevira** adalah aplikasi pelacak aktivitas fisik dan penjelajahan perjalanan (*travel & activity tracker*) berbasis Android berkinerja tinggi yang dibangun dengan arsitektur modern Jetpack Compose. Zevira dirancang untuk mencatat koordinat lokasi secara presisi, memvisualisasikan rute penjelajahan secara *real-time* di atas peta interaktif, menghitung pembakaran kalori berbasis bobot tubuh, serta memastikan pengguna tidak tersesat dengan fitur navigasi *Return to Start*.

Aplikasi ini dilengkapi dengan backend mandiri berbasis **Node.js, Express, dan MySQL**, memungkinkan sinkronisasi data antar-perangkat secara aman dan siap dipublikasikan ke publik.

---

## Arsitektur Sistem

Zevira menerapkan prinsip **Clean Architecture** dan pola desain **MVVM (Model-View-ViewModel)** dengan *Unidirectional Data Flow (UDF)*:

```
┌─────────────────────────────────────────────────────────────┐
│                      UI / Presentation                      │
│   Jetpack Compose · Material 3 · StateFlow · Custom Canvas   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observes UI State
┌──────────────────────────────▼──────────────────────────────┐
│                         ViewModels                          │
│     State Management · Coroutine Scope · Hilt Injection      │
└──────────────┬───────────────────────────────┬──────────────┘
               │                               │
┌──────────────▼──────────────┐ ┌──────────────▼──────────────┐
│      Location & Sensor      │ │       Repository Layer      │
│  · Foreground Service       │ │  · Data Synchronization     │
│  · FusedLocationProvider    │ │  · Network Request Handling │
│  · Kalman Filter + RDP      │ │  · Offline-First Strategy   │
│  · Sensor Fusion (Compass)  │ │                             │
└──────────────┬──────────────┘ └──────────────┬──────────────┘
               │                               │
┌──────────────▼───────────────────────────────▼──────────────┐
│                    Persistence & Network                    │
│   · Room ORM (SQLite) · Preferences DataStore · MySQL API   │
└─────────────────────────────────────────────────────────────┘
```

---

## Fitur Utama

### 1. Mesin Pelacak GPS Presisi Tinggi (Strava-Grade Engine)
- **Multi-Mode Tracking**: Mode *Jalan Santai* (interval adaptif hemat daya) dan mode *Olahraga/Lari* (interval tinggi 1.5 detik dengan kalkulasi pace dinamis).
- **Kalman Filtering & Ramer-Douglas-Peucker (RDP)**: Menghilangkan noise sinyal GPS, lompatan titik koordinat liar (*jitter/teleport*), dan mereduksi ukuran polyline rute tanpa mengurangi bentuk aslinya.
- **Kalkulasi Kalori Real-Time Berbasis Bobot Tubuh**: Estimasi pembakaran kalori dihitung secara proporsional sesuai berat badan ($kg$) yang diatur pada profil pengguna dengan formula standar MET (*Metabolic Equivalent of Task*).
- **Proteksi Sensor GPS Fisik**: Sistem mendeteksi ketersediaan sensor GPS perangkat. Pelacakan dicegah bila sensor lokasi perangkat belum aktif, disertai dialog pengalihan ke pengaturan sistem Android.
- **Background Foreground Service**: Dilengkapi notifikasi persisten (*sticky notification*) dengan kontrol jeda dan penghentian perjalanan tanpa terganggu oleh mekanisme *battery saver* sistem operasi.

### 2. Mesin Peta Interaktif & Live Polyline
- **Offline Leaflet Bundle**: Menggunakan Leaflet 1.9.4 yang dibundle secara lokal di dalam aset aplikasi tanpa ketergantungan CDN eksternal.
- **Dual Map Layer**:
  - *Citra Satelit*: Tampilan fotorealistis medan geografis.
  - *Minimalis Putih*: Tampilan grid ringan untuk efisiensi baterai dan kuota data.
- **Live Polyline Drawing**: Garis rute perjalanan digambar langsung di peta seiring pergerakan pengguna secara *real-time*.

### 3. Navigasi "Pulang ke Titik Awal" (Return to Start)
- Penunjuk arah vektor garis lurus (*azimuth*) dari posisi aktual kembali ke titik keberangkatan.
- Kalkulasi jarak garis lurus dan estimasi waktu tempuh berjalan kaki.
- Penyorotan rute awal di peta untuk memudahkan navigasi saat sinyal internet terbatas.

### 4. Sensor Kompas Orientasi Real-Time
- Menggunakan sensor fusi `TYPE_ROTATION_VECTOR` dengan *fallback* akselerometer dan magnetometer.
- *Low-pass filter* pada 30 FPS untuk pergerakan jarum kompas yang stabil dan responsif.
- Koreksi deklinasi magnetik (*true north calculation*) dan indikator kalibrasi sensor.

### 5. Riwayat, Timeline Log, dan Ekspor GPX
- Kartu riwayat perjalanan dengan thumbnail rute berbasis Compose Canvas yang sangat ringan dalam penggunaan memori.
- Timeline rute interaktif dengan log titik koordinat dan kecepatan.
- Ekspor berkas standar GPX 1.1 melalui Android `FileProvider` untuk diimpor ke Strava, Garmin Connect, atau Google Earth.

### 6. Desain Autentikasi Modern & Keamanan Sesi
- Tampilan autentikasi modern dengan integrasi kartu adaptif, hero header *StarSky*, dan validasi data instan.
- Personalisasi dinamis nama pengguna pada layar beranda.
- Mekanisme dialog konfirmasi keluar akun dengan pembersihan menyeluruh (*clean session wipe*) pada DataStore preferensi.

---

## Backend MySQL & Sinkronisasi Cloud

Zevira menyertakan layanan backend lengkap di direktori `server/` untuk mendukung integrasi multi-pengguna dan penyimpanan terpusat:

- **Teknologi**: Node.js, Express, MySQL 8.0, JSON Web Token (JWT), Bcrypt.
- **Endpoint API**:
  - `POST /api/auth/register` — Pendaftaran akun pengguna baru.
  - `POST /api/auth/login` — Autentikasi pengguna dan penerbitan token akses.
  - `POST /api/trips/sync` — Sinkronisasi perjalanan beserta titik koordinat GPS secara *batch*.
  - `GET /api/trips` — Mengambil riwayat perjalanan pengguna dari server.
  - `GET /api/leaderboard` — Papan peringkat jarak tempuh dan kalori antar-pengguna.

Panduan konfigurasi dan instalasi backend dapat dilihat pada [server/README.md](server/README.md).

---

## Teknologi yang Digunakan

| Kategori | Teknologi |
| :--- | :--- |
| **Bahasa Pemrograman** | Kotlin 2.0.0 |
| **UI Toolkit** | Jetpack Compose, Material Design 3, Accompanist |
| **Dependency Injection** | Dagger Hilt 2.51.1 |
| **Database Lokal** | Room Database (SQLite), AndroidX DataStore Preferences |
| **Asynchronous & Reactive** | Kotlin Coroutines, StateFlow, SharedFlow |
| **Location & Maps** | Google Play Services Location (Fused), Leaflet.js (Offline Bundle) |
| **Arsitektur** | Clean Architecture, MVVM, Repository Pattern |
| **Peralatan Build** | Gradle 8.10.2 (Kotlin DSL), Android Gradle Plugin 8.7.0 |
| **Target Lingkungan** | Min SDK 26 (Android 8.0), Target/Compile SDK 35 (Android 15) |

---

## Panduan Instalasi dan Menjalankan Proyek

### Prasyarat
1. **Java Development Kit (JDK)**: Versi 17 (disarankan Eclipse Temurin atau OpenJDK 17).
2. **Android SDK**: Build Tools 35.0.0.
3. **Android Studio**: Ladybug (2024.2.1) atau versi lebih baru.
4. Perangkat fisik Android dengan fitur *Developer Options* dan *USB Debugging* aktif.

### Langkah Build

1. **Kloning Repositori**:
   ```bash
   git clone https://github.com/projectmyhost/ZeviraApps.git
   cd ZeviraApps
   ```

2. **Kompilasi APK Debug**:
   ```bash
   ./gradlew assembleDebug
   ```
   Berkas APK akan dihasilkan pada:  
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Menjalankan Pengujian Unit**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

4. **Instalasi ke Perangkat Terhubung**:
   ```bash
   ./gradlew installDebug
   ```

5. **Kompilasi Bundle Rilis (AAB)**:
   ```bash
   ./gradlew bundleRelease
   ```
   Berkas Android App Bundle siap distribusi berada di:  
   `app/build/outputs/bundle/release/app-release.aab`

---

## Struktur Direktori

```
Zevira/
├── app/
│   ├── src/main/
│   │   ├── java/com/zevira/app/
│   │   │   ├── data/
│   │   │   │   ├── export/          # Konverter format GPX 1.1
│   │   │   │   ├── local/           # Room Database, DAO, Entity, DataStore
│   │   │   │   ├── location/        # TrackingService, KalmanFilter, Distance Calculator
│   │   │   │   └── sensor/          # Sensor Kompas dan Rotasi Vektor
│   │   │   ├── di/                  # Modul Dagger Hilt (DatabaseModule, ServiceModule)
│   │   │   ├── ui/
│   │   │   │   ├── components/      # Komponen UI kustom (Mascot, Card, CanvasThumbnail)
│   │   │   │   ├── navigation/      # NavHost, BottomBar, Rute Navigasi
│   │   │   │   ├── screens/         # Dashboard, Tracking, Compass, History, Detail, Auth, Profile
│   │   │   │   └── theme/           # Color, Typography, Shape, ZeviraTheme
│   │   │   └── ZeviraApp.kt         # Application class dengan inisialisasi Hilt & Timber
│   │   ├── assets/map/              # Leaflet.js, Leaflet CSS, map.html
│   │   └── res/                     # Asset grafis, warna, ikon vektor, strings
│   └── build.gradle.kts
├── server/                          # REST API Server (Node.js & MySQL)
│   ├── schema.sql                   # Skema basis data terpusat
│   ├── server.js                    # Logika API autentikasi dan sinkronisasi perjalanan
│   └── package.json
├── docs/                            # Dokumentasi teknis & kebijakan privasi
├── build.gradle.kts
└── README.md
```

---

## Hak Cipta & Lisensi

Hak Cipta (c) 2024-2026 Zevira Project. Hak cipta dilindungi undang-undang.  
Kode sumber ini disediakan untuk tujuan peninjauan teknis dan portofolio pengembangan. Dilarang mendistribusikan ulang atau menggunakan bagian dari perangkat lunak ini secara komersial tanpa persetujuan tertulis dari pemilik hak cipta.
