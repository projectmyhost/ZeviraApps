const express = require('express');
const cors = require('cors');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'zevira_secret_key_2026_travel_tracker';

app.use(cors());
app.use(express.json({ limit: '10mb' }));

// MySQL Connection Pool
let pool = null;

function getPool() {
    if (!pool) {
        pool = mysql.createPool({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'root',
            password: process.env.DB_PASSWORD || '',
            database: process.env.DB_NAME || 'zevira_db',
            port: parseInt(process.env.DB_PORT || '3306', 10),
            waitForConnections: true,
            connectionLimit: 10,
            queueLimit: 0
        });
    }
    return pool;
}

// Middleware: Authenticate JWT
async function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ success: false, message: 'Token otentikasi tidak ditemukan' });
    }

    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.user = decoded;
        next();
    } catch (err) {
        return res.status(403).json({ success: false, message: 'Token tidak valid atau kedaluwarsa' });
    }
}

// 1. Health Check
app.get('/api/health', async (req, res) => {
    try {
        const p = getPool();
        await p.query('SELECT 1');
        res.json({ status: 'ok', database: 'connected', service: 'Zevira API v1.0.0' });
    } catch (err) {
        res.json({ status: 'warning', database: 'disconnected', error: err.message, service: 'Zevira API v1.0.0' });
    }
});

// 2. Auth: Register
app.post('/api/auth/register', async (req, res) => {
    try {
        const { username, display_name, email, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({ success: false, message: 'Username dan password wajib diisi' });
        }

        const cleanUsername = username.toLowerCase().trim();
        const p = getPool();

        // Check if username already exists
        const [existing] = await p.query('SELECT id FROM users WHERE username = ?', [cleanUsername]);
        if (existing.length > 0) {
            return res.status(400).json({ success: false, message: 'Username sudah digunakan, silakan pilih yang lain' });
        }

        const salt = await bcrypt.genSalt(10);
        const hash = await bcrypt.hash(password, salt);
        const userId = 'usr_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
        const displayName = display_name ? display_name.trim() : cleanUsername;

        await p.query(
            'INSERT INTO users (id, username, display_name, email, password_hash) VALUES (?, ?, ?, ?, ?)',
            [userId, cleanUsername, displayName, email || null, hash]
        );

        const token = jwt.sign({ id: userId, username: cleanUsername }, JWT_SECRET, { expiresIn: '90d' });

        res.json({
            success: true,
            message: 'Registrasi berhasil',
            token,
            user: {
                id: userId,
                username: cleanUsername,
                display_name: displayName,
                total_stars: 0
            }
        });
    } catch (err) {
        console.error('Register error:', err);
        res.status(500).json({ success: false, message: 'Gagal mendaftar: ' + err.message });
    }
});

// 3. Auth: Login
app.post('/api/auth/login', async (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({ success: false, message: 'Username dan password wajib diisi' });
        }

        const cleanUsername = username.toLowerCase().trim();
        const p = getPool();

        const [rows] = await p.query('SELECT * FROM users WHERE username = ?', [cleanUsername]);
        if (rows.length === 0) {
            return res.status(401).json({ success: false, message: 'Username atau kata sandi salah' });
        }

        const user = rows[0];
        const isMatch = await bcrypt.compare(password, user.password_hash);
        if (!isMatch) {
            return res.status(401).json({ success: false, message: 'Username atau kata sandi salah' });
        }

        const token = jwt.sign({ id: user.id, username: user.username }, JWT_SECRET, { expiresIn: '90d' });

        res.json({
            success: true,
            message: 'Login berhasil',
            token,
            user: {
                id: user.id,
                username: user.username,
                display_name: user.display_name,
                weight_kg: user.weight_kg,
                total_stars: user.total_stars
            }
        });
    } catch (err) {
        console.error('Login error:', err);
        res.status(500).json({ success: false, message: 'Gagal masuk: ' + err.message });
    }
});

