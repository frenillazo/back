# Refactorización Módulo Subject - Arquitectura Hexagonal Pura
## Con Separación UseCase Interfaz/Implementación

**Fecha:** 22 Noviembre 2025
**Patrón:** UseCase Interface + UseCaseImpl

---

## 🎯 Nueva Estructura con UseCases Separados

### Cambio Arquitectónico

**ANTES:**
```
application/
├── ports/in/
│   └── CreateSubjectUseCase.java (interfaz)
└── services/
    └── SubjectService.java (implementa TODOS los use cases)
```

**DESPUÉS:**
```
application/
├── ports/in/
│   ├── CreateSubjectUseCase.java (interfaz)
│   ├── UpdateSubjectUseCase.java (interfaz)
│   ├── GetSubjectUseCase.java (interfaz)
│   └── DeleteSubjectUseCase.java (interfaz)
├── usecases/
│   ├── CreateSubjectUseCaseImpl.java (implementación)
│   ├── UpdateSubjectUseCaseImpl.java (implementación)
│   ├── GetSubjectUseCaseImpl.java (implementación)
│   └── DeleteSubjectUseCaseImpl.java (implementación)
├── ports/out/
│   └── SubjectRepositoryPort.java
└── mappers/
    └── SubjectDtoMapper.java
```

### Beneficios de esta Estructura

1. **Separación de Responsabilidades**: Cada use case tiene su propia clase
2. **Single Responsibility Principle**: Una clase, una responsabilidad
3. **Testabilidad**: Tests más enfocados por use case
4. **Claridad**: Fácil encontrar la lógica de cada operación
5. **Extensibilidad**: Fácil agregar nuevos use cases sin modificar existentes

---

## 📁 Estructura Completa del Módulo Subject

```
subject/
├── domain/
│   ├── model/
│   │   ├── Subject.java              ← POJO puro (NUEVO)
│   │   ├── Degree.java               ← Enum
│   │   └── SubjectStatus.java        ← Enum
│   ├── exception/
│   │   ├── SubjectNotFoundException.java
│   │   └── SubjectAlreadyExistsException.java
│   └── validation/
│       └── SubjectValidator.java
├── application/
│   ├── ports/
│   │   ├── in/
│   │   │   ├── CreateSubjectUseCase.java      ← Interfaz (NUEVO)
│   │   │   ├── UpdateSubjectUseCase.java      ← Interfaz (NUEVO)
│   │   │   ├── GetSubjectUseCase.java         ← Interfaz (NUEVO)
│   │   │   └── DeleteSubjectUseCase.java      ← Interfaz (NUEVO)
│   │   └── out/
│   │       └── SubjectRepositoryPort.java     ← Interfaz
│   ├── usecases/
│   │   ├── CreateSubjectUseCaseImpl.java      ← Implementación (NUEVO)
│   │   ├── UpdateSubjectUseCaseImpl.java      ← Implementación (NUEVO)
│   │   ├── GetSubjectUseCaseImpl.java         ← Implementación (NUEVO)
│   │   └── DeleteSubjectUseCaseImpl.java      ← Implementación (NUEVO)
│   └── mappers/
│       └── SubjectDtoMapper.java              ← NUEVO
└── infrastructure/
    └── adapters/
        ├── in/
        │   ├── rest/
        │   │   └── SubjectController.java     ← REFACTORIZAR
        │   └── dto/
        │       ├── CreateSubjectRequest.java
        │       ├── UpdateSubjectRequest.java
        │       ├── SubjectResponse.java
        │       └── SubjectFilterDTO.java
        └── out/
            └── persistence/
                ├── entities/
                │   └── SubjectJpaEntity.java  ← NUEVO
                ├── mappers/
                │   └── SubjectJpaMapper.java  ← NUEVO
                ├── repositories/
                │   └── SubjectJpaRepository.java ← RENOMBRAR
                └── adapters/
                    └── SubjectRepositoryAdapter.java ← REFACTORIZAR
```

---

## 📋 Archivos a Crear/Modificar

### Archivos NUEVOS (14):

**Domain (1):**
1. `Subject.java` (POJO puro - reemplaza @Entity)

**Application - Ports IN (4):**
2. `CreateSubjectUseCase.java` (interfaz)
3. `UpdateSubjectUseCase.java` (interfaz)
4. `GetSubjectUseCase.java` (interfaz)
5. `DeleteSubjectUseCase.java` (interfaz)

