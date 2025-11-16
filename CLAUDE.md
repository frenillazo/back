# Plan de Desarrollo - Sistema de Gestión Centro de Formación
## Backend con Spring Boot + Claude Code

---

## 📋 Resumen Ejecutivo

**Proyecto:** Sistema de Gestión para Centro de Formación de Ingeniería
**Duración Total:** 14 semanas (3.5 meses)
**Stack Tecnológico:**
- Backend: Spring Boot 3.5.3, Java 21
- Base de Datos: PostgreSQL (producción), H2 (desarrollo)
- Arquitectura: Hexagonal
- Cache: Redis
- Mensajería: RabbitMQ
- Pagos: Stripe
- Videoconferencia: Zoom API
- Autenticación: Spring Security + JWT
- Contenedores: Docker
- CI/CD: GitHub Actions

**Capacidad del Sistema:**
- 300-400 alumnos activos por cuatrimestre
- Hasta 1000 usuarios concurrentes
- 2 aulas × 24 plazas = 48 plazas presenciales máximo
- 4 profesores (2 administradores)

---

## 🏗️ FASE 0: Setup y Arquitectura (Sprint 0)
**Duración:** 1 semana
**Objetivo:** Establecer base sólida del proyecto

### Tareas Técnicas:
1. **Configuración del Proyecto**
   - Inicializar proyecto Spring Boot con dependencias necesarias
   - Configurar perfiles (dev, test, prod)
   - Setup Docker Compose para servicios locales
   - Configurar H2 para desarrollo y tests

2. **Arquitectura Hexagonal**
   ```
   src/main/java/com/formacion/
   ├── application/
   │   ├── ports/in/      # Use cases
   │   ├── ports/out/     # Repository interfaces
   │   └── services/      # Application services
   ├── domain/
   │   ├── model/         # Entities & Value Objects
   │   ├── exception/     # Domain exceptions
   │   └── validation/    # Business rules
   ├── infrastructure/
   │   ├── adapters/in/   # Controllers REST
   │   ├── adapters/out/  # JPA, External APIs
   │   └── config/        # Spring configurations
   ```

3. **Base de Datos - Diseño Inicial**
   ```sql
   -- Entidades principales
   users, roles, user_roles
   subjects, groups, sessions
   enrollments, attendance
   materials, payments
   classrooms, schedules
   ```

4. **CI/CD Pipeline**
   - GitHub Actions para build automático
   - Tests en cada PR
   - SonarQube para calidad de código
   - Deployment automático a staging

### Entregables:
- Proyecto base configurado
- Docker Compose funcional
- Pipeline CI/CD básico
- Documentación de arquitectura

---

## 🔐 FASE 1: Core y Seguridad (Sprints 1-2)
**Duración:** 2 semanas
**Objetivo:** Sistema de usuarios y autenticación robusto

### Sprint 1: Modelo de Usuarios y Autenticación
**Historias de Usuario:**
1. Como usuario, quiero registrarme en el sistema
2. Como usuario, quiero autenticarme de forma segura
3. Como administrador, quiero gestionar roles de usuarios

**Tareas Técnicas:**
- Implementar entidades User, Role, Permission
- Spring Security + JWT configuration
- Endpoints de autenticación y registro
- Refresh token mechanism
- Rate limiting para endpoints críticos
- Auditoría de accesos (AuditLog entity)

**Modelo de Datos:**
```java
@Entity
public class User {
    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private Set<Role> roles;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

@Entity
public class Role {
    private Long id;
    private RoleType type; // ADMIN, TEACHER, STUDENT
    private Set<Permission> permissions;
}
```

### Sprint 2: Gestión de Profesores y Permisos
**Historias de Usuario:**
1. Como administrador, quiero dar de alta/baja profesores
2. Como profesor, quiero ver mi perfil y horarios
3. Como administrador, quiero asignar permisos específicos

