# Plan de Migración a Arquitectura Hexagonal Pura
## Sistema de Gestión Centro de Formación

**Fecha:** 22 Noviembre 2025
**Versión:** 1.0
**Autor:** Claude Code

---

## 📊 Resumen Ejecutivo

### Estado Actual
El proyecto **YA IMPLEMENTA** arquitectura hexagonal, pero de forma **pragmática**:
- ✅ Separación en capas: domain / application / infrastructure
- ✅ Puertos y adaptadores definidos
- ✅ DTOs para comunicación REST
- ❌ **Entidades JPA usadas como entidades de dominio** (acoplamiento a framework)
- ❌ **Falta separación entre modelo de dominio y modelo de persistencia**
- ❌ **Mappers ausentes o manuales** (conversiones inline)
- ❌ **DTOs acoplados a entidades JPA**

### Estado Objetivo - Hexagonal Pura
- ✅ **Entidades de dominio puras** (sin anotaciones JPA, Lombok business only)
- ✅ **Entidades JPA separadas** (en infrastructure/adapters/out/persistence)
- ✅ **Mappers de infraestructura** (JPA Entity ↔ Domain Entity)
- ✅ **Mappers de aplicación** (Domain Entity ↔ DTO)
- ✅ **Todos los servicios implementan puertos formales**
- ✅ **Repositorios solo accesibles a través de puertos**
- ✅ **Cero dependencias del dominio hacia frameworks**

### Impacto Estimado
- **14 entidades** a migrar
- **18 servicios** a refactorizar
- **54+ DTOs** a revisar
- **20 repositorios** a adaptar
- **9 módulos** funcionales

**Esfuerzo Total Estimado:** 4-5 semanas (160-200 horas)

---

## 🏗️ Cambios en la Arquitectura

### ANTES (Hexagonal Pragmática)

```
{module}/
├── domain/
│   └── model/
│       └── User.java                    ← @Entity JPA + lógica de dominio
├── application/
│   ├── ports/in/
│   │   └── CreateUserUseCase.java
│   ├── ports/out/
│   │   └── UserRepositoryPort.java
│   └── services/
│       └── UserService.java             ← Usa User (JPA) directamente
└── infrastructure/
    └── adapters/
        ├── in/rest/
        │   ├── UserController.java
        │   └── dto/
        │       └── UserResponse.java    ← Mapea manualmente desde User (JPA)
        └── out/
            ├── UserRepository.java       ← Spring Data JPA
            └── UserRepositoryAdapter.java ← Implementa port
```

**Problemas:**
1. `User` en domain tiene `@Entity`, `@Table`, `@Id` → Acoplamiento a JPA
2. `UserService` trabaja con `User` (JPA) → Lógica de negocio acoplada a persistencia
3. `UserResponse` importa `User` (JPA) → DTOs acoplados a infraestructura
4. Sin mappers formales → Conversiones manuales, código duplicado

---

### DESPUÉS (Hexagonal Pura)

```
{module}/
├── domain/
│   ├── model/
│   │   └── User.java                    ← POJO puro, SIN anotaciones JPA
│   ├── exception/
│   │   └── UserNotFoundException.java
│   └── validation/
│       └── UserValidator.java
├── application/
│   ├── ports/in/
│   │   └── CreateUserUseCase.java       ← Interface del use case
│   ├── ports/out/
│   │   └── UserRepositoryPort.java      ← Interface del puerto
│   ├── services/
│   │   └── UserService.java             ← Usa User (domain) + implementa use case
│   └── mappers/
│       └── UserDtoMapper.java           ← Domain ↔ DTO (APPLICATION LAYER)
└── infrastructure/
    └── adapters/
        ├── in/rest/
        │   ├── UserController.java
        │   └── dto/
        │       └── UserResponse.java    ← Solo DTOs, sin importar domain
        └── out/
            ├── persistence/
            │   ├── entities/
            │   │   └── UserJpaEntity.java     ← @Entity JPA pura
            │   ├── mappers/
            │   │   └── UserJpaMapper.java     ← Domain ↔ JPA (INFRASTRUCTURE LAYER)
            │   ├── repositories/
            │   │   └── UserJpaRepository.java ← Spring Data JPA
            │   └── adapters/
            │       └── UserRepositoryAdapter.java ← Usa mapper
            └── config/
                └── JpaConfig.java
```

**Mejoras:**
1. ✅ `User` (domain) es un POJO puro → Sin acoplamiento
2. ✅ `UserService` trabaja con `User` (domain) → Lógica de negocio independiente
3. ✅ `UserJpaEntity` maneja persistencia → Responsabilidad única
4. ✅ `UserJpaMapper` convierte JPA ↔ Domain → Capa anti-corrupción
5. ✅ `UserDtoMapper` convierte Domain ↔ DTO → Separación clara
6. ✅ Todos los servicios implementan sus puertos → Contratos explícitos

---

## 🎯 Principios de la Migración

### 1. Separación de Responsabilidades

