# FASE 2: Gestión Académica - PLANIFICACIÓN

**Estado:** 📋 PLANIFICADA - LISTA PARA INICIAR
**Duración:** 2 semanas (67 horas)
**Fecha de inicio:** Diciembre 2024

---

## 📋 Resumen Ejecutivo

La Fase 2 implementará los módulos de gestión académica del sistema: **Subject (Asignaturas)**, **Group (Grupos)** y **Schedule (Horarios)**, siguiendo la arquitectura hexagonal pura establecida en la Fase 1.

### Objetivos de la Fase 2

🎯 Implementar módulo Subject con CRUD completo
🎯 Implementar módulo Group con validación de capacidad (24 plazas) y máximo 3 grupos por asignatura
🎯 Implementar módulo Schedule con detección de conflictos horarios
🎯 Integrar los 3 módulos con relaciones Subject → Group → Schedule
🎯 Tests unitarios e integración para cada módulo
🎯 API REST completa con filtros avanzados (Specifications)

---

## 🗓️ Planificación en Hitos

La Fase 2 se divide en **10 hitos incrementales** que permiten avanzar de forma ordenada y verificable.

---

## 📚 MÓDULO SUBJECT (Asignaturas)

### **HITO 2.1: Subject - Dominio y Aplicación** ⏱️ 8 horas

#### Objetivos
- Crear el núcleo de dominio del módulo Subject
- Definir entidades, excepciones y reglas de negocio
- Crear use cases y puertos
- Tests unitarios del dominio

#### Tareas Detalladas

**1. Entidades de Dominio** (2h)

```java
// subject/domain/model/Subject.java
public class Subject {
    private Long id;
    private String code;           // Código único: "ING101"
    private String name;           // "Programación I"
    private String description;
    private Integer credits;       // 6 créditos
    private Degree degree;         // Ingeniería Informática
    private SubjectStatus status;  // ACTIVE, INACTIVE

    // Métodos de dominio
    public boolean isActive() { }
    public boolean canCreateGroup() { }
    public int getCurrentGroupCount() { }
}

// subject/domain/model/SubjectStatus.java
public enum SubjectStatus {
    ACTIVE,      // Asignatura activa
    INACTIVE,    // Asignatura inactiva
    ARCHIVED     // Asignatura archivada
}

// subject/domain/model/Degree.java
public enum Degree {
    INGENIERIA_INFORMATICA("Ingeniería Informática"),
    INGENIERIA_SOFTWARE("Ingeniería de Software"),
    CIENCIAS_COMPUTACION("Ciencias de la Computación");

    private final String displayName;
}
```

**2. Excepciones de Dominio** (1h)

```java
// subject/domain/exception/SubjectNotFoundException.java
public class SubjectNotFoundException extends NotFoundException {
    public SubjectNotFoundException(Long id)
    public SubjectNotFoundException(String code)
}

// subject/domain/exception/DuplicateSubjectCodeException.java
public class DuplicateSubjectCodeException extends BusinessRuleException {
    public DuplicateSubjectCodeException(String code)
}

// subject/domain/exception/InvalidSubjectDataException.java
public class InvalidSubjectDataException extends ValidationException {
    public InvalidSubjectDataException(String message)
}
```

**3. Validaciones de Negocio** (1h)

```java
// subject/domain/validation/SubjectBusinessRules.java
public class SubjectBusinessRules {
    public static final int MIN_CREDITS = 3;
    public static final int MAX_CREDITS = 12;
    public static final int MAX_GROUPS_PER_SUBJECT = 3;

    public static void validateCredits(Integer credits) {
        if (credits < MIN_CREDITS || credits > MAX_CREDITS) {
            throw new InvalidSubjectDataException(
                "Credits must be between " + MIN_CREDITS + " and " + MAX_CREDITS
            );
        }
    }

    public static void validateCode(String code) {
        // Format: 3 letters + 3 digits (e.g., ING101)
        if (!code.matches("^[A-Z]{3}\\d{3}$")) {
            throw new InvalidSubjectDataException("Invalid subject code format");
        }
    }
}
```

**4. Use Cases (Puertos de Entrada)** (2h)

```java
// subject/application/port/in/CreateSubjectUseCase.java
public interface CreateSubjectUseCase {
    Subject create(CreateSubjectCommand command);
}

// subject/application/port/in/UpdateSubjectUseCase.java
public interface UpdateSubjectUseCase {
    Subject update(Long id, UpdateSubjectCommand command);
}

// subject/application/port/in/GetSubjectUseCase.java
public interface GetSubjectUseCase {
    Subject getById(Long id);
    Subject getByCode(String code);
    Page<Subject> findWithFilters(SubjectFilters filters);
}

// subject/application/port/in/DeleteSubjectUseCase.java
public interface DeleteSubjectUseCase {
    void delete(Long id);
    void archive(Long id);
}
```

**5. DTOs de Aplicación** (1h)

```java
// subject/application/dto/CreateSubjectCommand.java
public record CreateSubjectCommand(
    String code,
    String name,
    String description,
    Integer credits,
    Degree degree
) {}

// subject/application/dto/UpdateSubjectCommand.java
public record UpdateSubjectCommand(
    String name,
    String description,
    Integer credits,
    SubjectStatus status
) {}

// subject/application/dto/SubjectFilters.java
public record SubjectFilters(
    String code,
    String searchTerm,      // Buscar en code, name, description
    Degree degree,
    SubjectStatus status,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {}
```

**6. Puerto de Salida (Repository Port)** (0.5h)

