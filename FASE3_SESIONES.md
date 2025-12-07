# FASE 3: Gestión de Sesiones

**Duración estimada:** 1.5 semanas (38 horas)
**Objetivo:** Control completo del ciclo de vida de sesiones

---

## Resumen Ejecutivo

El módulo de sesiones permite gestionar las clases individuales que se derivan de los horarios (Schedule) de cada grupo. Cada sesión tiene un ciclo de vida con estados bien definidos y transiciones controladas.

### Modelo de Estados

```
PROGRAMADA ──┬──► EN_CURSO ──► COMPLETADA
             │
             ├──► CANCELADA
             │
             └──► POSPUESTA ──► PROGRAMADA (nueva fecha)
```

### Endpoints a Implementar

```
POST   /api/sessions                    # Crear sesión manual (ADMIN/TEACHER)
GET    /api/sessions                    # Listar con filtros
GET    /api/sessions/{id}               # Obtener sesión
PUT    /api/sessions/{id}               # Actualizar (ADMIN/TEACHER)
POST   /api/sessions/{id}/start         # Iniciar sesión (PROGRAMADA → EN_CURSO)
POST   /api/sessions/{id}/cancel        # Cancelar sesión (PROGRAMADA → CANCELADA)
POST   /api/sessions/{id}/complete      # Completar sesión (EN_CURSO → COMPLETADA)
POST   /api/sessions/{id}/postpone      # Posponer sesión (PROGRAMADA → POSPUESTA)
POST   /api/sessions/generate           # Generar sesiones desde Schedule (ADMIN)
GET    /api/sessions/group/{groupId}    # Sesiones de un grupo
GET    /api/sessions/teacher/{teacherId} # Sesiones de un profesor
```

---

## Hito 1: Dominio de Session

**Objetivo:** Crear el modelo de dominio completo para sesiones con su máquina de estados.
**Estimación:** 8 horas

### Tareas

| # | Tarea | Archivo | Descripción |
|---|-------|---------|-------------|
| 1.1 | Crear entidad Session | `session/domain/model/Session.java` | POJO con Lombok: id, groupId, sessionDate, startTime, endTime, status, type, mode, topic, notes, actualStartTime, actualEndTime, createdAt, updatedAt |
| 1.2 | Crear enum SessionStatus | `session/domain/model/SessionStatus.java` | PROGRAMADA, EN_CURSO, COMPLETADA, CANCELADA, POSPUESTA |
| 1.3 | Crear enum SessionType | `session/domain/model/SessionType.java` | REGULAR, RECUPERACION, EXAMEN |
| 1.4 | Crear enum SessionMode | `session/domain/model/SessionMode.java` | PRESENCIAL, ONLINE, HIBRIDO |
| 1.5 | Implementar query methods | `Session.java` | `isProgramada()`, `isEnCurso()`, `isCompletada()`, `isCancelada()`, `isPospuesta()`, `canStart()`, `canComplete()`, `canCancel()`, `canPostpone()`, `getDurationMinutes()` |
| 1.6 | Crear SessionNotFoundException | `session/domain/exception/SessionNotFoundException.java` | Extender NotFoundException base |
| 1.7 | Crear InvalidSessionStateException | `session/domain/exception/InvalidSessionStateException.java` | Para transiciones de estado inválidas |
| 1.8 | Crear SessionConflictException | `session/domain/exception/SessionConflictException.java` | Para conflictos de horario al reprogramar |
| 1.9 | Tests unitarios dominio | `session/domain/model/SessionTest.java` | Tests de query methods y validaciones de transición |

### Estructura del Dominio

