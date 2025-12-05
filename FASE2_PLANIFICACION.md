# FASE 2: Gestión Académica - PLANIFICACIÓN

**Estado:** 🚧 EN PROGRESO - Subject y Group COMPLETADOS
**Duración:** 2 semanas (67 horas)
**Fecha de inicio:** Diciembre 2024

---

## 📋 Resumen Ejecutivo

La Fase 2 implementará los módulos de gestión académica del sistema: **Subject (Asignaturas)**, **Group (Grupos)** y **Schedule (Horarios)**, siguiendo la arquitectura hexagonal pura establecida en la Fase 1.

### Objetivos de la Fase 2

🎯 ✅ Implementar módulo Subject con CRUD completo
🎯 ✅ Implementar módulo Group con validación de capacidad (24 para REGULAR, 50 para INTENSIVE)
🎯 ⏸️ Implementar módulo Schedule con detección de conflictos horarios
🎯 ✅ Integrar Subject y Group con relaciones por IDs (DDD purismo)
🎯 ⏸️ Tests unitarios e integración para cada módulo
🎯 ✅ API REST completa con filtros avanzados (Specifications)

---

## 🔧 Decisiones de Diseño Tomadas

### 1. Referencias entre Agregados (DDD Purismo)

**Decisión:** Usar **IDs (Long)** en lugar de entidades completas para mantener independencia de agregados.

```java
// ✅ IMPLEMENTADO
public class SubjectGroup {
    private Long subjectId;  // ID, no Subject entity
    private Long teacherId;  // ID, no User entity
}
```

**Razones:**
- Mantiene independencia entre agregados
- Evita problemas de lazy loading
- Facilita mapeo entre capas
- Simplifica transacciones

### 2. Records para DTOs

**Decisión:** Los DTOs de aplicación (`*Command`, `*Filters`) son **records** inmutables.

```java
// ✅ IMPLEMENTADO
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

**IMPORTANTE:** Los records NO soportan `@Builder`. Usar constructor parametrizado.

### 3. Regla de Negocio: Grupos por Asignatura

**Decisión CORREGIDA:** Una asignatura **puede tener múltiples grupos del mismo tipo** (sin límite).

```java
// ✅ VÁLIDO
Subject: "Programación I" (id=1)
  ├─ Group 1: REGULAR_Q1, Teacher A, Capacity 24
  ├─ Group 2: REGULAR_Q1, Teacher B, Capacity 24
  └─ Group 3: INTENSIVE_Q1, Teacher C, Capacity 50
