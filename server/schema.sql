-- ============================================================
-- Skema Database MySQL untuk Aplikasi Zevira (Travel Tracker)
-- Siap dieksekusi di phpMyAdmin, MySQL Workbench, atau VPS CLI
-- ============================================================

CREATE DATABASE IF NOT EXISTS `zevira_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `zevira_db`;

-- 1. Tabel Users (Data Akun Teman & Pengguna)
CREATE TABLE IF NOT EXISTS `users` (
    `id` VARCHAR(64) NOT NULL PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `display_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(120) NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `weight_kg` FLOAT DEFAULT 60.0,
    `total_stars` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tabel Trips (Riwayat Perjalanan / Petualangan)
CREATE TABLE IF NOT EXISTS `trips` (
    `id` VARCHAR(64) NOT NULL PRIMARY KEY,
    `user_id` VARCHAR(64) NOT NULL,
    `title` VARCHAR(150) NOT NULL,
    `mode` VARCHAR(20) NOT NULL DEFAULT 'WALK',
    `sub_activity` VARCHAR(20) NOT NULL DEFAULT 'WALK',
    `start_time` BIGINT NOT NULL,
    `end_time` BIGINT NULL,
    `distance_meters` DOUBLE DEFAULT 0.0,
    `duration_seconds` BIGINT DEFAULT 0,
    `moving_duration_seconds` BIGINT DEFAULT 0,
    `calories` DOUBLE DEFAULT 0.0,
    `avg_speed_kmh` DOUBLE DEFAULT 0.0,
    `max_speed_kmh` DOUBLE DEFAULT 0.0,
    `start_lat` DOUBLE NULL,
    `start_lng` DOUBLE NULL,
    `end_lat` DOUBLE NULL,
    `end_lng` DOUBLE NULL,
    `encoded_polyline` MEDIUMTEXT NULL,
    `is_completed` TINYINT(1) DEFAULT 1,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_trips` (`user_id`, `start_time` DESC),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tabel Trip Points (Detail Koordinat GPS Realtime)
CREATE TABLE IF NOT EXISTS `trip_points` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `trip_id` VARCHAR(64) NOT NULL,
    `latitude` DOUBLE NOT NULL,
    `longitude` DOUBLE NOT NULL,
    `altitude` DOUBLE DEFAULT 0.0,
    `accuracy` FLOAT DEFAULT 0.0,
    `speed` FLOAT DEFAULT 0.0,
    `bearing` FLOAT DEFAULT 0.0,
    `timestamp` BIGINT NOT NULL,
    INDEX `idx_trip_points` (`trip_id`, `timestamp` ASC),
    FOREIGN KEY (`trip_id`) REFERENCES `trips` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
