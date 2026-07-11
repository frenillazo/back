-- V3: las matriculas de cursos ya CERRADOS/CANCELADOS dejan de estar activas.
-- Semantica (11-jul-2026): cerrar un curso = terminado a efectos de matricula.
--   ACTIVE           -> COMPLETED (el alumno curso el curso)
--   PENDING_APPROVAL -> EXPIRED   (nunca llego a cursarlo)
--   WAITING_LIST     -> EXPIRED   (idem; se limpia la posicion de cola)
-- A partir de ahora esta transicion la hace la app al cerrar/cancelar un curso
-- (CloseCourseEnrollmentsUseCase); esta migracion arregla el historico.

UPDATE enrollments e
SET status = 'COMPLETED', updated_at = now()
WHERE e.status = 'ACTIVE'
  AND EXISTS (SELECT 1 FROM courses c
              WHERE c.id = e.course_id AND c.status IN ('CLOSED', 'CANCELLED'));

UPDATE enrollments e
SET status = 'EXPIRED', waiting_list_position = NULL, updated_at = now()
WHERE e.status IN ('PENDING_APPROVAL', 'WAITING_LIST')
  AND EXISTS (SELECT 1 FROM courses c
              WHERE c.id = e.course_id AND c.status IN ('CLOSED', 'CANCELLED'));
