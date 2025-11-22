# Ejemplo Completo de Migración - Módulo Subject
## De Hexagonal Pragmática a Hexagonal Pura

**Módulo:** Subject (Asignaturas)
**Complejidad:** ⭐⭐⭐ (Media)
**Esfuerzo:** 16 horas

---

## 📋 Índice

1. [Estado Actual](#estado-actual)
2. [Estado Objetivo](#estado-objetivo)
3. [Paso a Paso de Migración](#paso-a-paso-de-migración)
4. [Código Completo](#código-completo)
5. [Tests](#tests)
6. [Validación](#validación)

---

## 🔍 Estado Actual

### Estructura Actual

```
subject/
├── domain/
│   ├── model/
│   │   ├── Subject.java              ← @Entity JPA (PROBLEMA)
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
│   │   │   ├── CreateSubjectUseCase.java
│   │   │   ├── UpdateSubjectUseCase.java
│   │   │   ├── GetSubjectUseCase.java
│   │   │   └── DeleteSubjectUseCase.java
│   │   └── out/
│   │       └── SubjectRepositoryPort.java
│   └── services/
│       └── SubjectService.java       ← Usa Subject (@Entity)
└── infrastructure/
    └── adapters/
        ├── in/
        │   ├── rest/
        │   │   └── SubjectController.java
        │   └── dto/
        │       ├── CreateSubjectRequest.java
        │       ├── UpdateSubjectRequest.java
        │       ├── SubjectResponse.java
        │       └── SubjectFilterDTO.java
        └── out/
            ├── SubjectRepository.java    ← Spring Data JPA
            └── SubjectRepositoryAdapter.java ← Ya existe! (bien)
```

### Problema Principal: Subject.java (Actual)

```java
package acainfo.back.subject.domain.model;

import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import jakarta.persistence.*;  // ❌ PROBLEMA: JPA en domain
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity  // ❌ PROBLEMA
@Table(name = "subjects", indexes = {
    @Index(name = "idx_subject_code", columnList = "code", unique = true),
    @Index(name = "idx_subject_year_degree_semester", columnList = "year,degree,semester")
})
@EntityListeners(AuditingEntityListener.class)  // ❌ PROBLEMA
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id  // ❌ PROBLEMA
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer year; // 1-4

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Degree degree;

    @Column(nullable = false)
    private Integer semester; // 1 o 2

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubjectStatus status = SubjectStatus.ACTIVO;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)  // ❌ PROBLEMA
    @Builder.Default
    private List<SubjectGroup> subjectGroups = new ArrayList<>();

    @CreatedDate  // ❌ PROBLEMA
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate  // ❌ PROBLEMA
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== LÓGICA DE NEGOCIO (está bien aquí) =====

    public boolean isActive() {
        return status == SubjectStatus.ACTIVO;
    }

    public void activate() {
        if (isActive()) {
            throw new IllegalStateException("Subject is already active");
        }
        this.status = SubjectStatus.ACTIVO;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!isActive()) {
            throw new IllegalStateException("Subject is already inactive");
        }
        this.status = SubjectStatus.INACTIVO;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = SubjectStatus.ARCHIVADO;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canBeDeleted() {
        return subjectGroups.isEmpty();
    }
}
```

**Problemas identificados:**
1. ❌ Anotaciones JPA (@Entity, @Table, @Column, etc.) en domain
2. ❌ Dependencia de jakarta.persistence
3. ❌ Dependencia de Spring Data (@CreatedDate, @LastModifiedDate)
4. ❌ Lombok @Data genera setters (mutabilidad no controlada)
5. ✅ Lógica de negocio bien ubicada (isActive(), activate(), etc.)

---

## 🎯 Estado Objetivo

### Estructura Objetivo

```
subject/
├── domain/
│   ├── model/
│   │   ├── Subject.java              ← POJO puro (NUEVO)
│   │   ├── Degree.java               ← Enum (sin cambios)
│   │   └── SubjectStatus.java        ← Enum (sin cambios)
│   ├── exception/
│   │   ├── SubjectNotFoundException.java (sin cambios)
│   │   └── SubjectAlreadyExistsException.java (sin cambios)
│   └── validation/
│       └── SubjectValidator.java (sin cambios)
├── application/
│   ├── ports/
│   │   ├── in/
│   │   │   ├── CreateSubjectUseCase.java (sin cambios)
│   │   │   ├── UpdateSubjectUseCase.java (sin cambios)
│   │   │   ├── GetSubjectUseCase.java (sin cambios)
│   │   │   └── DeleteSubjectUseCase.java (sin cambios)
│   │   └── out/
│   │       └── SubjectRepositoryPort.java (sin cambios)
│   ├── services/
│   │   └── SubjectService.java       ← REFACTORIZAR (usar Subject domain)
│   └── mappers/
│       └── SubjectDtoMapper.java     ← NUEVO
└── infrastructure/
    └── adapters/
        ├── in/
        │   ├── rest/
        │   │   └── SubjectController.java ← REFACTORIZAR
        │   └── dto/ (sin cambios)
        └── out/
            └── persistence/
                ├── entities/
                │   └── SubjectJpaEntity.java ← NUEVO
                ├── mappers/
                │   └── SubjectJpaMapper.java ← NUEVO
                ├── repositories/
                │   └── SubjectJpaRepository.java ← RENOMBRAR (era SubjectRepository)
                └── adapters/
                    └── SubjectRepositoryAdapter.java ← REFACTORIZAR
```

---

## 🔧 Paso a Paso de Migración

### PASO 1: Crear Subject.java (Domain - POJO puro)

**Ubicación:** `src/main/java/acainfo/back/subject/domain/model/Subject.java`

**Cambios:**
- Eliminar TODAS las anotaciones JPA
- Implementar Builder pattern manual
- Mantener lógica de negocio
- Sin imports de jakarta.persistence o Spring

```java
package acainfo.back.subject.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio pura - Subject
 * Sin anotaciones de framework
 * Solo lógica de negocio
 */
public class Subject {

    // ===== CAMPOS =====

    private Long id;
    private String code;
    private String name;
    private Integer year; // 1-4
    private Degree degree;
    private Integer semester; // 1 o 2
    private SubjectStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== CONSTRUCTOR PRIVADO =====

    private Subject() {
        // Solo accesible desde Builder
    }

    // ===== BUILDER =====

    public static SubjectBuilder builder() {
        return new SubjectBuilder();
    }

    // ===== LÓGICA DE NEGOCIO =====

    public boolean isActive() {
        return status == SubjectStatus.ACTIVO;
    }

    public void activate() {
        if (isActive()) {
            throw new IllegalStateException("Subject is already active");
        }
        this.status = SubjectStatus.ACTIVO;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (!isActive()) {
            throw new IllegalStateException("Subject is already inactive");
        }
        this.status = SubjectStatus.INACTIVO;
        this.updatedAt = LocalDateTime.now();
    }

    public void archive() {
        this.status = SubjectStatus.ARCHIVADO;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean belongsToYear(Integer year) {
        return this.year.equals(year);
    }

    public boolean belongsToDegree(Degree degree) {
        return this.degree == degree;
    }

    public boolean belongsToSemester(Integer semester) {
        return this.semester.equals(semester);
    }

    // ===== GETTERS (SIN SETTERS - Inmutabilidad) =====

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getYear() {
        return year;
    }

    public Degree getDegree() {
        return degree;
    }

    public Integer getSemester() {
        return semester;
    }

    public SubjectStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ===== EQUALS & HASHCODE =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subject subject = (Subject) o;
        return Objects.equals(id, subject.id) &&
               Objects.equals(code, subject.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", degree=" + degree +
                ", semester=" + semester +
                ", status=" + status +
                '}';
    }

    // ===== BUILDER CLASS =====

    public static class SubjectBuilder {
        private Subject subject = new Subject();

        public SubjectBuilder id(Long id) {
            subject.id = id;
            return this;
        }

        public SubjectBuilder code(String code) {
            subject.code = code;
            return this;
        }

        public SubjectBuilder name(String name) {
            subject.name = name;
            return this;
        }

        public SubjectBuilder year(Integer year) {
            subject.year = year;
            return this;
        }

        public SubjectBuilder degree(Degree degree) {
            subject.degree = degree;
            return this;
        }

        public SubjectBuilder semester(Integer semester) {
            subject.semester = semester;
            return this;
        }

        public SubjectBuilder status(SubjectStatus status) {
            subject.status = status;
            return this;
        }

        public SubjectBuilder description(String description) {
            subject.description = description;
            return this;
        }

        public SubjectBuilder createdAt(LocalDateTime createdAt) {
            subject.createdAt = createdAt;
            return this;
        }

        public SubjectBuilder updatedAt(LocalDateTime updatedAt) {
            subject.updatedAt = updatedAt;
            return this;
        }

        public Subject build() {
            validate();
            return subject;
        }

        private void validate() {
            if (subject.code == null || subject.code.isBlank()) {
                throw new IllegalArgumentException("Subject code is required");
            }
            if (subject.name == null || subject.name.isBlank()) {
                throw new IllegalArgumentException("Subject name is required");
            }
            if (subject.year == null || subject.year < 1 || subject.year > 4) {
                throw new IllegalArgumentException("Subject year must be between 1 and 4");
            }
            if (subject.degree == null) {
                throw new IllegalArgumentException("Subject degree is required");
            }
            if (subject.semester == null || (subject.semester != 1 && subject.semester != 2)) {
                throw new IllegalArgumentException("Subject semester must be 1 or 2");
            }
            if (subject.status == null) {
                subject.status = SubjectStatus.ACTIVO;
            }
            if (subject.createdAt == null) {
                subject.createdAt = LocalDateTime.now();
            }
        }
    }
}
```

---

### PASO 2: Crear SubjectJpaEntity.java (Infrastructure)

**Ubicación:** `src/main/java/acainfo/back/subject/infrastructure/adapters/out/persistence/entities/SubjectJpaEntity.java`

```java
package acainfo.back.subject.infrastructure.adapters.out.persistence.entities;

import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad JPA pura - Subject
 * Solo mapeo de persistencia
 * SIN lógica de negocio
 */
@Entity
@Table(name = "subjects", indexes = {
    @Index(name = "idx_subject_code", columnList = "code", unique = true),
    @Index(name = "idx_subject_year_degree_semester", columnList = "year,degree,semester")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer year; // 1-4

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Degree degree;

    @Column(nullable = false)
    private Integer semester; // 1 o 2

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubjectStatus status = SubjectStatus.ACTIVO;

    @Column(length = 500)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // SIN métodos de negocio - Solo getters/setters de Lombok
    // SIN relación @OneToMany con SubjectGroup (manejado en el mapper)
}
```

**Nota:** Eliminamos la relación `@OneToMany` con `SubjectGroup` porque:
1. No es necesaria para la persistencia básica
2. Se puede manejar en queries específicas si se necesita
3. Evita lazy loading issues

---

### PASO 3: Crear SubjectJpaMapper.java

**Ubicación:** `src/main/java/acainfo/back/subject/infrastructure/adapters/out/persistence/mappers/SubjectJpaMapper.java`

```java
package acainfo.back.subject.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper de INFRAESTRUCTURA
 * Convierte: Domain Subject ↔ SubjectJpaEntity
 */
@Component
public class SubjectJpaMapper {

    /**
     * Domain → JPA (para guardar en BD)
     */
    public SubjectJpaEntity toJpaEntity(Subject domain) {
        if (domain == null) {
            return null;
        }

        return SubjectJpaEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .year(domain.getYear())
                .degree(domain.getDegree())
                .semester(domain.getSemester())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * JPA → Domain (para cargar desde BD)
     */
    public Subject toDomain(SubjectJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return Subject.builder()
                .id(jpa.getId())
                .code(jpa.getCode())
                .name(jpa.getName())
                .year(jpa.getYear())
                .degree(jpa.getDegree())
                .semester(jpa.getSemester())
                .status(jpa.getStatus())
                .description(jpa.getDescription())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Conversión de listas JPA → Domain
     */
    public List<Subject> toDomains(List<SubjectJpaEntity> jpaEntities) {
        if (jpaEntities == null) {
            return List.of();
        }

        return jpaEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Conversión de listas Domain → JPA
     */
    public List<SubjectJpaEntity> toJpaEntities(List<Subject> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }
}
```

---

### PASO 4: Crear SubjectDtoMapper.java (Application)

**Ubicación:** `src/main/java/acainfo/back/subject/application/mappers/SubjectDtoMapper.java`

```java
package acainfo.back.subject.application.mappers;

import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.in.dto.CreateSubjectRequest;
import acainfo.back.subject.infrastructure.adapters.in.dto.SubjectResponse;
import acainfo.back.subject.infrastructure.adapters.in.dto.UpdateSubjectRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper de APLICACIÓN
 * Convierte: Domain Subject ↔ DTOs
 */
@Component
public class SubjectDtoMapper {

    /**
     * Domain → Response DTO (salida API)
     */
    public SubjectResponse toResponse(Subject domain) {
        if (domain == null) {
            return null;
        }

        return SubjectResponse.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .year(domain.getYear())
                .degree(domain.getDegree())
                .semester(domain.getSemester())
                .status(domain.getStatus())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * Request DTO → Domain (entrada API - creación)
     */
    public Subject toDomain(CreateSubjectRequest request) {
        if (request == null) {
            return null;
        }

        return Subject.builder()
                .code(request.getCode())
                .name(request.getName())
                .year(request.getYear())
                .degree(request.getDegree())
                .semester(request.getSemester())
                .description(request.getDescription())
                .status(SubjectStatus.ACTIVO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Update DTO → Domain (entrada API - actualización)
     */
    public Subject updateDomainFromDto(Subject existing, UpdateSubjectRequest request) {
        return Subject.builder()
                .id(existing.getId())
                .code(existing.getCode()) // Code no se puede cambiar
                .name(request.getName() != null ? request.getName() : existing.getName())
                .year(existing.getYear()) // Year no se cambia después de creación
                .degree(existing.getDegree()) // Degree no se cambia
                .semester(existing.getSemester()) // Semester no se cambia
                .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Conversión de listas Domain → Response
     */
    public List<SubjectResponse> toResponses(List<Subject> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
```

---

### PASO 5: Renombrar y Refactorizar SubjectJpaRepository

**Ubicación:** `src/main/java/acainfo/back/subject/infrastructure/adapters/out/persistence/repositories/SubjectJpaRepository.java`

**ANTES (SubjectRepository.java):**
```java
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);
    List<Subject> findByStatus(SubjectStatus status);
    // ...
}
```

**DESPUÉS (SubjectJpaRepository.java):**
```java
package acainfo.back.subject.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA - Spring Data
 * Trabaja con SubjectJpaEntity
 */
@Repository
public interface SubjectJpaRepository extends
        JpaRepository<SubjectJpaEntity, Long>,
        JpaSpecificationExecutor<SubjectJpaEntity> {

    Optional<SubjectJpaEntity> findByCode(String code);

    List<SubjectJpaEntity> findByStatus(SubjectStatus status);

    List<SubjectJpaEntity> findByYear(Integer year);

    List<SubjectJpaEntity> findByDegree(Degree degree);

    List<SubjectJpaEntity> findByYearAndDegreeAndSemester(Integer year, Degree degree, Integer semester);

    boolean existsByCode(String code);
}
```

---

### PASO 6: Refactorizar SubjectRepositoryAdapter

**Ubicación:** `src/main/java/acainfo/back/subject/infrastructure/adapters/out/persistence/adapters/SubjectRepositoryAdapter.java`

**ANTES:**
```java
@Component
public class SubjectRepositoryAdapter implements SubjectRepositoryPort {
    private final SubjectRepository repository;

    @Override
    public Subject save(Subject subject) {
        return repository.save(subject); // Trabajaba directamente con Subject (@Entity)
    }
}
```

**DESPUÉS:**
```java
package acainfo.back.subject.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import acainfo.back.subject.infrastructure.adapters.out.persistence.mappers.SubjectJpaMapper;
import acainfo.back.subject.infrastructure.adapters.out.persistence.repositories.SubjectJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de SALIDA - Repository
 * Implementa SubjectRepositoryPort
 * Usa SubjectJpaMapper para conversiones
 * Delega a SubjectJpaRepository
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubjectRepositoryAdapter implements SubjectRepositoryPort {

    private final SubjectJpaRepository jpaRepository;
    private final SubjectJpaMapper mapper;

    @Override
    public Subject save(Subject subject) {
        log.debug("Saving subject: {}", subject.getCode());

        SubjectJpaEntity jpaEntity = mapper.toJpaEntity(subject);
        SubjectJpaEntity saved = jpaRepository.save(jpaEntity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Subject> findById(Long id) {
        log.debug("Finding subject by ID: {}", id);

        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Subject> findByCode(String code) {
        log.debug("Finding subject by code: {}", code);

        return jpaRepository.findByCode(code)
                .map(mapper::toDomain);
    }

    @Override
    public List<Subject> findAll() {
        log.debug("Finding all subjects");

        return mapper.toDomains(jpaRepository.findAll());
    }

    @Override
    public List<Subject> findByStatus(SubjectStatus status) {
        log.debug("Finding subjects by status: {}", status);

        return mapper.toDomains(jpaRepository.findByStatus(status));
    }

    @Override
    public List<Subject> findByYear(Integer year) {
        log.debug("Finding subjects by year: {}", year);

        return mapper.toDomains(jpaRepository.findByYear(year));
    }

    @Override
    public List<Subject> findByDegree(Degree degree) {
        log.debug("Finding subjects by degree: {}", degree);

        return mapper.toDomains(jpaRepository.findByDegree(degree));
    }

    @Override
    public List<Subject> findByYearAndDegreeAndSemester(Integer year, Degree degree, Integer semester) {
        log.debug("Finding subjects by year: {}, degree: {}, semester: {}", year, degree, semester);

        return mapper.toDomains(
                jpaRepository.findByYearAndDegreeAndSemester(year, degree, semester)
        );
    }

    @Override
    public void delete(Subject subject) {
        log.debug("Deleting subject: {}", subject.getCode());

        SubjectJpaEntity jpaEntity = mapper.toJpaEntity(subject);
        jpaRepository.delete(jpaEntity);
    }

    @Override
    public boolean existsByCode(String code) {
        log.debug("Checking if subject exists by code: {}", code);

        return jpaRepository.existsByCode(code);
    }
}
```

---

### PASO 7: Refactorizar SubjectService

**Ubicación:** `src/main/java/acainfo/back/subject/application/services/SubjectService.java`

**Cambios:**
1. Inyectar `SubjectDtoMapper`
2. Usar `SubjectDtoMapper` para conversiones DTO ↔ Domain
3. Trabajar solo con `Subject` (domain), nunca con `SubjectJpaEntity`

**DESPUÉS:**
```java
package acainfo.back.subject.application.services;

import acainfo.back.subject.application.mappers.SubjectDtoMapper;
import acainfo.back.subject.application.ports.in.*;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.exception.SubjectAlreadyExistsException;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.in.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación - Subject
 * Implementa use cases
 * Trabaja con entidades de dominio
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectService implements
        CreateSubjectUseCase,
        UpdateSubjectUseCase,
        GetSubjectUseCase,
        DeleteSubjectUseCase {

    private final SubjectRepositoryPort repository;
    private final SubjectDtoMapper mapper;

    @Override
    public Subject createSubject(CreateSubjectRequest request) {
        log.info("Creating subject with code: {}", request.getCode());

        // Validación de negocio
        if (repository.existsByCode(request.getCode())) {
            throw new SubjectAlreadyExistsException(request.getCode());
        }

        // Mapear DTO → Domain
        Subject subject = mapper.toDomain(request);

        // Guardar
        Subject saved = repository.save(subject);

        log.info("Subject created with ID: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getSubjectById(Long id) {
        log.debug("Getting subject by ID: {}", id);

        return repository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getSubjectByCode(String code) {
        log.debug("Getting subject by code: {}", code);

        return repository.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        log.debug("Getting all subjects");

        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByFilters(SubjectFilterDTO filters) {
        log.debug("Getting subjects by filters: {}", filters);

        // Si hay filtros específicos, aplicarlos
        if (filters.getYear() != null && filters.getDegree() != null && filters.getSemester() != null) {
            return repository.findByYearAndDegreeAndSemester(
                    filters.getYear(),
                    filters.getDegree(),
                    filters.getSemester()
            );
        }

        if (filters.getYear() != null) {
            return repository.findByYear(filters.getYear());
        }

        if (filters.getDegree() != null) {
            return repository.findByDegree(filters.getDegree());
        }

        if (filters.getStatus() != null) {
            return repository.findByStatus(filters.getStatus());
        }

        return repository.findAll();
    }

    @Override
    public Subject updateSubject(Long id, UpdateSubjectRequest request) {
        log.info("Updating subject with ID: {}", id);

        // Cargar existente
        Subject existing = getSubjectById(id);

        // Aplicar cambios usando mapper
        Subject updated = mapper.updateDomainFromDto(existing, request);

        // Guardar
        Subject saved = repository.save(updated);

        log.info("Subject updated successfully");
        return saved;
    }

    @Override
    public void deleteSubject(Long id) {
        log.info("Deleting subject with ID: {}", id);

        Subject subject = getSubjectById(id);

        // Lógica de negocio: validar que se puede eliminar
        // (implementar en domain model si es necesario)

        repository.delete(subject);

        log.info("Subject deleted successfully");
    }

    @Override
    public Subject activateSubject(Long id) {
        log.info("Activating subject with ID: {}", id);

        Subject subject = getSubjectById(id);
        subject.activate(); // Lógica de dominio

        return repository.save(subject);
    }

    @Override
    public Subject deactivateSubject(Long id) {
        log.info("Deactivating subject with ID: {}", id);

        Subject subject = getSubjectById(id);
        subject.deactivate(); // Lógica de dominio

        return repository.save(subject);
    }

    @Override
    public Subject archiveSubject(Long id) {
        log.info("Archiving subject with ID: {}", id);

        Subject subject = getSubjectById(id);
        subject.archive(); // Lógica de dominio

        return repository.save(subject);
    }
}
```

---

### PASO 8: Refactorizar SubjectController

**Ubicación:** `src/main/java/acainfo/back/subject/infrastructure/adapters/in/rest/SubjectController.java`

**Cambios:**
1. Inyectar use cases (no servicio directo)
2. Inyectar `SubjectDtoMapper`
3. Usar mapper para conversiones

**DESPUÉS:**
```java
package acainfo.back.subject.infrastructure.adapters.in.rest;

import acainfo.back.subject.application.mappers.SubjectDtoMapper;
import acainfo.back.subject.application.ports.in.*;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subject.infrastructure.adapters.in.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST - Subject
 * Usa use cases
 * Usa mapper para conversiones
 */
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    // Use cases
    private final CreateSubjectUseCase createSubjectUseCase;
    private final UpdateSubjectUseCase updateSubjectUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final DeleteSubjectUseCase deleteSubjectUseCase;

    // Mapper
    private final SubjectDtoMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        log.info("REST request to create subject with code: {}", request.getCode());

        Subject created = createSubjectUseCase.createSubject(request);
        SubjectResponse response = mapper.toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<SubjectResponse> getSubjectById(@PathVariable Long id) {
        log.info("REST request to get subject by ID: {}", id);

        Subject subject = getSubjectUseCase.getSubjectById(id);
        SubjectResponse response = mapper.toResponse(subject);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<SubjectResponse> getSubjectByCode(@PathVariable String code) {
        log.info("REST request to get subject by code: {}", code);

        Subject subject = getSubjectUseCase.getSubjectByCode(code);
        SubjectResponse response = mapper.toResponse(subject);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<SubjectResponse>> getAllSubjects(
            @ModelAttribute SubjectFilterDTO filters) {

        log.info("REST request to get all subjects with filters: {}", filters);

        List<Subject> subjects = getSubjectUseCase.getSubjectsByFilters(filters);
        List<SubjectResponse> responses = mapper.toResponses(subjects);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {

        log.info("REST request to update subject with ID: {}", id);

        Subject updated = updateSubjectUseCase.updateSubject(id, request);
        SubjectResponse response = mapper.toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        log.info("REST request to delete subject with ID: {}", id);

        deleteSubjectUseCase.deleteSubject(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> activateSubject(@PathVariable Long id) {
        log.info("REST request to activate subject with ID: {}", id);

        Subject activated = updateSubjectUseCase.activateSubject(id);
        SubjectResponse response = mapper.toResponse(activated);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> deactivateSubject(@PathVariable Long id) {
        log.info("REST request to deactivate subject with ID: {}", id);

        Subject deactivated = updateSubjectUseCase.deactivateSubject(id);
        SubjectResponse response = mapper.toResponse(deactivated);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubjectResponse> archiveSubject(@PathVariable Long id) {
        log.info("REST request to archive subject with ID: {}", id);

        Subject archived = updateSubjectUseCase.archiveSubject(id);
        SubjectResponse response = mapper.toResponse(archived);

        return ResponseEntity.ok(response);
    }
}
```

---

## ✅ Resumen de Cambios

### Archivos Creados (4):
1. `Subject.java` (domain/model - POJO puro) - REEMPLAZA anterior
2. `SubjectJpaEntity.java` (infrastructure/.../entities)
3. `SubjectJpaMapper.java` (infrastructure/.../mappers)
4. `SubjectDtoMapper.java` (application/mappers)

### Archivos Renombrados (1):
1. `SubjectRepository.java` → `SubjectJpaRepository.java`

### Archivos Modificados (2):
1. `SubjectRepositoryAdapter.java` - Usa mapper
2. `SubjectService.java` - Usa mapper DTO
3. `SubjectController.java` - Inyecta use cases y mapper

### Archivos Eliminados (1):
1. `Subject.java` (@Entity antiguo) - REEMPLAZADO por POJO puro

---

*Ejemplo completo de migración - Módulo Subject - 22 Noviembre 2025*
