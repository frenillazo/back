-- V6: jobs del generador/transcriptor LaTeX con la API de Claude.
-- Diseño: docs/diseno-generador-latex.md (repo raíz), §4. Primer patrón de job con
-- estado del codebase: la request devuelve el id y el front hace polling.
--   - source_material_id: solo TRANSCRIBE (material original). SET NULL si se borra.
--   - result_material_id: material publicado al completar. SET NULL si se borra.
--   - Las capturas de GENERATE NO se persisten (tmp, borradas al acabar): "relanzar"
--     siempre es crear un job nuevo re-enviando la petición original.

CREATE TABLE material_ai_jobs (
    id                 bigserial PRIMARY KEY,
    type               varchar(20) NOT NULL,
    subject_id         bigint NOT NULL REFERENCES subjects (id),
    source_material_id bigint REFERENCES materials (id) ON DELETE SET NULL,
    status             varchar(20) NOT NULL,
    error_message      varchar(2000),
    result_material_id bigint REFERENCES materials (id) ON DELETE SET NULL,
    created_by_id      bigint NOT NULL REFERENCES users (id),
    created_at         timestamp(6) NOT NULL,
    updated_at         timestamp(6) NOT NULL
);

CREATE INDEX idx_material_ai_job_status ON material_ai_jobs (status);
