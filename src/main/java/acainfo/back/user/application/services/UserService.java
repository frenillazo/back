package acainfo.back.user.application.services;

import acainfo.back.user.domain.exception.UserAlreadyExistsException;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.infrastructure.adapters.in.dto.CreateTeacherRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.UpdateTeacherRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.UserResponse;
import acainfo.back.user.infrastructure.adapters.out.PermissionRepository;
import acainfo.back.user.infrastructure.adapters.out.RoleRepository;
import acainfo.back.user.infrastructure.adapters.out.UserRepository;
import acainfo.back.user.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Get all users with a specific role
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(RoleType roleType) {
        log.info("Getting all users with role: {}", roleType);
        List<User> users = userRepository.findByRoleType(roleType);
        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Getting user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toUserResponse(user);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
        return toUserResponse(user);
    }

    /**
     * Create a new teacher
     */
    @Transactional
    public UserResponse createTeacher(CreateTeacherRequest request, User currentUser) {
        log.info("Creating new teacher: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        // Get TEACHER role
        Role teacherRole = roleRepository.findByType(RoleType.TEACHER)
                .orElseThrow(() -> new RuntimeException("Teacher role not found"));

        // Create user
        User teacher = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();

        teacher.addRole(teacherRole);

        // Add custom permissions if provided
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            Set<Permission> permissions = permissionRepository.findByNameIn(
                    request.getPermissions().stream().toList()
            );

            // Create a custom role for this teacher with specific permissions
            Role customRole = Role.builder()
                    .type(RoleType.TEACHER)
                    .name("TEACHER_" + request.getEmail().replaceAll("[^a-zA-Z0-9]", "_"))
                    .description("Custom permissions for " + request.getFirstName() + " " + request.getLastName())
                    .permissions(permissions)
                    .build();

            customRole = roleRepository.save(customRole);
            teacher.addRole(customRole);
        }

        teacher = userRepository.save(teacher);

        // Audit log
        auditService.log(currentUser, AuditAction.USER_CREATED, "User", teacher.getId(),
                "Created teacher: " + teacher.getEmail());

        log.info("Teacher created successfully: {}", teacher.getEmail());
        return toUserResponse(teacher);
    }

    /**
     * Update teacher
     */
    @Transactional
    public UserResponse updateTeacher(Long teacherId, UpdateTeacherRequest request, User currentUser) {
        log.info("Updating teacher: {}", teacherId);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        // Verify user is a teacher
        if (!teacher.hasRole(RoleType.TEACHER)) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        // Update fields if provided
        if (request.getEmail() != null && !request.getEmail().equals(teacher.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new UserAlreadyExistsException(request.getEmail());
            }
            teacher.setEmail(request.getEmail().toLowerCase());
        }

        if (request.getFirstName() != null) {
            teacher.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            teacher.setLastName(request.getLastName());
        }

        if (request.getPhone() != null) {
            teacher.setPhone(request.getPhone());
        }

        if (request.getStatus() != null) {
            teacher.setStatus(UserStatus.valueOf(request.getStatus()));
        }

        // Update permissions if provided
        if (request.getPermissions() != null) {
            Set<Permission> permissions = permissionRepository.findByNameIn(
                    request.getPermissions().stream().toList()
            );

            // Find or create custom role for this teacher
            User finalTeacher = teacher;
            Role customRole = teacher.getRoles().stream()
                    .filter(r -> r.getName().startsWith("TEACHER_"))
                    .findFirst()
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .type(RoleType.TEACHER)
                                .name("TEACHER_" + finalTeacher.getEmail().replaceAll("[^a-zA-Z0-9]", "_"))
                                .description("Custom permissions for " + finalTeacher.getFirstName() + " " + finalTeacher.getLastName())
                                .build();
                        newRole = roleRepository.save(newRole);
                        finalTeacher.addRole(newRole);
                        return newRole;
                    });

            // Update permissions
            customRole.getPermissions().clear();
            customRole.getPermissions().addAll(permissions);
            roleRepository.save(customRole);
        }

        teacher = userRepository.save(teacher);

        // Audit log
        auditService.log(currentUser, AuditAction.USER_UPDATED, "User", teacher.getId(),
                "Updated teacher: " + teacher.getEmail());

        log.info("Teacher updated successfully: {}", teacher.getEmail());
        return toUserResponse(teacher);
    }

    /**
     * Delete teacher (soft delete by setting status to INACTIVE)
     */
    @Transactional
    public void deleteTeacher(Long teacherId, User currentUser) {
        log.info("Deleting teacher: {}", teacherId);

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        // Verify user is a teacher
        if (!teacher.hasRole(RoleType.TEACHER)) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        // Soft delete: set status to INACTIVE
        teacher.setStatus(UserStatus.INACTIVE);
        userRepository.save(teacher);

        // Audit log
        auditService.log(currentUser, AuditAction.USER_DELETED, "User", teacher.getId(),
                "Deleted teacher: " + teacher.getEmail());

        log.info("Teacher deleted successfully: {}", teacher.getEmail());
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .roles(user.getRoles().stream()
                        .map(role -> UserResponse.RoleDto.builder()
                                .id(role.getId())
                                .type(role.getType().name())
                                .name(role.getName())
                                .permissions(role.getPermissions().stream()
                                        .map(Permission::getName)
                                        .collect(Collectors.toSet()))
                                .build())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
