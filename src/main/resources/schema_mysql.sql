-- ============================================================================
-- Railway Management System (RMS)
-- MySQL 8.x Database Schema Definition (DDL)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS railway_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE railway_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    role VARCHAR(20) NOT NULL DEFAULT 'PASSENGER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- 2. Stations Table
CREATE TABLE IF NOT EXISTS stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_code VARCHAR(10) NOT NULL UNIQUE,
    station_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    INDEX idx_station_code (station_code)
) ENGINE=InnoDB;

-- 3. Trains Table
CREATE TABLE IF NOT EXISTS trains (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(10) NOT NULL UNIQUE,
    train_name VARCHAR(100) NOT NULL,
    train_type VARCHAR(30) NOT NULL,
    source_station_id BIGINT NOT NULL,
    dest_station_id BIGINT NOT NULL,
    base_fare_per_km DECIMAL(6,2) NOT NULL DEFAULT 1.20,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (source_station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    FOREIGN KEY (dest_station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    INDEX idx_train_number (train_number)
) ENGINE=InnoDB;

-- 4. Route Stops Table
CREATE TABLE IF NOT EXISTS route_stops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    stop_sequence INT NOT NULL,
    arrival_time TIME NULL,
    departure_time TIME NULL,
    halt_minutes INT NOT NULL DEFAULT 2,
    distance_from_origin_km INT NOT NULL DEFAULT 0,
    scheduled_platform INT NOT NULL DEFAULT 1,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_train_stop_seq (train_id, stop_sequence),
    UNIQUE KEY uk_train_station (train_id, station_id),
    INDEX idx_route_lookup (train_id, station_id)
) ENGINE=InnoDB;

-- 5. Physical Seats Layout Table
CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    travel_class VARCHAR(10) NOT NULL,
    coach_number VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    berth_type VARCHAR(20) NOT NULL,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    UNIQUE KEY uk_train_coach_seat (train_id, coach_number, seat_number),
    INDEX idx_seat_class (train_id, travel_class)
) ENGINE=InnoDB;

-- 6. Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pnr_number VARCHAR(10) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    train_id BIGINT NOT NULL,
    journey_date DATE NOT NULL,
    from_station_id BIGINT NOT NULL,
    to_station_id BIGINT NOT NULL,
    travel_class VARCHAR(10) NOT NULL,
    total_passengers INT NOT NULL,
    total_fare DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    booking_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE RESTRICT,
    FOREIGN KEY (from_station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    FOREIGN KEY (to_station_id) REFERENCES stations(id) ON DELETE RESTRICT,
    INDEX idx_pnr (pnr_number),
    INDEX idx_user_bookings (user_id, booking_timestamp)
) ENGINE=InnoDB;

-- 7. Reservation Passengers Table
CREATE TABLE IF NOT EXISTS reservation_passengers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    berth_preference VARCHAR(20) DEFAULT 'NO_PREFERENCE',
    assigned_coach VARCHAR(5) NOT NULL,
    assigned_seat_number INT NOT NULL,
    assigned_berth VARCHAR(20) NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Seat Assignments Table
CREATE TABLE IF NOT EXISTS seat_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id BIGINT NOT NULL,
    journey_date DATE NOT NULL,
    seat_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OCCUPIED',
    FOREIGN KEY (train_id) REFERENCES trains(id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    INDEX idx_date_availability (train_id, journey_date, seat_id, status)
) ENGINE=InnoDB;

-- 9. Railway Rules Table
CREATE TABLE IF NOT EXISTS railway_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;
