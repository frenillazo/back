# AcaInfo Backend

Spring Boot 3.2.1 · Java 21 · monolito modular con estructura hexagonal · PostgreSQL (prod) / H2 (dev). Submódulo git (remote: frenillazo/back); tras commitear aquí, bumpear el puntero en el superproyecto.

## Build y verificación

- Compilar: `JAVA_HOME="C:/Users/pablo/.jdks/ms-21.0.8" ./mvnw compile -q` (JAVA_HOME no está en el PATH; ~1 min)
- **0 tests** (src/test vacío) — no hay red de seguridad para refactors; añadirlos es la primera prioridad
- Arranque local: perfil `dev` = H2 en memoria + `data-dev.sql` (admin@acainfo.com y ~40 usuarios seed, contraseña "password"); perfil `local` = Postgres localhost:5433
- Swagger: `/swagger-ui.html` — la fuente de verdad de la API (`docs/API_REFERENCE.md` está desfasado)

## Estructura por módulo (paquetes bajo `com.acainfo`)

`domain/model` (POJOs anémicos con Lombok) + `domain/exception` · `application/port/in|out` + `application/service` · `infrastructure/adapter/in/rest` (Controller + XxxResponseEnricher + `dto/`) + `adapter/out/persistence` (JpaEntity + JpaRepository + Adapter + Specifications) · mappers MapStruct.

Módulos: `user` (auth + profesores: no hay entidad Teacher, son users con rol), `subject`, `group`, `intensive`, `enrollment`, `session`, `schedule`, `reservation`, `material`, `payment`, `student` (agregador del dashboard), `security`, `shared`, `util` (vacío).

## Convenciones

- **Cross-módulo: inyección directa** de puertos (out o in) de otros módulos en los servicios — estilo canónico decidido en jul-2026. Existen 2 puertos "puristas" legacy (`AutoReservationPort`, `UserReactivationPort`); no replicar ese estilo salvo que sea trivial.
- Filtros de listado: Specifications. Respuestas enriquecidas: `XxxResponseEnricher` en infra/rest.
- Paginación: `shared/application/dto/PageResponse` (content, page, size, totalElements…).
- Estados calculados (p.ej. OVERDUE) se exponen como flags booleanos en el Response, no como valores del enum.
- Los enums de dominio viven en `{modulo}/domain/model/*.java` — el front los copia a mano (fuente de deriva conocida).

## Trampas

- `currentEnrollmentCount` (SubjectGroup, Intensive) SIEMPRE a 0 en BD: usar `enrollmentRepositoryPort.countActiveByGroupId()`. Los métodos de dominio `isFull()/canEnroll()` mienten.
- Crons **apagados a propósito** en `application.properties` (`cron=-`): generación mensual de sesiones y auto-disable de materiales. NO "arreglarlos". La generación de sesiones es manual.
- **Sin migraciones**: prod corre con `SPRING_JPA_HIBERNATE_DDL_AUTO=update` (env del compose, pisa el `validate` del perfil). Cualquier cambio de entidad JPA muta el esquema de prod en el siguiente deploy. Flyway llega con la unificación de cursos.
- `application-test.properties` usa nombres de propiedad JWT que no existen en `JwtProperties` (jwt.expiration vs jwt.access-token-expiration) — trampa para cuando haya tests.
- **OBSOLETO, no invertir** (se eliminará en la simplificación): `GroupRequest*` (flujo entero), `Payment*` (pagos manuales hasta 2027), asistencia y online-requests.

## Bugs conocidos de seguridad (pendientes, con ubicación)

- `MaterialDownloadService.isEnrollmentForSubject` (líneas ~144-149): TODO que devuelve `true` — cualquier alumno matriculado descarga materiales de todas las asignaturas.
- `PaymentAdminController`: sin `@PreAuthorize` (cualquier autenticado genera/cancela pagos).
- `WaitingQueueController` GETs (líneas ~36, 50): sin control de rol.
- `AuthController.logout`: exige body `{refreshToken}` que el front no envía (400 siempre, token nunca revocado); `logout/all` es un no-op con TODO.