```

**Cambios respecto al plan original:**
- ❌ Eliminada restricción `MAX_GROUPS_PER_SUBJECT = 3`
- ✅ Sin restricción de unicidad (subject_id, type)
- ✅ Múltiples grupos paralelos del mismo tipo permitidos

### 4. GroupType y Capacidades

**Decisión:** GroupType combina horario (REGULAR/INTENSIVE) y período (Q1/Q2).

```java
public enum GroupType {
    REGULAR_Q1,    // Regular primer cuatrimestre (max 24)
    INTENSIVE_Q1,  // Intensivo primer cuatrimestre (max 50)
    REGULAR_Q2,    // Regular segundo cuatrimestre (max 24)
    INTENSIVE_Q2;  // Intensivo segundo cuatrimestre (max 50)
}
```

**Capacidades:**
- `REGULAR`: Max 24 estudiantes (capacidad del aula física)
- `INTENSIVE`: Max 50 estudiantes (mayor flexibilidad)
- Custom capacity: Permitido dentro de los límites del tipo

---

## 📊 Progreso de Hitos

### ✅ MÓDULO SUBJECT (COMPLETADO)

**HITO 2.1: Subject - Dominio y Aplicación** ✅
- [x] Entidades de dominio (Subject, SubjectStatus, Degree)
- [x] Excepciones de dominio
- [x] Validaciones de negocio
- [x] Use Cases (interfaces)
- [x] DTOs (CreateSubjectCommand, UpdateSubjectCommand, SubjectFilters)
- [x] Repository Port
- [ ] Tests unitarios de dominio (pendiente)

**HITO 2.2: Subject - Infraestructura JPA** ✅
- [x] SubjectJpaEntity con auditoría
- [x] SubjectPersistenceMapper (MapStruct)
- [x] JpaSubjectRepository
- [x] SubjectRepositoryAdapter
- [x] SubjectSpecifications (Criteria Builder)
- [ ] Tests de integración repositorio (pendiente)

**HITO 2.3: Subject - REST API y Servicio** ✅
- [x] SubjectService implementado
- [x] DTOs REST (SubjectRequest, UpdateSubjectRequest, SubjectResponse)
- [x] SubjectRestMapper (MapStruct)
- [x] SubjectController con 7 endpoints
- [x] Documentación OpenAPI
- [ ] Tests unitarios servicio (pendiente)

**Endpoints Subject:**
```
POST   /api/subjects               # Crear asignatura (ADMIN)
GET    /api/subjects               # Listar con filtros
GET    /api/subjects/{id}          # Obtener asignatura
GET    /api/subjects/code/{code}   # Obtener por código
PUT    /api/subjects/{id}          # Actualizar (ADMIN)
DELETE /api/subjects/{id}          # Eliminar (ADMIN)
PUT    /api/subjects/{id}/archive  # Archivar (ADMIN)
```

---

### ✅ MÓDULO GROUP (COMPLETADO)

**HITO 2.4: Group - Dominio y Aplicación** ✅
- [x] Entidades de dominio (SubjectGroup, GroupStatus, GroupType)
- [x] Excepciones de dominio (GroupNotFoundException, etc.)
- [x] Validaciones: capacidad (24/50), sin límite de grupos
- [x] Use Cases (CreateGroupUseCase, UpdateGroupUseCase, etc.)
- [x] DTOs (CreateGroupCommand, UpdateGroupCommand, GroupFilters)
- [x] Repository Port
- [ ] Tests unitarios dominio (pendiente)

**HITO 2.5: Group - Infraestructura JPA** ✅
- [x] SubjectGroupJpaEntity con índices (sin restricción de unicidad)
- [x] GroupPersistenceMapper (MapStruct)
- [x] JpaGroupRepository
- [x] GroupRepositoryAdapter
- [x] GroupSpecifications (Criteria Builder)
- [x] GroupService con validaciones completas
- [ ] Tests de integración (pendiente)

**HITO 2.6: Group - REST API** ✅
- [x] DTOs REST (CreateGroupRequest, UpdateGroupRequest, GroupResponse)
- [x] GroupRestMapper (MapStruct)
- [x] GroupController con endpoints CRUD
- [x] Constructor parametrizado para records (corregido)
- [ ] Tests unitarios servicio (pendiente)

**Endpoints Group:**
```
POST   /api/groups              # Crear grupo (ADMIN)
GET    /api/groups/{id}         # Obtener grupo
GET    /api/groups              # Listar con filtros
PUT    /api/groups/{id}         # Actualizar (ADMIN)
DELETE /api/groups/{id}         # Eliminar (ADMIN)
POST   /api/groups/{id}/cancel  # Cancelar grupo (ADMIN)
```

**Validaciones Implementadas en GroupService:**
- ✅ Verificar que subject existe
- ✅ Verificar que teacher existe y tiene rol TEACHER o ADMIN
- ❌ NO verificar límite de grupos (restricción eliminada)
- ✅ Validar capacidad dentro de límites del tipo (24 o 50)
- ✅ Sincronizar Subject.currentGroupCount al crear/eliminar
- ✅ Impedir eliminación de grupos con enrollments

---

### ⏸️ MÓDULO SCHEDULE (PENDIENTE)

**HITO 2.7: Schedule - Dominio y Aplicación** ⏸️
- [ ] Entidades de dominio (Schedule, Classroom)
- [ ] Lógica de detección de conflictos
- [ ] Excepciones de dominio
- [ ] Use Cases
- [ ] DTOs y Repository Port

**HITO 2.8: Schedule - Infraestructura JPA** ⏸️
- [ ] Entidades JPA con relaciones
- [ ] Mappers
- [ ] Repository adapter
- [ ] Specifications

**HITO 2.9: Schedule - REST API y Servicio** ⏸️
- [ ] Service con validación de conflictos
- [ ] DTOs REST
- [ ] Controller
- [ ] Tests

**HITO 2.10: Integración y Tests E2E** ⏸️
- [ ] Tests de integración entre módulos
- [ ] Tests E2E de flujos completos
- [ ] Validación de Specifications

---

## 📈 Estado Actual

### ✅ Completado (Subject + Group)

**Dominio:**
- 6 entidades de dominio (Subject, SubjectStatus, Degree, SubjectGroup, GroupStatus, GroupType)
- 6+ excepciones de dominio
- Validaciones de negocio implementadas
- Referencias entre agregados por IDs (DDD purismo)

**Aplicación:**
- 8 Use Cases definidos
- 6 DTOs de Command/Query (records inmutables)
- 2 Repository Ports
- 2 Services implementados con validaciones

**Infraestructura:**
- 2 Entidades JPA con auditoría
- 4 Mappers MapStruct (Persistence + REST)
- 2 Repository Adapters
- 2 Specifications con Criteria Builder
- 2 Controllers REST con 13 endpoints totales
- Documentación OpenAPI

### ⏸️ Pendiente

**Módulo Schedule:**
- Dominio completo
- Infraestructura JPA
- REST API
- Lógica de detección de conflictos

**Tests:**
- Tests unitarios de dominio
- Tests unitarios de servicios
- Tests de integración de repositorios
- Tests de integración de controllers
- Tests E2E entre módulos

---

## 🔍 Lecciones Aprendidas

### 1. Flexibilidad en Reglas de Negocio

**Cambio:** Eliminada restricción `MAX_GROUPS_PER_SUBJECT = 3`

**Razón:** Permitir múltiples grupos paralelos del mismo tipo para alta demanda de estudiantes.

### 2. Records vs Builder Pattern

**Problema:** Records no soportan `@Builder` de Lombok.

**Solución:** Usar constructor parametrizado (patrón idiomático para records).

```java
// ❌ INCORRECTO
GroupFilters.builder()...build()

