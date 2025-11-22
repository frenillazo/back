# Patrones y Convenciones - Arquitectura Hexagonal Pura
## Guía de Implementación Práctica

**Complemento a:** HEXAGONAL_MIGRATION_PLAN.md
**Fecha:** 22 Noviembre 2025

---

## 📐 Convenciones de Nombres

### Paquetes

```
acainfo.back.{module}/
├── domain/
│   ├── model/              # Entidades de dominio (POJOs)
│   ├── exception/          # Excepciones de dominio
│   └── validation/         # Validadores de negocio
├── application/
│   ├── ports/
│   │   ├── in/             # Use Cases (interfaces)
│   │   └── out/            # Repository/Service Ports (interfaces)
│   ├── services/           # Implementación de use cases
│   └── mappers/            # Mappers Domain ↔ DTO
└── infrastructure/
    └── adapters/
        ├── in/
        │   ├── rest/       # Controllers REST
        │   └── dto/        # Request/Response DTOs
        └── out/
            └── persistence/
                ├── entities/    # Entidades JPA
                ├── mappers/     # Mappers Domain ↔ JPA
                ├── repositories/ # Spring Data JPA
                └── adapters/    # Implementación de repository ports
```

### Nomenclatura de Clases

| Tipo | Patrón | Ejemplo | Ubicación |
|------|--------|---------|-----------|
| Domain Model | `{Entity}` | `User` | domain/model |
| JPA Entity | `{Entity}JpaEntity` | `UserJpaEntity` | infrastructure/.../persistence/entities |
| Repository Port | `{Entity}RepositoryPort` | `UserRepositoryPort` | application/ports/out |
| Repository JPA | `{Entity}JpaRepository` | `UserJpaRepository` | infrastructure/.../persistence/repositories |
| Repository Adapter | `{Entity}RepositoryAdapter` | `UserRepositoryAdapter` | infrastructure/.../persistence/adapters |
| Use Case (IN Port) | `{Action}{Entity}UseCase` | `CreateUserUseCase` | application/ports/in |
| JPA Mapper | `{Entity}JpaMapper` | `UserJpaMapper` | infrastructure/.../persistence/mappers |
| DTO Mapper | `{Entity}DtoMapper` | `UserDtoMapper` | application/mappers |
| Request DTO | `{Action}{Entity}Request` | `CreateUserRequest` | infrastructure/.../in/dto |
| Response DTO | `{Entity}Response` | `UserResponse` | infrastructure/.../in/dto |
| Service | `{Entity}Service` | `UserService` | application/services |
| Controller | `{Entity}Controller` | `UserController` | infrastructure/.../in/rest |
| Domain Exception | `{Entity}{Error}Exception` | `UserNotFoundException` | domain/exception |

---

## 🏛️ Patrones de Diseño Aplicados

### 1. Patrón Hexagonal (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────┐
│                   INFRASTRUCTURE LAYER                  │
│  ┌─────────────────────────────────────────────────┐   │
│  │            ADAPTERS IN (REST)                   │   │
│  │  Controllers + DTOs                             │   │
│  └─────────────────────────────────────────────────┘   │
│                         ↓↑                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │            APPLICATION LAYER                    │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │  USE CASES (Ports IN)                    │   │   │
│  │  │  - CreateUserUseCase                     │   │   │
│  │  │  - UpdateUserUseCase                     │   │   │
│  │  └──────────────────────────────────────────┘   │   │
│  │                 ↓↑                              │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │  SERVICES                                │   │   │
│  │  │  - UserService implements use cases     │   │   │
│  │  └──────────────────────────────────────────┘   │   │
│  │                 ↓↑                              │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │  REPOSITORY PORTS (Ports OUT)            │   │   │
│  │  │  - UserRepositoryPort                    │   │   │
│  │  └──────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────┘   │
│                         ↓↑                              │
│                  ┌─────────────┐                        │
│                  │   DOMAIN    │                        │
│                  │   MODELS    │                        │
│                  │   - User    │                        │
│                  └─────────────┘                        │
│                         ↓↑                              │
│  ┌─────────────────────────────────────────────────┐   │
│  │       ADAPTERS OUT (PERSISTENCE)                │   │
│  │  - UserRepositoryAdapter                        │   │
│  │  - UserJpaMapper                                │   │
│  │  - UserJpaRepository                            │   │
│  │  - UserJpaEntity                                │   │
│  └─────────────────────────────────────────────────┘   │
│                         ↓↑                              │
│                    [ DATABASE ]                         │
└─────────────────────────────────────────────────────────┘
```

### 2. Repository Pattern

**Objetivo:** Encapsular acceso a datos

```java
// Puerto (Application Layer)
public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
}