**Entidad de Dominio (User.java - domain/model/)**
```java
package acainfo.back.shared.domain.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * Entidad de dominio pura - Sin anotaciones de framework
 * Contiene SOLO lógica de negocio
 */
public class User {
    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private UserStatus status;
    private Set<Role> roles = new HashSet<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor privado - usa Builder
    private User() {}

    // Builder pattern para construcción
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    // ===== LÓGICA DE NEGOCIO =====

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isStudent() {
        return roles.stream()
                .anyMatch(role -> role.getType() == RoleType.STUDENT);
    }

    public boolean isTeacher() {
        return roles.stream()
                .anyMatch(role -> role.getType() == RoleType.TEACHER);
    }

    public boolean hasPermission(String permissionCode) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getCode().equals(permissionCode));
    }

    public void activate() {
        validateCanBeActivated();
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate(String reason) {
        if (!isActive()) {
            throw new IllegalStateException("User is already inactive");
        }
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateCanBeActivated() {
        if (status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot activate suspended user");
        }
    }

    // Getters públicos (sin setters - inmutabilidad)
    public Long getId() { return id; }
    public String getEmail() { return email; }
    // ... más getters

    // ===== BUILDER =====

    public static class UserBuilder {
        private User user = new User();

        public UserBuilder id(Long id) {
            user.id = id;
            return this;
        }

        public UserBuilder email(String email) {
            user.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            user.password = password;
            return this;
        }

        // ... más setters de builder

        public User build() {
            validateUser();
            return user;
        }

        private void validateUser() {
            if (user.email == null || user.email.isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
            // Más validaciones de dominio
        }
    }
}
```

**Entidad JPA (UserJpaEntity.java - infrastructure/adapters/out/persistence/entities/)**
```java
package acainfo.back.shared.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad JPA pura - Solo mapeo de persistencia
 * SIN lógica de negocio
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<RoleJpaEntity> roles = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // SIN métodos de negocio - Solo getters/setters de Lombok
}
```

---

### 2. Mappers - Dos Capas

**Mapper de Infraestructura (UserJpaMapper.java)**
```java
package acainfo.back.shared.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper de INFRAESTRUCTURA
 * Convierte: Domain Entity ↔ JPA Entity
 *
 * Responsabilidad: Aislar el dominio de la implementación de persistencia
 */
@Component
public class UserJpaMapper {

    private final RoleJpaMapper roleMapper;

    public UserJpaMapper(RoleJpaMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /**
     * Domain → JPA (para guardar en BD)
     */
    public UserJpaEntity toJpaEntity(User domain) {
        if (domain == null) {
            return null;
        }

        return UserJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .phone(domain.getPhone())
                .status(domain.getStatus())
                .roles(roleMapper.toJpaEntities(domain.getRoles()))
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * JPA → Domain (para cargar desde BD)
     */
    public User toDomain(UserJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return User.builder()
                .id(jpa.getId())
                .email(jpa.getEmail())
                .password(jpa.getPassword())
                .firstName(jpa.getFirstName())
                .lastName(jpa.getLastName())
                .phone(jpa.getPhone())
                .status(jpa.getStatus())
                .roles(roleMapper.toDomains(jpa.getRoles()))
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    public Set<User> toDomains(Set<UserJpaEntity> jpaEntities) {
        return jpaEntities.stream()
                .map(this::toDomain)
                .collect(Collectors.toSet());
    }

    public Set<UserJpaEntity> toJpaEntities(Set<User> domains) {
        return domains.stream()
                .map(this::toJpaEntity)
                .collect(Collectors.toSet());
    }
}
```

**Mapper de Aplicación (UserDtoMapper.java)**
```java
package acainfo.back.shared.application.mappers;

import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.in.dto.*;
import org.springframework.stereotype.Component;

/**
 * Mapper de APLICACIÓN
 * Convierte: Domain Entity ↔ DTO
 *
 * Responsabilidad: Adaptar el dominio para las APIs REST
 */
@Component
public class UserDtoMapper {

    private final RoleDtoMapper roleMapper;

    public UserDtoMapper(RoleDtoMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /**
     * Domain → Response DTO (salida API)
     */
    public UserResponse toResponse(User domain) {
        if (domain == null) {
            return null;
        }

        return UserResponse.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .phone(domain.getPhone())
                .status(domain.getStatus())
                .roles(roleMapper.toResponses(domain.getRoles()))
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * Request DTO → Domain (entrada API)
     */
    public User toDomain(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword()) // Será encriptado por el servicio
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();
    }

    /**
     * Update DTO → Domain (actualización parcial)
     */
    public User updateDomainFromDto(User existing, UpdateUserRequest request) {
        return User.builder()
                .id(existing.getId())
                .email(existing.getEmail()) // Email no se puede cambiar
                .password(existing.getPassword())
                .firstName(request.getFirstName() != null ? request.getFirstName() : existing.getFirstName())
                .lastName(request.getLastName() != null ? request.getLastName() : existing.getLastName())
                .phone(request.getPhone() != null ? request.getPhone() : existing.getPhone())
                .status(existing.getStatus())
                .roles(existing.getRoles())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public List<UserResponse> toResponses(List<User> domains) {
        return domains.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
```

---

### 3. Servicios Implementando Puertos

