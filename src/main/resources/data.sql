-- =====================================================
-- Initial Data: Permissions, Roles and Associations
-- =====================================================

-- Insert Permissions
INSERT INTO permissions (id, name, description, created_at, updated_at) VALUES
(1, 'USER_READ', 'View user information', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'USER_WRITE', 'Create and update users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'USER_DELETE', 'Delete users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'ROLE_MANAGE', 'Manage roles and permissions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'TEACHER_MANAGE', 'Manage teachers', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'SUBJECT_READ', 'View subjects', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'SUBJECT_WRITE', 'Create and update subjects', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'SUBJECT_DELETE', 'Delete subjects', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'GROUP_READ', 'View groups', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'GROUP_WRITE', 'Create and update groups', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'GROUP_DELETE', 'Delete groups', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'SESSION_READ', 'View sessions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'SESSION_WRITE', 'Create and update sessions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'SESSION_DELETE', 'Delete sessions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'ENROLLMENT_READ', 'View enrollments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'ENROLLMENT_WRITE', 'Manage enrollments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'ATTENDANCE_READ', 'View attendance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'ATTENDANCE_WRITE', 'Register attendance', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'MATERIAL_READ', 'View materials', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'MATERIAL_WRITE', 'Upload and update materials', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'MATERIAL_DELETE', 'Delete materials', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'PAYMENT_READ', 'View payments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'PAYMENT_WRITE', 'Process payments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'ANALYTICS_READ', 'View analytics and reports', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'AUDIT_READ', 'View audit logs', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Roles
INSERT INTO roles (id, type, name, description, created_at, updated_at) VALUES
(1, 'ADMIN', 'Administrator', 'Full system access and management', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'TEACHER', 'Teacher', 'Manage courses, sessions and materials', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'STUDENT', 'Student', 'Access to enrolled courses and materials', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign Permissions to ADMIN Role (all permissions)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15),
(1, 16), (1, 17), (1, 18), (1, 19), (1, 20),
(1, 21), (1, 22), (1, 23), (1, 24), (1, 25);

-- Assign Permissions to TEACHER Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1),  -- USER_READ
(2, 6),  -- SUBJECT_READ
(2, 9),  -- GROUP_READ
(2, 12), -- SESSION_READ
(2, 13), -- SESSION_WRITE
(2, 15), -- ENROLLMENT_READ
(2, 17), -- ATTENDANCE_READ
(2, 18), -- ATTENDANCE_WRITE
(2, 19), -- MATERIAL_READ
(2, 20), -- MATERIAL_WRITE
(2, 21); -- MATERIAL_DELETE

-- Assign Permissions to STUDENT Role
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 6),  -- SUBJECT_READ
(3, 9),  -- GROUP_READ
(3, 12), -- SESSION_READ
(3, 15), -- ENROLLMENT_READ
(3, 16), -- ENROLLMENT_WRITE (self-enrollment)
(3, 17), -- ATTENDANCE_READ (own attendance)
(3, 19), -- MATERIAL_READ
(3, 22); -- PAYMENT_READ (own payments)

-- =====================================================
-- Test Users: Professors and Admin
-- =====================================================
-- IMPORTANT: Password hashes generated with BCrypt (strength 10)
-- CREDENTIALS FOR SWAGGER TESTING:
-- ┌──────────────────────────────┬──────────────┬────────────────┐
-- │ Email                        │ Password     │ Role           │
-- ├──────────────────────────────┼──────────────┼────────────────┤
-- │ admin@acainfo.com            │ Admin123!    │ ADMIN+TEACHER  │
-- │ profesor1@acainfo.com        │ Teacher123!  │ TEACHER        │
-- │ profesor2@acainfo.com        │ Teacher456!  │ TEACHER        │
-- │ profesor3@acainfo.com        │ Teacher789!  │ TEACHER        │
-- └──────────────────────────────┴──────────────┴────────────────┘

INSERT INTO users (id, email, password, first_name, last_name, phone, status, created_at, updated_at) VALUES
(1, 'admin@acainfo.com', '$2a$12$uZPOUn28mX04KOACU4ZXmegy54Ayk6v.y6n5TXMkVnAm3DdGDxxdW', 'Admin', 'Sistema', '+34600000001', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'profesor1@acainfo.com', '$2a$12$dA1RGFXDVJBrDyf6du5Sz.x7KD/pCINFAkn1iOC3vovXNTuHt2IqO', 'María', 'García López', '+34600000002', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'profesor2@acainfo.com', '$2a$12$dA1RGFXDVJBrDyf6du5Sz.x7KD/pCINFAkn1iOC3vovXNTuHt2IqO', 'Carlos', 'Rodríguez Pérez', '+34600000003', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'profesor3@acainfo.com', '$2a$12$dA1RGFXDVJBrDyf6du5Sz.x7KD/pCINFAkn1iOC3vovXNTuHt2IqO', 'Ana', 'Martínez Sánchez', '+34600000004', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign Roles to Test Users
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin@acainfo.com -> ADMIN
(1, 2), -- admin@acainfo.com -> TEACHER (dual role)
(2, 2), -- profesor1@acainfo.com -> TEACHER
(3, 2), -- profesor2@acainfo.com -> TEACHER
(4, 2); -- profesor3@acainfo.com -> TEACHER

-- Reset sequences for H2 (start after initial data)
ALTER TABLE permissions ALTER COLUMN id RESTART WITH 26;
ALTER TABLE roles ALTER COLUMN id RESTART WITH 4;
ALTER TABLE users ALTER COLUMN id RESTART WITH 5;
