# Plan de Implementación - FASE 4: Portal del Estudiante
## Sistema de Gestión Centro de Formación

---

## 📊 Estado Actual del Proyecto

### ✅ Completado (Fases 0-3)
- **Fase 0:** Setup y arquitectura hexagonal
- **Fase 1:** Core y seguridad (Auth, Users, Roles, JWT)
- **Fase 2:** Gestión académica (Subjects, SubjectGroups, Schedules)
- **Fase 3:** Gestión de sesiones (Sessions, Attendance)

### 🎯 Por Implementar (Fase 4)
**Objetivo:** Portal completo para estudiantes con inscripciones y materiales educativos
**Duración estimada:** 2 semanas (Sprints 7-8)
**Completitud actual:** 0%

---

## 🏗️ Arquitectura Actual del Proyecto

```
acainfo.back/
├── attendance/        ✅ Gestión de asistencia (REQUIERE REFACTORIZACIÓN)
├── schedule/          ✅ Gestión de horarios
├── session/           ✅ Gestión de sesiones
├── shared/            ✅ Auth, usuarios, roles, audit
├── subject/           ✅ Gestión de asignaturas
├── subjectgroup/      ✅ Gestión de grupos (con control de plazas)
├── enrollment/        ❌ A CREAR - Sprint 7
├── material/          ❌ A CREAR - Sprint 8
└── payment/           ❌ A CREAR - Base para Fase 5
```

---

## 📋 SPRINT 7: Inscripciones y Gestión de Grupos

### Objetivo
Implementar sistema completo de inscripciones con validaciones de negocio y control de plazas.

### Historias de Usuario
1. ✨ Como **estudiante**, quiero **inscribirme a grupos disponibles**
2. ✨ Como **estudiante**, quiero **cambiarme de grupo**
3. ✨ Como **estudiante**, quiero **solicitar creación de grupo nuevo** (mín. 8 estudiantes)
4. ✨ Como **estudiante con 2+ asignaturas**, quiero **asistir online si no hay plaza presencial**
5. ✨ Como **estudiante**, quiero **ver mi lista de inscripciones activas**
6. ✨ Como **profesor**, quiero **ver lista de estudiantes inscritos en mis grupos**

---

### 🔧 Tarea 1: Crear Módulo Enrollment

#### 1.1 Estructura de Paquetes
```
enrollment/
├── domain/
│   ├── model/
│   │   ├── Enrollment.java              # Entidad principal
│   │   ├── EnrollmentStatus.java        # ACTIVO, RETIRADO, EN_ESPERA
│   │   ├── AttendanceMode.java          # PRESENCIAL, ONLINE
│   │   └── GroupRequest.java            # Solicitudes de grupo nuevo
│   └── exception/
│       ├── EnrollmentNotFoundException.java
│       ├── GroupFullException.java
│       ├── AlreadyEnrolledException.java
│       └── PaymentRequiredException.java
├── application/
│   ├── ports/
│   │   ├── in/
│   │   │   ├── EnrollUseCase.java
│   │   │   ├── WithdrawEnrollmentUseCase.java
│   │   │   ├── ChangeGroupUseCase.java
│   │   │   └── RequestGroupUseCase.java
│   │   └── out/
│   │       ├── EnrollmentRepository.java
│   │       └── GroupRequestRepository.java
│   └── services/
│       ├── EnrollmentService.java       # Lógica principal
│       └── GroupRequestService.java     # Lógica de solicitudes
└── infrastructure/
    ├── adapters/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── EnrollmentController.java
    │   │       └── GroupRequestController.java
    │   └── out/
    │       ├── persistence/
    │       │   ├── JpaEnrollmentRepository.java
    │       │   └── JpaGroupRequestRepository.java
    │       └── mapper/
    │           └── EnrollmentMapper.java
    └── config/
        └── EnrollmentConfig.java
```

#### 1.2 Entidad Enrollment

```java
package acainfo.back.enrollment.domain.model;

import acainfo.back.user.domain.model.User;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", indexes = {
        @Index(name = "idx_enrollment_student", columnList = "student_id"),
        @Index(name = "idx_enrollment_group", columnList = "subject_group_id"),
        @Index(name = "idx_enrollment_status", columnList = "status"),
        @Index(name = "idx_enrollment_student_status", columnList = "student_id, status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Student is required")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    @NotNull(message = "Subject group is required")
    private SubjectGroup subjectGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private EnrollmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Attendance mode is required")
    private AttendanceMode attendanceMode;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime enrollmentDate;

    @Column
    private LocalDateTime withdrawalDate;

    @Column(length = 500)
    private String withdrawalReason;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Métodos de negocio

    public void withdraw(String reason) {
        if (this.status == EnrollmentStatus.RETIRADO) {
            throw new IllegalStateException("Enrollment is already withdrawn");
        }
        this.status = EnrollmentStatus.EN_ESPERA;
        this.withdrawalDate = LocalDateTime.now();
        this.withdrawalReason = reason;
    }

    public void activate() {
        if (this.status == EnrollmentStatus.ACTIVO) {
            throw new IllegalStateException("Enrollment is already active");
        }
        this.status = EnrollmentStatus.ACTIVO;
        this.withdrawalDate = null;
        this.withdrawalReason = null;
    }

    public boolean isActive() {
        return this.status == EnrollmentStatus.ACTIVO;
    }

    public boolean isOnlineMode() {
        return this.attendanceMode == AttendanceMode.ONLINE;
    }

    public boolean isPresentialMode() {
        return this.attendanceMode == AttendanceMode.PRESENCIAL;
    }
}
```

#### 1.3 Enums

