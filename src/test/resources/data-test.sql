-- ===========================================
-- Test Data for E2E Tests
-- ===========================================
-- This file is executed after schema creation to provide initial test data.
-- Includes roles and an admin user for authentication tests.

-- Insert roles (required for user creation)
INSERT INTO roles (id, type, description) VALUES
(1, 'ADMIN', 'System administrator with full access'),
(2, 'TEACHER', 'Teacher who can manage groups and sessions'),
(3, 'STUDENT', 'Student who can enroll in groups');

-- Insert admin user
-- Password: password (BCrypt encoded - standard test password)
-- Hash $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG corresponds to "password"
INSERT INTO users (id, email, password, first_name, last_name, status, created_at, updated_at) VALUES
(1, 'admin@acainfo.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 'User', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1);

-- Reset sequences to avoid conflicts (H2 specific)
-- After inserting with explicit IDs, we need to update the sequence
ALTER TABLE roles ALTER COLUMN id RESTART WITH 4;
ALTER TABLE users ALTER COLUMN id RESTART WITH 2;