```java
// subject/application/port/out/SubjectRepositoryPort.java
public interface SubjectRepositoryPort {
    Subject save(Subject subject);
    Optional<Subject> findById(Long id);
    Optional<Subject> findByCode(String code);
    Page<Subject> findWithFilters(SubjectFilters filters);
    boolean existsByCode(String code);
    void delete(Long id);
}
```

**7. Tests Unitarios de Dominio** (1.5h)

```java
// test/.../domain/model/SubjectTest.java
@Test void testCreateSubject()
@Test void testIsActive()
@Test void testCanCreateGroup()
@Test void testSubjectEquality()

// test/.../domain/model/SubjectStatusTest.java
@Test void testSubjectStatusValues()

// test/.../domain/model/DegreeTest.java
@Test void testDegreeValues()
@Test void testDegreeDisplayName()

// test/.../domain/validation/SubjectBusinessRulesTest.java
@Test void testValidateCredits_Valid()
@Test void testValidateCredits_TooLow()
@Test void testValidateCredits_TooHigh()
@Test void testValidateCode_Valid()
@Test void testValidateCode_Invalid()
```

#### Entregables del Hito 2.1
- [ ] 3 Entidades de dominio (Subject, SubjectStatus, Degree)
- [ ] 3 Excepciones de dominio
- [ ] 1 Clase de validación (SubjectBusinessRules)
- [ ] 4 Use Cases (interfaces)
- [ ] 3 DTOs de comando/query
- [ ] 1 Repository Port
- [ ] 4 Clases de test con >10 tests

---

### **HITO 2.2: Subject - Infraestructura JPA** ⏱️ 8 horas

#### Objetivos
- Implementar persistencia con JPA
- Crear mappers entre dominio y JPA
- Implementar adaptador de repositorio
- Crear Specifications para filtros dinámicos

#### Tareas Detalladas

**1. Entidades JPA** (2h)

```java
// subject/infrastructure/.../entity/SubjectJpaEntity.java
@Entity
@Table(name = "subjects", uniqueConstraints = {
    @UniqueConstraint(name = "uk_subject_code", columnNames = "code")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubjectJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 6)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer credits;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Degree degree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubjectStatus status = SubjectStatus.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Relación con Groups (se implementa en Hito 2.5)
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private Set<SubjectGroupJpaEntity> groups = new HashSet<>();
}
```

**2. Mapper de Persistencia (MapStruct)** (1.5h)

```java
// subject/infrastructure/mapper/SubjectPersistenceMapper.java
@Mapper(componentModel = "spring")
public interface SubjectPersistenceMapper {

    Subject toDomain(SubjectJpaEntity entity);

    SubjectJpaEntity toEntity(Subject domain);

    List<Subject> toDomainList(List<SubjectJpaEntity> entities);

    // Custom mapping si es necesario
    @Mapping(target = "groups", ignore = true)
    SubjectJpaEntity toEntityWithoutGroups(Subject domain);
}
```

**3. JPA Repository Interface** (0.5h)

```java
// subject/infrastructure/.../repository/JpaSubjectRepository.java
@Repository
public interface JpaSubjectRepository
        extends JpaRepository<SubjectJpaEntity, Long>,
                JpaSpecificationExecutor<SubjectJpaEntity> {

    Optional<SubjectJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT COUNT(g) FROM SubjectGroupJpaEntity g WHERE g.subject.id = :subjectId")
    int countGroupsBySubjectId(@Param("subjectId") Long subjectId);
}
```

**4. Repository Adapter** (2h)

```java
// subject/infrastructure/.../repository/SubjectRepositoryAdapter.java
@Component
@RequiredArgsConstructor
public class SubjectRepositoryAdapter implements SubjectRepositoryPort {

    private final JpaSubjectRepository jpaRepository;
    private final SubjectPersistenceMapper mapper;

    @Override
    public Subject save(Subject subject) {
        SubjectJpaEntity entity = mapper.toEntity(subject);
        SubjectJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Subject> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Subject> findByCode(String code) {
        return jpaRepository.findByCode(code)
            .map(mapper::toDomain);
    }

    @Override
    public Page<Subject> findWithFilters(SubjectFilters filters) {
        Specification<SubjectJpaEntity> spec = SubjectSpecifications.withFilters(filters);
        Pageable pageable = PageRequest.of(
            filters.page(),
            filters.size(),
            Sort.by(
                Sort.Direction.fromString(filters.sortDirection()),
                filters.sortBy()
            )
        );

        Page<SubjectJpaEntity> page = jpaRepository.findAll(spec, pageable);
        return page.map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }
}
```

**5. Specifications (Criteria Builder)** (2h)

```java
// subject/infrastructure/.../specification/SubjectSpecifications.java
public class SubjectSpecifications {

    public static Specification<SubjectJpaEntity> withFilters(SubjectFilters filters) {
        return Specification.where(hasCode(filters.code()))
            .and(searchByTerm(filters.searchTerm()))
            .and(hasDegree(filters.degree()))
            .and(hasStatus(filters.status()));
    }

    private static Specification<SubjectJpaEntity> hasCode(String code) {
        return (root, query, cb) ->
            code == null ? null : cb.equal(root.get("code"), code);
    }

    private static Specification<SubjectJpaEntity> searchByTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return null;
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("code")), pattern),
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private static Specification<SubjectJpaEntity> hasDegree(Degree degree) {
        return (root, query, cb) ->
            degree == null ? null : cb.equal(root.get("degree"), degree);
    }

    private static Specification<SubjectJpaEntity> hasStatus(SubjectStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }
}
```

**6. Tests de Integración (Repositorio)** (1h)