```java
// EnrollmentStatus.java
public enum EnrollmentStatus {
    ACTIVO,        // Inscripción activa
    RETIRADO,      // Estudiante se retiró del grupo
    EN_ESPERA      // En cola de espera (grupo lleno)
}

// AttendanceMode.java
public enum AttendanceMode {
    PRESENCIAL,    // Asiste presencialmente
    ONLINE         // Asiste online (cuando no hay plazas y tiene 2+ asignaturas)
}
```

#### 1.4 Entidad GroupRequest (Solicitudes de Grupo Nuevo)

```java
package acainfo.back.enrollment.domain.model;

import acainfo.back.user.domain.model.User;
import acainfo.back.subject.domain.model.Subject;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToMany
    @JoinTable(
            name = "group_request_supporters",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private Set<User> supporters = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRequestStatus status; // PENDIENTE, APROBADA, RECHAZADA

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column(length = 1000)
    private String rejectionReason;

    // Métodos de negocio

    public void addSupporter(User student) {
        this.supporters.add(student);
    }

    public boolean hasMinimumSupporters() {
        return this.supporters.size() >= 8; // Mínimo 8 estudiantes
    }

    public void approve() {
        if (this.status != GroupRequestStatus.PENDIENTE) {
            throw new IllegalStateException("Only pending requests can be approved");
        }
        this.status = GroupRequestStatus.APROBADA;
        this.resolvedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        if (this.status != GroupRequestStatus.PENDIENTE) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        this.status = GroupRequestStatus.RECHAZADA;
        this.resolvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }
}
```

---

### 🔧 Tarea 2: Implementar EnrollmentService

#### Lógica de Negocio Compleja

```java
package acainfo.back.enrollment.application.services;

import acainfo.back.enrollment.domain.model.*;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepository;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.user.domain.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final SubjectGroupService subjectGroupService;
    private final PaymentService paymentService; // Para validar pagos

    /**
     * Inscribir estudiante a un grupo
     *
     * Reglas de negocio:
     * 1. Verificar que el grupo esté activo
     * 2. Verificar que el estudiante no esté ya inscrito
     * 3. Verificar pagos al día
     * 4. Si hay plazas disponibles -> PRESENCIAL
     * 5. Si NO hay plazas y estudiante tiene 2+ asignaturas -> ONLINE
     * 6. Si NO hay plazas y estudiante tiene <2 asignaturas -> EN_ESPERA
     * 7. Actualizar ocupación del grupo
     * 8. Notificar al estudiante
     */
    @Transactional
    public Enrollment enroll(Long studentId, Long groupId) {

        // 1. Obtener entidades
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));
        SubjectGroup group = subjectGroupService.findById(groupId);

        // 2. Validaciones
        validateEnrollment(student, group);

        // 3. Verificar pagos al día
        if (!paymentService.hasValidPaymentStatus(studentId)) {
            throw new PaymentRequiredException("Student has overdue payments");
        }

        // 4. Determinar modo de asistencia
        AttendanceMode mode = determineAttendanceMode(student, group);
        EnrollmentStatus status = determineEnrollmentStatus(group, mode);

        // 5. Crear inscripción
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .subjectGroup(group)
                .status(status)
                .attendanceMode(mode)
                .build();

        // 6. Actualizar ocupación del grupo si es presencial
        if (mode == AttendanceMode.PRESENCIAL && status == EnrollmentStatus.ACTIVO) {
            group.incrementOccupancy(); // Ya implementado en SubjectGroup
            subjectGroupService.save(group);
        }

        // 7. Guardar inscripción
        enrollment = enrollmentRepository.save(enrollment);

        // 8. TODO: Enviar notificación
        // notificationService.notifyEnrollment(enrollment);

        return enrollment;
    }

    private void validateEnrollment(User student, SubjectGroup group) {
        // Verificar que el grupo esté activo
        if (!group.isActive()) {
            throw new GroupNotActiveException("Group is not active");
        }

        // Verificar que no esté ya inscrito
        if (enrollmentRepository.existsByStudentIdAndSubjectGroupIdAndStatus(
                student.getId(), group.getId(), EnrollmentStatus.ACTIVO)) {
            throw new AlreadyEnrolledException("Student is already enrolled in this group");
        }

        // TODO: Verificar conflictos de horario
        // scheduleValidationService.validateNoConflicts(student, group);
    }

    private AttendanceMode determineAttendanceMode(User student, SubjectGroup group) {
        // Si hay plazas disponibles -> PRESENCIAL
        if (group.hasAvailablePlaces()) {
            return AttendanceMode.PRESENCIAL;
        }

        // Si no hay plazas, contar inscripciones activas del estudiante
        long activeEnrollments = enrollmentRepository.countByStudentIdAndStatus(
                student.getId(), EnrollmentStatus.ACTIVO);

        // Si tiene 2 o más asignaturas -> puede asistir ONLINE
        if (activeEnrollments >= 2) {
            return AttendanceMode.ONLINE;
        }

        // Si tiene menos de 2 asignaturas -> debe esperar
        throw new GroupFullException("Group is full and student does not qualify for online mode");
    }

    private EnrollmentStatus determineEnrollmentStatus(SubjectGroup group, AttendanceMode mode) {
        // Si es online o hay plazas -> ACTIVO
        if (mode == AttendanceMode.ONLINE || group.hasAvailablePlaces()) {
            return EnrollmentStatus.ACTIVO;
        }

        // Si no hay plazas -> EN_ESPERA
        return EnrollmentStatus.EN_ESPERA;
    }

    /**
     * Cambiar de grupo
     */
    @Transactional
    public Enrollment changeGroup(Long enrollmentId, Long newGroupId) {
        Enrollment currentEnrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        SubjectGroup newGroup = subjectGroupService.findById(newGroupId);

        // Validar que sea de la misma asignatura
        if (!currentEnrollment.getSubjectGroup().getSubject().getId()
                .equals(newGroup.getSubject().getId())) {
            throw new IllegalArgumentException("New group must be of the same subject");
        }

        // Retirar de grupo actual
        withdraw(enrollmentId, "Cambio de grupo");

        // Inscribir en nuevo grupo
        return enroll(currentEnrollment.getStudent().getId(), newGroupId);
    }

    /**
     * Retirarse de un grupo
     */
    @Transactional
    public void withdraw(Long enrollmentId, String reason) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // Marcar como retirado
        enrollment.withdraw(reason);

        // Liberar plaza si era presencial
        if (enrollment.isPresentialMode()) {
            SubjectGroup group = enrollment.getSubjectGroup();
            group.decrementOccupancy();
            subjectGroupService.save(group);

            // TODO: Procesar cola de espera
            // processWaitingQueue(group);
        }

        enrollmentRepository.save(enrollment);
    }

    /**
     * Obtener inscripciones activas de un estudiante
     */
    public List<Enrollment> getActiveEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVO);
    }

    /**
     * Obtener estudiantes inscritos en un grupo
     */
    public List<Enrollment> getGroupEnrollments(Long groupId) {
        return enrollmentRepository.findBySubjectGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVO);
    }
}
```