**Tareas Técnicas:**
- CRUD de profesores con validaciones
- Sistema de permisos granular
- Gestión de sesiones activas
- Implementar @PreAuthorize annotations
- Tests de seguridad exhaustivos

**Endpoints principales:**
```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
GET    /api/users/profile
PUT    /api/users/profile
POST   /api/admin/teachers
DELETE /api/admin/teachers/{id}
GET    /api/teachers/schedule
```

---

## 📚 FASE 2: Gestión Académica (Sprints 3-4)
**Duración:** 2 semanas
**Objetivo:** Core académico del sistema

### Sprint 3: Asignaturas y Grupos
**Historias de Usuario:**
1. Como administrador, quiero gestionar asignaturas (CRUD)
2. Como administrador, quiero crear grupos para asignaturas
3. Como estudiante, quiero ver grupos disponibles con filtros

**Tareas Técnicas:**
- Entidades Subject, Group, AcademicPeriod
- Validaciones de negocio (máx 3 grupos por asignatura)
- Sistema de filtros avanzados
- Control de capacidad (24 plazas por aula)

**Modelo de Datos:**
```java
@Entity
public class Subject {
    private Long id;
    private String code;
    private String name;
    private Integer year; // 1-4
    private Degree degree; // INDUSTRIAL, INFORMATICA
    private Integer semester; // 1 o 2
    private SubjectStatus status;
    private List<Group> groups;
}

@Entity
public class Group {
    private Long id;
    private Subject subject;
    private GroupType type; // REGULAR, INTENSIVO
    private GroupStatus status; // ACTIVO, INACTIVO, COMPLETO
    private Integer maxCapacity;
    private Integer currentOccupancy;
    private Teacher teacher;
    private Classroom classroom;
    private Schedule schedule;
}
```

### Sprint 4: Gestión de Horarios y Aulas
**Historias de Usuario:**
1. Como administrador, quiero gestionar horarios sin conflictos
2. Como profesor, quiero consultar mi horario semanal
3. Como estudiante, quiero ver disponibilidad de aulas

**Tareas Técnicas:**
- Sistema de detección de conflictos horarios
- Algoritmo de asignación de aulas
- Vista de calendario semanal
- Validaciones complejas:
  - Profesor no puede estar en 2 lugares
  - Aula no puede tener 2 clases simultáneas
  - No solapar asignaturas del mismo curso

**Lógica de Validación:**
```java
@Service
public class ScheduleValidationService {
    public void validateSchedule(Schedule newSchedule) {
        checkTeacherAvailability(newSchedule);
        checkClassroomAvailability(newSchedule);
        checkStudentConflicts(newSchedule);
    }
}
```

---

## 📅 FASE 3: Gestión de Sesiones (Sprints 5-6)
**Duración:** 2 semanas
**Objetivo:** Control completo de sesiones y asistencia

### Sprint 5: Sesiones y Modalidades
**Historias de Usuario:**
1. Como profesor, quiero cambiar modalidad de sesión (presencial/dual/online)
2. Como profesor, quiero posponer una sesión
3. Como administrador, quiero programar recuperaciones

**Tareas Técnicas:**
- Entidad Session con estados complejos
- Sistema de notificaciones con RabbitMQ
- Gestión de cambios de última hora
- Histórico de modificaciones

**Modelo de Datos:**
```java
@Entity
public class Session {
    private Long id;
    private Group group;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private SessionMode mode; // PRESENCIAL, DUAL, ONLINE
    private SessionStatus status; // PROGRAMADA, POSPUESTA, CANCELADA, COMPLETADA
    private String cancellationReason;
    private Session recoverySession; // Para recuperaciones
}
```

### Sprint 6: Control de Asistencia
**Historias de Usuario:**
1. Como profesor, quiero registrar asistencia
2. Como estudiante, quiero consultar mi historial de asistencia
3. Como administrador, quiero ver estadísticas de asistencia