**Puerto de Entrada (CreateUserUseCase.java)**
```java
package acainfo.back.shared.application.ports.in;

import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.in.dto.RegisterRequest;

/**
 * Puerto de ENTRADA (Use Case)
 * Define QUÉ puede hacer el sistema, no CÓMO
 */
public interface CreateUserUseCase {

    /**
     * Crea un nuevo usuario en el sistema
     *
     * @param request Datos del usuario a crear
     * @return Usuario creado con ID asignado
     * @throws UserAlreadyExistsException si el email ya está registrado
     * @throws InvalidUserDataException si los datos son inválidos
     */
    User createUser(RegisterRequest request);
}
```

**Puerto de Salida (UserRepositoryPort.java)**
```java
package acainfo.back.shared.application.ports.out;

import acainfo.back.shared.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de SALIDA (Repository)
 * Define cómo el dominio accede a la persistencia, sin saber CÓMO se implementa
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByStatus(UserStatus status);

    void delete(User user);

    boolean existsByEmail(String email);
}
```

**Servicio (UserService.java)**
```java
package acainfo.back.shared.application.services;

import acainfo.back.shared.application.mappers.UserDtoMapper;
import acainfo.back.shared.application.ports.in.CreateUserUseCase;
import acainfo.back.shared.application.ports.in.UpdateUserUseCase;
import acainfo.back.shared.application.ports.out.UserRepositoryPort;
import acainfo.back.shared.domain.exception.UserAlreadyExistsException;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.in.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación
 * IMPLEMENTA los puertos de entrada (use cases)
 * USA los puertos de salida (repositories)
 * TRABAJA SOLO con entidades de dominio
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService implements CreateUserUseCase, UpdateUserUseCase {

    // Dependencias de puertos (interfaces)
    private final UserRepositoryPort userRepository;
    private final UserDtoMapper userDtoMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(RegisterRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // Validación de negocio
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        // Mapear DTO → Domain
        User user = userDtoMapper.toDomain(request);

        // Lógica de negocio (encriptar password)
        User userWithEncryptedPassword = User.builder()
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(UserStatus.ACTIVE)
                .build();

        // Guardar usando el puerto
        User savedUser = userRepository.save(userWithEncryptedPassword);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Más métodos implementando use cases...
}
```

---

### 4. Adaptador de Repositorio

**UserRepositoryAdapter.java**
```java
package acainfo.back.shared.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.shared.application.ports.out.UserRepositoryPort;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.shared.infrastructure.adapters.out.persistence.mappers.UserJpaMapper;
import acainfo.back.shared.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de SALIDA
 * IMPLEMENTA el puerto de repositorio
 * USA el mapper para convertir Domain ↔ JPA
 * DELEGA las operaciones al JpaRepository
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;
    private final UserJpaMapper mapper;

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity = mapper.toJpaEntity(user);
        UserJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(User user) {
        UserJpaEntity jpaEntity = mapper.toJpaEntity(user);
        jpaRepository.delete(jpaEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
```

**UserJpaRepository.java (Spring Data JPA)**
```java
package acainfo.back.shared.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.shared.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA - Solo definición de queries
 * SIN lógica de negocio
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    List<UserJpaEntity> findByStatus(UserStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserJpaEntity u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<UserJpaEntity> findByEmailWithRoles(String email);
}
```

---

### 5. Controlador REST

**UserController.java**
```java
package acainfo.back.shared.infrastructure.adapters.in.rest;

import acainfo.back.shared.application.mappers.UserDtoMapper;
import acainfo.back.shared.application.ports.in.CreateUserUseCase;
import acainfo.back.shared.application.ports.in.GetUserUseCase;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.in.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controlador REST
 * USA los puertos de entrada (use cases)
 * USA el mapper de aplicación para convertir Domain ↔ DTO
 * NUNCA accede directamente a repositorios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    // Dependencias de use cases (interfaces)
    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UserDtoMapper userDtoMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest request) {
        log.info("REST Request to create user: {}", request.getEmail());

        // Ejecutar use case
        User createdUser = createUserUseCase.createUser(request);

        // Mapear Domain → DTO Response
        UserResponse response = userDtoMapper.toResponse(createdUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwner(#id)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("REST Request to get user by ID: {}", id);

        User user = getUserUseCase.getUserById(id);
        UserResponse response = userDtoMapper.toResponse(user);

        return ResponseEntity.ok(response);
    }

    // Más endpoints...
}
```

---

## 📦 Plan de Migración Módulo a Módulo

### Estrategia General
1. **Migración incremental** - Un módulo a la vez
2. **Tests como red de seguridad** - Escribir tests antes de migrar
3. **Feature flags** - Activar nueva arquitectura progresivamente
4. **Coexistencia temporal** - Antiguo y nuevo código pueden coexistir

### Secuencia de Migración (Orden por Dependencias)