---

### 🔧 Tarea 3: Crear EnrollmentController

```java
package acainfo.back.enrollment.infrastructure.adapters.in.rest;

import acainfo.back.enrollment.application.services.EnrollmentService;
import acainfo.back.enrollment.domain.model.Enrollment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "Enrollment management API")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll student in a group")
    public ResponseEntity<EnrollmentResponse> enroll(@RequestBody EnrollmentRequest request) {
        Enrollment enrollment = enrollmentService.enroll(
            request.getStudentId(),
            request.getGroupId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(EnrollmentMapper.toResponse(enrollment));
    }

    @PutMapping("/{id}/change-group")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Change to a different group")
    public ResponseEntity<EnrollmentResponse> changeGroup(
            @PathVariable Long id,
            @RequestBody ChangeGroupRequest request) {
        Enrollment enrollment = enrollmentService.changeGroup(id, request.getNewGroupId());
        return ResponseEntity.ok(EnrollmentMapper.toResponse(enrollment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "Withdraw from a group")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        enrollmentService.withdraw(id, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get student enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getStudentEnrollments(
            @PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.getActiveEnrollments(studentId);
        return ResponseEntity.ok(enrollments.stream()
            .map(EnrollmentMapper::toResponse)
            .toList());
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get group enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getGroupEnrollments(
            @PathVariable Long groupId) {
        List<Enrollment> enrollments = enrollmentService.getGroupEnrollments(groupId);
        return ResponseEntity.ok(enrollments.stream()
            .map(EnrollmentMapper::toResponse)
            .toList());
    }
}
```

---

### 🔧 Tarea 4: Refactorizar Módulo Attendance

**Problema actual:** `Attendance` usa `Long studentId` en lugar de relación con `Enrollment`

**Refactorización necesaria:**

```java
// ANTES (actual)
@Entity
public class Attendance {
    @Column(nullable = false)
    private Long studentId; // TODO: Refactor to @ManyToOne Enrollment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;
}

// DESPUÉS (refactorizado)
@Entity
public class Attendance {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment; // ✅ Ahora usa Enrollment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;
}
```

**Script de migración SQL:**

```sql
-- 1. Agregar columna enrollment_id
ALTER TABLE attendance ADD COLUMN enrollment_id BIGINT;

-- 2. Migrar datos (asociar attendance con enrollment correspondiente)
UPDATE attendance a
SET enrollment_id = (
    SELECT e.id
    FROM enrollments e
    WHERE e.student_id = a.student_id
      AND e.subject_group_id = (
          SELECT s.subject_group_id
          FROM sessions s
          WHERE s.id = a.session_id
      )
      AND e.status = 'ACTIVO'
    LIMIT 1
);

-- 3. Hacer NOT NULL
ALTER TABLE attendance ALTER COLUMN enrollment_id SET NOT NULL;

-- 4. Agregar FK constraint
ALTER TABLE attendance
ADD CONSTRAINT fk_attendance_enrollment
FOREIGN KEY (enrollment_id) REFERENCES enrollments(id);

-- 5. Eliminar columna student_id (después de verificar)
ALTER TABLE attendance DROP COLUMN student_id;
```

---

### 🔧 Tarea 5: Implementar Sistema de Cola de Espera

```java
@Service
public class WaitingQueueService {

    /**
     * Procesar cola de espera cuando se libera una plaza
     */
    @Transactional
    public void processWaitingQueue(SubjectGroup group) {
        if (!group.hasAvailablePlaces()) {
            return; // No hay plazas disponibles
        }

        // Obtener primera inscripción en espera (FIFO)
        List<Enrollment> waitingList = enrollmentRepository
            .findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(
                group.getId(),
                EnrollmentStatus.EN_ESPERA
            );

        if (waitingList.isEmpty()) {
            return; // No hay nadie en espera
        }

        Enrollment nextInQueue = waitingList.get(0);

        // Activar inscripción
        nextInQueue.activate();
        nextInQueue.setAttendanceMode(AttendanceMode.PRESENCIAL);

        // Incrementar ocupación
        group.incrementOccupancy();

        // Guardar cambios
        enrollmentRepository.save(nextInQueue);
        subjectGroupService.save(group);

        // Notificar al estudiante
        notificationService.notifyPlaceAvailable(nextInQueue);
    }
}
```

---

