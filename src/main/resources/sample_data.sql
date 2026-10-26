-- ============================================================================
-- Railway Management System: Realistic Sample Data
--
-- Note: Pre-hashed passwords use standard BCrypt (cost factor 12):
--   'Admin@123' -> $2a$12$K1d0wQYV4f7eFp5tG3yRMeA3kY6qV8mO9uXz7W1sP4nE2tB6yU8wG
--   'Rahul@123' -> $2a$12$Z0H7u5aCskZtz7QvWvWcguB53PjP23b/d8o2iFmZlC1o6rC/4f0mS
-- ============================================================================

-- 1. Initial Users (1 Administrator, 1 Passenger)
INSERT INTO users (username, password_hash, full_name, email, phone_number, role)
VALUES 
  ('admin', '$2a$12$JZMbJWlkXvMjd29jrFmOuu/w/p/1Xpsd3TK4tEZusCAz2IstyfHwO', 'System Administrator', 'admin@railway.gov.in', '9876543210', 'ADMIN'),
  ('rahul', '$2a$12$s/RbEmpMfG8MdmKOh/UQv.POLKZ96jwPkZPWPrbIPzBXAh1op5DOu', 'Rahul Sharma', 'rahul.sharma@example.com', '9123456780', 'PASSENGER')
ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- 2. Master Stations
INSERT INTO stations (station_code, station_name, city, state)
VALUES 
  ('NDLS', 'New Delhi Railway Station', 'New Delhi', 'Delhi'),
  ('GWL',  'Gwalior Junction', 'Gwalior', 'Madhya Pradesh'),
  ('BPL',  'Bhopal Junction', 'Bhopal', 'Madhya Pradesh'),
  ('CNB',  'Kanpur Central', 'Kanpur', 'Uttar Pradesh'),
  ('HWH',  'Howrah Junction', 'Kolkata', 'West Bengal'),
  ('MMCT', 'Mumbai Central', 'Mumbai', 'Maharashtra')
ON CONFLICT (station_code) DO NOTHING;

-- 3. Trains Directory
-- 12952: New Delhi to Mumbai Central (Rajdhani)
-- 12002: New Delhi to Bhopal (Shatabdi)
INSERT INTO trains (train_number, train_name, train_type, source_station_id, dest_station_id, base_fare_per_km, active)
SELECT '12952', 'Mumbai Rajdhani Express', 'RAJDHANI', s1.id, s2.id, 1.80, TRUE
FROM stations s1, stations s2
WHERE s1.station_code = 'NDLS' AND s2.station_code = 'MMCT'
ON CONFLICT (train_number) DO NOTHING;

INSERT INTO trains (train_number, train_name, train_type, source_station_id, dest_station_id, base_fare_per_km, active)
SELECT '12002', 'Bhopal Shatabdi Express', 'SHATABDI', s1.id, s2.id, 1.50, TRUE
FROM stations s1, stations s2
WHERE s1.station_code = 'NDLS' AND s2.station_code = 'BPL'
ON CONFLICT (train_number) DO NOTHING;

-- 4. Route Stops for 12952 (NDLS -> MMCT)
INSERT INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform)
SELECT t.id, s.id, 1, '16:55:00', '16:55:00', 0, 0, 1
FROM trains t, stations s WHERE t.train_number = '12952' AND s.station_code = 'NDLS'
ON CONFLICT DO NOTHING;

INSERT INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform)
SELECT t.id, s.id, 2, '08:35:00', '08:35:00', 0, 1384, 5
FROM trains t, stations s WHERE t.train_number = '12952' AND s.station_code = 'MMCT'
ON CONFLICT DO NOTHING;

-- Route Stops for 12002 (NDLS -> GWL -> BPL)
INSERT INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform)
SELECT t.id, s.id, 1, '06:00:00', '06:00:00', 0, 0, 1
FROM trains t, stations s WHERE t.train_number = '12002' AND s.station_code = 'NDLS'
ON CONFLICT DO NOTHING;

INSERT INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform)
SELECT t.id, s.id, 2, '09:23:00', '09:28:00', 5, 319, 2
FROM trains t, stations s WHERE t.train_number = '12002' AND s.station_code = 'GWL'
ON CONFLICT DO NOTHING;

INSERT INTO route_stops (train_id, station_id, stop_sequence, arrival_time, departure_time, halt_minutes, distance_from_origin_km, scheduled_platform)
SELECT t.id, s.id, 3, '14:40:00', '14:40:00', 0, 707, 1
FROM trains t, stations s WHERE t.train_number = '12002' AND s.station_code = 'BPL'
ON CONFLICT DO NOTHING;

-- 5. Physical Coach and Seat Layouts
-- Train 12952: Coach A1 (2A - 12 seats), Coach B1 (3A - 18 seats)
INSERT INTO seats (train_id, travel_class, coach_number, seat_number, berth_type)
SELECT t.id, '2A', 'A1', seat_num, 
       CASE (seat_num % 4)
         WHEN 1 THEN 'LOWER'
         WHEN 2 THEN 'UPPER'
         WHEN 3 THEN 'SIDE_LOWER'
         ELSE 'SIDE_UPPER'
       END
FROM trains t, generate_series(1, 12) AS seat_num
WHERE t.train_number = '12952'
ON CONFLICT DO NOTHING;

INSERT INTO seats (train_id, travel_class, coach_number, seat_number, berth_type)
SELECT t.id, '3A', 'B1', seat_num, 
       CASE (seat_num % 6)
         WHEN 1 THEN 'LOWER'
         WHEN 2 THEN 'MIDDLE'
         WHEN 3 THEN 'UPPER'
         WHEN 4 THEN 'SIDE_LOWER'
         WHEN 5 THEN 'SIDE_UPPER'
         ELSE 'LOWER'
       END
FROM trains t, generate_series(1, 18) AS seat_num
WHERE t.train_number = '12952'
ON CONFLICT DO NOTHING;

-- Train 12002: Coach C1 (CC - 20 seats)
INSERT INTO seats (train_id, travel_class, coach_number, seat_number, berth_type)
SELECT t.id, 'CC', 'C1', seat_num, 
       CASE (seat_num % 3)
         WHEN 1 THEN 'WINDOW'
         WHEN 2 THEN 'AISLE'
         ELSE 'WINDOW'
       END
FROM trains t, generate_series(1, 20) AS seat_num
WHERE t.train_number = '12002'
ON CONFLICT DO NOTHING;

-- 6. Informational Railway Rules
INSERT INTO railway_rules (category, title, content)
VALUES 
  ('Baggage', 'Free Luggage Allowance', 'AC First Class: 70kg, AC 2-Tier: 50kg, AC 3-Tier/Chair Car: 40kg, Sleeper: 40kg. Maximum permissible luggage dimensions are 100cm x 60cm x 25cm.'),
  ('Cancellation', 'Ticket Cancellation & Refund Slabs', 'Cancellations 48+ hours before scheduled departure incur a flat deduction per passenger (1A: Rs.240, 2A: Rs.200, 3A/CC: Rs.180, SL: Rs.120). Cancellations between 12-48 hours incur 25% deduction. Between 4-12 hours incur 50% deduction. Zero refund within 4 hours of departure.'),
  ('General', 'Identification & Journey Guidelines', 'Passengers must carry a valid original Government photo ID (Aadhaar, PAN, Voter ID, Driving License, or Passport) during the journey. Platform tickets are valid for 2 hours from time of issuance.')
ON CONFLICT DO NOTHING;
