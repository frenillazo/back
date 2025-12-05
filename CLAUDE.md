# CLAUDE.md - Plan de Reimplementación
## Sistema de Gestión Centro de Formación - Arquitectura Hexagonal Pura

---

## 📋 Resumen Ejecutivo

**Proyecto:** Sistema de Gestión para Centro de Formación de Ingeniería (Reimplementación)  
**Enfoque:** Arquitectura Hexagonal Pura con módulos independientes  
**Duración Total:** 13 semanas (~330 horas)  

**Stack Tecnológico:**
- **Backend:** Spring Boot 3.2.1, Java 21
- **Base de Datos:** PostgreSQL (producción), H2 (desarrollo/test)
- **Arquitectura:** Hexagonal Pura Modular (Domain-Driven Design)
- **Mapeo:** MapStruct 1.5.5.Final
- **Seguridad:** Spring Security 6 + JWT (io.jsonwebtoken 0.12.6)
- **Documentación:** SpringDoc OpenAPI 2.3.0
- **Build:** Maven 3.9.11
- **Contenedores:** Docker + Docker Compose
- **Pagos:** Stripe API

**Capacidad del Sistema:**
- 300-400 alumnos activos por cuatrimestre
- 2 aulas × 24 plazas = 48 plazas presenciales máximo
- 4 profesores (2 con rol administrador)

---

## 🎯 Principios Arquitectónicos

### Arquitectura Hexagonal Pura

```
módulo/
├── domain/                    # 🔵 NÚCLEO - Java Puro (sin frameworks)
│   ├── model/                 # Entidades POJO puras
│   ├── exception/             # Excepciones de dominio
│   └── validation/            # Reglas de negocio
│
├── application/               # 🟢 CASOS DE USO
│   ├── port/in/               # Use Cases (interfaces)
│   ├── port/out/              # Repository Ports (interfaces)
│   ├── service/               # Implementación lógica
│   ├── dto/                   # Commands y Queries
│   └── mapper/                # Mappers de aplicación
│
└── infrastructure/            # 🟠 ADAPTADORES
    ├── adapter/in/rest/       # Controllers + DTOs REST
    ├── adapter/out/persistence/
    │   ├── entity/            # Entidades JPA (*JpaEntity)
    │   ├── repository/        # JPA Repos + Adapters
    │   └── specification/     # Criteria Builder
    └── mapper/                # Mappers de infraestructura
```

### Reglas Fundamentales

1. **Dominio Anémico con Lombok**: Entidades POJO con `@Getter/@Setter` y lógica mínima
2. **Separación JPA**: Entidades JPA separadas con sufijo `*JpaEntity`
3. **MapStruct**: Conversiones automáticas entre capas
4. **Lógica en Services**: Reglas de negocio no triviales en capa de aplicación
5. **Seguridad Simplificada**: `isAdmin()`, `isTeacher()`, `isStudent()` (sin entidad Permission)
6. **RefreshToken**: No es dominio, vive en `security/` como infraestructura
7. **Lombok para reducir boilerplate**: Aceptable en todas las capas (no viola arquitectura)
8. **Referencias entre Agregados**: Usar IDs (Long) en lugar de entidades completas para mantener independencia de agregados (DDD purismo)
9. **DTOs de Aplicación**: Los `*Filters` y `*Command` son **records** (Java 14+) para inmutabilidad
10. **Constructor vs Builder**: Los **records usan constructor parametrizado**, NO builder pattern (incompatible con Lombok @Builder)

---

## 🏛️ Modelo de Dominio: Anémico vs Rico

**Decisión Arquitectónica:** Utilizamos **Modelo de Dominio Anémico** (Anemic Domain Model)

### Filosofía

Las entidades de dominio son **POJOs simples** con:
- ✅ `@Getter` y `@Setter` de Lombok
- ✅ Validaciones básicas de invariantes
- ✅ Métodos de consulta simples (query methods)
- ❌ NO tienen lógica de negocio compleja
- ❌ NO orquestan operaciones

### Responsabilidades por Capa

#### 🔵 Domain Layer (POJOs)
```java
@Getter
@Setter  // Setters automáticos SIN validación (Lombok los genera)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "email")
public class User {
    private Long id;
    private String email;
    private UserStatus status;
    private Set<Role> roles = new HashSet<>();

    // ✅ SOLO métodos de consulta (query methods)
    public boolean isAdmin() {
        return roles.stream().anyMatch(Role::isAdmin);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ❌ NO hay validaciones (ni en setters ni en métodos)
    // ❌ NO hay lógica de negocio
    // Los setters son generados por Lombok automáticamente
}
```

#### 🟢 Application Layer (Services)
```java
@Service
public class UserService {

    // ✅ Validaciones de negocio
    public User createUser(String email, String password, String firstName, String lastName) {
        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Invalid email format");
        }

        // Validar email único
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        // Validar password
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }

        User user = new User();
        user.setEmail(email.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setStatus(UserStatus.PENDING_ACTIVATION);

        return userRepository.save(user);
    }

    // ✅ Lógica de negocio compleja
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new UserBlockedException(user.getEmail());
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Orquestación: enviar email, log, etc.
        emailService.sendActivationEmail(user);
        auditService.log("User activated: " + user.getEmail());
    }

    // ✅ Reglas que requieren datos externos
    public boolean canEnroll(User user, SubjectGroup group) {
        boolean hasPaymentsUpToDate = paymentService.isUpToDate(user.getId());
        int currentEnrollments = enrollmentRepository.countByUserId(user.getId());
        return hasPaymentsUpToDate && currentEnrollments < MAX_ENROLLMENTS;
    }
}
```