```java
// test/.../infrastructure/adapter/out/persistence/SubjectRepositoryAdapterIntegrationTest.java
@DataJpaTest
@Import({SubjectRepositoryAdapter.class, SubjectPersistenceMapperImpl.class})
class SubjectRepositoryAdapterIntegrationTest {

    @Test void testSaveAndFindById()
    @Test void testFindByCode()
    @Test void testExistsByCode()
    @Test void testFindWithFilters_ByCode()
    @Test void testFindWithFilters_BySearchTerm()
    @Test void testFindWithFilters_ByDegree()
    @Test void testFindWithFilters_Pagination()
}
```

#### Entregables del Hito 2.2
- [ ] 1 Entidad JPA (SubjectJpaEntity)
- [ ] 1 Mapper de persistencia (MapStruct)
- [ ] 1 JPA Repository interface
- [ ] 1 Repository Adapter implementado
- [ ] 1 Clase de Specifications con 4 filtros
- [ ] Tests de integración del repositorio

---

### **HITO 2.3: Subject - REST API y Servicio** ⏱️ 6 horas

#### Objetivos
- Implementar servicio de aplicación
- Crear API REST con DTOs
- Documentar con OpenAPI
- Tests unitarios del servicio

#### Tareas Detalladas

**1. Servicio de Aplicación** (2h)

```java
// subject/application/service/SubjectService.java
@Service
@RequiredArgsConstructor
@Transactional
public class SubjectService implements
        CreateSubjectUseCase,
        UpdateSubjectUseCase,
        GetSubjectUseCase,
        DeleteSubjectUseCase {

    private final SubjectRepositoryPort subjectRepository;

    @Override
    public Subject create(CreateSubjectCommand command) {
        // Validar código único
        if (subjectRepository.existsByCode(command.code())) {
            throw new DuplicateSubjectCodeException(command.code());
        }

        // Validar reglas de negocio
        SubjectBusinessRules.validateCode(command.code());
        SubjectBusinessRules.validateCredits(command.credits());

        // Crear subject
        Subject subject = new Subject();
        subject.setCode(command.code().toUpperCase());
        subject.setName(command.name());
        subject.setDescription(command.description());
        subject.setCredits(command.credits());
        subject.setDegree(command.degree());
        subject.setStatus(SubjectStatus.ACTIVE);

        return subjectRepository.save(subject);
    }

    @Override
    public Subject update(Long id, UpdateSubjectCommand command) {
        Subject subject = getById(id);

        // Validar reglas de negocio
        if (command.credits() != null) {
            SubjectBusinessRules.validateCredits(command.credits());
        }

        // Actualizar campos
        if (command.name() != null) {
            subject.setName(command.name());
        }
        if (command.description() != null) {
            subject.setDescription(command.description());
        }
        if (command.credits() != null) {
            subject.setCredits(command.credits());
        }
        if (command.status() != null) {
            subject.setStatus(command.status());
        }

        return subjectRepository.save(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getById(Long id) {
        return subjectRepository.findById(id)
            .orElseThrow(() -> new SubjectNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getByCode(String code) {
        return subjectRepository.findByCode(code)
            .orElseThrow(() -> new SubjectNotFoundException(code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Subject> findWithFilters(SubjectFilters filters) {
        return subjectRepository.findWithFilters(filters);
    }

    @Override
    public void delete(Long id) {
        Subject subject = getById(id);
        // Verificar que no tenga grupos activos
        // (se implementa cuando exista Group)
        subjectRepository.delete(id);
    }

    @Override
    public void archive(Long id) {
        Subject subject = getById(id);
        subject.setStatus(SubjectStatus.ARCHIVED);
        subjectRepository.save(subject);
    }
}
```

**2. DTOs REST** (1h)

```java
// subject/infrastructure/adapter/in/rest/dto/SubjectRequest.java
public record SubjectRequest(
    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Z]{3}\\d{3}$", message = "Code must be 3 letters + 3 digits")
    String code,

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    String name,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    @NotNull(message = "Credits is required")
    @Min(value = 3, message = "Minimum credits is 3")
    @Max(value = 12, message = "Maximum credits is 12")
    Integer credits,

    @NotNull(message = "Degree is required")
    Degree degree
) {}

// subject/infrastructure/adapter/in/rest/dto/UpdateSubjectRequest.java
public record UpdateSubjectRequest(
    @Size(max = 100)
    String name,

    @Size(max = 500)
    String description,

    @Min(3) @Max(12)
    Integer credits,

    SubjectStatus status
) {}

// subject/infrastructure/adapter/in/rest/dto/SubjectResponse.java
public record SubjectResponse(
    Long id,
    String code,
    String name,
    String description,
    Integer credits,
    Degree degree,
    SubjectStatus status,
    Integer groupCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

**3. Mapper REST** (0.5h)

```java
// subject/infrastructure/mapper/SubjectRestMapper.java
@Mapper(componentModel = "spring")
public interface SubjectRestMapper {

    CreateSubjectCommand toCreateCommand(SubjectRequest request);

    UpdateSubjectCommand toUpdateCommand(UpdateSubjectRequest request);

    @Mapping(target = "groupCount", expression = "java(0)")
    SubjectResponse toResponse(Subject subject);

