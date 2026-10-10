# Rekam Keputusan Arsitektur & Desain (DECISIONS.md) - Navira

Dokumen ini mencatat setiap keputusan teknis yang diambil selama implementasi Navira (Travel Tracker Android) untuk memastikan transparansi, pemeliharaan jangka panjang, dan kepatuhan terhadap PRD.

---

### DECISION-001: Version Catalog & Gradle Configuration
- **Konteks:** Diperlukan build system modern, stabil, dan siap Google Play 2024/2025.
- **Keputusan:**
  - `libs.versions.toml` sebagai single source of truth dependensi.
  - Gradle 8.10.2 + Android Gradle Plugin 8.7.2 + Kotlin 2.0.21.
  - `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26` (Android 8.0 Oreo).
  - Kotlin Compose Compiler Plugin (bawaan Kotlin 2.0+).
- **Rasional:** Sesuai standar Google Play terbaru, menghindari fragmentasi versi lib.

### DECISION-002: Offline-First & Demo Mode Architecture
- **Konteks:** User dapat menggunakan aplikasi sebagai Tamu, Demo, atau Member. Akun demo tidak boleh bercampur dengan data produksi cloud.
- **Keputusan:**
  - Room Database menjadi single source of truth lokal.
  - Setiap data di-namespace berdasarkan `userId` (termasuk Anonymous Firebase UID).
  - Jika Firebase google-services.json belum diisi oleh user, aplikasi tetap berjalan sempurna dalam mode lokal/demo fallback tanpa crash.

### DECISION-003: Leaflet.js Offline Bundling & Web Security
- **Konteks:** Peta menggunakan Leaflet.js yang harus offline-first, cepat, dan aman dari kerentanan WebView.
- **Keputusan:**
  - Asset Leaflet 1.9.4 (JS + CSS + marker icons) di-bundle lokal di `assets/map/`.
  - Menggunakan `WebViewAssetLoader` dengan domain aman `https://appassets.androidplatform.net`.
  - `allowFileAccess = false`, `allowContentAccess = false`, CSP ketat.
  - Mode "Putih Polos" merender grid vector tanpa tile network untuk hemat baterai dan kuota.
  - Mode "Real Life" menggunakan satellite tile Esri World Imagery dengan caching HTTP.

### DECISION-004: Tracking Precision & Battery Balance
- **Konteks:** Tracking GPS harus akurat namun tidak membuat HP cepat panas atau boros baterai (POCO F4 / HP kentang).
- **Keputusan:**
  - Filter Kalman ringan 1-dimensi untuk menghaluskan fluktuasi akurasi lat/lon.
  - Deteksi gap sinyal > 30 detik untuk memotong segmen garis lurus semu.
  - Auto-pause waktu bergerak saat kecepatan < 0.5 m/s selama 20 detik.
  - Long-press pada tombol Stop untuk mencegah penghentian tak disengaja.

### DECISION-005: Desain Kartun Gen Z & Maskot Canvas
- **Konteks:** Desain bertema kartun stiker biru-putih dengan maskot "Jejak" dan animasi bintang.
- **Keputusan:**
  - Maskot "Jejak" digambar dengan Canvas Compose murni tanpa ketergantungan library animasi berat pihak ketiga.
  - Sistem token warna biru langit (`#2D7BFF`), kuning bintang (`#FFD84D`), night navy (`#0B1B4D`), border 2dp navy, rounded 16-24dp.
  - StarSky Canvas dengan pembatasan frame rate 30fps dan paralaks hemat daya.