### Ventajas del Modelo Anémico

1. **Separación clara**: Datos (POJOs) vs Lógica (Services)
2. **Testeable**: Services se testean fácilmente con mocks
3. **Transaccional**: Lógica en Services permite control de @Transactional
4. **Reutilizable**: Misma lógica desde diferentes casos de uso
5. **Simple**: Menos complejidad en las entidades

### Desventajas (aceptadas)

1. No sigue DDD estricto (Rich Domain Model)
2. Posible dispersión de lógica si no se organiza bien
3. Tentación de hacer Services muy grandes (mitigar con casos de uso)

### Qué va en cada lugar

| Concepto | Dominio (POJO) | Aplicación (Service) |
|----------|----------------|----------------------|
| Validación de formato email | ✅ | ❌ |
| Verificar si es admin | ✅ | ❌ |
| Activar usuario | ❌ | ✅ |
| Cambiar contraseña | ❌ | ✅ |
| Registrar usuario completo | ❌ | ✅ |
| Consultar pagos para inscribirse | ❌ | ✅ |
| Enviar email | ❌ | ✅ |

---

## 🔧 Decisiones de Diseño Específicas

### Referencias entre Agregados (DDD Purismo)

**Decisión:** Usar **IDs (Long)** en lugar de entidades completas para referencias entre agregados independientes.

**Ejemplo:**
```java
// ✅ CORRECTO - Mantiene independencia de agregados
public class SubjectGroup {
    private Long subjectId;  // ID, no Subject entity
    private Long teacherId;  // ID, no User entity
}

// ❌ INCORRECTO - Crea acoplamiento entre agregados
public class SubjectGroup {
    private Subject subject;  // ❌
    private User teacher;     // ❌
}
```

**Razones:**
1. Mantiene independencia entre agregados (Subject, User, Group son agregados separados)
2. Evita lazy loading issues de JPA
3. Facilita el mapeo entre capas (Domain ↔ JPA)
4. Simplifica las transacciones

**Excepción:** User-Role es un caso especial donde Role eventualmente se refactorizará a enum.

### Records para DTOs (Java 14+)

**Decisión:** Los DTOs de aplicación (`*Command`, `*Filters`) son **records** para inmutabilidad.

**Ejemplo:**
```java
// ✅ CORRECTO - Record inmutable
public record GroupFilters(
    Long subjectId,
    Long teacherId,
    GroupType type,
    GroupStatus status,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {}

// Uso: Constructor parametrizado (NO builder)
GroupFilters filters = new GroupFilters(
    subjectId, teacherId, type, status,
    page, size, sortBy, sortDirection
);
```

**Razones:**
1. Inmutabilidad por defecto (thread-safe)
2. Constructor canónico automático
3. equals(), hashCode(), toString() generados automáticamente
4. Menos boilerplate que clases tradicionales

**IMPORTANTE:** Los records **NO soportan** `@Builder` de Lombok. Usar constructor parametrizado.

### Reglas de Negocio: Grupos por Asignatura

**Decisión:** Una asignatura **puede tener múltiples grupos del mismo tipo**.

**Ejemplo:**
```java
// ✅ VÁLIDO - Múltiples grupos REGULAR_Q1 para la misma asignatura
Subject: "Programación I" (id=1)
  ├─ Group 1: REGULAR_Q1, Teacher A, Capacity 24
  ├─ Group 2: REGULAR_Q1, Teacher B, Capacity 24
  └─ Group 3: INTENSIVE_Q1, Teacher C, Capacity 50
```

**Razones:**
1. Flexibilidad para alta demanda de estudiantes
2. Permite crear grupos paralelos con diferentes profesores
3. No hay restricción de unicidad (subject_id, type)

**Capacidades:**
- `REGULAR`: Max 24 estudiantes (capacidad del aula)
- `INTENSIVE`: Max 50 estudiantes (mayor flexibilidad)
- Custom capacity: Permitido dentro de los límites del tipo

---

## 📁 Estructura de Módulos

```
src/main/java/com/acainfo/
├── AcaInfoApplication.java
│
├── shared/                    # Configuración y elementos compartidos
├── security/                  # JWT, RefreshToken (NO es dominio)
│
├── user/                      # 👤 Gestión de usuarios y roles
├── subject/                   # 📚 Gestión de asignaturas
├── group/                     # 👥 Gestión de grupos
├── schedule/                  # 📅 Gestión de horarios
├── session/                   # 🎓 Gestión de sesiones
├── enrollment/                # 📝 Inscripciones y cola de espera
├── attendance/                # ✅ Control de asistencia
├── material/                  # 📄 Materiales educativos
├── payment/                   # 💳 Gestión de pagos
└── student/                   # 🎒 Dashboard estudiante (agregación)
```

---

## 🗓️ FASE 0: Setup Inicial
**Duración:** 1 semana (15 horas)  
**Objetivo:** Establecer base del proyecto con arquitectura hexagonal

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 0.1 | Crear proyecto Spring Boot 3.2.1 con estructura hexagonal modular | 3h | Proyecto base |
| 0.2 | Configurar MapStruct + Lombok (annotation processors) | 2h | pom.xml configurado |
| 0.3 | Configurar perfiles: dev (H2), test (H2), prod (PostgreSQL) | 2h | application-*.properties |
| 0.4 | Crear módulo `shared/` con excepciones base y DTOs comunes | 3h | Módulo shared |
| 0.5 | Configurar Docker Compose (PostgreSQL, pgAdmin) | 2h | docker-compose.yml |
| 0.6 | Configurar tests (JUnit 5 + Mockito + AssertJ) | 2h | Estructura de tests |
| 0.7 | Documentar arquitectura en README | 1h | README.md |

