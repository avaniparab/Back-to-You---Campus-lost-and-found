-- ============================================================================
-- Back to You — Campus Lost & Found System
-- Database Initialization Script (Phase 3)
-- Target RDBMS: MySQL 8.0+ / MariaDB
-- ============================================================================

-- 1. Database Creation
DROP DATABASE IF EXISTS back_to_you;
CREATE DATABASE back_to_you 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE back_to_you;

-- ============================================================================
-- 2. Table: users
-- Description: Stores student, faculty, staff, and admin accounts.
-- ============================================================================
CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    role ENUM('STUDENT', 'ADMIN') NOT NULL DEFAULT 'STUDENT',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 3. Table: items
-- Description: Stores lost and found property reports submitted by campus users.
-- ============================================================================
CREATE TABLE items (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(150) NOT NULL,
    date DATE NOT NULL,
    type ENUM('LOST', 'FOUND') NOT NULL,
    image VARCHAR(255) DEFAULT NULL,
    status ENUM('ACTIVE', 'RESOLVED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_users
        FOREIGN KEY (user_id) 
        REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- 4. Clean Database Initialization (Schema Only)
-- ============================================================================
-- Tables and foreign key constraints created above.