// Adaptador (Infrastructure Layer)
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository jpaRepository;
    private final UserJpaMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity jpa = mapper.toJpaEntity(user);
        UserJpaEntity saved = jpaRepository.save(jpa);
        return mapper.toDomain(saved);
    }
}
```

### 3. Use Case Pattern

**Objetivo:** Definir operaciones de negocio como contratos

```java
// Use Case (Application Layer - Port IN)
public interface CreateUserUseCase {
    /**
     * Crea un nuevo usuario
     * @param request datos del usuario
     * @return usuario creado
     * @throws UserAlreadyExistsException si email ya existe
     */
    User createUser(RegisterRequest request);
}

// Implementación (Application Layer - Service)
@Service
public class UserService implements CreateUserUseCase {

    private final UserRepositoryPort repository;
    private final UserDtoMapper mapper;

    @Override
    public User createUser(RegisterRequest request) {
        // Validación
        if (repository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        // Mapear DTO → Domain
        User user = mapper.toDomain(request);

        // Guardar
        return repository.save(user);
    }
}
```

### 4. Builder Pattern (Domain Models)

**Objetivo:** Construcción fluida de entidades con validación

```java
public class User {
    private Long id;
    private String email;
    private String password;
    // ... más campos

    private User() {}

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private User user = new User();

        public UserBuilder email(String email) {
            user.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            user.password = password;
            return this;
        }

        public User build() {
            validate();
            return user;
        }

        private void validate() {
            if (user.email == null) {
                throw new IllegalArgumentException("Email required");
            }
            // Más validaciones
        }
    }
}

// Uso
User user = User.builder()
    .email("test@example.com")
    .password("secret")
    .build();
```

### 5. Mapper Pattern

**Objetivo:** Conversión entre capas sin acoplamiento

```java
@Component
public class UserJpaMapper {

    // Domain → JPA (para persistir)
    public UserJpaEntity toJpaEntity(User domain) {
        return UserJpaEntity.builder()
            .id(domain.getId())
            .email(domain.getEmail())
            .password(domain.getPassword())
            .build();
    }

    // JPA → Domain (para cargar)
    public User toDomain(UserJpaEntity jpa) {
        return User.builder()
            .id(jpa.getId())
            .email(jpa.getEmail())
            .password(jpa.getPassword())
            .build();
    }
}
```

### 6. Exception Handling Pattern

**Objetivo:** Excepciones específicas de dominio

```java
// Domain Exception (domain/exception)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found with ID: " + id);
    }
}

// Global Exception Handler (Infrastructure)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

---

## 🔒 Reglas de Dependencia

### Regla de Oro: Dependency Inversion

```
Infrastructure → Application → Domain
         ↓            ↓          ↓
     DEPENDE       DEPENDE    NO DEPENDE
                                DE NADIE
```

### Dependencias Permitidas

| Capa | Puede Depender De | NO Puede Depender De |
|------|-------------------|---------------------|
| **Domain** | - Java estándar<br>- Enums propios | - Spring<br>- JPA<br>- Application<br>- Infrastructure |
| **Application** | - Domain<br>- Java estándar | - Infrastructure<br>- JPA<br>- DTOs |
| **Infrastructure** | - Application<br>- Domain<br>- Spring<br>- JPA | Nada (capa más externa) |

### Ejemplo de Violación (❌ MAL)

```java
// domain/model/User.java
@Entity  // ❌ NO - Domain depende de JPA
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
}
```

### Ejemplo Correcto (✅ BIEN)

```java
// domain/model/User.java
public class User {  // ✅ SÍ - POJO puro
    private Long id;
    private String email;

    // Sin anotaciones de framework
}

// infrastructure/.../entities/UserJpaEntity.java
@Entity  // ✅ SÍ - JPA en infrastructure
@Table(name = "users")
public class UserJpaEntity {
    @Id
    @GeneratedValue
    private Long id;
}
```

---

## 🎨 Patrones de Código

### 1. Domain Model - Template