### Entregables Fase 0
- [ ] Proyecto Spring Boot compilando
- [ ] Estructura de paquetes hexagonal creada
- [ ] MapStruct generando mappers
- [ ] Perfiles de configuración funcionando
- [ ] Docker Compose levantando PostgreSQL
- [ ] Tests ejecutándose correctamente

### Configuración pom.xml (Dependencias Clave)

```xml
<!-- MapStruct -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>

<!-- Lombok (solo infraestructura, NO en dominio) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>

<!-- Annotation Processors -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
        </annotationProcessorPaths>
        <compilerArgs>
            <arg>-Amapstruct.defaultComponentModel=spring</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

---

## 🔐 FASE 1: Módulo User + Seguridad
**Duración:** 2 semanas (64 horas)  
**Objetivo:** Sistema de usuarios, roles y autenticación JWT

### Semana 1: Dominio y Persistencia (32h)

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 1.1 | Crear `user/domain/model/`: User, Role, UserStatus, RoleType | 4h | Entidades de dominio |
| 1.2 | Implementar lógica de negocio en User: `isAdmin()`, `isTeacher()`, `isStudent()` | 2h | Métodos de dominio |
| 1.3 | Crear `user/domain/exception/`: UserNotFoundException, DuplicateEmailException, etc. | 2h | Excepciones |
| 1.4 | Crear `user/infrastructure/.../entity/`: UserJpaEntity, RoleJpaEntity | 4h | Entidades JPA |
| 1.5 | Crear `user/infrastructure/mapper/`: UserPersistenceMapper, RolePersistenceMapper | 3h | Mappers JPA |
| 1.6 | Crear `user/application/port/out/`: UserRepositoryPort, RoleRepositoryPort | 2h | Puertos de salida |
| 1.7 | Crear `user/infrastructure/.../repository/`: JpaUserRepository, UserRepositoryAdapter | 4h | Adaptadores |
| 1.8 | Crear `user/infrastructure/.../specification/`: UserSpecifications (Criteria Builder) | 3h | Filtros dinámicos |
| 1.9 | Tests unitarios de dominio User (sin Spring) | 4h | UserTest.java |
| 1.10 | Tests integración repositorio (@DataJpaTest) | 4h | UserRepositoryAdapterIntegrationTest |

### Semana 2: Aplicación y REST (32h)

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 1.11 | Crear `security/`: JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig | 6h | Seguridad JWT |
| 1.12 | Crear `security/refresh/`: RefreshTokenEntity, RefreshTokenRepository, RefreshTokenService | 4h | Refresh tokens |
| 1.13 | Crear `user/application/port/in/`: RegisterUserUseCase, AuthenticateUserUseCase, etc. | 3h | Use Cases |
| 1.14 | Crear `user/application/service/`: UserService, AuthService, TeacherService | 5h | Servicios |
| 1.15 | Crear `user/application/dto/`: Commands y Queries | 2h | DTOs aplicación |
| 1.16 | Crear `user/infrastructure/adapter/in/rest/dto/`: Requests y Responses | 3h | DTOs REST |
| 1.17 | Crear `user/infrastructure/mapper/`: UserRestMapper | 2h | Mapper REST |
| 1.18 | Crear Controllers: AuthController, UserController, AdminController, TeacherController | 4h | Controladores |
| 1.19 | Tests unitarios de servicios (Mockito) | 3h | UserServiceTest, AuthServiceTest |

### Entregables Fase 1
- [ ] Módulo `user/` completo con arquitectura hexagonal
- [ ] Módulo `security/` con JWT funcionando
- [ ] Endpoints: `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`
- [ ] Endpoints: `/api/users/profile`, `/api/admin/teachers`
- [ ] Tests unitarios dominio (>80% cobertura)
- [ ] Tests integración repositorio
- [ ] Tests integración controllers

### Endpoints Fase 1

```
POST   /api/auth/register          # Registro de usuario
POST   /api/auth/login             # Login (retorna JWT + RefreshToken)
POST   /api/auth/refresh           # Renovar JWT
POST   /api/auth/logout            # Invalidar RefreshToken
GET    /api/users/profile          # Perfil del usuario autenticado
PUT    /api/users/profile          # Actualizar perfil
POST   /api/admin/teachers         # Crear profesor (ADMIN)
GET    /api/admin/teachers         # Listar profesores (ADMIN)
PUT    /api/admin/teachers/{id}    # Actualizar profesor (ADMIN)
DELETE /api/admin/teachers/{id}    # Eliminar profesor (ADMIN)
```

---

## 📚 FASE 2: Gestión Académica
**Duración:** 2 semanas (67 horas)  
**Objetivo:** Asignaturas, grupos y horarios

### Semana 3: Módulo Subject + Group (35h) ✅ COMPLETADO

| # | Tarea | Estado | Horas | Entregable |
|---|-------|--------|-------|------------|
| 2.1 | Crear `subject/domain/model/`: Subject, SubjectStatus, Degree | ✅ | 3h | Dominio Subject |
| 2.2 | Crear `subject/domain/exception/` y `validation/` | ✅ | 2h | Excepciones y reglas |
| 2.3 | Crear `subject/infrastructure/`: JPA entities, mappers, repository | ✅ | 5h | Infraestructura Subject |
| 2.4 | Crear `subject/application/`: ports, service, DTOs | ✅ | 4h | Aplicación Subject |
| 2.5 | Crear `subject/infrastructure/adapter/in/rest/`: Controller + DTOs | ✅ | 3h | REST Subject |
| 2.6 | Crear `group/domain/model/`: SubjectGroup, GroupStatus, GroupType | ✅ | 3h | Dominio Group |
| 2.7 | Implementar reglas: control de capacidad (24/50), sin límite de grupos | ✅ | 2h | Validaciones |
| 2.8 | Crear `group/infrastructure/`: JPA entities, mappers, repository | ✅ | 5h | Infraestructura Group |
| 2.9 | Crear `group/application/`: ports, service, DTOs | ✅ | 4h | Aplicación Group |
| 2.10 | Crear `group/infrastructure/adapter/in/rest/`: Controller + DTOs | ✅ | 3h | REST Group |
| 2.11 | Tests módulo Subject | ⏸️ | 2h | Tests (pendiente) |
| 2.12 | Tests módulo Group | ⏸️ | 2h | Tests (pendiente) |

**Decisiones de Diseño Tomadas:**
- ✅ Usar IDs (Long) para referencias entre agregados (subjectId, teacherId)
- ✅ Eliminada restricción de unicidad (subject_id, type) - Una asignatura PUEDE tener múltiples grupos del mismo tipo
- ✅ GroupType combina horario y período: REGULAR_Q1, INTENSIVE_Q1, REGULAR_Q2, INTENSIVE_Q2
- ✅ Capacidades: REGULAR max 24, INTENSIVE max 50
- ✅ DTOs como records (GroupFilters, CreateGroupCommand, etc.)
- ✅ Constructor parametrizado para records (NO builder pattern)

### Semana 4: Módulo Schedule (32h) ⏸️ PENDIENTE

| # | Tarea | Estado | Horas | Entregable |
|---|-------|--------|-------|------------|
| 2.13 | Crear `schedule/domain/model/`: Schedule, Classroom, DayOfWeek | ⏸️ | 3h | Dominio Schedule |
| 2.14 | Implementar validación de conflictos horarios | ⏸️ | 4h | ScheduleBusinessRules |
| 2.15 | Crear `schedule/domain/exception/`: ScheduleConflictException | ⏸️ | 2h | Excepciones |
| 2.16 | Crear `schedule/infrastructure/`: JPA entities, mappers, repository | ⏸️ | 5h | Infraestructura |
| 2.17 | Crear `schedule/application/`: ports, service, DTOs | ⏸️ | 4h | Aplicación |
| 2.18 | Crear `schedule/infrastructure/adapter/in/rest/`: Controller + DTOs | ⏸️ | 3h | REST |
| 2.19 | Crear Specifications para filtros avanzados (Subject, Group, Schedule) | ⏸️ | 4h | Filtros Criteria |
| 2.20 | Tests módulo Schedule | ⏸️ | 3h | Tests |
| 2.21 | Tests integración entre módulos (Subject-Group-Schedule) | ⏸️ | 4h | Tests integración |

### Entregables Fase 2
- [x] Módulo `subject/` completo (Domain, Application, Infrastructure, REST)
- [x] Módulo `group/` completo con validación de capacidad
- [ ] Módulo `schedule/` completo con detección de conflictos
- [x] Specifications con Criteria Builder funcionando (SubjectSpecifications, GroupSpecifications)
- [ ] Tests unitarios e integración (pendiente)

### Endpoints Fase 2

```
# Subjects
POST   /api/subjects               # Crear asignatura (ADMIN)
GET    /api/subjects               # Listar con filtros
GET    /api/subjects/{id}          # Obtener asignatura
PUT    /api/subjects/{id}          # Actualizar (ADMIN)
DELETE /api/subjects/{id}          # Eliminar (ADMIN)