**Tareas Técnicas:**
- Sistema de registro de asistencia
- Cálculo automático de estadísticas
- Reportes de asistencia por período
- Integración con control de acceso a material

---

## 👥 FASE 4: Portal del Estudiante (Sprints 7-8)
**Duración:** 2 semanas
**Objetivo:** Funcionalidades completas para estudiantes

### Sprint 7: Inscripciones y Gestión de Grupos
**Historias de Usuario:**
1. Como estudiante, quiero inscribirme a grupos disponibles
2. Como estudiante, quiero cambiarme de grupo
3. Como estudiante, quiero solicitar creación de grupo nuevo
4. Como estudiante con 2+ asignaturas, quiero asistir online si no hay plaza

**Tareas Técnicas:**
- Sistema de inscripciones con validaciones
- Control de plazas en tiempo real
- Cola de espera automática
- Sistema de solicitudes (8 mínimo para grupo nuevo)
- Lógica de asistencia flexible entre grupos paralelos

**Reglas de Negocio:**
```java
@Service
public class EnrollmentService {
    public EnrollmentResult enroll(Student student, Group group) {
        // 1. Verificar grupo activo
        // 2. Verificar plazas disponibles
        // 3. Si no hay plazas y tiene 2+ asignaturas -> modo online
        // 4. Verificar pagos al día
        // 5. Crear inscripción
        // 6. Actualizar ocupación del grupo
        // 7. Notificar al estudiante
    }
}
```

### Sprint 8: Material y Recursos Educativos
**Historias de Usuario:**
1. Como estudiante inscrito, quiero acceder al material
2. Como profesor, quiero subir material (.pdf, .java, .cpp, .h)
3. Como estudiante con pagos pendientes, no puedo acceder al material

**Tareas Técnicas:**
- Sistema de gestión de archivos
- Control de acceso basado en pagos
- Almacenamiento local organizado
- Migración desde WhatsApp
- Versionado de materiales

---

## 💳 FASE 5: Sistema de Pagos (Sprint 9)
**Duración:** 1 semana
**Objetivo:** Gestión completa de pagos y facturación

### Sprint 9: Integración con Stripe
**Historias de Usuario:**
1. Como estudiante, quiero pagar mi cuota mensual
2. Como sistema, debo bloquear acceso si hay impago (5 días)
3. Como administrador, quiero ver estado de pagos

**Tareas Técnicas:**
- Integración Stripe API
- Webhooks para confirmación de pagos
- Sistema de facturación automática
- Cálculo de devoluciones proporcionales
- Bloqueo automático por impago

**Modelo de Pagos:**
```java
@Entity
public class Payment {
    private Long id;
    private Student student;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentType type; // MENSUAL, INTENSIVO
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String stripePaymentId;
}

@Service
public class PaymentService {
    @Scheduled(cron = "0 0 9 * * *") // Diario a las 9am
    public void checkOverduePayments() {
        // Bloquear acceso si > 5 días de retraso
    }
}
```

---

## 🔌 FASE 6: Integraciones (Sprint 10)
**Duración:** 1 semana
**Objetivo:** Conectar servicios externos

### Sprint 10: Zoom y Notificaciones
**Historias de Usuario:**
1. Como administrador, quiero crear reuniones Zoom para grupos nuevos
2. Como estudiante, quiero recibir notificaciones de cambios
3. Como profesor, quiero notificar cambios de modalidad

**Tareas Técnicas:**
- Integración Zoom API
- Sistema de notificaciones con RabbitMQ
- Templates de notificaciones
- Gestión de preferencias de notificación
- Logs de notificaciones enviadas

---

## 📊 FASE 7: Analytics y Reporting (Sprint 11)
**Duración:** 1 semana
**Objetivo:** Dashboard administrativo completo