```java
package acainfo.back.{module}.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad de dominio pura - {Entity}
 * Sin anotaciones de framework
 * Solo lógica de negocio
 */
public class {Entity} {

    // ===== CAMPOS =====

    private Long id;
    private String someField;
    private {Status}Enum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== CONSTRUCTOR PRIVADO =====

    private {Entity}() {
        // Solo accesible desde Builder
    }

    // ===== BUILDER =====

    public static {Entity}Builder builder() {
        return new {Entity}Builder();
    }

    // ===== LÓGICA DE NEGOCIO =====

    public boolean isActive() {
        return status == {Status}.ACTIVE;
    }

    public void activate() {
        validateCanBeActivated();
        this.status = {Status}.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateCanBeActivated() {
        if (isActive()) {
            throw new IllegalStateException("Already active");
        }
    }

    // ===== GETTERS (SIN SETTERS - Inmutabilidad) =====

    public Long getId() {
        return id;
    }

    public String getSomeField() {
        return someField;
    }

    // ===== EQUALS & HASHCODE =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {Entity} entity = ({Entity}) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ===== BUILDER CLASS =====

    public static class {Entity}Builder {
        private {Entity} entity = new {Entity}();

        public {Entity}Builder id(Long id) {
            entity.id = id;
            return this;
        }

        public {Entity}Builder someField(String someField) {
            entity.someField = someField;
            return this;
        }

        public {Entity}Builder status({Status} status) {
            entity.status = status;
            return this;
        }

        public {Entity} build() {
            validate();
            return entity;
        }

        private void validate() {
            if (entity.someField == null || entity.someField.isBlank()) {
                throw new IllegalArgumentException("someField is required");
            }
        }
    }
}
```

### 2. JPA Entity - Template

```java
package acainfo.back.{module}.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad JPA - Solo mapeo de persistencia
 * Sin lógica de negocio
 */
@Entity
@Table(
    name = "{table_name}",
    indexes = {
        @Index(name = "idx_{table}_{field}", columnList = "{field}", unique = true)
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class {Entity}JpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String someField;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private {Status} status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Solo Lombok - SIN métodos de negocio
}
```

### 3. JPA Mapper - Template

```java
package acainfo.back.{module}.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.out.persistence.entities.{Entity}JpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper de INFRAESTRUCTURA
 * Convierte: Domain ↔ JPA
 */
@Component
public class {Entity}JpaMapper {

    /**
     * Domain → JPA (para guardar)
     */
    public {Entity}JpaEntity toJpaEntity({Entity} domain) {
        if (domain == null) {
            return null;
        }

        return {Entity}JpaEntity.builder()
                .id(domain.getId())
                .someField(domain.getSomeField())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * JPA → Domain (para cargar)
     */
    public {Entity} toDomain({Entity}JpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return {Entity}.builder()
                .id(jpa.getId())
                .someField(jpa.getSomeField())
                .status(jpa.getStatus())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Conversión de listas
     */
    public List<{Entity}> toDomains(List<{Entity}JpaEntity> jpaEntities) {
        return jpaEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<{Entity}JpaEntity> toJpaEntities(List<{Entity}> domains) {
        return domains.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toList());
    }

    /**
     * Conversión de sets
     */
    public Set<{Entity}> toDomainSet(Set<{Entity}JpaEntity> jpaEntities) {
        return jpaEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toSet());
    }

    public Set<{Entity}JpaEntity> toJpaEntitySet(Set<{Entity}> domains) {
        return domains.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toSet());
    }
}
```

### 4. DTO Mapper - Template

```java
package acainfo.back.{module}.application.mappers;

import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.in.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper de APLICACIÓN
 * Convierte: Domain ↔ DTO
 */
@Component
public class {Entity}DtoMapper {

    /**
     * Domain → Response DTO (salida API)
     */
    public {Entity}Response toResponse({Entity} domain) {
        if (domain == null) {
            return null;
        }

        return {Entity}Response.builder()
                .id(domain.getId())
                .someField(domain.getSomeField())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * Request DTO → Domain (entrada API - creación)
     */
    public {Entity} toDomain(Create{Entity}Request request) {
        if (request == null) {
            return null;
        }

        return {Entity}.builder()
                .someField(request.getSomeField())
                .status({Status}.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Update DTO → Domain (entrada API - actualización)
     */
    public {Entity} updateDomainFromDto({Entity} existing, Update{Entity}Request request) {
        return {Entity}.builder()
                .id(existing.getId())
                .someField(request.getSomeField() != null ? request.getSomeField() : existing.getSomeField())
                .status(existing.getStatus())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Conversión de listas
     */
    public List<{Entity}Response> toResponses(List<{Entity}> domains) {
        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
```