# Groups
POST   /api/groups                 # Crear grupo (ADMIN)
GET    /api/groups                 # Listar con filtros
GET    /api/groups/{id}            # Obtener grupo con detalle
PUT    /api/groups/{id}            # Actualizar (ADMIN)
DELETE /api/groups/{id}            # Eliminar (ADMIN)

# Schedules
POST   /api/schedules              # Crear horario (ADMIN)
GET    /api/schedules              # Listar horarios
GET    /api/schedules/teacher/{id} # Horario de profesor
PUT    /api/schedules/{id}         # Actualizar (ADMIN)
DELETE /api/schedules/{id}         # Eliminar (ADMIN)
```

### Reglas de Negocio Fase 2

```java
// group/domain/model/SubjectGroup.java
public class SubjectGroup {
    public static final int REGULAR_MAX_CAPACITY = 24;
    public static final int INTENSIVE_MAX_CAPACITY = 50;

    // GroupType: REGULAR_Q1, INTENSIVE_Q1, REGULAR_Q2, INTENSIVE_Q2
    // Una asignatura PUEDE tener múltiples grupos del mismo tipo
    // Sin restricción de unicidad (subject_id, type)

    public int getMaxCapacity() {
        return capacity != null ? capacity
            : (isIntensive() ? INTENSIVE_MAX_CAPACITY : REGULAR_MAX_CAPACITY);
    }