```java
// Session.java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true) @EqualsAndHashCode(of = "id")
public class Session {
    private Long id;
    private Long groupId;              // Referencia al grupo (ID, no entidad)
    private LocalDate sessionDate;     // Fecha de la sesión
    private LocalTime startTime;       // Hora inicio programada
    private LocalTime endTime;         // Hora fin programada
    private SessionStatus status;      // Estado actual
    private SessionType type;          // Tipo de sesión
    private SessionMode mode;          // Modalidad
    private String topic;              // Tema de la sesión (opcional)
    private String notes;              // Notas adicionales (opcional)
    private LocalTime actualStartTime; // Hora inicio real (cuando inicia)
    private LocalTime actualEndTime;   // Hora fin real (cuando completa)
    private String cancellationReason; // Motivo cancelación (si aplica)
    private LocalDate postponedToDate; // Nueva fecha (si pospuesta)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Query methods
    public boolean isProgramada() { return status == SessionStatus.PROGRAMADA; }
    public boolean isEnCurso() { return status == SessionStatus.EN_CURSO; }
    public boolean canStart() { return isProgramada(); }
    public boolean canCancel() { return isProgramada(); }
    public boolean canComplete() { return isEnCurso(); }
    public boolean canPostpone() { return isProgramada(); }
    // ...
}
```

---

## Hito 2: Capa de Persistencia

**Objetivo:** Implementar la infraestructura de persistencia con JPA.
**Estimación:** 6 horas

### Tareas

| # | Tarea | Archivo | Descripción |
|---|-------|---------|-------------|
| 2.1 | Crear SessionJpaEntity | `session/infrastructure/adapter/out/persistence/entity/SessionJpaEntity.java` | Entidad JPA con @Table, @Column, relaciones |
| 2.2 | Crear JpaSessionRepository | `session/infrastructure/adapter/out/persistence/repository/JpaSessionRepository.java` | Interface Spring Data JPA con queries custom |
| 2.3 | Crear SessionPersistenceMapper | `session/infrastructure/mapper/SessionPersistenceMapper.java` | MapStruct: Domain ↔ JPA Entity |
| 2.4 | Crear SessionRepositoryAdapter | `session/infrastructure/adapter/out/persistence/repository/SessionRepositoryAdapter.java` | Implementa SessionRepositoryPort |
| 2.5 | Crear SessionSpecifications | `session/infrastructure/adapter/out/persistence/specification/SessionSpecifications.java` | Criteria Builder: groupId, status, dateRange, type, teacherId |
| 2.6 | Script de migración | `resources/db/migration/` | Crear tabla sessions (si usamos Flyway) o actualizar schema |

### Estructura JPA