### 5. Repository Port - Template

```java
package acainfo.back.{module}.application.ports.out;

import acainfo.back.{module}.domain.model.{Entity};

import java.util.List;
import java.util.Optional;

/**
 * Puerto de SALIDA - Repository
 * Define cómo acceder a persistencia (sin implementación)
 */
public interface {Entity}RepositoryPort {

    /**
     * Guarda una entidad
     */
    {Entity} save({Entity} entity);

    /**
     * Busca por ID
     */
    Optional<{Entity}> findById(Long id);

    /**
     * Busca todos
     */
    List<{Entity}> findAll();

    /**
     * Busca por campo específico
     */
    Optional<{Entity}> findBySomeField(String someField);

    /**
     * Elimina
     */
    void delete({Entity} entity);

    /**
     * Verifica existencia
     */
    boolean existsBySomeField(String someField);
}
```

### 6. Use Case Port - Template

```java
package acainfo.back.{module}.application.ports.in;

import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.in.dto.Create{Entity}Request;

/**
 * Puerto de ENTRADA - Use Case
 * Define operación de negocio
 */
public interface Create{Entity}UseCase {

    /**
     * Crea una nueva entidad
     *
     * @param request datos de la entidad
     * @return entidad creada
     * @throws {Entity}AlreadyExistsException si ya existe
     * @throws Invalid{Entity}DataException si datos inválidos
     */
    {Entity} create{Entity}(Create{Entity}Request request);
}
```

### 7. Service - Template

```java
package acainfo.back.{module}.application.services;

import acainfo.back.{module}.application.mappers.{Entity}DtoMapper;
import acainfo.back.{module}.application.ports.in.*;
import acainfo.back.{module}.application.ports.out.{Entity}RepositoryPort;
import acainfo.back.{module}.domain.exception.*;
import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.in.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación
 * Implementa use cases
 * Coordina operaciones entre domain y repositories
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class {Entity}Service implements
        Create{Entity}UseCase,
        Update{Entity}UseCase,
        Get{Entity}UseCase,
        Delete{Entity}UseCase {

    // Puertos (interfaces)
    private final {Entity}RepositoryPort repository;
    private final {Entity}DtoMapper mapper;

    @Override
    public {Entity} create{Entity}(Create{Entity}Request request) {
        log.info("Creating {entity} with someField: {}", request.getSomeField());

        // Validación de negocio
        if (repository.existsBySomeField(request.getSomeField())) {
            throw new {Entity}AlreadyExistsException(request.getSomeField());
        }

        // Mapear DTO → Domain
        {Entity} entity = mapper.toDomain(request);

        // Guardar
        {Entity} saved = repository.save(entity);

        log.info("{Entity} created with ID: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public {Entity} get{Entity}ById(Long id) {
        log.debug("Getting {entity} by ID: {}", id);

        return repository.findById(id)
                .orElseThrow(() -> new {Entity}NotFoundException(id));
    }

    @Override
    public {Entity} update{Entity}(Long id, Update{Entity}Request request) {
        log.info("Updating {entity} with ID: {}", id);

        // Cargar existente
        {Entity} existing = get{Entity}ById(id);

        // Aplicar cambios
        {Entity} updated = mapper.updateDomainFromDto(existing, request);

        // Guardar
        return repository.save(updated);
    }

    @Override
    public void delete{Entity}(Long id) {
        log.info("Deleting {entity} with ID: {}", id);

        {Entity} entity = get{Entity}ById(id);
        repository.delete(entity);

        log.info("{Entity} deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<{Entity}> getAll{Entity}s() {
        log.debug("Getting all {entity}s");
        return repository.findAll();
    }
}
```

### 8. Repository Adapter - Template