    public boolean canEnroll() {
        return isOpen() && hasAvailableSeats();
    }
}
```

**Cambios respecto al plan original:**
- ❌ Eliminada restricción `MAX_GROUPS_PER_SUBJECT = 3`
- ✅ Una asignatura puede tener múltiples grupos del mismo tipo (sin límite)
- ✅ Capacidad customizable dentro de límites del tipo (24 para REGULAR, 50 para INTENSIVE)

---

## 🎓 FASE 3: Gestión de Sesiones
**Duración:** 1.5 semanas (38 horas)  
**Objetivo:** Control completo del ciclo de vida de sesiones

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 3.1 | Crear `session/domain/model/`: Session, SessionStatus, SessionType, SessionMode | 4h | Dominio Session |
| 3.2 | Implementar máquina de estados: PROGRAMADA → EN_CURSO → COMPLETADA/CANCELADA/POSPUESTA | 3h | Lógica de estados |
| 3.3 | Crear `session/domain/exception/`: InvalidSessionStateException, etc. | 2h | Excepciones |
| 3.4 | Crear `session/infrastructure/`: JPA entities, mappers, repository | 5h | Infraestructura |
| 3.5 | Crear `session/application/port/in/`: CreateSession, CancelSession, CompleteSession, PostponeSession, GenerateSessions | 3h | Use Cases |
| 3.6 | Crear `session/application/service/`: SessionService con lógica completa | 6h | Servicio |
| 3.7 | Implementar generación automática de sesiones desde Schedule | 4h | GenerateSessionsUseCase |
| 3.8 | Crear `session/infrastructure/adapter/in/rest/`: Controller + DTOs | 4h | REST |
| 3.9 | Crear SessionSpecifications para filtros por grupo, fecha, estado | 3h | Specifications |
| 3.10 | Tests unitarios SessionService | 3h | Tests |
| 3.11 | Tests integración | 2h | Tests |

### Entregables Fase 3
- [ ] Módulo `session/` completo
- [ ] Ciclo de vida de sesiones funcionando
- [ ] Generación automática desde horarios
- [ ] Tests completos

### Endpoints Fase 3

```
POST   /api/sessions                    # Crear sesión manual (ADMIN/TEACHER)
GET    /api/sessions                    # Listar con filtros
GET    /api/sessions/{id}               # Obtener sesión
PUT    /api/sessions/{id}               # Actualizar (ADMIN/TEACHER)
POST   /api/sessions/{id}/cancel        # Cancelar sesión
POST   /api/sessions/{id}/complete      # Marcar como completada
POST   /api/sessions/{id}/postpone      # Posponer sesión
POST   /api/sessions/generate           # Generar sesiones desde Schedule (ADMIN)
GET    /api/sessions/group/{groupId}    # Sesiones de un grupo
GET    /api/sessions/teacher/{teacherId} # Sesiones de un profesor
```

### Modelo de Estados Session

```
PROGRAMADA ──┬──► EN_CURSO ──► COMPLETADA
             │
             ├──► CANCELADA
             │
             └──► POSPUESTA ──► PROGRAMADA (nueva fecha)
```

---

## 📝 FASE 4: Inscripciones
**Duración:** 2 semanas (53 horas)  
**Objetivo:** Sistema completo de inscripciones, cola de espera y solicitudes de grupo

### Semana 5: Enrollment Core (28h)

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 4.1 | Crear `enrollment/domain/model/`: Enrollment, EnrollmentStatus, AttendanceMode | 4h | Dominio Enrollment |
| 4.2 | Implementar regla: estudiante con 2+ asignaturas puede asistir online | 3h | Regla de negocio |
| 4.3 | Crear `enrollment/domain/exception/`: AlreadyEnrolledException, PaymentRequiredException | 2h | Excepciones |
| 4.4 | Crear `enrollment/infrastructure/`: JPA entities, mappers, repository | 5h | Infraestructura |
| 4.5 | Crear `enrollment/application/port/in/`: EnrollStudent, WithdrawEnrollment, ChangeGroup | 3h | Use Cases |
| 4.6 | Crear `enrollment/application/service/`: EnrollmentService | 5h | Servicio principal |
| 4.7 | Implementar WaitingQueueService (cola FIFO automática) | 4h | Cola de espera |
| 4.8 | Tests unitarios EnrollmentService | 2h | Tests |

### Semana 6: GroupRequest + Integración (25h)

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 4.9 | Crear `enrollment/domain/model/`: GroupRequest, GroupRequestStatus | 3h | Dominio GroupRequest |
| 4.10 | Implementar regla: mínimo 8 apoyos para crear grupo | 2h | Validación |
| 4.11 | Crear `enrollment/infrastructure/`: GroupRequest JPA, mappers, repository | 4h | Infraestructura |
| 4.12 | Crear `enrollment/application/service/`: GroupRequestService | 4h | Servicio solicitudes |
| 4.13 | Crear Controllers: EnrollmentController, WaitingQueueController, GroupRequestController | 5h | REST |
| 4.14 | Crear EnrollmentSpecifications | 3h | Filtros |
| 4.15 | Tests integración módulo completo | 4h | Tests |

### Entregables Fase 4
- [ ] Módulo `enrollment/` completo
- [ ] Cola de espera automática funcionando
- [ ] Sistema de solicitudes de grupo nuevo
- [ ] Cambio de grupo entre grupos paralelos
- [ ] Tests completos

### Endpoints Fase 4

```
# Enrollments
POST   /api/enrollments                      # Inscribirse a grupo
GET    /api/enrollments                      # Listar inscripciones
GET    /api/enrollments/{id}                 # Obtener inscripción
DELETE /api/enrollments/{id}                 # Retirarse
PUT    /api/enrollments/{id}/change-group    # Cambiar de grupo

# Waiting Queue
GET    /api/waiting-queue/group/{groupId}    # Cola de un grupo
GET    /api/waiting-queue/student/{studentId} # Colas del estudiante
DELETE /api/waiting-queue/{id}               # Salir de cola

