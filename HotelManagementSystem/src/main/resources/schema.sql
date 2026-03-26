-- ============================================================
--  Hotel Management System — Database Schema
--  MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS hotel_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_db;

-- ─── Clients ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS clients (
    id          INT          AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) UNIQUE,
    phone       VARCHAR(20)  NOT NULL,
    address     VARCHAR(255),
    id_proof    VARCHAR(100),          -- passport / national ID number
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ─── Employees ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS employees (
    id          INT          AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) UNIQUE,
    phone       VARCHAR(20),
    role        VARCHAR(60)  NOT NULL,  -- e.g. Receptionist, Manager, Housekeeping
    salary      DECIMAL(10,2),
    hire_date   DATE,
    username    VARCHAR(60)  UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,  -- BCrypt hash
    is_active   BOOLEAN      DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ─── Room Types ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS room_types (
    id          INT          AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(60)  NOT NULL UNIQUE,   -- Standard, Deluxe
    price_per_night DECIMAL(10,2) NOT NULL,
    description TEXT
);

-- ─── Rooms ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rooms (
    id          INT          AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10)  NOT NULL UNIQUE,
    floor       INT,
    room_type_id INT         NOT NULL,
    status      ENUM('AVAILABLE','OCCUPIED','MAINTENANCE') DEFAULT 'AVAILABLE',
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- ─── Reservations ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS reservations (
    id              INT          AUTO_INCREMENT PRIMARY KEY,
    client_id       INT          NOT NULL,
    room_id         INT          NOT NULL,
    employee_id     INT,                        -- staff who booked
    check_in_date   DATE         NOT NULL,
    check_out_date  DATE         NOT NULL,
    status          ENUM('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED') DEFAULT 'CONFIRMED',
    special_requests TEXT,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id)   REFERENCES clients(id),
    FOREIGN KEY (room_id)     REFERENCES rooms(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ─── Billing ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS billing (
    id              INT          AUTO_INCREMENT PRIMARY KEY,
    reservation_id  INT          NOT NULL UNIQUE,
    nights          INT          NOT NULL,
    room_charge     DECIMAL(10,2) NOT NULL,
    extra_charges   DECIMAL(10,2) DEFAULT 0.00,
    discount        DECIMAL(10,2) DEFAULT 0.00,
    total_amount    DECIMAL(10,2) NOT NULL,
    payment_method  ENUM('CASH','CARD','UPI','ONLINE') DEFAULT 'CASH',
    paid_at         TIMESTAMP    NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- ─── Seed data ──────────────────────────────────────────────
INSERT IGNORE INTO room_types (name, price_per_night, description) VALUES
    ('Standard', 1500.00, 'Comfortable room with basic amenities'),
    ('Deluxe',   3000.00, 'Spacious room with premium furnishings and city view');

-- Default admin user  (password: admin123  — stored as plain text for demo; use BCrypt in production)
INSERT IGNORE INTO employees (name, email, phone, role, salary, hire_date, username, password, is_active)
VALUES ('Demo Admin', 'demo@hotel.com', '9999999999', 'Manager', 50000.00, CURDATE(), 'demo_admin', 'demo123', TRUE);

-- Sample rooms
INSERT IGNORE INTO rooms (room_number, floor, room_type_id, status) VALUES
    ('101', 1, 1, 'AVAILABLE'), ('102', 1, 1, 'AVAILABLE'), ('103', 1, 1, 'AVAILABLE'),
    ('201', 2, 2, 'AVAILABLE'), ('202', 2, 2, 'AVAILABLE'), ('203', 2, 2, 'AVAILABLE'),
    ('301', 3, 1, 'AVAILABLE'), ('302', 3, 1, 'AVAILABLE'),
    ('401', 4, 2, 'AVAILABLE'), ('402', 4, 2, 'AVAILABLE');