### 🔧 Tarea 6: Implementar Sistema de Solicitudes de Grupo

```java
@Service
@RequiredArgsConstructor
public class GroupRequestService {

    private final GroupRequestRepository groupRequestRepository;

    @Transactional
    public GroupRequest createRequest(Long subjectId, Long studentId) {
        // Verificar que no exista solicitud pendiente
        if (groupRequestRepository.existsBySubjectIdAndStatus(
                subjectId, GroupRequestStatus.PENDIENTE)) {
            throw new RequestAlreadyExistsException("There is already a pending request for this subject");
        }

        GroupRequest request = GroupRequest.builder()
            .subject(subjectRepository.findById(subjectId).orElseThrow())
            .requestedBy(userRepository.findById(studentId).orElseThrow())
            .status(GroupRequestStatus.PENDIENTE)
            .build();

        // Agregar al solicitante como primer supporter
        request.addSupporter(request.getRequestedBy());

        return groupRequestRepository.save(request);
    }

    @Transactional
    public void supportRequest(Long requestId, Long studentId) {
        GroupRequest request = groupRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException(requestId));

        User student = userRepository.findById(studentId).orElseThrow();

        request.addSupporter(student);
        groupRequestRepository.save(request);

        // Si alcanzó el mínimo, notificar a administrador
        if (request.hasMinimumSupporters()) {
            notificationService.notifyAdminGroupRequestReady(request);
        }
    }

    @Transactional
    public void approveRequest(Long requestId, Long adminId) {
        GroupRequest request = groupRequestRepository.findById(requestId)
            .orElseThrow(() -> new RequestNotFoundException(requestId));

        if (!request.hasMinimumSupporters()) {
            throw new IllegalStateException("Request does not have minimum supporters");
        }

        request.approve();
        groupRequestRepository.save(request);

        // TODO: Crear nuevo grupo automáticamente
        // subjectGroupService.createGroupFromRequest(request);
    }
}
```

---

## 📋 SPRINT 8: Material y Recursos Educativos

### Objetivo
Implementar sistema de gestión de materiales educativos con control de acceso.

### Historias de Usuario
1. ✨ Como **estudiante inscrito**, quiero **acceder al material de mis grupos**
2. ✨ Como **profesor**, quiero **subir material** (.pdf, .java, .cpp, .h)
3. ✨ Como **sistema**, debo **bloquear acceso a material si hay pagos pendientes**
4. ✨ Como **profesor**, quiero **organizar materiales por temas/unidades**
5. ✨ Como **estudiante**, quiero **descargar materiales disponibles**

---

### 🔧 Tarea 7: Crear Módulo Material

#### 7.1 Estructura de Paquetes

```
material/
├── domain/
│   ├── model/
│   │   ├── Material.java
│   │   ├── MaterialType.java
│   │   └── MaterialVersion.java
│   └── exception/
│       ├── MaterialNotFoundException.java
│       ├── UnauthorizedAccessException.java
│       └── InvalidFileTypeException.java
├── application/
│   ├── ports/
│   │   ├── in/
│   │   │   ├── UploadMaterialUseCase.java
│   │   │   ├── DownloadMaterialUseCase.java
│   │   │   └── DeleteMaterialUseCase.java
│   │   └── out/
│   │       ├── MaterialRepository.java
│   │       └── FileStoragePort.java
│   └── services/
│       ├── MaterialService.java
│       └── FileStorageService.java
└── infrastructure/
    ├── adapters/
    │   ├── in/
    │   │   └── rest/
    │   │       └── MaterialController.java
    │   └── out/
    │       ├── persistence/
    │       │   └── JpaMaterialRepository.java
    │       └── storage/
    │           └── LocalFileStorageAdapter.java
    └── config/
        └── FileStorageConfig.java
```

#### 7.2 Entidad Material

```java
package acainfo.back.material.domain.model;

import acainfo.back.user.domain.model.User;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "materials", indexes = {
        @Index(name = "idx_material_group", columnList = "subject_group_id"),
        @Index(name = "idx_material_type", columnList = "type"),
        @Index(name = "idx_material_active", columnList = "is_active")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    private SubjectGroup subjectGroup;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType type; // PDF, JAVA, CPP, HEADER

    @Column(nullable = false)
    private Long fileSize; // bytes

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String topic; // Tema o unidad

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private Boolean requiresPayment; // Control de acceso

    @Column(nullable = false)
    private Boolean isActive; // Para soft delete

    @Version
    private Integer version; // Para versionado

    // Métodos de negocio

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isPdf() {
        return this.type == MaterialType.PDF;
    }

    public boolean isCode() {
        return this.type == MaterialType.JAVA ||
                this.type == MaterialType.CPP ||
                this.type == MaterialType.HEADER;
    }
}
```

#### 7.3 Enum MaterialType

```java
public enum MaterialType {
    PDF("application/pdf", ".pdf"),
    JAVA("text/x-java", ".java"),
    CPP("text/x-c++src", ".cpp"),
    HEADER("text/x-c++hdr", ".h");

    private final String mimeType;
    private final String extension;

    MaterialType(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public static MaterialType fromFileName(String fileName) {
        String lowerCase = fileName.toLowerCase();
        if (lowerCase.endsWith(".pdf")) return PDF;
        if (lowerCase.endsWith(".java")) return JAVA;
        if (lowerCase.endsWith(".cpp")) return CPP;
        if (lowerCase.endsWith(".h")) return HEADER;
        throw new InvalidFileTypeException("Unsupported file type: " + fileName);
    }
}
```

---

### 🔧 Tarea 8: Implementar MaterialService con Control de Acceso

