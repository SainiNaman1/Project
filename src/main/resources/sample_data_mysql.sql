-- ============================================================================
-- Railway Management System: MySQL 8.x Sample Data
-- ============================================================================

USE railway_db;

-- 1. Initial Users (admin / Admin@123, rahul / Rahul@123)
INSERT INTO users (username, password_hash, full_name, email, phone_number, role)
VALUES 
  ('admin', '$2a$12$JZMbJWlkXvMjd29jrFmOuu/w/p/1Xpsd3TK4tEZusCAz2IstyfHwO', 'System Administrator', 'admin@railway.gov.in', '9876543210', 'ADMIN'),
  ('rahul', '$2a$12$s/RbEmpMfG8MdmKOh/UQv.POLKZ96jwPkZPWPrbIPzBXAh1op5DOu', 'Rahul Sharma', 'rahul.sharma@example.com', '9123456780', 'PASSENGER')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);

-- 2. Master Stations
INSERT IGNORE INTO stations (station_code, station_name, city, state)
VALUES 
  ('NDLS', 'New Delhi Railway Station', 'New Delhi', 'Delhi'),
  ('GWL',  'Gwalior Junction', 'Gwalior', 'Madhya Pradesh'),
  ('BPL',  'Bhopal Junction', 'Bhopal', 'Madhya Pradesh'),
  ('CNB',  'Kanpur Central', 'Kanpur', 'Uttar Pradesh'),
  ('HWH',  'Howrah Junction', 'Kolkata', 'West Bengal'),
  ('MMCT', 'Mumbai Central', 'Mumbai', 'Maharashtra');

-- 3. Trains Directory
INSERT IGNORE INTO trains (id, train_number, train_name, train_type, source_station_id, dest_station_id, base_fare_per_km, active)
VALUES 
  (1, '12952', 'Mumbai Rajdhani Express', 'RAJDHANI', 1, 6, 1.80, TRUE),
  (2, '12002', 'Bhopal Shatabdi Express', 'SHATABDI', 1, 3, 1.50, TRUE);

-- 4. Route Stops for 12952 (NDLS -> MMCT)
INSERT IGNORE INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform)
VALUES 
  (1, 1, 1, '16:55:00', '16:55:00', 0, 0, 1),
  (1, 6, 2, '08:35:00', '08:35:00', 0, 1384, 5);

-- Route Stops for 12002 (NDLS -> GWL -> BPL)
INSERT IGNORE INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform)
VALUES 
  (2, 1, 1, '06:00:00', '06:00:00', 0, 0, 1),
  (2, 2, 2, '09:23:00', '09:28:00', 5, 319, 2),
  (2, 3, 3, '14:40:00', '14:40:00', 0, 707, 1);

-- 5. Physical Coach and Seat Layouts
-- Train 1 (12952): Coach A1 (2A - 12 seats)
INSERT IGNORE INTO seats (train_id, travel_class, coach_number, seat_number, berth_type) VALUES
  (1, '2A', 'A1', 1, 'LOWER'),
  (1, '2A', 'A1', 2, 'UPPER'),
  (1, '2A', 'A1', 3, 'SIDE_LOWER'),
  (1, '2A', 'A1', 4, 'SIDE_UPPER'),
  (1, '2A', 'A1', 5, 'LOWER'),
  (1, '2A', 'A1', 6, 'UPPER'),
  (1, '2A', 'A1', 7, 'SIDE_LOWER'),
  (1, '2A', 'A1', 8, 'SIDE_UPPER'),
  (1, '2A', 'A1', 9, 'LOWER'),
  (1, '2A', 'A1', 10, 'UPPER'),
  (1, '2A', 'A1', 11, 'SIDE_LOWER'),
  (1, '2A', 'A1', 12, 'SIDE_UPPER');

