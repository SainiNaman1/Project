-- ============================================================================
-- Railway Management System (RMS)
-- PostgreSQL 14+ Database Schema Definition (DDL)
--
-- Design Notes:
-- 1. All primary keys use standard ANSI SQL 'BIGINT GENERATED ALWAYS AS IDENTITY'.
-- 2. Passwords store 60-character BCrypt hashes, never plain text.
-- 3. Monetary values (base_fare_per_km, total_fare) use NUMERIC(10,2) to avoid
--    floating-point rounding inaccuracies in financial arithmetic.
-- 4. Active seat allocation uniqueness is enforced via a PostgreSQL Partial Unique
--    Index on (train_id, journey_date, seat_id) WHERE status = 'OCCUPIED'.
-- ============================================================================

-- 1. Users Table
-- Stores credentials and user profiles. Role is restricted to 'PASSENGER' or 'ADMIN'.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    role VARCHAR(20) NOT NULL DEFAULT 'PASSENGER' CHECK (role IN ('PASSENGER', 'ADMIN')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_user_role ON users (role);

-- 2. Stations Table
-- Master directory of railway stations with unique station codes (e.g., NDLS, MMCT).
CREATE TABLE IF NOT EXISTS stations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    station_code VARCHAR(10) NOT NULL UNIQUE,
    station_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_station_code ON stations (station_code);

-- 3. Trains Table
-- Master directory of trains. Operational status can be toggled via 'active'.
CREATE TABLE IF NOT EXISTS trains (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    train_number VARCHAR(10) NOT NULL UNIQUE,
    train_name VARCHAR(100) NOT NULL,
    train_type VARCHAR(30) NOT NULL CHECK (train_type IN ('RAJDHANI', 'SHATABDI', 'MAIL_EXPRESS', 'SUPERFAST')),
    source_station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
    dest_station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
    base_fare_per_km NUMERIC(6,2) NOT NULL DEFAULT 1.20,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_train_number ON trains (train_number);

-- 4. Route Stops Table
-- Ordered intermediate stops for each train route.
-- uk_train_stop_seq ensures sequence numbers 1, 2, 3... are unique per train.
-- uk_train_station prevents duplicate stop entries for the same station on one train.
CREATE TABLE IF NOT EXISTS route_stops (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
    station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
    stop_sequence INT NOT NULL,
    arrival_time TIME NULL,
    departure_time TIME NULL,
    halt_minutes INT NOT NULL DEFAULT 2,
    distance_from_origin_km INT NOT NULL DEFAULT 0,
    scheduled_platform INT NOT NULL DEFAULT 1,
    CONSTRAINT uk_train_stop_seq UNIQUE (train_id, stop_sequence),
    CONSTRAINT uk_train_station UNIQUE (train_id, station_id)
);

CREATE INDEX IF NOT EXISTS idx_route_lookup ON route_stops (train_id, station_id);

-- 5. Physical Seats Layout Table
-- Physical coach and seat blueprint configured for each train.
CREATE TABLE IF NOT EXISTS seats (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
    travel_class VARCHAR(10) NOT NULL CHECK (travel_class IN ('1A', '2A', '3A', 'SL', 'CC')),
    coach_number VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    berth_type VARCHAR(20) NOT NULL CHECK (berth_type IN ('LOWER', 'MIDDLE', 'UPPER', 'SIDE_LOWER', 'SIDE_UPPER', 'WINDOW', 'AISLE')),
    CONSTRAINT uk_train_coach_seat UNIQUE (train_id, coach_number, seat_number)
);

CREATE INDEX IF NOT EXISTS idx_seat_class ON seats (train_id, travel_class);

-- 6. Reservations Table
-- Master booking record identified by a 10-digit unique PNR.
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pnr_number VARCHAR(10) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE RESTRICT,
    journey_date DATE NOT NULL,
    from_station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
    to_station_id BIGINT NOT NULL REFERENCES stations(id) ON DELETE RESTRICT,
    travel_class VARCHAR(10) NOT NULL CHECK (travel_class IN ('1A', '2A', '3A', 'SL', 'CC')),
    total_passengers INT NOT NULL CHECK (total_passengers BETWEEN 1 AND 6),
    total_fare NUMERIC(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    booking_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pnr ON reservations (pnr_number);
CREATE INDEX IF NOT EXISTS idx_user_bookings ON reservations (user_id, booking_timestamp);

-- 7. Reservation Passengers Table
-- Individual passengers traveling under a reservation with specific seat/berth assignments.
CREATE TABLE IF NOT EXISTS reservation_passengers (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    passenger_name VARCHAR(100) NOT NULL,
    age INT NOT NULL CHECK (age BETWEEN 1 AND 120),
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    berth_preference VARCHAR(20) DEFAULT 'NO_PREFERENCE',
    assigned_coach VARCHAR(5) NOT NULL,
    assigned_seat_number INT NOT NULL,
    assigned_berth VARCHAR(20) NOT NULL
);

-- 8. Seat Assignments Table
-- Date-specific seat tracking. When a passenger books a seat, a row is inserted with status 'OCCUPIED'.
-- On cancellation, status transitions to 'RELEASED'.
-- The partial unique index below strictly prohibits two 'OCCUPIED' rows for the same train, date, and seat.
CREATE TABLE IF NOT EXISTS seat_assignments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    train_id BIGINT NOT NULL REFERENCES trains(id) ON DELETE CASCADE,
    journey_date DATE NOT NULL,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    reservation_id BIGINT NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'OCCUPIED' CHECK (status IN ('OCCUPIED', 'RELEASED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_seat_assignment 
ON seat_assignments (train_id, journey_date, seat_id) 
WHERE status = 'OCCUPIED';

CREATE INDEX IF NOT EXISTS idx_seat_assignment_lookup 
ON seat_assignments (train_id, journey_date, seat_id);

-- 9. Railway Rules & Bulletins Table
-- Informational policies (baggage limits, refund slabs, senior citizen rules).
CREATE TABLE IF NOT EXISTS railway_rules (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
