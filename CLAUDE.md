# AcaInfo Backend

Spring Boot 3.5.3 · Java 21 · monolito modular con estructura hexagonal · PostgreSQL (prod) / H2 (dev). Submódulo git (remote: frenillazo/back); tras commitear aquí, bumpear el puntero en el superproyecto.

## Build y verificación

- Compilar: `JAVA_HOME="C:/Users/pablo/.jdks/ms-21.0.8" ./mvnw compile -q` (JAVA_HOME no está en el PATH; ~1 min)
- Tests: `JAVA_HOME="C:/Users/pablo/.jdks/ms-21.0.8" ./mvnw test` — **135 tests** (Mockito puro de los servicios críticos + smoke @SpringBootTest H2). Deben estar SIEMPRE en verde.
- Arranque local: perfil `dev` = H2 en memoria + `data-dev.sql` (admin@acainfo.com y ~40 usuarios seed, contraseña "password"); perfil `local` = Postgres localhost:5433
- Swagger: `/swagger-ui.html` (`/v3/api-docs`) — la fuente de verdad de la API. Los docs Markdown de `docs/` se borraron el 13-jul-2026 por desfasados; el código y Swagger son la referencia.

## Estructura por módulo (paquetes bajo `com.acainfo`)

`domain/model` (POJOs anémicos con Lombok) + `domain/exception` · `application/port/in|out` + `application/service` · `infrastructure/adapter/in/rest` (Controller + XxxResponseEnricher + `dto/`) + `adapter/out/persistence` (JpaEntity + JpaRepository + Adapter + Specifications) · mappers MapStruct.

Módulos: `user` (auth + profesores: no hay entidad Teacher, son users con rol), `subject` (incluye `subject_interest`, el "me interesa"), `course` (modelo unificado: capacity nullable = ilimitado/virtual, price_per_month informativo, teacher opcional), `enrollment`, `session` (REGULAR/EXTRA), `schedule`, `reservation`, `material`, `student` (agregador del dashboard), `security`, `shared`.

## Convenciones

- **Cross-módulo: inyección directa** de puertos (out o in) de otros módulos en los servicios — estilo canónico decidido en jul-2026. Existen 2 puertos "puristas" legacy (`AutoReservationPort`, `UserReactivationPort`); no replicar ese estilo salvo que sea trivial.
- Filtros de listado: Specifications. Respuestas enriquecidas: `XxxResponseEnricher` en infra/rest.
- Paginación: `shared/application/dto/PageResponse` (content, page, size, totalElements…).
- Estados calculados (p.ej. OVERDUE) se exponen como flags booleanos en el Response, no como valores del enum.
- Los enums de dominio viven en `{modulo}/domain/model/*.java` — el front los copia a mano (fuente de deriva conocida).

## Trampas

- La ocupación de un curso NO se persiste: usar `enrollmentRepositoryPort.countActiveByCourseId()`; el `CourseResponseEnricher` la calcula para la API. `capacity` NULL = sin cupo ni lista de espera.
- Crons **apagados a propósito** en `application.properties` (`cron=-`): generación mensual de sesiones y auto-disable de materiales. NO "arreglarlos". La generación de sesiones es manual. Crons SÍ activos: purga diaria de tokens expirados (`security/cleanup/TokenCleanupService`, 05:00) y limpieza de usuarios sin verificar (04:00).
- **Flyway manda sobre el esquema** (src/main/resources/db/migration; V1 baseline de prod + V2 curso unificado, aplicadas en prod el 11-jul-2026). Todo cambio de entidad JPA exige su migración V*.sql; prod corre `ddl-auto=validate`.
- `src/main/resources/application-test.properties` usa nombres de propiedad JWT incorrectos (el bueno es el de `src/test/resources`, ya corregido).

## Bugs conocidos pendientes

(Spring Boot actualizado a 3.5.3 el 13-jul-2026 — Flyway 11 necesita `flyway-database-postgresql`, ya en el pom. Logout y purga de tokens arreglados el 11-jul-2026; los demás bugs de la auditoría jul-2026 cayeron con la migración curso unificado.)