```java
package acainfo.back.{module}.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.{module}.application.ports.out.{Entity}RepositoryPort;
import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.out.persistence.entities.{Entity}JpaEntity;
import acainfo.back.{module}.infrastructure.adapters.out.persistence.mappers.{Entity}JpaMapper;
import acainfo.back.{module}.infrastructure.adapters.out.persistence.repositories.{Entity}JpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de SALIDA - Repository
 * Implementa puerto de repositorio
 * Delega a JPA Repository
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class {Entity}RepositoryAdapter implements {Entity}RepositoryPort {

    private final {Entity}JpaRepository jpaRepository;
    private final {Entity}JpaMapper mapper;

    @Override
    public {Entity} save({Entity} entity) {
        log.debug("Saving {entity}: {}", entity.getId());

        {Entity}JpaEntity jpaEntity = mapper.toJpaEntity(entity);
        {Entity}JpaEntity saved = jpaRepository.save(jpaEntity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<{Entity}> findById(Long id) {
        log.debug("Finding {entity} by ID: {}", id);

        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<{Entity}> findAll() {
        log.debug("Finding all {entity}s");

        return mapper.toDomains(jpaRepository.findAll());
    }

    @Override
    public Optional<{Entity}> findBySomeField(String someField) {
        log.debug("Finding {entity} by someField: {}", someField);

        return jpaRepository.findBySomeField(someField)
                .map(mapper::toDomain);
    }

    @Override
    public void delete({Entity} entity) {
        log.debug("Deleting {entity}: {}", entity.getId());

        {Entity}JpaEntity jpaEntity = mapper.toJpaEntity(entity);
        jpaRepository.delete(jpaEntity);
    }

    @Override
    public boolean existsBySomeField(String someField) {
        log.debug("Checking existence by someField: {}", someField);

        return jpaRepository.existsBySomeField(someField);
    }
}
```

### 9. Controller - Template

```java
package acainfo.back.{module}.infrastructure.adapters.in.rest;

import acainfo.back.{module}.application.mappers.{Entity}DtoMapper;
import acainfo.back.{module}.application.ports.in.*;
import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.in.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST
 * Usa use cases (puertos de entrada)
 * Usa mapper para conversiones Domain ↔ DTO
 */
@RestController
@RequestMapping("/api/{entities}")
@RequiredArgsConstructor
@Slf4j
public class {Entity}Controller {

    // Use cases (interfaces)
    private final Create{Entity}UseCase create{Entity}UseCase;
    private final Update{Entity}UseCase update{Entity}UseCase;
    private final Get{Entity}UseCase get{Entity}UseCase;
    private final Delete{Entity}UseCase delete{Entity}UseCase;

    // Mapper
    private final {Entity}DtoMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<{Entity}Response> create(@Valid @RequestBody Create{Entity}Request request) {
        log.info("REST request to create {entity}");

        {Entity} created = create{Entity}UseCase.create{Entity}(request);
        {Entity}Response response = mapper.toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<{Entity}Response> getById(@PathVariable Long id) {
        log.info("REST request to get {entity} by ID: {}", id);

        {Entity} entity = get{Entity}UseCase.get{Entity}ById(id);
        {Entity}Response response = mapper.toResponse(entity);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<{Entity}Response>> getAll() {
        log.info("REST request to get all {entity}s");

        List<{Entity}> entities = get{Entity}UseCase.getAll{Entity}s();
        List<{Entity}Response> responses = mapper.toResponses(entities);

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<{Entity}Response> update(
            @PathVariable Long id,
            @Valid @RequestBody Update{Entity}Request request) {

        log.info("REST request to update {entity} with ID: {}", id);

        {Entity} updated = update{Entity}UseCase.update{Entity}(id, request);
        {Entity}Response response = mapper.toResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete {entity} with ID: {}", id);

        delete{Entity}UseCase.delete{Entity}(id);

        return ResponseEntity.noContent().build();
    }
}
```

---

## 🧪 Patrones de Testing

### 1. Domain Model Test

```java
package acainfo.back.{module}.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class {Entity}Test {

    @Test
    void shouldCreateValidEntity() {
        // Given
        String someField = "test";

        // When
        {Entity} entity = {Entity}.builder()
                .someField(someField)
                .status({Status}.ACTIVE)
                .build();

        // Then
        assertThat(entity.getSomeField()).isEqualTo(someField);
        assertThat(entity.getStatus()).isEqualTo({Status}.ACTIVE);
    }

    @Test
    void shouldThrowExceptionWhenSomeFieldIsNull() {
        // When/Then
        assertThatThrownBy(() ->
                {Entity}.builder()
                        .someField(null)
                        .build()
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("someField is required");
    }

    @Test
    void shouldActivateEntity() {
        // Given
        {Entity} entity = {Entity}.builder()
                .someField("test")
                .status({Status}.INACTIVE)
                .build();

        // When
        entity.activate();

        // Then
        assertThat(entity.isActive()).isTrue();
    }
}
```

### 2. Mapper Test