```
Fase 1: Módulos Base (Sin dependencias externas)
  ├── 1.1 Shared (Users, Roles, Permissions) ← Primer módulo
  ├── 1.2 Subject                             ← Sin dependencias
  └── 1.3 Schedule (Classroom)                ← Solo depende de shared

Fase 2: Módulos de Gestión Académica
  ├── 2.1 SubjectGroup                         ← Depende: Subject, Shared
  ├── 2.2 Session                              ← Depende: SubjectGroup, Schedule
  └── 2.3 Material                             ← Depende: SubjectGroup, Shared

Fase 3: Módulos de Estudiantes
  ├── 3.1 Enrollment                           ← Depende: SubjectGroup, Shared
  ├── 3.2 Attendance                           ← Depende: Enrollment, Session
  └── 3.3 Payment                              ← Depende: Shared

Fase 4: Limpieza y Optimización
  └── 4.1 Refactorización global
  └── 4.2 Optimización de queries
  └── 4.3 Tests de integración E2E
```

---

## 🔧 Migración Detallada por Módulo

### MÓDULO 1.1: SHARED (Users, Roles, Permissions)

**Complejidad:** ⭐⭐⭐⭐⭐ (Alta - es la base)
**Esfuerzo Estimado:** 40 horas
**Prioridad:** 1 (Crítico - todos dependen de esto)

#### Entidades a migrar (5):
1. User
2. Role
3. Permission
4. RefreshToken
5. AuditLog

#### Estructura Actual
```
shared/
├── domain/model/
│   ├── User.java (@Entity)
│   ├── Role.java (@Entity)
│   ├── Permission.java (@Entity)
│   ├── RefreshToken.java (@Entity)
│   └── AuditLog.java (@Entity)
├── application/services/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── RefreshTokenService.java
│   └── AuditService.java
└── infrastructure/adapters/
    ├── in/rest/
    │   ├── AuthController.java
    │   ├── StudentController.java
    │   └── dto/ (16 DTOs)
    └── out/
        ├── UserRepository.java
        ├── RoleRepository.java
        ├── PermissionRepository.java
        ├── RefreshTokenRepository.java
        └── AuditLogRepository.java
```

#### Estructura Objetivo
```
shared/
├── domain/
│   ├── model/
│   │   ├── User.java (POJO puro)
│   │   ├── Role.java (POJO puro)
│   │   ├── Permission.java (POJO puro)
│   │   ├── RefreshToken.java (POJO puro)
│   │   └── AuditLog.java (POJO puro)
│   ├── exception/
│   │   ├── UserNotFoundException.java
│   │   ├── UserAlreadyExistsException.java
│   │   ├── InvalidCredentialsException.java
│   │   └── TokenExpiredException.java
│   └── validation/
│       └── UserValidator.java
├── application/
│   ├── ports/in/
│   │   ├── CreateUserUseCase.java
│   │   ├── UpdateUserUseCase.java
│   │   ├── GetUserUseCase.java
│   │   ├── DeleteUserUseCase.java
│   │   ├── LoginUseCase.java
│   │   ├── RegisterUseCase.java
│   │   └── RefreshTokenUseCase.java
│   ├── ports/out/
│   │   ├── UserRepositoryPort.java
│   │   ├── RoleRepositoryPort.java
│   │   ├── PermissionRepositoryPort.java
│   │   ├── RefreshTokenRepositoryPort.java
│   │   └── AuditLogRepositoryPort.java
│   ├── services/
│   │   ├── AuthService.java (implementa login/register use cases)
│   │   ├── UserService.java (implementa user CRUD use cases)
│   │   ├── RefreshTokenService.java
│   │   └── AuditService.java
│   └── mappers/
│       ├── UserDtoMapper.java
│       ├── RoleDtoMapper.java
│       ├── PermissionDtoMapper.java
│       ├── RefreshTokenDtoMapper.java
│       └── AuditLogDtoMapper.java
└── infrastructure/
    └── adapters/
        ├── in/rest/
        │   ├── AuthController.java
        │   ├── StudentController.java
        │   ├── TeacherController.java
        │   └── dto/ (16 DTOs - sin cambios)
        └── out/
            └── persistence/
                ├── entities/
                │   ├── UserJpaEntity.java
                │   ├── RoleJpaEntity.java
                │   ├── PermissionJpaEntity.java
                │   ├── RefreshTokenJpaEntity.java
                │   └── AuditLogJpaEntity.java
                ├── mappers/
                │   ├── UserJpaMapper.java
                │   ├── RoleJpaMapper.java
                │   ├── PermissionJpaMapper.java
                │   ├── RefreshTokenJpaMapper.java
                │   └── AuditLogJpaMapper.java
                ├── repositories/
                │   ├── UserJpaRepository.java
                │   ├── RoleJpaRepository.java
                │   ├── PermissionJpaRepository.java
                │   ├── RefreshTokenJpaRepository.java
                │   └── AuditLogJpaRepository.java
                └── adapters/
                    ├── UserRepositoryAdapter.java
                    ├── RoleRepositoryAdapter.java
                    ├── PermissionRepositoryAdapter.java
                    ├── RefreshTokenRepositoryAdapter.java
                    └── AuditLogRepositoryAdapter.java
```

#### Pasos de Migración (User como ejemplo)

**PASO 1: Crear entidad de dominio pura**
```bash
# Crear User.java (domain/model) - POJO puro
# - Sin @Entity, @Table, @Column
# - Solo lógica de negocio
# - Builder pattern
# - Métodos de dominio: isActive(), isStudent(), hasPermission(), etc.
```