    default PageResponse<SubjectResponse> toPageResponse(Page<Subject> page) {
        return PageResponse.of(page.map(this::toResponse));
    }
}
```

**4. Controller REST** (1.5h)

```java
// subject/infrastructure/adapter/in/rest/SubjectController.java
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subjects", description = "Subject management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubjectController {

    private final SubjectService subjectService;
    private final SubjectRestMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create subject", description = "Creates new subject (ADMIN only)")
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        log.info("Create subject request: {}", request.code());

        CreateSubjectCommand command = mapper.toCreateCommand(request);
        Subject subject = subjectService.create(command);
        SubjectResponse response = mapper.toResponse(subject);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List subjects", description = "Returns paginated subjects with filters")
    public ResponseEntity<PageResponse<SubjectResponse>> list(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Degree degree,
            @RequestParam(required = false) SubjectStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        SubjectFilters filters = new SubjectFilters(
            code, searchTerm, degree, status, page, size, sortBy, sortDirection
        );

        Page<Subject> subjects = subjectService.findWithFilters(filters);
        PageResponse<SubjectResponse> response = mapper.toPageResponse(subjects);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get subject by ID")
    public ResponseEntity<SubjectResponse> getById(@PathVariable Long id) {
        Subject subject = subjectService.getById(id);
        return ResponseEntity.ok(mapper.toResponse(subject));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get subject by code")
    public ResponseEntity<SubjectResponse> getByCode(@PathVariable String code) {
        Subject subject = subjectService.getByCode(code);
        return ResponseEntity.ok(mapper.toResponse(subject));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update subject")
    public ResponseEntity<SubjectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {

        UpdateSubjectCommand command = mapper.toUpdateCommand(request);
        Subject subject = subjectService.update(id, command);
        return ResponseEntity.ok(mapper.toResponse(subject));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete subject")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.ok(MessageResponse.of("Subject deleted successfully"));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Archive subject")
    public ResponseEntity<MessageResponse> archive(@PathVariable Long id) {
        subjectService.archive(id);
        return ResponseEntity.ok(MessageResponse.of("Subject archived successfully"));
    }
}
```

**5. Tests Unitarios del Servicio** (1h)

```java
// test/.../application/service/SubjectServiceTest.java
@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock private SubjectRepositoryPort subjectRepository;
    @InjectMocks private SubjectService subjectService;

    @Test void testCreate_Success()
    @Test void testCreate_DuplicateCode()
    @Test void testCreate_InvalidCredits()
    @Test void testUpdate_Success()
    @Test void testGetById_Success()
    @Test void testGetById_NotFound()
    @Test void testGetByCode_Success()
    @Test void testFindWithFilters()
    @Test void testDelete_Success()
    @Test void testArchive_Success()
}
```

#### Entregables del Hito 2.3
- [ ] 1 Service implementado (SubjectService)
- [ ] 3 DTOs REST (Request, UpdateRequest, Response)
- [ ] 1 Mapper REST (MapStruct)
- [ ] 1 Controller con 7 endpoints
- [ ] Documentación OpenAPI completa
- [ ] Tests unitarios del servicio (>10 tests)

---

## 👥 MÓDULO GROUP (Grupos)

### **HITO 2.4: Group - Dominio y Aplicación** ⏱️ 9 horas

#### Objetivos
- Crear dominio del módulo Group con relación a Subject
- Implementar reglas de negocio: máximo 3 grupos por asignatura, capacidad 24
- Crear use cases y DTOs
- Tests unitarios

#### Tareas Detalladas

**1. Entidades de Dominio** (3h)

```java
// group/domain/model/SubjectGroup.java
public class SubjectGroup {
    private Long id;
    private String name;                 // "Grupo A"
    private Long subjectId;              // Referencia a Subject
    private String subjectCode;          // Desnormalizado para consultas
    private Long teacherId;              // Profesor asignado
    private GroupType type;              // THEORETICAL, PRACTICAL, LAB
    private GroupStatus status;          // OPEN, CLOSED, FULL, CANCELLED
    private AcademicPeriod academicPeriod;
    private Integer capacity = 24;       // Capacidad máxima
    private Integer currentEnrollments;  // Inscritos actuales

    // Métodos de dominio
    public boolean isFull() {
        return currentEnrollments >= capacity;
    }

    public boolean canEnroll() {
        return status == GroupStatus.OPEN && !isFull();
    }

    public boolean isOpen() {
        return status == GroupStatus.OPEN;
    }

    public int getAvailableSeats() {
        return capacity - currentEnrollments;
    }
}

// group/domain/model/GroupType.java
public enum GroupType {
    THEORETICAL("Teórico"),
    PRACTICAL("Práctico"),
    LAB("Laboratorio"),
    THEORETICAL_PRACTICAL("Teórico-Práctico");

    private final String displayName;
}

// group/domain/model/GroupStatus.java
public enum GroupStatus {
    OPEN,        // Abierto para inscripciones
    CLOSED,      // Cerrado, no acepta inscripciones
    FULL,        // Lleno (capacidad máxima)
    CANCELLED,   // Cancelado
    ARCHIVED     // Archivado
}

// group/domain/model/AcademicPeriod.java
public class AcademicPeriod {
    private String year;              // "2024"
    private Semester semester;        // FIRST, SECOND
    private LocalDate startDate;
    private LocalDate endDate;

    public enum Semester {
        FIRST("Primer Cuatrimestre"),
        SECOND("Segundo Cuatrimestre");
        private final String displayName;
    }
}
```

**2. Excepciones de Dominio** (1h)

```java
// group/domain/exception/GroupNotFoundException.java
public class GroupNotFoundException extends NotFoundException

// group/domain/exception/MaxGroupsPerSubjectException.java
public class MaxGroupsPerSubjectException extends BusinessRuleException

// group/domain/exception/GroupFullException.java
public class GroupFullException extends BusinessRuleException

// group/domain/exception/InvalidGroupStatusException.java
public class InvalidGroupStatusException extends BusinessRuleException
```

**3. Reglas de Negocio** (2h)

```java
// group/domain/validation/GroupBusinessRules.java
public class GroupBusinessRules {
    public static final int MAX_GROUPS_PER_SUBJECT = 3;
    public static final int DEFAULT_CAPACITY = 24;
    public static final int MIN_CAPACITY = 10;
    public static final int MAX_CAPACITY = 30;

    public static void validateNewGroup(int currentGroupCount) {
        if (currentGroupCount >= MAX_GROUPS_PER_SUBJECT) {
            throw new MaxGroupsPerSubjectException(
                "Cannot create more than " + MAX_GROUPS_PER_SUBJECT +
                " groups per subject"
            );
        }
    }

    public static void validateCapacity(Integer capacity) {
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY) {
            throw new InvalidGroupDataException(
                "Capacity must be between " + MIN_CAPACITY +
                " and " + MAX_CAPACITY
            );
        }
    }

    public static void validateEnrollment(SubjectGroup group) {
        if (!group.canEnroll()) {
            if (group.isFull()) {
                throw new GroupFullException(group.getId());
            }
            throw new InvalidGroupStatusException(
                "Group is not open for enrollment"
            );
        }
    }
}
```

**4. Use Cases** (2h)

```java
// group/application/port/in/CreateGroupUseCase.java
public interface CreateGroupUseCase {
    SubjectGroup create(CreateGroupCommand command);
}

// group/application/port/in/UpdateGroupUseCase.java
public interface UpdateGroupUseCase {
    SubjectGroup update(Long id, UpdateGroupCommand command);
}

// group/application/port/in/GetGroupUseCase.java
public interface GetGroupUseCase {
    SubjectGroup getById(Long id);
    Page<SubjectGroup> findWithFilters(GroupFilters filters);
}

// group/application/port/in/ManageGroupStatusUseCase.java
public interface ManageGroupStatusUseCase {
    void closeGroup(Long id);
    void openGroup(Long id);
    void cancelGroup(Long id);
}
```

**5. DTOs y Repository Port** (1h)

```java
// group/application/dto/CreateGroupCommand.java
public record CreateGroupCommand(
    String name,
    Long subjectId,
    Long teacherId,
    GroupType type,
    Integer capacity,
    AcademicPeriod academicPeriod
) {}

// group/application/dto/UpdateGroupCommand.java
public record UpdateGroupCommand(
    String name,
    Long teacherId,
    Integer capacity,
    GroupStatus status
) {}

// group/application/dto/GroupFilters.java
public record GroupFilters(
    Long subjectId,
    Long teacherId,
    GroupType type,
    GroupStatus status,
    String academicYear,
    AcademicPeriod.Semester semester,
    String searchTerm,
    Integer page,
    Integer size,
    String sortBy,
    String sortDirection
) {}

// group/application/port/out/GroupRepositoryPort.java
public interface GroupRepositoryPort {
    SubjectGroup save(SubjectGroup group);
    Optional<SubjectGroup> findById(Long id);
    Page<SubjectGroup> findWithFilters(GroupFilters filters);
    int countBySubjectId(Long subjectId);
    void delete(Long id);
}
```

#### Entregables del Hito 2.4
- [ ] 4 Entidades de dominio (SubjectGroup, GroupType, GroupStatus, AcademicPeriod)
- [ ] 4 Excepciones de dominio
- [ ] 1 Clase GroupBusinessRules con validaciones
- [ ] 4 Use Cases
- [ ] 3 DTOs de comando/query
- [ ] 1 Repository Port
- [ ] Tests unitarios de dominio

---

### **HITO 2.5: Group - Infraestructura JPA** ⏱️ 8 horas

#### Objetivos
- Implementar persistencia con relación a Subject
- Crear mappers y adapters
- Specifications para filtros complejos

#### Tareas Detalladas

**1. Entidad JPA con Relación** (2.5h)

```java
// group/infrastructure/.../entity/SubjectGroupJpaEntity.java
@Entity
@Table(name = "subject_groups")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubjectGroupJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    // Relación ManyToOne con Subject
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectJpaEntity subject;

    // Relación con Teacher (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private UserJpaEntity teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GroupType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupStatus status = GroupStatus.OPEN;

    @Column(nullable = false)
    private Integer capacity = 24;

    @Column(nullable = false)
    private Integer currentEnrollments = 0;

    // Academic Period (embedded)
    @Embedded
    private AcademicPeriodEmbeddable academicPeriod;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

// AcademicPeriodEmbeddable.java
@Embeddable
@Getter @Setter
public class AcademicPeriodEmbeddable {

    @Column(name = "academic_year", nullable = false, length = 4)
    private String year;

    @Enumerated(EnumType.STRING)
    @Column(name = "semester", nullable = false, length = 20)
    private AcademicPeriod.Semester semester;

    @Column(name = "period_start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate endDate;
}
```

**2. Mappers** (2h)

```java
// group/infrastructure/mapper/GroupPersistenceMapper.java
@Mapper(componentModel = "spring")
public interface GroupPersistenceMapper {

    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.code", target = "subjectCode")
    @Mapping(source = "teacher.id", target = "teacherId")
    SubjectGroup toDomain(SubjectGroupJpaEntity entity);

    @Mapping(source = "subjectId", target = "subject.id")
    @Mapping(source = "teacherId", target = "teacher.id")
    SubjectGroupJpaEntity toEntity(SubjectGroup domain);

    AcademicPeriod toDomain(AcademicPeriodEmbeddable embeddable);

    AcademicPeriodEmbeddable toEmbeddable(AcademicPeriod domain);
}
```

**3. JPA Repository y Adapter** (2h)

```java
// group/infrastructure/.../repository/JpaGroupRepository.java
@Repository
public interface JpaGroupRepository
        extends JpaRepository<SubjectGroupJpaEntity, Long>,
                JpaSpecificationExecutor<SubjectGroupJpaEntity> {

    @Query("SELECT COUNT(g) FROM SubjectGroupJpaEntity g WHERE g.subject.id = :subjectId")
    int countBySubjectId(@Param("subjectId") Long subjectId);

    List<SubjectGroupJpaEntity> findBySubjectId(Long subjectId);

    List<SubjectGroupJpaEntity> findByTeacherId(Long teacherId);
}

// GroupRepositoryAdapter.java
@Component
@RequiredArgsConstructor
public class GroupRepositoryAdapter implements GroupRepositoryPort {

    private final JpaGroupRepository jpaRepository;
    private final GroupPersistenceMapper mapper;

    @Override
    public SubjectGroup save(SubjectGroup group) {
        SubjectGroupJpaEntity entity = mapper.toEntity(group);
        SubjectGroupJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SubjectGroup> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Page<SubjectGroup> findWithFilters(GroupFilters filters) {
        Specification<SubjectGroupJpaEntity> spec =
            GroupSpecifications.withFilters(filters);

        Pageable pageable = PageRequest.of(
            filters.page(),
            filters.size(),
            Sort.by(
                Sort.Direction.fromString(filters.sortDirection()),
                filters.sortBy()
            )
        );

        return jpaRepository.findAll(spec, pageable)
            .map(mapper::toDomain);
    }

    @Override
    public int countBySubjectId(Long subjectId) {
        return jpaRepository.countBySubjectId(subjectId);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }
}
```

**4. Specifications** (2.5h)

```java
// group/infrastructure/.../specification/GroupSpecifications.java
public class GroupSpecifications {

    public static Specification<SubjectGroupJpaEntity> withFilters(GroupFilters filters) {
        return Specification.where(hasSubjectId(filters.subjectId()))
            .and(hasTeacherId(filters.teacherId()))
            .and(hasType(filters.type()))
            .and(hasStatus(filters.status()))
            .and(hasAcademicYear(filters.academicYear()))
            .and(hasSemester(filters.semester()))
            .and(searchByTerm(filters.searchTerm()));
    }

    private static Specification<SubjectGroupJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, cb) ->
            subjectId == null ? null : cb.equal(root.get("subject").get("id"), subjectId);
    }

    private static Specification<SubjectGroupJpaEntity> hasTeacherId(Long teacherId) {
        return (root, query, cb) ->
            teacherId == null ? null : cb.equal(root.get("teacher").get("id"), teacherId);
    }

    private static Specification<SubjectGroupJpaEntity> hasType(GroupType type) {
        return (root, query, cb) ->
            type == null ? null : cb.equal(root.get("type"), type);
    }

    private static Specification<SubjectGroupJpaEntity> hasStatus(GroupStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<SubjectGroupJpaEntity> hasAcademicYear(String year) {
        return (root, query, cb) ->
            year == null ? null : cb.equal(root.get("academicPeriod").get("year"), year);
    }

    private static Specification<SubjectGroupJpaEntity> hasSemester(AcademicPeriod.Semester semester) {
        return (root, query, cb) ->
            semester == null ? null : cb.equal(root.get("academicPeriod").get("semester"), semester);
    }

    private static Specification<SubjectGroupJpaEntity> searchByTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return null;
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("subject").get("name")), pattern),
                cb.like(cb.lower(root.get("subject").get("code")), pattern)
            );
        };
    }
}
```

#### Entregables del Hito 2.5
- [ ] 1 Entidad JPA con relaciones (SubjectGroupJpaEntity)
- [ ] 1 Embeddable (AcademicPeriodEmbeddable)
- [ ] 1 Mapper de persistencia
- [ ] 1 JPA Repository con queries personalizadas
- [ ] 1 Repository Adapter
- [ ] 1 Specifications con 7 filtros
- [ ] Tests de integración

---

### **HITO 2.6: Group - REST API y Servicio** ⏱️ 6 horas

#### Objetivos
- Implementar servicio con validación de reglas de negocio
- API REST completa
- Tests unitarios

#### Tareas Detalladas

**1. Servicio de Aplicación** (2.5h)

```java
// group/application/service/GroupService.java
@Service
@RequiredArgsConstructor
@Transactional
public class GroupService implements
        CreateGroupUseCase,
        UpdateGroupUseCase,
        GetGroupUseCase,
        ManageGroupStatusUseCase {

    private final GroupRepositoryPort groupRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final UserRepositoryPort userRepository;

    @Override
    public SubjectGroup create(CreateGroupCommand command) {
        // Verificar que subject existe
        Subject subject = subjectRepository.findById(command.subjectId())
            .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));

        // Verificar límite de grupos por asignatura
        int currentGroupCount = groupRepository.countBySubjectId(command.subjectId());
        GroupBusinessRules.validateNewGroup(currentGroupCount);

        // Verificar capacidad
        Integer capacity = command.capacity() != null ?
            command.capacity() : GroupBusinessRules.DEFAULT_CAPACITY;
        GroupBusinessRules.validateCapacity(capacity);

        // Verificar que teacher existe y es profesor
        User teacher = userRepository.findById(command.teacherId())
            .orElseThrow(() -> new UserNotFoundException(command.teacherId()));

        if (!teacher.isTeacher()) {
            throw new InvalidTeacherException(teacher.getId());
        }

        // Crear grupo
        SubjectGroup group = new SubjectGroup();
        group.setName(command.name());
        group.setSubjectId(subject.getId());
        group.setSubjectCode(subject.getCode());
        group.setTeacherId(teacher.getId());
        group.setType(command.type());
        group.setCapacity(capacity);
        group.setCurrentEnrollments(0);
        group.setStatus(GroupStatus.OPEN);
        group.setAcademicPeriod(command.academicPeriod());

        return groupRepository.save(group);
    }

    @Override
    public SubjectGroup update(Long id, UpdateGroupCommand command) {
        SubjectGroup group = getById(id);

        if (command.name() != null) {
            group.setName(command.name());
        }

        if (command.teacherId() != null) {
            User teacher = userRepository.findById(command.teacherId())
                .orElseThrow(() -> new UserNotFoundException(command.teacherId()));
            if (!teacher.isTeacher()) {
                throw new InvalidTeacherException(teacher.getId());
            }
            group.setTeacherId(teacher.getId());
        }

        if (command.capacity() != null) {
            GroupBusinessRules.validateCapacity(command.capacity());
            // No permitir reducir capacidad por debajo de inscritos actuales
            if (command.capacity() < group.getCurrentEnrollments()) {
                throw new InvalidCapacityException(
                    "Cannot reduce capacity below current enrollments"
                );
            }
            group.setCapacity(command.capacity());
        }

        if (command.status() != null) {
            group.setStatus(command.status());
        }

        return groupRepository.save(group);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectGroup getById(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new GroupNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectGroup> findWithFilters(GroupFilters filters) {
        return groupRepository.findWithFilters(filters);
    }

    @Override
    public void closeGroup(Long id) {
        SubjectGroup group = getById(id);
        group.setStatus(GroupStatus.CLOSED);
        groupRepository.save(group);
    }

    @Override
    public void openGroup(Long id) {
        SubjectGroup group = getById(id);
        if (group.isFull()) {
            throw new GroupFullException(id);
        }
        group.setStatus(GroupStatus.OPEN);
        groupRepository.save(group);
    }

    @Override
    public void cancelGroup(Long id) {
        SubjectGroup group = getById(id);
        group.setStatus(GroupStatus.CANCELLED);
        groupRepository.save(group);
    }
}
```

**2. DTOs REST y Mapper** (1.5h)

**3. Controller** (1h)

**4. Tests** (1h)

#### Entregables del Hito 2.6
- [ ] 1 Service implementado
- [ ] 3 DTOs REST
- [ ] 1 Mapper REST
- [ ] 1 Controller con endpoints CRUD
- [ ] Tests unitarios del servicio

---

## 📅 MÓDULO SCHEDULE (Horarios)

### **HITO 2.7: Schedule - Dominio y Aplicación** ⏱️ 9 horas

#### Objetivos
- Crear dominio con detección de conflictos horarios
- Implementar lógica de validación de horarios
- Use cases y DTOs

#### Tareas Detalladas

**1. Entidades de Dominio** (3h)

```java
// schedule/domain/model/Schedule.java
public class Schedule {
    private Long id;
    private Long groupId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Classroom classroom;

    // Métodos de dominio
    public boolean conflictsWith(Schedule other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }

        return timeOverlaps(other) &&
               (classroomConflicts(other) || teacherConflicts(other));
    }

    private boolean timeOverlaps(Schedule other) {
        return this.startTime.isBefore(other.endTime) &&
               this.endTime.isAfter(other.startTime);
    }

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }
}

