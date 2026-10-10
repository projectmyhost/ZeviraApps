# Panduan Konfigurasi Firebase untuk Navira

Aplikasi **Navira** dirancang dengan arsitektur **Offline-First**. Anda dapat langsung mengompilasi dan menjalankan aplikasi dalam mode **Tamu** atau **Demo** di HP/emulator tanpa Firebase terlebih dahulu.

Jika ingin mengaktifkan sinkronisasi cloud, autentikasi Member, dan App Check, ikuti langkah-langkah di bawah ini.

---

## 1. Buat Project di Firebase Console

1. Buka [Firebase Console](https://console.firebase.google.com/).
2. Buat project baru bernama `Navira` (atau nama pilihan Anda).
3. Daftarkan aplikasi Android baru dengan package name:
   ```
   com.navira.app
   ```
4. Tambahkan **SHA-1** dan **SHA-256** dari keystore debug / release Anda:
   - Untuk debug keystore di Windows:
     ```bash
     keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
     ```
5. Unduh file `google-services.json` dan letakkan di dalam folder `app/`:
   ```
   NaviraProject/app/google-services.json
   ```
   *(File ini sudah dimasukkan ke `.gitignore` agar tidak bocor ke publik).*

---

## 2. Aktifkan Fitur Firebase Authentication

Masuk ke menu **Build > Authentication > Sign-in method**:

1. **Anonymous**: Aktifkan (digunakan untuk sesi akun Demo unik per perangkat).
2. **Email/Password**: Aktifkan (digunakan untuk registrasi dan login username internal Navira).
3. **Google**: Aktifkan.
   - Salin **Web Client ID** yang dihasilkan untuk konfigurasi Credential Manager.

---

## 3. Aktifkan Cloud Firestore

1. Masuk ke menu **Build > Firestore Database**.
2. Klik **Create database** dan pilih lokasi terdekat (misal: `asia-southeast2` Jakarta).
3. Deploy aturan keamanan dari file `firestore.rules` di root project:
   ```bash
   firebase deploy --only firestore:rules
   ```

---

## 4. Aktifkan App Check (Play Integrity)

1. Masuk ke menu **Build > App Check**.
2. Daftarkan aplikasi `com.navira.app` dengan provider **Play Integrity**.
3. Enforce App Check untuk Cloud Firestore dan Authentication guna melindungi kuota dan mencegah penyalahgunaan API.

---

## 5. Sinkronisasi & Rebuild Aplikasi

Setelah `app/google-services.json` dipasang:
```bash
.\gradlew.bat assembleDebug
```
Plugin Google Services dan Crashlytics akan otomatis terdeteksi dan diaktifkan oleh build script.
