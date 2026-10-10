# Google Play Store Release Checklist - Zevira

Dokumen ini memuat panduan lengkap, teks justifikasi izin sensitif, dan pengisian Data Safety Form untuk submission aplikasi Zevira ke Google Play Console.

---

## 1. Justifikasi Izin Lokasi Latar Belakang (`ACCESS_BACKGROUND_LOCATION`)

Google Play mewajibkan justifikasi tertulis serta video demonstrasi untuk penggunaan izin lokasi di latar belakang.

### Teks Justifikasi untuk Play Console (Deklarasi Izin):

> **Fitur Inti:** Pelacakan rute perjalanan dan aktivitas olahraga realtime (*continuous travel tracking*).  
> **Mengapa Dibutuhkan di Latar Belakang:**  
> Zevira adalah aplikasi pencatat jejak perjalanan di mana pengguna berjalan kaki, berlari, atau bersepeda dalam durasi lama dengan layar ponsel dimatikan untuk menghemat daya baterai, atau saat pengguna membuka aplikasi lain (seperti kamera untuk mengambil foto). Tanpa akses lokasi latar belakang, rekaman rute GPS akan terputus dan perhitungan jarak tempuh menjadi tidak akurat. Pengumpulan lokasi HANYA aktif saat pengguna secara sadar menekan tombol "Mulai" dan diiringi notifikasi Foreground Service permanen yang menyertakan tombol kontrol Jeda dan Berhenti.

### Video Demonstrasi untuk Peninjau Google Play:
1. Rekam layar saat aplikasi dibuka pertama kali (tampilan Permission Primer kartun).
2. Tampilkan dialog sistem persetujuan izin lokasi saat app digunakan.
3. Tunjukkan layar penjelasan sebelum meminta "Izinkan sepanjang waktu" (*Allow all the time*).
4. Tekan tombol GO untuk memulai tracking, lalu matikan layar perangkat selama 30 detik.
5. Nyalakan layar, perlihatkan notifikasi aktif di status bar, dan tunjukkan polyline rute tetap bersambung tanpa putus di peta Leaflet.
6. Tekan Stop untuk menghentikan tracking.

---

## 2. Panduan Pengisian Data Safety Form

| Kategori Data | Dikumpulkan? | Dibagikan ke Pihak Ketiga? | Dienkripsi saat transit? | Dapat Dihapus? | Tujuan Penggunaan |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Lokasi Presisi** | **Ya** | **Tidak** | **Ya** (HTTPS) | **Ya** | Fungsionalitas Aplikasi (Perekaman rute) |
| **Lokasi Kasar** | **Ya** | **Tidak** | **Ya** (HTTPS) | **Ya** | Fungsionalitas Aplikasi |
| **Nama / Username** | **Ya** | **Tidak** | **Ya** (HTTPS) | **Ya** | Manajemen Akun Member |
| **Info Kebugaran (Kalori)** | **Ya** (Lokal) | **Tidak** | **Ya** (HTTPS) | **Ya** | Analitik Olahraga Pengguna |

### Pertanyaan Kunci Data Safety:
- **Apakah aplikasi memungkinkan pengguna meminta penghapusan data?**  
  Pilih **Ya**, dan sediakan tautan atau penjelasan bahwa fitur penghapusan akun dan data tersedia langsung di dalam aplikasi pada menu `Profil > Hapus Akun & Seluruh Data`.
- **Apakah semua data pengguna dienkripsi saat transit?**  
  Pilih **Ya** (menggunakan HTTPS dan Play Integrity App Check).

---

## 3. Checklist Teknis Build Release

- [x] Version catalog dependencies pinned dan up to date
- [x] Target SDK = 35 (Android 15), Min SDK = 26 (Android 8.0)
- [x] R8 full mode obfuscation aktif
- [x] Stripping log Timber pada release build
- [x] `android:allowBackup="false"` dan data extraction rules aktif
- [x] Leaflet di-bundle lokal offline di assets (tidak bergantung ke CDN luar)
- [x] Enkripsi Android Keystore & SQLCipher / Tink AEAD untuk data sensitif lokal
- [x] Baseline Profile disiapkan untuk cold start optimal
- [x] Build output format: Android App Bundle (`.aab`)