```java
package acainfo.back.material.application.services;

import acainfo.back.material.domain.model.Material;
import acainfo.back.material.application.ports.out.MaterialRepository;
import acainfo.back.enrollment.application.services.EnrollmentService;
import acainfo.back.payment.application.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final FileStorageService fileStorageService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;

    /**
     * Subir material
     * Solo profesores y administradores pueden subir
     */
    @Transactional
    public Material uploadMaterial(
            MultipartFile file,
            Long groupId,
            Long uploadedById,
            String description,
            String topic,
            Boolean requiresPayment) throws IOException {

        // Validar tipo de archivo
        MaterialType type = MaterialType.fromFileName(file.getOriginalFilename());

        // Guardar archivo en el sistema de archivos
        String filePath = fileStorageService.store(file, groupId);

        // Crear entidad Material
        Material material = Material.builder()
            .subjectGroup(subjectGroupService.findById(groupId))
            .fileName(file.getOriginalFilename())
            .filePath(filePath)
            .type(type)
            .fileSize(file.getSize())
            .description(description)
            .topic(topic)
            .uploadedBy(userRepository.findById(uploadedById).orElseThrow())
            .requiresPayment(requiresPayment != null ? requiresPayment : true)
            .isActive(true)
            .build();

        return materialRepository.save(material);
    }

    /**
     * Verificar acceso a material
     *
     * Reglas:
     * 1. Debe estar inscrito en el grupo
     * 2. Si requiresPayment = true, debe tener pagos al día
     * 3. Opcionalmente: asistencia mínima 75%
     */
    public boolean canAccess(Long studentId, Long materialId) {
        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // Verificar inscripción activa
        boolean isEnrolled = enrollmentService.isStudentEnrolled(
            studentId,
            material.getSubjectGroup().getId()
        );

        if (!isEnrolled) {
            return false;
        }

        // Verificar pagos si es requerido
        if (material.getRequiresPayment()) {
            return paymentService.hasValidPaymentStatus(studentId);
        }

        return true;
    }

    /**
     * Descargar material
     */
    public byte[] downloadMaterial(Long studentId, Long materialId) throws IOException {
        // Verificar acceso
        if (!canAccess(studentId, materialId)) {
            throw new UnauthorizedAccessException("You don't have access to this material");
        }

        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new MaterialNotFoundException(materialId));

        // Leer archivo del sistema de archivos
        return fileStorageService.load(material.getFilePath());
    }

    /**
     * Listar materiales de un grupo (solo accesibles para el estudiante)
     */
    public List<Material> getAccessibleMaterials(Long studentId, Long groupId) {
        // Verificar inscripción
        if (!enrollmentService.isStudentEnrolled(studentId, groupId)) {
            throw new UnauthorizedAccessException("You are not enrolled in this group");
        }

        List<Material> allMaterials = materialRepository.findBySubjectGroupIdAndIsActiveTrue(groupId);

        // Filtrar por acceso
        return allMaterials.stream()
            .filter(material -> {
                if (!material.getRequiresPayment()) {
                    return true; // Acceso libre
                }
                return paymentService.hasValidPaymentStatus(studentId);
            })
            .toList();
    }

    /**
     * Eliminar material (soft delete)
     */
    @Transactional
    public void deleteMaterial(Long materialId) {
        Material material = materialRepository.findById(materialId)
            .orElseThrow(() -> new MaterialNotFoundException(materialId));

        material.deactivate();
        materialRepository.save(material);
    }
}
```

---

### 🔧 Tarea 9: Implementar FileStorageService

```java
package acainfo.back.material.application.services;

import acainfo.back.material.domain.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${file.storage.location:uploads}") String location) {
        this.rootLocation = Paths.get(location);
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize storage location", e);
        }
    }

    /**
     * Almacenar archivo
     * Estructura: uploads/{groupId}/{uuid}_{filename}
     */
    public String store(MultipartFile file, Long groupId) throws IOException {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file");
        }

        // Crear directorio para el grupo
        Path groupPath = this.rootLocation.resolve(groupId.toString());
        Files.createDirectories(groupPath);

        // Generar nombre único
        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path destinationFile = groupPath.resolve(uniqueFileName);

        // Copiar archivo
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        return groupId + "/" + uniqueFileName;
    }

    /**
     * Cargar archivo
     */
    public byte[] load(String filePath) throws IOException {
        Path file = this.rootLocation.resolve(filePath);

        if (!Files.exists(file)) {
            throw new FileStorageException("File not found: " + filePath);
        }

        return Files.readAllBytes(file);
    }

    /**
     * Eliminar archivo físicamente
     */
    public void delete(String filePath) throws IOException {
        Path file = this.rootLocation.resolve(filePath);
        Files.deleteIfExists(file);
    }
}
```

---

### 🔧 Tarea 10: Crear MaterialController

```java
package acainfo.back.material.infrastructure.adapters.in.rest;

import acainfo.back.material.application.services.MaterialService;
import acainfo.back.material.domain.model.Material;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Upload material")
    public ResponseEntity<MaterialResponse> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("groupId") Long groupId,
            @RequestParam("uploadedById") Long uploadedById,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Boolean requiresPayment) throws IOException {

        Material material = materialService.uploadMaterial(
            file, groupId, uploadedById, description, topic, requiresPayment
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(MaterialMapper.toResponse(material));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Download material")
    public ResponseEntity<ByteArrayResource> downloadMaterial(
            @PathVariable Long id,
            @RequestParam Long studentId) throws IOException {

        byte[] data = materialService.downloadMaterial(studentId, id);
        Material material = materialService.findById(id);

        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + material.getFileName() + "\"")
            .contentType(MediaType.parseMediaType(material.getType().getMimeType()))
            .contentLength(data.length)
            .body(resource);
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get group materials")
    public ResponseEntity<List<MaterialResponse>> getGroupMaterials(
            @PathVariable Long groupId,
            @RequestParam Long studentId) {

        List<Material> materials = materialService.getAccessibleMaterials(studentId, groupId);

        return ResponseEntity.ok(materials.stream()
            .map(MaterialMapper::toResponse)
            .toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Delete material")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        materialService.deleteMaterial(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 💳 Módulo Payment (Base para Control de Acceso)

### Objetivo
Crear estructura básica de pagos para controlar acceso a materiales. La integración con Stripe será en la Fase 5.

### 🔧 Tarea 11: Crear Módulo Payment Básico

```java
package acainfo.back.payment.domain.model;