### Sprint 11: Estadísticas y Reportes
**Historias de Usuario:**
1. Como administrador, quiero ver métricas del centro
2. Como profesor, quiero ver estadísticas de mis clases
3. Como administrador, quiero exportar reportes

**Métricas Clave:**
- Tasa de ocupación por asignatura/grupo
- Asistencia promedio
- Ingresos mensuales
- Tasa de impagos
- Solicitudes de grupos nuevos
- Utilización de aulas

**Implementación:**
```java
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    @GetMapping("/dashboard")
    public DashboardDTO getDashboard(
        @RequestParam LocalDate from,
        @RequestParam LocalDate to
    ) {
        // Agregación de métricas con consultas optimizadas
    }
}
```

---

## 🧪 FASE 8: Testing y Optimización (Sprint 12)
**Duración:** 1 semana
**Objetivo:** Asegurar calidad y rendimiento

### Sprint 12: Testing Completo
**Tareas:**
1. **Tests Unitarios (80% cobertura)**
   - Servicios de negocio
   - Validaciones
   - Utilidades

2. **Tests de Integración**
   - Controllers REST
   - Repositorios
   - Servicios externos

3. **Tests E2E**
   - Flujos críticos de usuario
   - Procesos de pago
   - Inscripciones

4. **Performance Testing**
   - Load testing con JMeter
   - Optimización de queries
   - Configuración de índices

5. **Security Testing**
   - Penetration testing básico
   - OWASP compliance check
   - GDPR/LOPD audit

---

## 🚀 FASE 9: Deployment (Sprint 13)
**Duración:** 1 semana
**Objetivo:** Puesta en producción

### Sprint 13: Deployment y Documentación
**Tareas:**
1. **Preparación de Producción**
   - Configuración PostgreSQL producción
   - Setup Redis cluster
   - Configuración RabbitMQ
   - SSL/TLS certificates

2. **Documentación**
   - API documentation (OpenAPI/Swagger)
   - Manual de administrador
   - Guía de usuario
   - Runbook de operaciones

3. **Migración de Datos**
   - Script migración desde sistema actual
   - Importación de usuarios existentes
   - Carga de históricos

4. **Monitoreo**
   - Configurar Prometheus + Grafana
   - Alertas críticas
   - Logs centralizados (ELK)

---

## 🔄 FASE 10: Estabilización (Sprint 14)
**Duración:** 1 semana
**Objetivo:** Refinamiento post-lanzamiento

### Sprint 14: Ajustes y Mejoras
- Corrección de bugs encontrados
- Ajustes de performance
- Mejoras de UX basadas en feedback
- Capacitación final a usuarios
- Preparación próxima iteración

---

## 📋 Consideraciones Técnicas Importantes