// 4. Trips: Sync/Upload Trip
app.post('/api/trips/sync', authenticateToken, async (req, res) => {
    try {
        const { trip, points } = req.body;
        if (!trip || !trip.id) {
            return res.status(400).json({ success: false, message: 'Data trip tidak valid' });
        }

        const p = getPool();
        const userId = req.user.id;

        await p.query(
            `INSERT INTO trips (
                id, user_id, title, mode, sub_activity, start_time, end_time,
                distance_meters, duration_seconds, moving_duration_seconds,
                calories, avg_speed_kmh, max_speed_kmh, start_lat, start_lng,
                end_lat, end_lng, encoded_polyline, is_completed
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                title = VALUES(title),
                distance_meters = VALUES(distance_meters),
                duration_seconds = VALUES(duration_seconds),
                calories = VALUES(calories),
                encoded_polyline = VALUES(encoded_polyline)`,
            [
                trip.id,
                userId,
                trip.title || 'Petualangan Zevira',
                trip.mode || 'WALK',
                trip.sub_activity || 'WALK',
                trip.start_time || Date.now(),
                trip.end_time || Date.now(),
                trip.distance_meters || 0.0,
                trip.duration_seconds || 0,
                trip.moving_duration_seconds || 0,
                trip.calories || 0.0,
                trip.avg_speed_kmh || 0.0,
                trip.max_speed_kmh || 0.0,
                trip.start_lat || null,
                trip.start_lng || null,
                trip.end_lat || null,
                trip.end_lng || null,
                trip.encoded_polyline || null,
                1
            ]
        );

        // Insert points if available
        if (Array.isArray(points) && points.length > 0) {
            const values = points.map(pt => [
                trip.id,
                pt.latitude,
                pt.longitude,
                pt.altitude || 0.0,
                pt.accuracy || 0.0,
                pt.speed || 0.0,
                pt.bearing || 0.0,
                pt.timestamp || Date.now()
            ]);

            await p.query(
                `INSERT INTO trip_points (trip_id, latitude, longitude, altitude, accuracy, speed, bearing, timestamp)
                 VALUES ?`,
                [values]
            );
        }

        // Add stars to user
        const starsEarned = Math.max(1, Math.floor((trip.distance_meters || 0) / 1000));
        await p.query('UPDATE users SET total_stars = total_stars + ? WHERE id = ?', [starsEarned, userId]);

        res.json({ success: true, message: 'Trip berhasil disinkronkan ke MySQL', starsEarned });
    } catch (err) {
        console.error('Sync trip error:', err);
        res.status(500).json({ success: false, message: 'Gagal sinkronisasi trip: ' + err.message });
    }
});

// 5. Trips: Get All Friends Trips (Feed / Timeline)
app.get('/api/trips', authenticateToken, async (req, res) => {
    try {
        const p = getPool();
        const [rows] = await p.query(
            `SELECT t.*, u.username, u.display_name
             FROM trips t
             JOIN users u ON t.user_id = u.id
             ORDER BY t.start_time DESC
             LIMIT 50`
        );
        res.json({ success: true, trips: rows });
    } catch (err) {
        console.error('Fetch trips error:', err);
        res.status(500).json({ success: false, message: 'Gagal mengambil data trip: ' + err.message });
    }
});

// 6. Leaderboard: Peringkat Bintang dan Jarak Antar Teman
app.get('/api/leaderboard', async (req, res) => {
    try {
        const p = getPool();
        const [rows] = await p.query(
            `SELECT id, username, display_name, total_stars,
                (SELECT COALESCE(SUM(distance_meters), 0) FROM trips WHERE user_id = users.id) as total_distance_meters
             FROM users
             ORDER BY total_stars DESC, total_distance_meters DESC
             LIMIT 20`
        );
        res.json({ success: true, leaderboard: rows });
    } catch (err) {
        console.error('Leaderboard error:', err);
        res.status(500).json({ success: false, message: 'Gagal mengambil leaderboard: ' + err.message });
    }
});

app.listen(PORT, () => {
    console.log(`🚀 Zevira MySQL Backend REST API berjalan pada port ${PORT}`);
});