**PASO 2: Crear entidad JPA**
```bash
# Crear UserJpaEntity.java (infrastructure/.../persistence/entities)
# - Con todas las anotaciones JPA
# - @Entity, @Table, @Column, relaciones
# - Solo Lombok: @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
# - SIN métodos de negocio
```

**PASO 3: Crear mapper JPA**
```bash
# Crear UserJpaMapper.java (infrastructure/.../persistence/mappers)
# - toDomain(UserJpaEntity): User
# - toJpaEntity(User): UserJpaEntity
# - Gestionar relaciones (roles, permissions)
```

**PASO 4: Crear mapper DTO**
```bash
# Crear UserDtoMapper.java (application/mappers)
# - toResponse(User): UserResponse
# - toDomain(RegisterRequest): User
# - updateDomainFromDto(User, UpdateRequest): User
```

**PASO 5: Crear puertos**
```bash
# Crear UserRepositoryPort.java (application/ports/out)
# - Definir métodos: save, findById, findByEmail, etc.

# Crear Use Cases (application/ports/in)
# - CreateUserUseCase.java
# - UpdateUserUseCase.java
# - GetUserUseCase.java
# - DeleteUserUseCase.java
```

**PASO 6: Crear adaptador de repositorio**
```bash
# Crear UserJpaRepository.java (interface Spring Data)
# - Queries JPA

# Crear UserRepositoryAdapter.java
# - Implementa UserRepositoryPort
# - Usa UserJpaMapper
# - Delega a UserJpaRepository
```

**PASO 7: Refactorizar servicios**
```bash
# Actualizar UserService.java
# - Implementar use cases (interfaces)
# - Usar UserRepositoryPort (no repository directo)
# - Trabajar con User (domain), no UserJpaEntity
# - Usar UserDtoMapper para conversiones
```

**PASO 8: Actualizar controladores**
```bash
# Actualizar UserController.java
# - Inyectar use cases (no servicios directos)
# - Usar UserDtoMapper
# - Eliminar imports de entidades
```

**PASO 9: Tests**
```bash
# Unit tests
# - UserTest.java (lógica de dominio)
# - UserServiceTest.java (use cases)
# - UserJpaMapperTest.java
# - UserDtoMapperTest.java

# Integration tests
# - UserControllerIntegrationTest.java
# - UserRepositoryAdapterIntegrationTest.java
```

**PASO 10: Migrar entidades relacionadas**
```bash
# Repetir pasos 1-9 para:
# - Role
# - Permission
# - RefreshToken
# - AuditLog
```

#### Archivos a crear (User + relacionados): 35 archivos nuevos

| Categoría | Archivos |
|-----------|----------|
| Domain Models | User.java, Role.java, Permission.java, RefreshToken.java, AuditLog.java (5) |
| Domain Exceptions | UserNotFoundException.java, UserAlreadyExistsException.java, etc. (4) |
| Use Case Ports (IN) | CreateUserUseCase.java, UpdateUserUseCase.java, GetUserUseCase.java, DeleteUserUseCase.java, LoginUseCase.java, RegisterUseCase.java, RefreshTokenUseCase.java (7) |
| Repository Ports (OUT) | UserRepositoryPort.java, RoleRepositoryPort.java, PermissionRepositoryPort.java, RefreshTokenRepositoryPort.java, AuditLogRepositoryPort.java (5) |
| JPA Entities | UserJpaEntity.java, RoleJpaEntity.java, PermissionJpaEntity.java, RefreshTokenJpaEntity.java, AuditLogJpaEntity.java (5) |
| JPA Mappers | UserJpaMapper.java, RoleJpaMapper.java, PermissionJpaMapper.java, RefreshTokenJpaMapper.java, AuditLogJpaMapper.java (5) |
| DTO Mappers | UserDtoMapper.java, RoleDtoMapper.java, PermissionDtoMapper.java, RefreshTokenDtoMapper.java, AuditLogDtoMapper.java (5) |
| Repository Adapters | UserRepositoryAdapter.java, RoleRepositoryAdapter.java, PermissionRepositoryAdapter.java, RefreshTokenRepositoryAdapter.java, AuditLogRepositoryAdapter.java (5) |
| Tests | 20+ test files |

#### Archivos a modificar: 10 archivos

| Archivo | Cambios |
|---------|---------|
| AuthService.java | Implementar use cases, usar ports, trabajar con domain models |
| UserService.java | Implementar use cases, usar ports, trabajar con domain models |
| RefreshTokenService.java | Implementar use cases, usar ports |
| AuditService.java | Usar ports |
| AuthController.java | Inyectar use cases, usar mappers |
| StudentController.java | Inyectar use cases, usar mappers |
| TeacherController.java | Inyectar use cases, usar mappers |
| AdminController.java | Inyectar use cases, usar mappers |
| UserJpaRepository.java | Renombrar a UserJpaRepository, trabajar con UserJpaEntity |
| ... otros repositorios | Renombrar, trabajar con JpaEntities |

#### Archivos a eliminar: 5 archivos

