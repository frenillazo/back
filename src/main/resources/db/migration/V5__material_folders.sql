-- V5: carpetas de materiales por asignatura, que SUSTITUYEN al enum category.
-- Diseño: docs/diseno-carpetas-materiales.md (repo raíz). Decisiones cerradas 15-jul-2026:
--   - UN solo nivel (sin parent_id); anidar sería una migración aditiva futura.
--   - materials.folder_id NULL = raíz de la asignatura ("sin carpeta").
--   - Backfill espejo: una carpeta por (asignatura, categoría) CON materiales, nombre =
--     displayName de la categoría — el alumno ve exactamente la misma agrupación que hoy.
--   - El storage físico NO se toca: storage_path manda; los ficheros no se mueven jamás.

-- ============================================================
-- 1. material_folders
-- ============================================================
CREATE TABLE material_folders (
    id          bigserial PRIMARY KEY,
    subject_id  bigint NOT NULL REFERENCES subjects (id),
    name        varchar(100) NOT NULL,
    position    integer NOT NULL DEFAULT 0,
    created_at  timestamp(6) NOT NULL,
    updated_at  timestamp(6) NOT NULL,
    CONSTRAINT uk_material_folder_subject_name UNIQUE (subject_id, name)
);

-- ============================================================
-- 2. materials.folder_id (SET NULL: borrar carpeta manda a raíz, nunca borra materiales)
-- ============================================================
ALTER TABLE materials
    ADD COLUMN folder_id bigint REFERENCES material_folders (id) ON DELETE SET NULL;

-- ============================================================
-- 3. Backfill espejo de las categorías usadas
--    position = orden del enum, para conservar el orden visual actual.
-- ============================================================
INSERT INTO material_folders (subject_id, name, position, created_at, updated_at)
SELECT t.subject_id,
       CASE t.category
           WHEN 'TEORIA'       THEN 'Teoría'
           WHEN 'EJERCICIOS'   THEN 'Ejercicios'
           WHEN 'EXAMENES'     THEN 'Exámenes'
           WHEN 'PROYECTOS'    THEN 'Proyectos'
           WHEN 'LABORATORIOS' THEN 'Laboratorios'
           ELSE 'Otros'
       END,
       CASE t.category
           WHEN 'TEORIA'       THEN 0
           WHEN 'EJERCICIOS'   THEN 1
           WHEN 'EXAMENES'     THEN 2
           WHEN 'PROYECTOS'    THEN 3
           WHEN 'LABORATORIOS' THEN 4
           ELSE 5
       END,
       now(), now()
FROM (SELECT DISTINCT subject_id, category FROM materials) t;

UPDATE materials mat
SET folder_id = mf.id
FROM material_folders mf
WHERE mf.subject_id = mat.subject_id
  AND mf.name = CASE mat.category
           WHEN 'TEORIA'       THEN 'Teoría'
           WHEN 'EJERCICIOS'   THEN 'Ejercicios'
           WHEN 'EXAMENES'     THEN 'Exámenes'
           WHEN 'PROYECTOS'    THEN 'Proyectos'
           WHEN 'LABORATORIOS' THEN 'Laboratorios'
           ELSE 'Otros'
       END;

-- ============================================================
-- 4. category muere (constraint + índice + columna)
-- ============================================================
ALTER TABLE materials DROP CONSTRAINT materials_category_check;
DROP INDEX idx_material_category;
ALTER TABLE materials DROP COLUMN category;