**Application - UseCases (4):**
6. `CreateSubjectUseCaseImpl.java`
7. `UpdateSubjectUseCaseImpl.java`
8. `GetSubjectUseCaseImpl.java`
9. `DeleteSubjectUseCaseImpl.java`

**Application - Mappers (1):**
10. `SubjectDtoMapper.java`

**Infrastructure - Persistence (3):**
11. `SubjectJpaEntity.java`
12. `SubjectJpaMapper.java`
13. `SubjectJpaRepository.java` (renombrado de SubjectRepository)

**Infrastructure - Adapters (1):**
14. `SubjectRepositoryAdapter.java` (refactorizado)

### Archivos a MODIFICAR (1):
1. `SubjectController.java`

### Archivos a ELIMINAR (2):
1. `Subject.java` (@Entity antiguo)
2. `SubjectService.java` (reemplazado por UseCaseImpl)

---

## 🔧 Orden de Implementación

### FASE 1: Domain Layer
1. Crear `Subject.java` (POJO puro)
2. Verificar enums (Degree, SubjectStatus)
3. Verificar excepciones

### FASE 2: Infrastructure - Persistence
4. Crear `SubjectJpaEntity.java`
5. Crear `SubjectJpaMapper.java`
6. Renombrar `SubjectRepository` → `SubjectJpaRepository`
7. Refactorizar `SubjectRepositoryAdapter`

### FASE 3: Application - Ports
8. Crear interfaces de Use Cases (4 archivos)

### FASE 4: Application - UseCases
9. Crear `SubjectDtoMapper.java`
10. Crear `CreateSubjectUseCaseImpl.java`
11. Crear `UpdateSubjectUseCaseImpl.java`
12. Crear `GetSubjectUseCaseImpl.java`
13. Crear `DeleteSubjectUseCaseImpl.java`

### FASE 5: Infrastructure - REST
14. Refactorizar `SubjectController.java`

### FASE 6: Cleanup
15. Eliminar `SubjectService.java`
16. Eliminar `Subject.java` (@Entity antiguo)

### FASE 7: Testing
17. Tests unitarios de domain
18. Tests de mappers
19. Tests de use cases
20. Tests de controller

---

## ⏱️ Estimación

- **Tiempo Total:** 16 horas
- **Complejidad:** ⭐⭐⭐ (Media)

**Desglose:**
- FASE 1 (Domain): 2h
- FASE 2 (Infrastructure): 3h
- FASE 3 (Ports): 1h
- FASE 4 (UseCases): 4h
- FASE 5 (Controller): 2h
- FASE 6 (Cleanup): 1h
- FASE 7 (Testing): 3h

---

## ✅ Checklist de Validación

### Domain Layer
- [ ] Subject.java es POJO puro (sin @Entity)
- [ ] Builder pattern implementado
- [ ] Validaciones en build()
- [ ] Lógica de negocio (activate, deactivate, archive)
- [ ] Tests unitarios de domain

### Infrastructure Layer - Persistence
- [ ] SubjectJpaEntity solo tiene anotaciones JPA
- [ ] SubjectJpaMapper convierte JPA ↔ Domain
- [ ] SubjectJpaRepository trabaja con JpaEntity
- [ ] SubjectRepositoryAdapter usa mapper
- [ ] Tests de mapper
- [ ] Tests de adapter

### Application Layer - Ports
- [ ] Interfaces de Use Cases definen contratos claros
- [ ] JavaDoc en cada método
- [ ] Excepciones documentadas

### Application Layer - UseCases
- [ ] Cada UseCase tiene su implementación
- [ ] @Service annotation
- [ ] Usa SubjectRepositoryPort (no repositorio directo)
- [ ] Usa SubjectDtoMapper
- [ ] Lógica de negocio correcta
- [ ] Tests unitarios con mocks

### Infrastructure Layer - REST
- [ ] Controller inyecta use cases (no servicio)
- [ ] Controller usa SubjectDtoMapper
- [ ] No imports de entidades JPA
- [ ] Tests de integración

### Cleanup
- [ ] SubjectService.java eliminado
- [ ] Subject.java (@Entity) eliminado
- [ ] No quedan referencias a clases eliminadas
- [ ] Build exitoso
- [ ] Todos los tests pasan

---

*Documento de refactorización actualizado - 22 Noviembre 2025*