-- Train 1 (12952): Coach B1 (3A - 18 seats)
INSERT IGNORE INTO seats (train_id, travel_class, coach_number, seat_number, berth_type) VALUES
  (1, '3A', 'B1', 1, 'LOWER'),
  (1, '3A', 'B1', 2, 'MIDDLE'),
  (1, '3A', 'B1', 3, 'UPPER'),
  (1, '3A', 'B1', 4, 'SIDE_LOWER'),
  (1, '3A', 'B1', 5, 'SIDE_UPPER'),
  (1, '3A', 'B1', 6, 'LOWER'),
  (1, '3A', 'B1', 7, 'MIDDLE'),
  (1, '3A', 'B1', 8, 'UPPER'),
  (1, '3A', 'B1', 9, 'SIDE_LOWER'),
  (1, '3A', 'B1', 10, 'SIDE_UPPER'),
  (1, '3A', 'B1', 11, 'LOWER'),
  (1, '3A', 'B1', 12, 'MIDDLE'),
  (1, '3A', 'B1', 13, 'UPPER'),
  (1, '3A', 'B1', 14, 'SIDE_LOWER'),
  (1, '3A', 'B1', 15, 'SIDE_UPPER'),
  (1, '3A', 'B1', 16, 'LOWER'),
  (1, '3A', 'B1', 17, 'MIDDLE'),
  (1, '3A', 'B1', 18, 'UPPER');

-- Train 2 (12002): Coach C1 (CC - 20 seats)
INSERT IGNORE INTO seats (train_id, travel_class, coach_number, seat_number, berth_type) VALUES
  (2, 'CC', 'C1', 1, 'WINDOW'),
  (2, 'CC', 'C1', 2, 'AISLE'),
  (2, 'CC', 'C1', 3, 'WINDOW'),
  (2, 'CC', 'C1', 4, 'AISLE'),
  (2, 'CC', 'C1', 5, 'WINDOW'),
  (2, 'CC', 'C1', 6, 'AISLE'),
  (2, 'CC', 'C1', 7, 'WINDOW'),
  (2, 'CC', 'C1', 8, 'AISLE'),
  (2, 'CC', 'C1', 9, 'WINDOW'),
  (2, 'CC', 'C1', 10, 'AISLE'),
  (2, 'CC', 'C1', 11, 'WINDOW'),
  (2, 'CC', 'C1', 12, 'AISLE'),
  (2, 'CC', 'C1', 13, 'WINDOW'),
  (2, 'CC', 'C1', 14, 'AISLE'),
  (2, 'CC', 'C1', 15, 'WINDOW'),
  (2, 'CC', 'C1', 16, 'AISLE'),
  (2, 'CC', 'C1', 17, 'WINDOW'),
  (2, 'CC', 'C1', 18, 'AISLE'),
  (2, 'CC', 'C1', 19, 'WINDOW'),
  (2, 'CC', 'C1', 20, 'AISLE');

-- 6. Informational Railway Rules
INSERT IGNORE INTO railway_rules (category, title, content)
VALUES 
  ('Baggage', 'Free Luggage Allowance', 'AC First Class: 70kg, AC 2-Tier: 50kg, AC 3-Tier/Chair Car: 40kg, Sleeper: 40kg. Maximum permissible luggage dimensions are 100cm x 60cm x 25cm.'),
  ('Cancellation', 'Ticket Cancellation & Refund Slabs', 'Cancellations 48+ hours before scheduled departure incur a flat deduction per passenger (1A: Rs.240, 2A: Rs.200, 3A/CC: Rs.180, SL: Rs.120). Cancellations between 12-48 hours incur 25% deduction. Between 4-12 hours incur 50% deduction. Zero refund within 4 hours of departure.'),
  ('General', 'Identification & Journey Guidelines', 'Passengers must carry a valid original Government photo ID (Aadhaar, PAN, Voter ID, Driving License, or Passport) during the journey. Platform tickets are valid for 2 hours from time of issuance.');