```java
// SessionJpaEntity.java
@Entity
@Table(name = "sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SessionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionMode mode;

    private String topic;
    private String notes;

    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "postponed_to_date")
    private LocalDate postponedToDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## Hito 3: Capa de Aplicación

**Objetivo:** Implementar la lógica de negocio con máquina de estados completa.
**Estimación:** 10 horas

### Tareas

| # | Tarea | Archivo | Descripción |
|---|-------|---------|-------------|
| 3.1 | Crear SessionRepositoryPort | `session/application/port/out/SessionRepositoryPort.java` | Interface del repositorio |
| 3.2 | Crear CreateSessionUseCase | `session/application/port/in/CreateSessionUseCase.java` | Crear sesión manual |
| 3.3 | Crear GetSessionUseCase | `session/application/port/in/GetSessionUseCase.java` | Obtener sesiones con filtros |
| 3.4 | Crear UpdateSessionUseCase | `session/application/port/in/UpdateSessionUseCase.java` | Actualizar datos de sesión |
| 3.5 | Crear DeleteSessionUseCase | `session/application/port/in/DeleteSessionUseCase.java` | Eliminar sesión |
| 3.6 | Crear StartSessionUseCase | `session/application/port/in/StartSessionUseCase.java` | PROGRAMADA → EN_CURSO |
| 3.7 | Crear CancelSessionUseCase | `session/application/port/in/CancelSessionUseCase.java` | PROGRAMADA → CANCELADA |
| 3.8 | Crear CompleteSessionUseCase | `session/application/port/in/CompleteSessionUseCase.java` | EN_CURSO → COMPLETADA |
| 3.9 | Crear PostponeSessionUseCase | `session/application/port/in/PostponeSessionUseCase.java` | PROGRAMADA → POSPUESTA |
| 3.10 | Crear GenerateSessionsUseCase | `session/application/port/in/GenerateSessionsUseCase.java` | Generar desde Schedule |
| 3.11 | Crear DTOs de aplicación | `session/application/dto/*.java` | CreateSessionCommand, UpdateSessionCommand, SessionFilters, PostponeSessionCommand, GenerateSessionsCommand, CancelSessionCommand |
| 3.12 | Crear SessionService | `session/application/service/SessionService.java` | Implementar todos los use cases |

### DTOs de Aplicación (Records)

```java
// CreateSessionCommand.java
public record CreateSessionCommand(
    Long groupId,
    LocalDate sessionDate,
    LocalTime startTime,
    LocalTime endTime,
    SessionType type,
    SessionMode mode,
    String topic,
    String notes
) {}

// UpdateSessionCommand.java
public record UpdateSessionCommand(
    LocalDate sessionDate,
    LocalTime startTime,
    LocalTime endTime,
    SessionType type,
    SessionMode mode,
    String topic,
    String notes
) {}

// SessionFilters.java
public record SessionFilters(
    Long groupId,
    Long teacherId,
    SessionStatus status,
    SessionType type,
    LocalDate fromDate,
    LocalDate toDate,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {}

// PostponeSessionCommand.java
public record PostponeSessionCommand(
    LocalDate newDate,
    LocalTime newStartTime,
    LocalTime newEndTime,
    String reason
) {}

// CancelSessionCommand.java
public record CancelSessionCommand(
    String reason
) {}

// GenerateSessionsCommand.java
public record GenerateSessionsCommand(
    Long groupId,
    LocalDate fromDate,
    LocalDate toDate
) {}
```

### Lógica de Generación Automática

```java
// En SessionService.generateSessions()
public List<Session> generateSessions(GenerateSessionsCommand command) {
    // 1. Obtener los schedules del grupo
    List<Schedule> schedules = scheduleRepository.findByGroupId(command.groupId());

    // 2. Para cada día en el rango [fromDate, toDate]
    // 3. Si el día coincide con algún schedule (DayOfWeek)
    // 4. Crear sesión con estado PROGRAMADA
    // 5. Evitar duplicados (verificar si ya existe sesión para ese grupo/fecha/hora)

    List<Session> generatedSessions = new ArrayList<>();
    LocalDate current = command.fromDate();

    while (!current.isAfter(command.toDate())) {
        for (Schedule schedule : schedules) {
            if (current.getDayOfWeek() == schedule.getDayOfWeek()) {
                // Verificar que no exista ya
                if (!sessionExists(command.groupId(), current, schedule.getStartTime())) {
                    Session session = Session.builder()
                        .groupId(command.groupId())
                        .sessionDate(current)
                        .startTime(schedule.getStartTime())
                        .endTime(schedule.getEndTime())
                        .status(SessionStatus.PROGRAMADA)
                        .type(SessionType.REGULAR)
                        .mode(determineMode(schedule.getClassroom()))
                        .build();
                    generatedSessions.add(sessionRepository.save(session));
                }
            }
        }
        current = current.plusDays(1);
    }

    return generatedSessions;
}
```

---

## Hito 4: Capa REST

**Objetivo:** Exponer los endpoints REST para gestión de sesiones.
**Estimación:** 6 horas

### Tareas

| # | Tarea | Archivo | Descripción |
|---|-------|---------|-------------|
| 4.1 | Crear CreateSessionRequest | `session/infrastructure/adapter/in/rest/dto/CreateSessionRequest.java` | Request para crear sesión |
| 4.2 | Crear UpdateSessionRequest | `session/infrastructure/adapter/in/rest/dto/UpdateSessionRequest.java` | Request para actualizar |
| 4.3 | Crear PostponeSessionRequest | `session/infrastructure/adapter/in/rest/dto/PostponeSessionRequest.java` | Request con nueva fecha |
| 4.4 | Crear CancelSessionRequest | `session/infrastructure/adapter/in/rest/dto/CancelSessionRequest.java` | Request con motivo |
| 4.5 | Crear GenerateSessionsRequest | `session/infrastructure/adapter/in/rest/dto/GenerateSessionsRequest.java` | Request con rango de fechas |
| 4.6 | Crear SessionResponse | `session/infrastructure/adapter/in/rest/dto/SessionResponse.java` | Response completo |
| 4.7 | Crear SessionRestMapper | `session/infrastructure/mapper/SessionRestMapper.java` | MapStruct: Domain ↔ REST DTOs |
| 4.8 | Crear SessionController | `session/infrastructure/adapter/in/rest/SessionController.java` | Todos los endpoints |

### Estructura del Controller

```java
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    // CRUD básico
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request)

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(@PathVariable Long id)

    @GetMapping
    public ResponseEntity<Page<SessionResponse>> getSessionsWithFilters(...)

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> updateSession(@PathVariable Long id, @Valid @RequestBody UpdateSessionRequest request)

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id)

    // Transiciones de estado
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> startSession(@PathVariable Long id)

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> cancelSession(@PathVariable Long id, @RequestBody CancelSessionRequest request)

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> completeSession(@PathVariable Long id)

    @PostMapping("/{id}/postpone")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<SessionResponse> postponeSession(@PathVariable Long id, @Valid @RequestBody PostponeSessionRequest request)

    // Generación automática
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionResponse>> generateSessions(@Valid @RequestBody GenerateSessionsRequest request)

    // Consultas específicas
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SessionResponse>> getSessionsByGroup(@PathVariable Long groupId)

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<Page<SessionResponse>> getSessionsByTeacher(@PathVariable Long teacherId, ...)
}
```

---

## Hito 5: Tests

**Objetivo:** Cobertura completa de tests unitarios y E2E.
**Estimación:** 8 horas

### Tareas

| # | Tarea | Archivo | Descripción |
|---|-------|---------|-------------|
| 5.1 | Tests dominio Session | `session/domain/model/SessionTest.java` | Query methods, validaciones de transición |
| 5.2 | Tests dominio enums | `session/domain/model/SessionStatusTest.java` | Verificar valores y métodos |
| 5.3 | Tests SessionService | `session/application/service/SessionServiceTest.java` | Unit tests con Mockito |
| 5.4 | Tests generación | `SessionServiceTest.java` | Verificar generación desde Schedule |
| 5.5 | Tests E2E CRUD | `session/e2e/SessionE2ETest.java` | Create, Read, Update, Delete |
| 5.6 | Tests E2E transiciones | `SessionE2ETest.java` | Start, Cancel, Complete, Postpone |
| 5.7 | Tests E2E generación | `SessionE2ETest.java` | POST /api/sessions/generate |
| 5.8 | Actualizar TestDataHelper | `shared/e2e/TestDataHelper.java` | Añadir métodos para crear sesiones de test |

### Casos de Test E2E

```java
@DisplayName("Session E2E Tests")
class SessionE2ETest extends BaseE2ETest {

    @Nested
    @DisplayName("POST /api/sessions")
    class CreateSessionTests {
        // Should create session as admin
        // Should create session as teacher
        // Should reject session creation by student
        // Should reject invalid data
    }

    @Nested
    @DisplayName("POST /api/sessions/{id}/start")
    class StartSessionTests {
        // Should start PROGRAMADA session
        // Should reject starting non-PROGRAMADA session
        // Should reject start by student
    }

    @Nested
    @DisplayName("POST /api/sessions/{id}/cancel")
    class CancelSessionTests {
        // Should cancel PROGRAMADA session with reason
        // Should reject cancelling EN_CURSO session
    }

    @Nested
    @DisplayName("POST /api/sessions/{id}/complete")
    class CompleteSessionTests {
        // Should complete EN_CURSO session
        // Should reject completing PROGRAMADA session
    }

    @Nested
    @DisplayName("POST /api/sessions/{id}/postpone")
    class PostponeSessionTests {
        // Should postpone PROGRAMADA session to new date
        // Should reject postponing past date
    }

    @Nested
    @DisplayName("POST /api/sessions/generate")
    class GenerateSessionsTests {
        // Should generate sessions from schedule
        // Should not duplicate existing sessions
        // Should reject invalid date range
    }
}
```

---

## Resumen de Estructura de Archivos

```
src/main/java/com/acainfo/session/
├── domain/
│   ├── model/
│   │   ├── Session.java
│   │   ├── SessionStatus.java
│   │   ├── SessionType.java
│   │   └── SessionMode.java
│   └── exception/
│       ├── SessionNotFoundException.java
│       ├── InvalidSessionStateException.java
│       └── SessionConflictException.java
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreateSessionUseCase.java
│   │   │   ├── GetSessionUseCase.java
│   │   │   ├── UpdateSessionUseCase.java
│   │   │   ├── DeleteSessionUseCase.java
│   │   │   ├── StartSessionUseCase.java
│   │   │   ├── CancelSessionUseCase.java
│   │   │   ├── CompleteSessionUseCase.java
│   │   │   ├── PostponeSessionUseCase.java
│   │   │   └── GenerateSessionsUseCase.java
│   │   └── out/
│   │       └── SessionRepositoryPort.java
│   ├── dto/
│   │   ├── CreateSessionCommand.java
│   │   ├── UpdateSessionCommand.java
│   │   ├── SessionFilters.java
│   │   ├── PostponeSessionCommand.java
│   │   ├── CancelSessionCommand.java
│   │   └── GenerateSessionsCommand.java
│   └── service/
│       └── SessionService.java
│
└── infrastructure/
    ├── adapter/
    │   ├── in/rest/
    │   │   ├── dto/
    │   │   │   ├── CreateSessionRequest.java
    │   │   │   ├── UpdateSessionRequest.java
    │   │   │   ├── PostponeSessionRequest.java
    │   │   │   ├── CancelSessionRequest.java
    │   │   │   ├── GenerateSessionsRequest.java
    │   │   │   └── SessionResponse.java
    │   │   └── SessionController.java
    │   └── out/persistence/
    │       ├── entity/
    │       │   └── SessionJpaEntity.java
    │       ├── repository/
    │       │   ├── JpaSessionRepository.java
    │       │   └── SessionRepositoryAdapter.java
    │       └── specification/
    │           └── SessionSpecifications.java
    └── mapper/
        ├── SessionPersistenceMapper.java
        └── SessionRestMapper.java

src/test/java/com/acainfo/session/
├── domain/model/
│   ├── SessionTest.java
│   └── SessionStatusTest.java
├── application/service/
│   └── SessionServiceTest.java
└── e2e/
    └── SessionE2ETest.java
```

---

## Resumen de Hitos

| Hito | Descripción | Tareas | Estimación |
|------|-------------|--------|------------|
| **1** | Dominio de Session | 9 | 8h |
| **2** | Capa de Persistencia | 6 | 6h |
| **3** | Capa de Aplicación | 12 | 10h |
| **4** | Capa REST | 8 | 6h |
| **5** | Tests | 8 | 8h |
| **TOTAL** | | **43** | **38h** |

---

## Dependencias

- **Requiere:** Módulo Group (para groupId) y Schedule (para generación automática)
- **Usado por:** Módulo Attendance (Fase 5) - registrará asistencia por sesión

---

## Notas de Implementación

1. **Transiciones de estado:** Validar siempre el estado actual antes de permitir transición
2. **Generación automática:** No duplicar sesiones existentes, usar fechas del cuatrimestre
3. **Permisos:** Admin puede todo, Teacher solo sus grupos, Student solo lectura
4. **Auditoría:** Registrar quién y cuándo realizó cada cambio de estado

---

*Documento generado: Diciembre 2024*
*Arquitectura: Hexagonal Pura Modular*