import acainfo.back.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_student", columnList = "student_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status; // PENDIENTE, PAGADO, VENCIDO, CANCELADO

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type; // MENSUAL, INTENSIVO

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column
    private LocalDate paidDate;

    @Column(length = 100)
    private String stripePaymentId; // Para Fase 5

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Métodos de negocio

    public boolean isOverdue() {
        return this.status == PaymentStatus.PENDIENTE &&
                LocalDate.now().isAfter(this.dueDate.plusDays(5));
    }

    public boolean isPaid() {
        return this.status == PaymentStatus.PAGADO;
    }

    public void markAsPaid() {
        this.status = PaymentStatus.PAGADO;
        this.paidDate = LocalDate.now();
    }

    public void markAsOverdue() {
        if (this.status == PaymentStatus.PENDIENTE && isOverdue()) {
            this.status = PaymentStatus.VENCIDO;
        }
    }
}
```

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Verificar si el estudiante tiene pagos al día
     * (sin pagos vencidos hace más de 5 días)
     */
    public boolean hasValidPaymentStatus(Long studentId) {
        List<Payment> overduePayments = paymentRepository
            .findByStudentIdAndStatus(studentId, PaymentStatus.VENCIDO);

        // Si tiene pagos vencidos, verificar si pasaron más de 5 días
        return overduePayments.stream()
            .noneMatch(payment ->
                LocalDate.now().isAfter(payment.getDueDate().plusDays(5))
            );
    }

    /**
     * Tarea programada para marcar pagos como vencidos
     */
    @Scheduled(cron = "0 0 9 * * *") // Diario a las 9am
    @Transactional
    public void checkOverduePayments() {
        List<Payment> pendingPayments = paymentRepository
            .findByStatus(PaymentStatus.PENDIENTE);

        pendingPayments.forEach(payment -> {
            if (payment.isOverdue()) {
                payment.markAsOverdue();
                paymentRepository.save(payment);

                // TODO: Notificar al estudiante
                // notificationService.notifyOverduePayment(payment);
            }
        });
    }
}
```

---

## 👨‍🎓 StudentController (Portal del Estudiante)

### 🔧 Tarea 12: Crear StudentController

```java
package acainfo.back.shared.infrastructure.adapters.in.rest;

import acainfo.back.enrollment.application.services.EnrollmentService;
import acainfo.back.material.application.services.MaterialService;
import acainfo.back.attendance.application.services.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Portal", description = "Student portal API")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final EnrollmentService enrollmentService;
    private final MaterialService materialService;
    private final AttendanceService attendanceService;

    @GetMapping("/me/enrollments")
    @Operation(summary = "Get my enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(@RequestParam Long studentId) {
        var enrollments = enrollmentService.getActiveEnrollments(studentId);
        return ResponseEntity.ok(enrollments.stream()
            .map(EnrollmentMapper::toResponse)
            .toList());
    }

    @PostMapping("/me/enrollments")
    @Operation(summary = "Enroll in a group")
    public ResponseEntity<EnrollmentResponse> enrollInGroup(@RequestBody EnrollStudentRequest request) {
        var enrollment = enrollmentService.enroll(request.getStudentId(), request.getGroupId());
        return ResponseEntity.ok(EnrollmentMapper::toResponse(enrollment));
    }

    @GetMapping("/me/materials")
    @Operation(summary = "Get my accessible materials")
    public ResponseEntity<List<MaterialResponse>> getMyMaterials(@RequestParam Long studentId) {
        var enrollments = enrollmentService.getActiveEnrollments(studentId);

        List<MaterialResponse> allMaterials = enrollments.stream()
            .flatMap(enrollment ->
                materialService.getAccessibleMaterials(
                    studentId,
                    enrollment.getSubjectGroup().getId()
                ).stream()
            )
            .map(MaterialMapper::toResponse)
            .toList();

        return ResponseEntity.ok(allMaterials);
    }

    @GetMapping("/me/attendance")
    @Operation(summary = "Get my attendance")
    public ResponseEntity<AttendanceStatisticsResponse> getMyAttendance(@RequestParam Long studentId) {
        var statistics = attendanceService.getStudentStatistics(studentId);
        return ResponseEntity.ok(statistics);
    }

    @PostMapping("/me/group-requests")
    @Operation(summary = "Request new group")
    public ResponseEntity<GroupRequestResponse> requestNewGroup(@RequestBody CreateGroupRequestRequest request) {
        var groupRequest = groupRequestService.createRequest(
            request.getSubjectId(),
            request.getStudentId()
        );
        return ResponseEntity.ok(GroupRequestMapper.toResponse(groupRequest));
    }
}
```

---

## 🧪 Testing (Tareas 13-16)

### Tests Unitarios - EnrollmentService