### 1. **Gestión de Transacciones**
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void processEnrollment(EnrollmentRequest request) {
    // Operaciones críticas con control de concurrencia
}
```

### 2. **Cache Strategy**
- Cache de grupos y horarios (Redis, TTL 5 min)
- Cache de material estático (Redis, TTL 1 hora)
- Invalidación selectiva en cambios

### 3. **Seguridad GDPR/LOPD**
- Encriptación de datos sensibles
- Right to be forgotten implementation
- Audit logs de acceso a datos personales
- Consent management

### 4. **Manejo de Concurrencia**
- Optimistic locking para inscripciones
- Pessimistic locking para pagos
- Control de plazas con Redis atomic operations

### 5. **Backup y Recovery**
- Backup diario automático
- Point-in-time recovery
- Disaster recovery plan

---

## 🎯 Métricas de Éxito del Proyecto

1. **Técnicas:**
   - Tiempo de respuesta < 200ms (p95)
   - Disponibilidad > 99.9%
   - Zero vulnerabilidades críticas
   - Cobertura de tests > 80%

2. **Negocio:**
   - Reducción 90% uso WhatsApp
   - Automatización 100% inscripciones
   - Reducción 50% tareas administrativas
   - Satisfacción usuarios > 4/5

---

## 🚦 Riesgos y Mitigaciones

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| Migración de datos compleja | Alta | Alto | Comenzar migración en Sprint 10, pruebas exhaustivas |
| Resistencia al cambio usuarios | Media | Medio | Capacitación progresiva, UI intuitiva |
| Integración Zoom falla | Baja | Alto | Plan B: Jitsi Meet como alternativa |
| Picos de carga en inscripciones | Alta | Medio | Auto-scaling, cache agresivo |
| Complejidad horarios | Media | Alto | Algoritmo validado exhaustivamente |

---

## 📅 Timeline Visual

```
Semana:  1  | 2-3  | 4-5  | 6-7  | 8-9  | 10 | 11 | 12 | 13 | 14
Sprint:  0  | 1-2  | 3-4  | 5-6  | 7-8  |  9 | 10 | 11 | 12 | 13-14
Fase:  Setup| Auth |Academic|Session|Student|Pay|Int|Stat|Test|Deploy
```

---

## 🛠️ Stack Tecnológico Detallado

### Backend
- **Framework:** Spring Boot 3.2.x
- **Java:** 17 LTS
- **Build:** Maven
- **ORM:** Spring Data JPA + Hibernate
- **Validación:** Bean Validation
- **Documentación:** SpringDoc OpenAPI

### Infraestructura
- **BD Principal:** PostgreSQL 15
- **BD Desarrollo:** H2
- **Cache:** Redis 7
- **Message Queue:** RabbitMQ
- **Contenedores:** Docker + Docker Compose
- **Reverse Proxy:** Nginx

### Integraciones
- **Pagos:** Stripe API
- **Video:** Zoom API
- **Almacenamiento:** Local FileSystem (futuro: S3)

### Herramientas de Desarrollo
- **IDE:** IntelliJ IDEA / VS Code
- **API Testing:** Postman / Insomnia
- **Version Control:** Git + GitHub
- **CI/CD:** GitHub Actions
- **Code Quality:** SonarQube
- **Monitoring:** Prometheus + Grafana

---

## 💡 Recomendaciones para Claude Code

### Estructura de Prompts Efectivos:
1. **Para generar entidades:**
   ```
   "Generate JPA entity for Student with validations, 
   including enrollment relationship, payment status, 
   and audit fields. Use Lombok annotations."
   ```

2. **Para servicios con lógica compleja:**
   ```
   "Create EnrollmentService with transactional method 
   to enroll student, checking: group capacity, payment 
   status, schedule conflicts. Include proper exception 
   handling and logging."
   ```

3. **Para tests:**
   ```
   "Generate comprehensive unit tests for PaymentService 
   including edge cases: overdues, refunds, stripe 
   webhook failures. Use Mockito and AssertJ."
   ```

### Mejores Prácticas:
- Pedir código por capas (entity → repository → service → controller)
- Solicitar tests junto con la implementación
- Especificar patrones de diseño cuando aplique
- Pedir validaciones de negocio explícitas
- Solicitar manejo de errores específico

---

## 📝 Notas Finales

Este plan está diseñado para ser implementado con Claude Code de manera eficiente. Cada sprint tiene entregables claros y puede ser desarrollado de forma incremental. La arquitectura hexagonal permite desarrollo paralelo de diferentes módulos sin conflictos.

**Próximos pasos recomendados:**
1. Validar el plan con stakeholders
2. Configurar el entorno de desarrollo
3. Comenzar con Sprint 0 inmediatamente
4. Establecer reuniones semanales de seguimiento
5. Preparar ambiente de staging desde Sprint 1

**Factores de éxito:**
- Comunicación constante con usuarios finales
- Testing continuo desde el día 1
- Documentación actualizada
- Despliegues incrementales a staging
- Feedback loops cortos

---

*Documento preparado para desarrollo con Spring Boot y Claude Code*
*Última actualización: Enero 2025*