| Archivo | Razón |
|---------|-------|
| User.java (domain/model @Entity) | Reemplazado por User.java (POJO) + UserJpaEntity.java |
| Role.java (domain/model @Entity) | Reemplazado por Role.java (POJO) + RoleJpaEntity.java |
| Permission.java (domain/model @Entity) | Reemplazado por Permission.java (POJO) + PermissionJpaEntity.java |
| RefreshToken.java (domain/model @Entity) | Reemplazado por RefreshToken.java (POJO) + RefreshTokenJpaEntity.java |
| AuditLog.java (domain/model @Entity) | Reemplazado por AuditLog.java (POJO) + AuditLogJpaEntity.java |

---

### MÓDULO 1.2: SUBJECT (Asignaturas)

**Complejidad:** ⭐⭐⭐ (Media)
**Esfuerzo Estimado:** 16 horas
**Prioridad:** 2

#### Entidades a migrar (1):
1. Subject

#### Estructura Objetivo (resumen)
```
subject/
├── domain/model/
│   └── Subject.java (POJO puro)
├── application/
│   ├── ports/in/ (4 use cases)
│   ├── ports/out/ (1 port)
│   ├── services/SubjectService.java
│   └── mappers/SubjectDtoMapper.java
└── infrastructure/adapters/
    ├── in/rest/dto/ (4 DTOs - sin cambios)
    └── out/persistence/
        ├── entities/SubjectJpaEntity.java
        ├── mappers/SubjectJpaMapper.java
        ├── repositories/SubjectJpaRepository.java
        └── adapters/SubjectRepositoryAdapter.java
```

#### Archivos nuevos: 8
- Subject.java (domain - POJO)
- SubjectJpaEntity.java
- SubjectJpaMapper.java
- SubjectDtoMapper.java
- SubjectRepositoryPort.java
- CreateSubjectUseCase.java, UpdateSubjectUseCase.java, GetSubjectUseCase.java, DeleteSubjectUseCase.java
- SubjectRepositoryAdapter.java
- Tests (5+)

#### Archivos a modificar: 3
- SubjectService.java
- SubjectController.java
- SubjectRepository.java → renombrar a SubjectJpaRepository.java

---

### MÓDULO 1.3: SCHEDULE (Horarios + Aulas)

**Complejidad:** ⭐⭐⭐⭐ (Media-Alta)
**Esfuerzo Estimado:** 20 horas
**Prioridad:** 3

#### Entidades a migrar (2):
1. Schedule
2. Classroom

#### Consideraciones especiales:
- Lógica compleja de validación de conflictos
- ScheduleValidationService debe trabajar con domain models
- Relaciones con SubjectGroup

#### Archivos nuevos: 14
- Schedule.java, Classroom.java (domain - POJOs)
- ScheduleJpaEntity.java, ClassroomJpaEntity.java
- ScheduleJpaMapper.java, ClassroomJpaMapper.java
- ScheduleDtoMapper.java, ClassroomDtoMapper.java
- 2 Repository Ports, 4 Use Cases
- 2 Repository Adapters
- Tests (8+)

---

### MÓDULO 2.1: SUBJECTGROUP (Grupos)

**Complejidad:** ⭐⭐⭐⭐ (Alta)
**Esfuerzo Estimado:** 24 horas
**Prioridad:** 4

#### Entidades a migrar (1):
1. SubjectGroup

#### Consideraciones especiales:
- Relaciones con Subject, User (teacher), Schedule, Session
- Lógica de negocio compleja: incrementOccupancy(), isFull(), etc.
- Estas operaciones deben estar en el domain model

#### Archivos nuevos: 10
- SubjectGroup.java (domain - POJO con lógica de capacidad)
- SubjectGroupJpaEntity.java
- SubjectGroupJpaMapper.java
- SubjectGroupDtoMapper.java
- 1 Repository Port, 4 Use Cases
- 1 Repository Adapter
- Tests (6+)

---

### MÓDULO 2.2: SESSION (Sesiones)

**Complejidad:** ⭐⭐⭐⭐ (Alta)
**Esfuerzo Estimado:** 24 horas
**Prioridad:** 5

#### Entidades a migrar (1):
1. Session

#### Consideraciones especiales:
- Relaciones complejas con SubjectGroup, Schedule
- Gestión de sesiones de recuperación
- Cambios de modalidad (PRESENCIAL/DUAL/ONLINE)
- Notificaciones (integración con RabbitMQ)

#### Archivos nuevos: 12
- Session.java (domain)
- SessionJpaEntity.java
- SessionJpaMapper.java, SessionDtoMapper.java
- 1 Repository Port, 6 Use Cases
- 1 Repository Adapter
- Tests (8+)

---

### MÓDULO 2.3: MATERIAL (Materiales)

**Complejidad:** ⭐⭐⭐ (Media)
**Esfuerzo Estimado:** 16 horas
**Prioridad:** 6

#### Entidades a migrar (1):
1. Material

#### Consideraciones especiales:
- Integración con FileStorageService
- Control de acceso basado en pagos

#### Archivos nuevos: 10
- Material.java (domain)
- MaterialJpaEntity.java
- MaterialJpaMapper.java, MaterialDtoMapper.java
- 1 Repository Port, 3 Use Cases
- 1 Repository Adapter
- Tests (6+)

