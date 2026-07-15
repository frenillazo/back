-- V4: año académico del material. Diseño: docs/diseno-material-por-curso-academico.md (repo raíz).
-- Entero = año de inicio del curso académico (2025 = curso "2025-26").
-- Corte sep→ago: mes >= 9 pertenece al año natural; mes <= 8 al anterior.
-- Regla: los STUDENT solo ven material de visible=true Y del año académico actual;
-- admin/teacher ven todo el histórico y pueden re-etiquetar el año desde el PATCH.

ALTER TABLE materials ADD COLUMN academic_year integer;

UPDATE materials
SET academic_year = CASE
    WHEN EXTRACT(MONTH FROM uploaded_at) >= 9
        THEN EXTRACT(YEAR FROM uploaded_at)::int
    ELSE EXTRACT(YEAR FROM uploaded_at)::int - 1
END;

ALTER TABLE materials ALTER COLUMN academic_year SET NOT NULL;

CREATE INDEX idx_material_subject_year ON materials (subject_id, academic_year);