// schedule/domain/model/Classroom.java
public class Classroom {
    private Long id;
    private String code;           // "AULA-A1"
    private String building;       // "Edificio A"
    private Integer capacity;
    private ClassroomType type;

    public enum ClassroomType {
        THEORY,
        LABORATORY,
        COMPUTER_LAB,
        WORKSHOP
    }
}

// schedule/domain/model/DayOfWeek (usar java.time.DayOfWeek)
```

**2. Validación de Conflictos** (3h)

```java
// schedule/domain/validation/ScheduleBusinessRules.java
public class ScheduleBusinessRules {
    public static final LocalTime MIN_START_TIME = LocalTime.of(8, 0);
    public static final LocalTime MAX_END_TIME = LocalTime.of(22, 0);
    public static final int MIN_DURATION_MINUTES = 60;
    public static final int MAX_DURATION_MINUTES = 240;

    public static void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(MIN_START_TIME) || endTime.isAfter(MAX_END_TIME)) {
            throw new InvalidScheduleTimeException(
                "Schedule must be between " + MIN_START_TIME + " and " + MAX_END_TIME
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidScheduleTimeException("Start time must be before end time");
        }

        Duration duration = Duration.between(startTime, endTime);
        long minutes = duration.toMinutes();

        if (minutes < MIN_DURATION_MINUTES || minutes > MAX_DURATION_MINUTES) {
            throw new InvalidScheduleTimeException(
                "Duration must be between " + MIN_DURATION_MINUTES +
                " and " + MAX_DURATION_MINUTES + " minutes"
            );
        }
    }

    public static void validateNoConflicts(
            Schedule newSchedule,
            List<Schedule> existingSchedules) {

        for (Schedule existing : existingSchedules) {
            if (newSchedule.conflictsWith(existing)) {
                throw new ScheduleConflictException(
                    "Schedule conflicts with existing schedule: " + existing.getId()
                );
            }
        }
    }
}
```

**3. Excepciones** (1h)

**4. Use Cases y DTOs** (2h)

#### Entregables del Hito 2.7
- [ ] 2 Entidades de dominio (Schedule, Classroom)
- [ ] Lógica de detección de conflictos implementada
- [ ] 3 Excepciones de dominio
- [ ] 3 Use Cases
- [ ] DTOs y Repository Port
- [ ] Tests unitarios de lógica de conflictos

---

### **HITO 2.8: Schedule - Infraestructura JPA** ⏱️ 7 horas

#### Tareas
- Entidades JPA con relaciones
- Mappers
- Repository adapter
- Specifications

#### Entregables del Hito 2.8
- [ ] 2 Entidades JPA
- [ ] Mappers
- [ ] Repository adapter con queries de conflictos
- [ ] Specifications

---

### **HITO 2.9: Schedule - REST API y Servicio** ⏱️ 6 horas

#### Tareas
- Service con validación de conflictos
- DTOs REST
- Controller
- Tests

#### Entregables del Hito 2.9
- [ ] Service implementado
- [ ] DTOs REST
- [ ] Controller
- [ ] Tests

---

### **HITO 2.10: Integración y Tests E2E** ⏱️ 6 horas

#### Objetivos
- Verificar integración completa Subject → Group → Schedule
- Tests de casos de uso complejos
- Validación de flujos completos

#### Tareas

**1. Tests de Integración entre Módulos** (3h)

```java
// test/.../integration/SubjectGroupIntegrationTest.java
@SpringBootTest
@Transactional
class SubjectGroupIntegrationTest {