```java
package acainfo.back.{module}.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.out.persistence.entities.{Entity}JpaEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class {Entity}JpaMapperTest {

    private {Entity}JpaMapper mapper = new {Entity}JpaMapper();

    @Test
    void shouldMapDomainToJpa() {
        // Given
        {Entity} domain = {Entity}.builder()
                .id(1L)
                .someField("test")
                .status({Status}.ACTIVE)
                .build();

        // When
        {Entity}JpaEntity jpa = mapper.toJpaEntity(domain);

        // Then
        assertThat(jpa).isNotNull();
        assertThat(jpa.getId()).isEqualTo(domain.getId());
        assertThat(jpa.getSomeField()).isEqualTo(domain.getSomeField());
        assertThat(jpa.getStatus()).isEqualTo(domain.getStatus());
    }

    @Test
    void shouldMapJpaToDomain() {
        // Given
        {Entity}JpaEntity jpa = {Entity}JpaEntity.builder()
                .id(1L)
                .someField("test")
                .status({Status}.ACTIVE)
                .build();

        // When
        {Entity} domain = mapper.toDomain(jpa);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(jpa.getId());
        assertThat(domain.getSomeField()).isEqualTo(jpa.getSomeField());
        assertThat(domain.getStatus()).isEqualTo(jpa.getStatus());
    }

    @Test
    void shouldReturnNullWhenInputIsNull() {
        assertThat(mapper.toJpaEntity(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
    }
}
```

### 3. Service Test

```java
package acainfo.back.{module}.application.services;

import acainfo.back.{module}.application.mappers.{Entity}DtoMapper;
import acainfo.back.{module}.application.ports.out.{Entity}RepositoryPort;
import acainfo.back.{module}.domain.exception.*;
import acainfo.back.{module}.domain.model.{Entity};
import acainfo.back.{module}.infrastructure.adapters.in.dto.Create{Entity}Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class {Entity}ServiceTest {

    @Mock
    private {Entity}RepositoryPort repository;

    @Mock
    private {Entity}DtoMapper mapper;

    @InjectMocks
    private {Entity}Service service;

    @Test
    void shouldCreateEntitySuccessfully() {
        // Given
        Create{Entity}Request request = new Create{Entity}Request();
        request.setSomeField("test");

        {Entity} entity = {Entity}.builder()
                .someField("test")
                .status({Status}.ACTIVE)
                .build();

        {Entity} savedEntity = {Entity}.builder()
                .id(1L)
                .someField("test")
                .status({Status}.ACTIVE)
                .build();

        when(repository.existsBySomeField(anyString())).thenReturn(false);
        when(mapper.toDomain(any(Create{Entity}Request.class))).thenReturn(entity);
        when(repository.save(any({Entity}.class))).thenReturn(savedEntity);

        // When
        {Entity} result = service.create{Entity}(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).existsBySomeField("test");
        verify(mapper).toDomain(request);
        verify(repository).save(entity);
    }

    @Test
    void shouldThrowExceptionWhenEntityAlreadyExists() {
        // Given
        Create{Entity}Request request = new Create{Entity}Request();
        request.setSomeField("existing");

        when(repository.existsBySomeField(anyString())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> service.create{Entity}(request))
                .isInstanceOf({Entity}AlreadyExistsException.class);

        verify(repository).existsBySomeField("existing");
        verifyNoMoreInteractions(mapper, repository);
    }
}
```

---

## ✅ Checklist de Revisión de Código

### Al crear una nueva entidad, verificar:

- [ ] Domain model es POJO puro (sin @Entity, @Table)
- [ ] Domain model tiene Builder pattern
- [ ] Domain model tiene validaciones en el build()
- [ ] Lógica de negocio está en domain model, no en JPA entity
- [ ] JPA entity solo tiene anotaciones JPA, sin lógica
- [ ] JPA entity usa Lombok (@Data, @Builder, etc.)
- [ ] JPA mapper convierte correctamente en ambas direcciones
- [ ] DTO mapper convierte correctamente en ambas direcciones
- [ ] Repository port define contrato (interface)
- [ ] Repository adapter implementa port y usa JPA mapper
- [ ] Use cases están definidos como interfaces
- [ ] Service implementa use cases
- [ ] Service usa repository port, no JPA repository directo
- [ ] Controller usa use cases, no service directo
- [ ] Controller usa DTO mapper
- [ ] No hay imports de JPA en domain o application layers
- [ ] Tests de domain model (lógica de negocio)
- [ ] Tests de mappers (conversiones)
- [ ] Tests de service (use cases con mocks)
- [ ] Tests de controller (integration con MockMvc)

---

*Guía de patrones para migración hexagonal pura - 22 Noviembre 2025*