```java
@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private SubjectGroupService subjectGroupService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void shouldEnrollStudentWithAvailablePlaces() {
        // Given
        User student = createStudent();
        SubjectGroup group = createGroupWithPlaces(10, 5); // 5 plazas ocupadas de 10

        when(paymentService.hasValidPaymentStatus(student.getId())).thenReturn(true);
        when(subjectGroupService.findById(group.getId())).thenReturn(group);

        // When
        Enrollment enrollment = enrollmentService.enroll(student.getId(), group.getId());

        // Then
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVO);
        assertThat(enrollment.getAttendanceMode()).isEqualTo(AttendanceMode.PRESENCIAL);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void shouldEnrollOnlineWhenNoPlacesAndStudentHas2PlusSubjects() {
        // Given
        User student = createStudent();
        SubjectGroup fullGroup = createFullGroup();

        when(paymentService.hasValidPaymentStatus(student.getId())).thenReturn(true);
        when(subjectGroupService.findById(fullGroup.getId())).thenReturn(fullGroup);
        when(enrollmentRepository.countByStudentIdAndStatus(student.getId(), EnrollmentStatus.ACTIVO))
            .thenReturn(2L); // Ya tiene 2 inscripciones

        // When
        Enrollment enrollment = enrollmentService.enroll(student.getId(), fullGroup.getId());

        // Then
        assertThat(enrollment.getAttendanceMode()).isEqualTo(AttendanceMode.ONLINE);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVO);
    }

    @Test
    void shouldThrowExceptionWhenStudentHasOverduePayments() {
        // Given
        when(paymentService.hasValidPaymentStatus(anyLong())).thenReturn(false);

        // When & Then
        assertThrows(PaymentRequiredException.class, () ->
            enrollmentService.enroll(1L, 1L)
        );
    }

    @Test
    void shouldThrowExceptionWhenAlreadyEnrolled() {
        // Given
        when(enrollmentRepository.existsByStudentIdAndSubjectGroupIdAndStatus(
            anyLong(), anyLong(), eq(EnrollmentStatus.ACTIVO)
        )).thenReturn(true);

        // When & Then
        assertThrows(AlreadyEnrolledException.class, () ->
            enrollmentService.enroll(1L, 1L)
        );
    }
}
```

### Tests de Integración - EnrollmentController

```java
@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldEnrollSuccessfully() throws Exception {
        EnrollmentRequest request = new EnrollmentRequest(1L, 1L);

        mockMvc.perform(post("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ACTIVO"))
            .andExpect(jsonPath("$.attendanceMode").value("PRESENCIAL"));
    }
}
```

---

## 📊 Resumen de Tareas y Estimaciones

| # | Tarea | Descripción | Estimación | Sprint |
|---|-------|-------------|------------|--------|
| 1 | Crear módulo enrollment | Estructura de paquetes y entidades | 4h | 7 |
| 2 | Implementar EnrollmentService | Lógica de negocio compleja | 8h | 7 |
| 3 | Crear EnrollmentController | REST API endpoints | 3h | 7 |
| 4 | Refactorizar Attendance | Usar @ManyToOne Enrollment | 4h | 7 |
| 5 | Sistema de cola de espera | WaitingQueueService | 3h | 7 |
| 6 | Sistema de solicitudes | GroupRequestService | 4h | 7 |
| 7 | Crear módulo material | Estructura y entidades | 3h | 8 |
| 8 | Implementar MaterialService | Control de acceso | 6h | 8 |
| 9 | Implementar FileStorageService | Almacenamiento local | 4h | 8 |
| 10 | Crear MaterialController | REST API con upload/download | 4h | 8 |
| 11 | Módulo payment básico | Entidad y validaciones | 3h | 8 |
| 12 | Crear StudentController | Portal del estudiante | 3h | 8 |
| 13-16 | Tests completos | Unitarios + integración | 12h | 7-8 |

**Total estimado:** ~61 horas (~2 semanas con 1 desarrollador a tiempo completo)

---

## 🚀 Orden de Implementación Recomendado

### Día 1-3 (Sprint 7)
1. ✅ Crear estructura del módulo `enrollment/`
2. ✅ Implementar entidad `Enrollment` + enums
3. ✅ Crear `EnrollmentRepository` con queries
4. ✅ Implementar `EnrollmentService` (lógica core)
5. ✅ Crear `EnrollmentController`
6. ✅ Tests unitarios de `EnrollmentService`

### Día 4-5 (Sprint 7)
7. ✅ Implementar `GroupRequest` y `GroupRequestService`
8. ✅ Implementar `WaitingQueueService`
9. ✅ Refactorizar módulo `Attendance`
10. ✅ Migración SQL para Attendance
11. ✅ Tests de integración de enrollment

### Día 6-7 (Sprint 8)
12. ✅ Crear estructura del módulo `material/`
13. ✅ Implementar entidad `Material`
14. ✅ Crear `FileStorageService`
15. ✅ Implementar `MaterialService` con control de acceso

### Día 8-9 (Sprint 8)
16. ✅ Crear `MaterialController` con upload/download
17. ✅ Módulo `payment/` básico
18. ✅ Crear `StudentController`
19. ✅ Tests unitarios de `MaterialService`

### Día 10 (Final)
20. ✅ Tests de integración completos
21. ✅ Documentación OpenAPI/Swagger
22. ✅ Pruebas E2E de flujos críticos
23. ✅ Code review y ajustes finales

---

## 🔒 Validaciones de Negocio Críticas

### Inscripciones
- ✅ Grupo debe estar activo
- ✅ No permitir doble inscripción
- ✅ Verificar pagos al día
- ✅ Control de plazas con concurrencia
- ✅ Regla de 2+ asignaturas para modo online
- ✅ Cola de espera automática

