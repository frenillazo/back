-- V2: unificacion de SubjectGroup + Intensive en Course, y limpieza de entidades obsoletas.
-- Diseño: docs/diseno-curso-unificado.md (repo raiz). Decisiones cerradas 10-jul-2026:
--   - Course unico con start_date/end_date; sin tipos de grupo ni cuatrimestres.
--   - capacity nullable: 24 = aula fisica; NULL = virtual/dual ilimitada (sin cupo).
--   - price_per_month informativo (NULL tras migrar; se rellena desde admin).
--   - Muere: intensives, group_requests(+supporters), payments, asistencia y online-requests.
--   - Nace: subject_interest (el "me interesa" minimo), migrando requesters+supporters.
-- Verificado en prod (10-jul-2026): 13 subject_groups, 0 intensives, 0 sessions/enrollments
-- con group_id NULL, 647 sesiones todas REGULAR, 17 group_requests + 17 supporters.

-- ============================================================
-- 1. courses
-- ============================================================
CREATE TABLE courses (
    id              bigserial PRIMARY KEY,
    subject_id      bigint NOT NULL,
    teacher_id      bigint,
    name            varchar(80) NOT NULL,
    status          varchar(20) NOT NULL,
    capacity        integer,
    price_per_month numeric(10,2),
    start_date      date NOT NULL,
    end_date        date NOT NULL,
    created_at      timestamp(6) NOT NULL,
    updated_at      timestamp(6) NOT NULL,
    CONSTRAINT courses_status_check CHECK (status IN ('OPEN','CLOSED','CANCELLED'))
);

-- Los grupos existentes se convierten en cursos conservando su id
-- (asi el backfill de course_id en tablas hijas es un copy de group_id).
INSERT INTO courses (id, subject_id, teacher_id, name, status, capacity,
                     price_per_month, start_date, end_date, created_at, updated_at)
SELECT id, subject_id, teacher_id, name, status, capacity,
       NULL, start_date, end_date, created_at, updated_at
FROM subject_groups;

-- (intensives esta vacia en prod; si hubiera filas fallariamos aqui a proposito
--  para decidir el desplazamiento de ids a mano)

SELECT setval('courses_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM courses), 1));

CREATE INDEX idx_course_subject_id ON courses (subject_id);
CREATE INDEX idx_course_teacher_id ON courses (teacher_id);
CREATE INDEX idx_course_status     ON courses (status);
CREATE INDEX idx_course_dates      ON courses (start_date, end_date);

-- ============================================================
-- 2. course_id en tablas hijas
-- ============================================================
-- enrollments
ALTER TABLE enrollments ADD COLUMN course_id bigint;
UPDATE enrollments SET course_id = group_id;
ALTER TABLE enrollments ALTER COLUMN course_id SET NOT NULL;
ALTER TABLE enrollments ADD CONSTRAINT fk_enrollment_course
    FOREIGN KEY (course_id) REFERENCES courses (id);

CREATE INDEX idx_enrollment_course_id     ON enrollments (course_id);
CREATE INDEX idx_enrollment_course_status ON enrollments (course_id, status);
CREATE INDEX idx_enrollment_waiting_list_course
    ON enrollments (course_id, status, waiting_list_position);
-- unicidad: una sola matricula "viva" por alumno y curso (recreacion del indice
-- parcial uk_enrollment_student_group_active_states, que cae al dropear group_id)
CREATE UNIQUE INDEX uk_enrollment_student_course_active_states
    ON enrollments (student_id, course_id)
    WHERE status IN ('PENDING_APPROVAL','ACTIVE','WAITING_LIST');

-- sessions
ALTER TABLE sessions ADD COLUMN course_id bigint;
UPDATE sessions SET course_id = group_id;
ALTER TABLE sessions ALTER COLUMN course_id SET NOT NULL;
ALTER TABLE sessions ADD CONSTRAINT fk_session_course
    FOREIGN KEY (course_id) REFERENCES courses (id);

CREATE INDEX idx_session_course_id   ON sessions (course_id);
CREATE INDEX idx_session_course_date ON sessions (course_id, date);

-- SCHEDULING desaparece como tipo de sesion (0 filas en prod, todas REGULAR)
ALTER TABLE sessions DROP CONSTRAINT sessions_type_check;
ALTER TABLE sessions ADD CONSTRAINT sessions_type_check
    CHECK (type IN ('REGULAR','EXTRA'));

-- schedules: renombrado directo (group_id era NOT NULL)
ALTER TABLE schedules RENAME COLUMN group_id TO course_id;
ALTER TABLE schedules ADD CONSTRAINT fk_schedule_course
    FOREIGN KEY (course_id) REFERENCES courses (id);
ALTER INDEX idx_schedule_group_id RENAME TO idx_schedule_course_id;

-- ============================================================
-- 3. subject_interest (el "me interesa" minimo)
-- ============================================================
CREATE TABLE subject_interest (
    id         bigserial PRIMARY KEY,
    subject_id bigint NOT NULL REFERENCES subjects (id),
    student_id bigint NOT NULL REFERENCES users (id),
    created_at timestamp(6) NOT NULL,
    CONSTRAINT uk_subject_interest UNIQUE (subject_id, student_id)
);
CREATE INDEX idx_subject_interest_subject ON subject_interest (subject_id);

-- Migracion de datos: requesters y supporters de group_requests (feb-2026)
INSERT INTO subject_interest (subject_id, student_id, created_at)
SELECT gr.subject_id, gr.requester_id, MIN(gr.created_at)
FROM group_requests gr
GROUP BY gr.subject_id, gr.requester_id
ON CONFLICT (subject_id, student_id) DO NOTHING;

INSERT INTO subject_interest (subject_id, student_id, created_at)
SELECT gr.subject_id, s.supporter_id, MIN(gr.created_at)
FROM group_request_supporters s
JOIN group_requests gr ON gr.id = s.group_request_id
WHERE s.supporter_id IS NOT NULL
GROUP BY gr.subject_id, s.supporter_id
ON CONFLICT (subject_id, student_id) DO NOTHING;

-- ============================================================
-- 4. drops: columnas y tablas obsoletas
-- ============================================================
ALTER TABLE enrollments
    DROP COLUMN group_id,
    DROP COLUMN intensive_id,
    DROP COLUMN price_per_hour;

ALTER TABLE sessions
    DROP COLUMN group_id,
    DROP COLUMN intensive_id;

-- asistencia y solicitudes online (sin uso real desde feb-2026)
ALTER TABLE session_reservations
    DROP COLUMN attendance_recorded_at,
    DROP COLUMN attendance_recorded_by_id,
    DROP COLUMN attendance_status,
    DROP COLUMN online_request_processed_at,
    DROP COLUMN online_request_processed_by_id,
    DROP COLUMN online_request_status,
    DROP COLUMN online_requested_at;

DROP TABLE group_request_supporters;
DROP TABLE group_requests;
DROP TABLE payments;
DROP TABLE intensives;
DROP TABLE subject_groups;

-- ============================================================
-- 5. purga de oportunidad: tablas de tokens sin limpieza historica
-- ============================================================
DELETE FROM refresh_tokens WHERE expires_at < now() OR revoked = true;
DELETE FROM email_verification_tokens WHERE expires_at < now() OR used = true;
DELETE FROM password_reset_tokens WHERE expires_at < now() OR used = true;