# Group Requests
POST   /api/group-requests                   # Crear solicitud
GET    /api/group-requests                   # Listar solicitudes
POST   /api/group-requests/{id}/support      # Apoyar solicitud
GET    /api/group-requests/{id}/supporters   # Ver apoyos
PUT    /api/group-requests/{id}/approve      # Aprobar (ADMIN)
PUT    /api/group-requests/{id}/reject       # Rechazar (ADMIN)
```

### Reglas de Negocio Fase 4

```java
// enrollment/domain/validation/EnrollmentBusinessRules.java
public class EnrollmentBusinessRules {
    public static final int MIN_SUBJECTS_FOR_ONLINE = 2;
    public static final int MIN_SUPPORTERS_FOR_NEW_GROUP = 8;
    
    public AttendanceMode determineAttendanceMode(
            SubjectGroup group, 
            int studentSubjectCount) {
        if (group.hasAvailableSeats()) {
            return AttendanceMode.PRESENCIAL;
        }
        if (studentSubjectCount >= MIN_SUBJECTS_FOR_ONLINE) {
            return AttendanceMode.ONLINE;
        }
        throw new NoSeatsAvailableException(group.getId());
    }
}
```

---

## ✅ FASE 5: Control de Asistencia
**Duración:** 1 semana (26 horas)  
**Objetivo:** Registro y estadísticas de asistencia

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 5.1 | Crear `attendance/domain/model/`: Attendance, AttendanceStatus | 3h | Dominio |
| 5.2 | Crear `attendance/domain/exception/`: AttendanceAlreadyRegisteredException | 2h | Excepciones |
| 5.3 | Crear `attendance/infrastructure/`: JPA entities, mappers, repository | 4h | Infraestructura |
| 5.4 | Crear `attendance/application/port/in/`: RegisterAttendance, GetStatistics | 2h | Use Cases |
| 5.5 | Crear `attendance/application/service/`: AttendanceService | 4h | Servicio |
| 5.6 | Implementar cálculo de estadísticas (% asistencia por estudiante/grupo) | 3h | Estadísticas |
| 5.7 | Crear AttendanceController + DTOs | 3h | REST |
| 5.8 | Crear AttendanceSpecifications | 2h | Filtros |
| 5.9 | Tests unitarios e integración | 3h | Tests |

### Entregables Fase 5
- [ ] Módulo `attendance/` completo
- [ ] Registro individual y masivo de asistencia
- [ ] Estadísticas por estudiante/grupo/período
- [ ] Tests completos

### Endpoints Fase 5

```
POST   /api/attendance                           # Registrar asistencia individual
POST   /api/attendance/bulk                      # Registrar asistencia masiva
GET    /api/attendance/session/{sessionId}       # Asistencia de una sesión
GET    /api/attendance/student/{studentId}       # Historial del estudiante
PUT    /api/attendance/{id}                      # Modificar registro
PUT    /api/attendance/{id}/justify              # Justificar ausencia
GET    /api/attendance/statistics/student/{id}   # Estadísticas estudiante
GET    /api/attendance/statistics/group/{id}     # Estadísticas grupo
```

---

## 📄 FASE 6: Materiales Educativos
**Duración:** 1 semana (28 horas)  
**Objetivo:** Gestión de archivos con control de acceso

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 6.1 | Crear `material/domain/model/`: Material, MaterialType (.pdf, .java, .cpp, .h) | 3h | Dominio |
| 6.2 | Crear `material/domain/exception/`: InvalidFileTypeException, UnauthorizedAccessException | 2h | Excepciones |
| 6.3 | Crear `material/application/port/out/`: FileStoragePort | 2h | Puerto almacenamiento |
| 6.4 | Crear `material/infrastructure/.../storage/`: LocalFileStorageAdapter | 4h | Adaptador local |
| 6.5 | Crear `material/infrastructure/`: JPA entities, mappers, repository | 4h | Infraestructura |
| 6.6 | Crear `material/application/service/`: MaterialService | 4h | Servicio |
| 6.7 | Implementar control de acceso (solo estudiantes al día con pagos) | 3h | Validación acceso |
| 6.8 | Crear MaterialController con upload/download | 3h | REST |
| 6.9 | Tests | 3h | Tests |

### Entregables Fase 6
- [ ] Módulo `material/` completo
- [ ] Upload/download de archivos funcionando
- [ ] Control de acceso basado en estado de pago
- [ ] Tests completos

### Endpoints Fase 6

```
POST   /api/materials/upload                # Subir material (ADMIN/TEACHER)
GET    /api/materials                       # Listar materiales
GET    /api/materials/{id}                  # Metadata del material
GET    /api/materials/{id}/download         # Descargar archivo
DELETE /api/materials/{id}                  # Eliminar (ADMIN/TEACHER)
GET    /api/materials/group/{groupId}       # Materiales de un grupo
GET    /api/materials/subject/{subjectId}   # Materiales de una asignatura
```

---

## 💳 FASE 7: Sistema de Pagos
**Duración:** 1.5 semanas (33 horas)  
**Objetivo:** Integración con Stripe y control de impagos

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 7.1 | Crear `payment/domain/model/`: Payment, PaymentStatus | 3h | Dominio |
| 7.2 | Implementar regla: bloqueo acceso tras 5 días de impago | 2h | Regla de negocio |
| 7.3 | Crear `payment/domain/exception/`: PaymentProcessingException, OverduePaymentException | 2h | Excepciones |
| 7.4 | Crear `payment/application/port/out/`: PaymentGatewayPort | 2h | Puerto de pago |
| 7.5 | Crear `payment/infrastructure/.../stripe/`: StripePaymentGatewayAdapter | 6h | Integración Stripe |
| 7.6 | Crear `payment/infrastructure/`: JPA entities, mappers, repository | 4h | Infraestructura |
| 7.7 | Crear `payment/application/service/`: PaymentService | 5h | Servicio |
| 7.8 | Crear PaymentController + StripeWebhookController | 4h | REST |
| 7.9 | Implementar job de verificación de impagos | 3h | Scheduled task |
| 7.10 | Tests | 2h | Tests |

### Entregables Fase 7
- [ ] Módulo `payment/` completo
- [ ] Integración Stripe funcionando (o stub para desarrollo)
- [ ] Bloqueo automático por impago
- [ ] Webhooks procesando eventos de Stripe
- [ ] Tests completos

### Endpoints Fase 7

```
POST   /api/payments                        # Crear intención de pago
GET    /api/payments                        # Listar pagos
GET    /api/payments/{id}                   # Obtener pago
GET    /api/payments/student/{studentId}    # Pagos del estudiante
GET    /api/payments/pending                # Pagos pendientes (ADMIN)
GET    /api/payments/overdue                # Pagos vencidos (ADMIN)
POST   /api/webhooks/stripe                 # Webhook de Stripe
```

### Reglas de Negocio Fase 7

```java
// payment/domain/validation/PaymentBusinessRules.java
public class PaymentBusinessRules {
    public static final int DAYS_BEFORE_BLOCK = 5;
    