---

### MÓDULO 3.1: ENROLLMENT (Inscripciones)

**Complejidad:** ⭐⭐⭐⭐⭐ (Muy Alta)
**Esfuerzo Estimado:** 32 horas
**Prioridad:** 7

#### Entidades a migrar (2):
1. Enrollment
2. GroupRequest

#### Consideraciones especiales:
- Lógica de negocio MUY compleja:
  - Control de capacidad
  - Modo PRESENCIAL vs ONLINE
  - Cola de espera
  - Estudiantes con 2+ asignaturas → online si no hay plaza
- Transacciones críticas (control de concurrencia)

#### Archivos nuevos: 18
- Enrollment.java, GroupRequest.java (domain con lógica compleja)
- EnrollmentJpaEntity.java, GroupRequestJpaEntity.java
- 2 JPA Mappers, 2 DTO Mappers
- 2 Repository Ports, 6 Use Cases
- 2 Repository Adapters
- Tests (12+)

---

### MÓDULO 3.2: ATTENDANCE (Asistencia)

**Complejidad:** ⭐⭐⭐ (Media)
**Esfuerzo Estimado:** 16 horas
**Prioridad:** 8

#### Entidades a migrar (1):
1. Attendance

#### Archivos nuevos: 10
- Attendance.java (domain)
- AttendanceJpaEntity.java
- AttendanceJpaMapper.java, AttendanceDtoMapper.java
- 1 Repository Port, 4 Use Cases
- 1 Repository Adapter
- Tests (6+)

---

### MÓDULO 3.3: PAYMENT (Pagos)

**Complejidad:** ⭐⭐⭐⭐ (Alta)
**Esfuerzo Estimado:** 24 horas
**Prioridad:** 9

#### Entidades a migrar (1):
1. Payment

#### Consideraciones especiales:
- Integración con Stripe (webhooks)
- Lógica de bloqueo por impago
- Cálculo de devoluciones
- Tareas programadas (@Scheduled)

#### Archivos nuevos: 12
- Payment.java (domain con lógica de negocio)
- PaymentJpaEntity.java
- PaymentJpaMapper.java, PaymentDtoMapper.java
- 1 Repository Port, 5 Use Cases
- 1 Repository Adapter
- Tests (8+)

---

## 📊 Resumen de Esfuerzo Total

| Módulo | Entidades | Complejidad | Horas | Archivos Nuevos | Archivos Modificados |
|--------|-----------|-------------|-------|-----------------|---------------------|
| 1.1 Shared | 5 | ⭐⭐⭐⭐⭐ | 40 | 35 | 10 |
| 1.2 Subject | 1 | ⭐⭐⭐ | 16 | 8 | 3 |
| 1.3 Schedule | 2 | ⭐⭐⭐⭐ | 20 | 14 | 5 |
| 2.1 SubjectGroup | 1 | ⭐⭐⭐⭐ | 24 | 10 | 4 |
| 2.2 Session | 1 | ⭐⭐⭐⭐ | 24 | 12 | 5 |
| 2.3 Material | 1 | ⭐⭐⭐ | 16 | 10 | 4 |
| 3.1 Enrollment | 2 | ⭐⭐⭐⭐⭐ | 32 | 18 | 6 |
| 3.2 Attendance | 1 | ⭐⭐⭐ | 16 | 10 | 4 |
| 3.3 Payment | 1 | ⭐⭐⭐⭐ | 24 | 12 | 5 |
| **SUBTOTAL** | **14** | - | **212** | **129** | **46** |
| **Fase 4: Limpieza** | - | - | **20** | - | - |
| **TOTAL** | **14** | - | **232 horas** | **129** | **46** |

**Estimación:** 5-6 semanas de trabajo dedicado (40h/semana)

---

## 🛡️ Estrategia de Testing

### Tipos de Tests a Crear

1. **Domain Model Tests**
   - Tests unitarios de lógica de negocio
   - Ejemplo: `UserTest.java` → testIsActive(), testHasPermission(), etc.

2. **Mapper Tests**
   - Tests de conversión JPA ↔ Domain
   - Tests de conversión Domain ↔ DTO
   - Verificar que no se pierden datos

3. **Service Tests (Use Cases)**
   - Mockear puertos
   - Verificar lógica de aplicación

4. **Repository Adapter Tests**
   - Tests de integración con H2
   - Verificar que el mapper funciona correctamente

5. **Controller Tests**
   - Tests de integración REST
   - MockMvc

6. **E2E Tests**
   - Flujos completos
   - Ejemplo: Registro → Login → Inscripción → Pago

### Cobertura Objetivo
- Domain: 90%+
- Services: 85%+
- Adapters: 80%+
- Global: 85%+

---

## 🚀 Estrategia de Despliegue

### Opción 1: Feature Flag (Recomendado)

