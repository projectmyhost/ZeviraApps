# Panduan Menjalankan & Deploy Backend MySQL Zevira

Backend ini dibuat dengan **Node.js + Express + MySQL** untuk menghubungkan aplikasi Android Zevira dengan database MySQL di cloud agar data perjalanan Anda dan teman-teman tersinkronisasi secara realtime dan siap dipublikasikan ke publik.

---

## 1. Persiapan Database MySQL
1. Buka MySQL Anda (via **phpMyAdmin**, **MySQL Workbench**, atau terminal).
2. Buat database dan tabel dengan mengimpor file `schema.sql`:
   ```bash
   mysql -u root -p < schema.sql
   ```
   *(Atau buka phpMyAdmin > Buat database `zevira_db` > Import file `schema.sql`).*

---

## 2. Menjalankan di Lokal / Komputer Anda
1. Buka folder `server` di terminal:
   ```bash
   cd c:\NaviraProject\server
   ```
2. Salin `.env.example` menjadi `.env`:
   ```bash
   copy .env.example .env
   ```
3. Sesuaikan konfigurasi database Anda di file `.env`:
   ```ini
   PORT=3000
   DB_HOST=localhost
   DB_USER=root
   DB_PASSWORD=password_mysql_kamu
   DB_NAME=zevira_db
   DB_PORT=3306
   ```
4. Install dependensi dan jalankan:
   ```bash
   npm install
   npm start
   ```

---

## 3. Cara Deploy Online Gratis untuk Teman-teman (Agar Bisa Diakses dari Luar)

### Opsi A: Menggunakan Railway.app (Sangat Direkomendasikan - 1 Klik Selesai)
1. Buka [Railway.app](https://railway.app/).
2. Buat project baru dan pilih **Provision MySQL**.
3. Buka tab **Data**, klik **Import** lalu masukkan isi file `schema.sql`.
4. Deploy folder `server` ini ke Railway (atau sambungkan via GitHub).
5. Railway akan memberikan URL publik seperti:
   `https://zevira-api-production.up.railway.app`
6. Masukkan URL tersebut ke aplikasi Android Zevira.

### Opsi B: Menggunakan VPS Ubuntu (DigitalOcean, Linode, AWS, dsb.)
1. Install Node.js dan MySQL di VPS:
   ```bash
   sudo apt update
   sudo apt install -y nodejs npm mysql-server
   ```
2. Upload folder `server` ke VPS.
3. Jalankan dengan **PM2** agar jalan 24 jam nonstop di background:
   ```bash
   npm install -g pm2
   pm2 start server.js --name "zevira-api"
   pm2 save
   ```

---

## 4. Endpoint REST API yang Tersedia

| Method | Endpoint | Deskripsi |
|---|---|---|
| `GET` | `/api/health` | Cek status server & koneksi database MySQL |
| `POST` | `/api/auth/register` | Mendaftarkan akun teman baru ke database |
| `POST` | `/api/auth/login` | Masuk ke akun dan mendapatkan token JWT |
| `POST` | `/api/trips/sync` | Mengunggah jejak perjalanan & titik GPS realtime |
| `GET` | `/api/trips` | Mengambil linimasa perjalanan dari semua teman |
| `GET` | `/api/leaderboard` | Peringkat bintang dan jarak tempuh antar teman |