    public boolean shouldBlockAccess(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDIENTE) {
            return false;
        }
        return payment.getDueDate()
            .plusDays(DAYS_BEFORE_BLOCK)
            .isBefore(LocalDate.now());
    }
}
```

---

## 🎒 FASE 8: Dashboard Estudiante + Integración
**Duración:** 1 semana (20 horas)  
**Objetivo:** Agregación de datos y módulo student

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 8.1 | Crear `student/application/service/`: StudentDashboardService | 4h | Servicio agregación |
| 8.2 | Crear DTOs de dashboard: StudentDashboardResponse, StudentProfileResponse | 2h | DTOs |
| 8.3 | Crear StudentController | 3h | Controller |
| 8.4 | Implementar endpoint de perfil completo del estudiante | 3h | Endpoint perfil |
| 8.5 | Verificar integración entre todos los módulos | 4h | Verificación |
| 8.6 | Tests de integración E2E de flujos principales | 4h | Tests E2E |

### Entregables Fase 8
- [ ] Módulo `student/` completo
- [ ] Dashboard con datos agregados funcionando
- [ ] Todos los módulos integrados correctamente

### Endpoints Fase 8

```
GET    /api/student/dashboard               # Dashboard completo
GET    /api/student/profile                 # Perfil del estudiante
GET    /api/student/enrollments             # Inscripciones activas
GET    /api/student/attendance              # Resumen de asistencia
GET    /api/student/payments                # Estado de pagos
GET    /api/student/materials               # Materiales accesibles
```

---

## 🧪 FASE 9: Testing y Calidad
**Duración:** 1 semana (25 horas)  
**Objetivo:** Cobertura >80% y tests de rendimiento

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 9.1 | Completar tests unitarios de dominio (todos los módulos) | 6h | Tests dominio |
| 9.2 | Completar tests de servicios con Mockito | 5h | Tests servicios |
| 9.3 | Completar tests de integración (@DataJpaTest) | 4h | Tests repositorio |
| 9.4 | Tests de integración de controllers (@SpringBootTest) | 4h | Tests controllers |
| 9.5 | Tests E2E de flujos críticos | 4h | Tests E2E |
| 9.6 | Verificar cobertura con JaCoCo (>80%) | 2h | Reporte cobertura |

### Entregables Fase 9
- [ ] Cobertura de tests >80%
- [ ] Todos los flujos críticos testeados
- [ ] Reporte de cobertura generado

### Estructura de Tests

```
src/test/java/com/acainfo/
├── user/
│   ├── domain/model/
│   │   ├── UserTest.java                    # Tests unitarios dominio
│   │   └── RoleTest.java
│   ├── application/service/
│   │   ├── UserServiceTest.java             # Tests con mocks
│   │   └── AuthServiceTest.java
│   └── infrastructure/
│       ├── adapter/in/rest/
│       │   └── UserControllerIntegrationTest.java
│       └── adapter/out/persistence/
│           └── UserRepositoryAdapterIntegrationTest.java
├── [resto de módulos con misma estructura...]
└── e2e/
    ├── EnrollmentFlowE2ETest.java
    ├── PaymentFlowE2ETest.java
    └── AttendanceFlowE2ETest.java