```java
@Configuration
public class ArchitectureConfig {

    @Value("${app.hexagonal.pure.enabled:false}")
    private boolean hexagonalPureEnabled;

    @Bean
    @ConditionalOnProperty(name = "app.hexagonal.pure.enabled", havingValue = "true")
    public UserRepositoryPort userRepositoryPortNew() {
        return new UserRepositoryAdapter(...);
    }

    @Bean
    @ConditionalOnProperty(name = "app.hexagonal.pure.enabled", havingValue = "false", matchIfMissing = true)
    public UserRepositoryPort userRepositoryPortLegacy() {
        return new UserRepositoryLegacyAdapter(...);
    }
}
```

**Ventajas:**
- Rollback instantáneo si hay problemas
- Migración gradual módulo a módulo
- Testing en producción con % de usuarios

### Opción 2: Branch de Migración

- Crear branch `feature/hexagonal-pure-migration`
- Migrar todo
- Merge cuando esté completo

**Ventajas:**
- Limpio, sin código legacy
- Merge atómico

**Desventajas:**
- Branch grande, difícil de mantener
- Merge conflicts

### Recomendación

Usar **Opción 1** (Feature Flag) para migración incremental y segura.

---

## 📝 Checklist de Migración por Módulo

### Template de Checklist

```markdown
## Módulo: [NOMBRE]

### Domain Layer
- [ ] Crear entidad de dominio pura (POJO)
- [ ] Implementar lógica de negocio
- [ ] Crear excepciones de dominio
- [ ] Tests de domain model (90%+ cobertura)

### Application Layer
- [ ] Definir puertos IN (use cases)
- [ ] Definir puertos OUT (repositories)
- [ ] Refactorizar servicio para implementar use cases
- [ ] Refactorizar servicio para usar puertos OUT
- [ ] Crear mapper DTO (Domain ↔ DTO)
- [ ] Tests de servicios (85%+ cobertura)
- [ ] Tests de mappers DTO

### Infrastructure Layer - Persistence
- [ ] Crear entidad JPA
- [ ] Crear mapper JPA (Domain ↔ JPA)
- [ ] Renombrar repository a JpaRepository
- [ ] Crear repository adapter
- [ ] Tests de mapper JPA
- [ ] Tests de repository adapter (integración)

### Infrastructure Layer - REST
- [ ] Refactorizar controlador para usar use cases
- [ ] Refactorizar controlador para usar mapper DTO
- [ ] Eliminar imports de entidades
- [ ] Tests de controlador (integración)

### Cleanup
- [ ] Eliminar entidad @Entity antigua
- [ ] Actualizar imports en todo el módulo
- [ ] Eliminar código muerto
- [ ] Verificar que no quedan dependencias legacy

### Validation
- [ ] Tests E2E pasando
- [ ] Performance sin degradación
- [ ] Sin regresiones funcionales
- [ ] Code review aprobado
```

---

## 🎯 Beneficios de la Migración

### 1. Independencia del Framework
- Domain models sin anotaciones JPA
- Cambiar de JPA a MongoDB sin tocar el dominio
- Testear dominio sin Spring

### 2. Testabilidad
- Unit tests rápidos (sin DB)
- Mockear puertos fácilmente
- Tests de domain sin framework

### 3. Claridad Arquitectónica
- Separación clara de responsabilidades
- Flujo de datos explícito
- Fácil onboarding de nuevos devs

### 4. Escalabilidad
- Fácil agregar nuevos adaptadores (GraphQL, gRPC)
- Fácil agregar nuevos repositorios (Redis, Elasticsearch)
- Lógica de negocio centralizada

### 5. Mantenibilidad
- Cambios en persistencia no afectan dominio
- Cambios en API no afectan dominio
- Código más limpio y legible

---

## 🔍 Ejemplo Completo: Migración de User

Ver secciones anteriores para código completo de:
1. User (domain/model) - POJO puro
2. UserJpaEntity (infrastructure) - Entidad JPA
3. UserJpaMapper - Domain ↔ JPA
4. UserDtoMapper - Domain ↔ DTO
5. UserRepositoryPort - Puerto OUT
6. CreateUserUseCase - Puerto IN
7. UserService - Implementa use cases
8. UserRepositoryAdapter - Implementa puerto
9. UserController - Usa use cases

---

## 📚 Referencias y Recursos

### Libros
- "Clean Architecture" - Robert C. Martin
- "Implementing Domain-Driven Design" - Vaughn Vernon
- "Get Your Hands Dirty on Clean Architecture" - Tom Hombergs

### Artículos
- [Hexagonal Architecture with Java and Spring](https://reflectoring.io/spring-hexagonal/)
- [DDD, Hexagonal, Onion, Clean, CQRS... How I put it all together](https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/)

### Ejemplos
- [Spring Boot Hexagonal Architecture Example](https://github.com/thombergs/buckpal)

---

## ✅ Próximos Pasos

1. **Revisar este plan con el equipo**
2. **Aprobar la estrategia de migración**
3. **Configurar entorno de desarrollo**
4. **Comenzar con Módulo 1.1 (Shared)**
5. **Iterar módulo a módulo**

---

## 📞 Soporte

Para dudas sobre la migración, consultar:
- Documentación de Spring Boot Hexagonal
- Clean Architecture guidelines
- DDD patterns

---

*Documento creado por Claude Code - 22 Noviembre 2025*
*Versión: 1.0*