// ✅ CORRECTO
new GroupFilters(subjectId, teacherId, ...)
```

### 3. IDs vs Entidades en Agregados

**Decisión:** Usar IDs para mantener independencia de agregados.

**Beneficios:**
- Sin lazy loading issues
- Mapeo más simple
- Transacciones más claras

---

## 📚 Próximos Pasos

1. **Completar Módulo Schedule** (Semana 4)
   - Crear dominio con detección de conflictos
   - Implementar infraestructura JPA
   - Crear REST API

2. **Implementar Tests** (Fase 9)
   - Tests unitarios de dominio
   - Tests de servicios con Mockito
   - Tests de integración

3. **Fase 3: Módulo Session**
   - Gestión de sesiones individuales
   - Generación automática desde Schedule
   - Ciclo de vida: PROGRAMADA → EN_CURSO → COMPLETADA

---

## ✅ Checklist de Verificación

### Arquitectura Hexagonal ✅
- [x] Dominio sin dependencias de framework
- [x] Use Cases como interfaces
- [x] DTOs separados por capa (Application vs REST)
- [x] Entidades JPA con sufijo *JpaEntity
- [x] Mappers MapStruct funcionando
- [x] Repository Adapters implementando puertos
- [x] Specifications con Criteria Builder

### Decisiones de Diseño ✅
- [x] Referencias por IDs entre agregados
- [x] DTOs como records inmutables
- [x] Constructor parametrizado (no builder)
- [x] Sin restricción de unicidad subject+type
- [x] Capacidades: REGULAR=24, INTENSIVE=50

---

**Fase 2: 2/3 módulos completados** 🚧
**Arquitectura hexagonal pura mantenida** ✅

*Última actualización: Diciembre 2024*