```

---

## 🚀 FASE 10: Documentación y Deployment
**Duración:** 1 semana (25 horas)  
**Objetivo:** Documentación completa y preparación para producción

### Tareas

| # | Tarea | Horas | Entregable |
|---|-------|-------|------------|
| 10.1 | Configurar SpringDoc OpenAPI completo | 3h | Swagger UI |
| 10.2 | Documentar todos los endpoints con anotaciones | 4h | API documentada |
| 10.3 | Crear script de migración de datos inicial | 3h | data.sql |
| 10.4 | Configurar Docker para producción | 3h | Dockerfile |
| 10.5 | Configurar GitHub Actions CI/CD | 4h | Pipeline CI/CD |
| 10.6 | Escribir README completo del proyecto | 2h | README.md |
| 10.7 | Documentar arquitectura y decisiones | 3h | ARCHITECTURE.md |
| 10.8 | Revisión de seguridad (OWASP básico) | 3h | Security checklist |

### Entregables Fase 10
- [ ] Swagger UI funcionando con todos los endpoints
- [ ] Pipeline CI/CD configurado
- [ ] Docker listo para producción
- [ ] Documentación completa
- [ ] README con instrucciones de setup

---

## 📊 Resumen de Fases

| Fase | Duración | Horas | Módulos/Entregables |
|------|----------|-------|---------------------|
| **0** | 1 semana | 15h | Setup, arquitectura, configuración |
| **1** | 2 semanas | 64h | `user/`, `security/` |
| **2** | 2 semanas | 67h | `subject/`, `group/`, `schedule/` |
| **3** | 1.5 semanas | 38h | `session/` |
| **4** | 2 semanas | 53h | `enrollment/` (inscripciones, cola, solicitudes) |
| **5** | 1 semana | 26h | `attendance/` |
| **6** | 1 semana | 28h | `material/` |
| **7** | 1.5 semanas | 33h | `payment/` |
| **8** | 1 semana | 20h | `student/`, integración |
| **9** | 1 semana | 25h | Testing completo |
| **10** | 1 semana | 25h | Documentación, deployment |
| **TOTAL** | **~13 semanas** | **~394h** | |

---

## ✅ Checklist de Verificación Arquitectónica

### Por cada módulo verificar:

**Dominio:**
- [ ] Entidades son POJOs puros sin anotaciones de framework
- [ ] Lógica de negocio está en las entidades de dominio
- [ ] No hay imports de Spring, JPA, Lombok en dominio
- [ ] Builder pattern implementado manualmente (sin Lombok)

**Aplicación:**
- [ ] Use cases definen contratos claros (interfaces)
- [ ] Servicios solo dependen de puertos (interfaces)
- [ ] DTOs de Command/Query separados
- [ ] Mappers de aplicación usan MapStruct

**Infraestructura:**
- [ ] Entidades JPA separadas con sufijo `*JpaEntity`
- [ ] Enums JPA separados con sufijo `*Jpa`
- [ ] Repository Adapters implementan puertos
- [ ] Specifications encapsulan Criteria Builder
- [ ] Mappers de persistencia (Domain ↔ JPA)
- [ ] Mappers REST (Domain ↔ DTO REST)
- [ ] Lombok solo en infraestructura

**Tests:**
- [ ] Tests unitarios para dominio (sin Spring)
- [ ] Tests unitarios para servicios (con Mockito)
- [ ] Tests de integración para repositorios (@DataJpaTest)
- [ ] Tests de integración para controllers (@SpringBootTest)

---

## 🔒 Seguridad Simplificada

```java
// user/domain/model/User.java
public class User {
    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserStatus status;
    private Set<Role> roles;
    
    // Métodos de seguridad (sin entidad Permission)
    public boolean isAdmin() {
        return hasRole(RoleType.ADMIN);
    }
    
    public boolean isTeacher() {
        return hasRole(RoleType.TEACHER);
    }
    
    public boolean isStudent() {
        return hasRole(RoleType.STUDENT);
    }
    
    private boolean hasRole(RoleType roleType) {
        return roles.stream()
            .anyMatch(role -> role.getType() == roleType);
    }
    
    // Métodos de conveniencia para reglas de negocio
    public boolean canManageGroups() {
        return isAdmin();
    }
    
    public boolean canRegisterAttendance() {
        return isAdmin() || isTeacher();
    }
    
    public boolean canUploadMaterials() {
        return isAdmin() || isTeacher();
    }
}
```

---

## 💡 Recomendaciones para Claude Code

### Secuencia de Generación por Módulo:

1. **Dominio primero:**
   ```
   "Genera la entidad de dominio User como POJO puro con builder pattern 
   manual, incluyendo métodos isAdmin(), isTeacher(), isStudent()"
   ```

2. **Excepciones de dominio:**
   ```
   "Genera UserNotFoundException que extienda DomainException"
   ```

3. **Entidad JPA:**
   ```
   "Genera UserJpaEntity con anotaciones JPA, Lombok y auditoría"
   ```

4. **Mappers:**
   ```
   "Genera UserPersistenceMapper con MapStruct para convertir 
   entre User (dominio) y UserJpaEntity"
   ```

5. **Puertos:**
   ```
   "Genera UserRepositoryPort con métodos save, findById, 
   findByEmail, findWithFilters"
   ```

6. **Adapter:**
   ```
   "Genera UserRepositoryAdapter que implemente UserRepositoryPort 
   usando JpaUserRepository y UserPersistenceMapper"
   ```

7. **Specifications:**
   ```
   "Genera UserSpecifications con Criteria Builder para filtrar 
   por email, status, roleType y searchTerm"
   ```

8. **Servicios:**
   ```
   "Genera UserService implementando los use cases, 
   usando UserRepositoryPort (no JPA directamente)"
   ```

9. **Controller + DTOs REST:**
   ```
   "Genera UserController con DTOs de request/response separados 
   y UserRestMapper"
   ```

10. **Tests:**
    ```
    "Genera tests unitarios para User (dominio, sin Spring) 
    y tests de integración para UserRepositoryAdapter (@DataJpaTest)"
    ```

---

## 📝 Notas Finales

Este plan está diseñado para ser implementado con **Claude Code Desktop** de manera incremental. Cada fase tiene entregables claros y verificables.

**Principios clave:**
- Arquitectura hexagonal pura con dominio aislado
- Módulos independientes y autocontenidos
- MapStruct para eliminar boilerplate de mapeo
- Tests en cada capa (unitarios + integración)
- Seguridad simplificada sin entidad Permission

**Factores de éxito:**
- Seguir la secuencia de generación por módulo
- Verificar el checklist arquitectónico en cada módulo
- Mantener el dominio libre de dependencias de framework
- Tests desde el día 1

---

*Documento preparado para desarrollo con Claude Code Desktop*  
*Arquitectura: Hexagonal Pura Modular*  
*Última actualización: Diciembre 2025*