### Materiales
- ✅ Solo estudiantes inscritos pueden acceder
- ✅ Bloqueo por pagos pendientes (>5 días)
- ✅ Tipos de archivo permitidos: .pdf, .java, .cpp, .h
- ✅ Solo profesores/admin pueden subir
- ✅ Soft delete para auditoría

### Pagos
- ✅ Bloqueo automático tras 5 días de impago
- ✅ Verificación antes de inscripción
- ✅ Verificación antes de acceso a material

---

## 📈 Métricas de Éxito

### Técnicas
- ✅ Cobertura de tests > 80%
- ✅ Tiempo de respuesta < 200ms (p95)
- ✅ Sin vulnerabilidades críticas
- ✅ 0 N+1 queries en endpoints de listado

### Funcionales
- ✅ Estudiante puede inscribirse en <10 segundos
- ✅ Descarga de material < 2 segundos
- ✅ 0 sobre-inscripciones por race conditions
- ✅ Notificaciones enviadas en <5 segundos

---

## 🎯 Puntos de Integración con Código Existente

### SubjectGroup (ya implementado)
```java
// Usar métodos ya existentes:
group.hasAvailablePlaces()
group.incrementOccupancy()
group.decrementOccupancy()
group.isFull()
```

### Attendance (requiere refactorización)
```java
// ANTES: Long studentId
// DESPUÉS: @ManyToOne Enrollment enrollment

// Actualizar AttendanceService:
attendanceService.getEnrollmentStatistics(enrollmentId)
```

### Security (ya implementado)
```java
// Usar annotations existentes:
@PreAuthorize("hasRole('STUDENT')")
@PreAuthorize("hasRole('TEACHER')")
```

---

## 🔄 Migraciones de Base de Datos

### V1__create_enrollment_tables.sql
```sql
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    subject_group_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attendance_mode VARCHAR(20) NOT NULL,
    enrollment_date TIMESTAMP NOT NULL,
    withdrawal_date TIMESTAMP,
    withdrawal_reason VARCHAR(500),
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_enrollment_group FOREIGN KEY (subject_group_id) REFERENCES subject_groups(id),
    CONSTRAINT uk_enrollment_student_group UNIQUE (student_id, subject_group_id, status)
);

CREATE INDEX idx_enrollment_student ON enrollments(student_id);
CREATE INDEX idx_enrollment_group ON enrollments(subject_group_id);
CREATE INDEX idx_enrollment_status ON enrollments(status);

CREATE TABLE group_requests (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    rejection_reason VARCHAR(1000),
    CONSTRAINT fk_request_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_request_user FOREIGN KEY (requested_by) REFERENCES users(id)
);

CREATE TABLE group_request_supporters (
    request_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (request_id, student_id),
    CONSTRAINT fk_supporter_request FOREIGN KEY (request_id) REFERENCES group_requests(id),
    CONSTRAINT fk_supporter_student FOREIGN KEY (student_id) REFERENCES users(id)
);
```

### V2__create_material_tables.sql
```sql
CREATE TABLE materials (
    id BIGSERIAL PRIMARY KEY,
    subject_group_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    type VARCHAR(20) NOT NULL,
    file_size BIGINT NOT NULL,
    description VARCHAR(1000),
    topic VARCHAR(100),
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    requires_payment BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_material_group FOREIGN KEY (subject_group_id) REFERENCES subject_groups(id),
    CONSTRAINT fk_material_uploader FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

CREATE INDEX idx_material_group ON materials(subject_group_id);
CREATE INDEX idx_material_type ON materials(type);
CREATE INDEX idx_material_active ON materials(is_active);
```

### V3__create_payment_tables.sql
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    stripe_payment_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payment_student FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE INDEX idx_payment_student ON payments(student_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_due_date ON payments(due_date);
```

### V4__refactor_attendance.sql
```sql
-- Ver script completo en Tarea 4
```

---

## ✅ Checklist Final

### Sprint 7 - Inscripciones
- [ ] Módulo enrollment creado
- [ ] Entidades Enrollment + GroupRequest implementadas
- [ ] EnrollmentService con todas las reglas de negocio
- [ ] EnrollmentController con endpoints REST
- [ ] Sistema de cola de espera funcional
- [ ] Sistema de solicitudes de grupo
- [ ] Attendance refactorizado
- [ ] Migraciones SQL ejecutadas
- [ ] Tests unitarios (cobertura >80%)
- [ ] Tests de integración

### Sprint 8 - Materiales
- [ ] Módulo material creado
- [ ] Entidad Material implementada
- [ ] FileStorageService funcional
- [ ] MaterialService con control de acceso
- [ ] MaterialController con upload/download
- [ ] Módulo payment básico
- [ ] StudentController completo
- [ ] Tests unitarios (cobertura >80%)
- [ ] Tests de integración
- [ ] Documentación Swagger actualizada

---

## 📚 Recursos y Documentación

- **Spring Data JPA:** Para repositorios y queries
- **Spring Security:** Para control de acceso basado en roles
- **Multipart File Upload:** Para gestión de archivos
- **Transacciones:** Para garantizar consistencia
- **Optimistic Locking:** Para control de concurrencia en inscripciones

---

## 🎉 Conclusión

Este plan detallado te permitirá implementar la **Fase 4 - Portal del Estudiante** de forma estructurada y siguiendo la arquitectura hexagonal ya establecida en el proyecto.

**Próximos pasos:**
1. Revisar y aprobar este plan
2. Comenzar con la Tarea 1 (crear módulo enrollment)
3. Seguir el orden de implementación recomendado
4. Ejecutar tests continuamente
5. Code review antes de merge

**Archivos generados por este plan:**
- `/PLAN_FASE_4.md` - Este documento
- Todo list actualizada con 20 tareas

¿Quieres que comience con la implementación? 🚀