    @Test void testCreateGroupForSubject()
    @Test void testMaxGroupsPerSubject()
    @Test void testGroupCapacityValidation()
    @Test void testDeleteSubjectWithGroups()
}

// test/.../integration/GroupScheduleIntegrationTest.java
@SpringBootTest
@Transactional
class GroupScheduleIntegrationTest {

    @Test void testCreateScheduleForGroup()
    @Test void testScheduleConflictDetection()
    @Test void testTeacherScheduleConflicts()
    @Test void testClassroomScheduleConflicts()
}
```

**2. Tests de Flujos Completos** (2h)

```java
// test/.../integration/AcademicManagementE2ETest.java
@SpringBootTest
@Transactional
class AcademicManagementE2ETest {

    @Test
    void testCompleteAcademicSetup() {
        // 1. Crear asignatura
        // 2. Crear 3 grupos para la asignatura
        // 3. Asignar horarios a cada grupo
        // 4. Verificar que no hay conflictos
        // 5. Intentar crear 4to grupo (debe fallar)
    }

    @Test
    void testScheduleConflictScenario() {
        // 1. Crear 2 grupos con mismo profesor
        // 2. Asignar horario al grupo 1
        // 3. Intentar asignar horario conflictivo al grupo 2
        // 4. Verificar excepción de conflicto
    }
}
```

**3. Validación de Specifications** (1h)

- Verificar que todos los filtros funcionan correctamente
- Validar paginación y ordenamiento
- Tests de consultas complejas

#### Entregables del Hito 2.10
- [ ] Tests de integración entre módulos
- [ ] Tests E2E de flujos completos
- [ ] Validación de Specifications
- [ ] Documentación de endpoints actualizada

---

## 📊 Resumen de la Fase 2

### Módulos Implementados

| Módulo | Entidades Dominio | Use Cases | Endpoints | Tests |
|--------|-------------------|-----------|-----------|-------|
| Subject | 3 | 4 | 7 | >15 |
| Group | 4 | 4 | 8 | >15 |
| Schedule | 2 | 3 | 6 | >12 |
| **TOTAL** | **9** | **11** | **21** | **>42** |

### Capacidades Implementadas

✅ CRUD completo de asignaturas
✅ CRUD completo de grupos con validación de capacidad
✅ Máximo 3 grupos por asignatura
✅ Capacidad configurable (default 24, rango 10-30)
✅ CRUD completo de horarios
✅ Detección automática de conflictos horarios
✅ Filtros avanzados con Specifications
✅ Paginación y ordenamiento en todas las consultas
✅ Documentación OpenAPI completa

### Relaciones Implementadas

```
Subject (1) ──── (N) SubjectGroup
SubjectGroup (1) ──── (N) Schedule
SubjectGroup (N) ──── (1) Teacher (User)
Schedule (N) ──── (1) Classroom
```

---

## 🚀 Próximos Pasos: Fase 3

Una vez completada la Fase 2, el sistema tendrá la estructura académica completa. La **Fase 3** implementará:

- **Módulo Session** - Gestión de sesiones individuales con ciclo de vida
- Generación automática de sesiones desde Schedule
- Estados: PROGRAMADA → EN_CURSO → COMPLETADA/CANCELADA

---

## ✅ Checklist de Verificación por Hito

Cada hito debe cumplir:

### Dominio
- [ ] Entidades POJO sin dependencias de framework
- [ ] Lógica de negocio en entidades (métodos de consulta)
- [ ] Excepciones de dominio creadas
- [ ] Tests unitarios sin Spring

### Aplicación
- [ ] Use Cases definidos como interfaces
- [ ] DTOs de Command/Query creados
- [ ] Repository Ports definidos
- [ ] Services implementados

### Infraestructura
- [ ] Entidades JPA con sufijo *JpaEntity
- [ ] Mappers MapStruct funcionando
- [ ] Repository Adapter implementado
- [ ] Specifications con Criteria Builder
- [ ] Controller con documentación OpenAPI

### Tests
- [ ] Tests unitarios dominio (>80% cobertura)
- [ ] Tests unitarios servicios con Mockito
- [ ] Tests integración repositorio
- [ ] Tests integración controller

---

**Fase 2 lista para iniciar** 🚀
**Arquitectura hexagonal pura mantenida** ✅

*Documento generado: Diciembre 2024*
