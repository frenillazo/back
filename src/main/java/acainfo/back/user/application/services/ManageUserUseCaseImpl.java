package acainfo.back.user.application.services;

import acainfo.back.user.application.ports.in.ManageUserUseCase;
import acainfo.back.user.application.ports.out.RoleRepositoryPort;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.exception.DuplicateEmailException;
import acainfo.back.user.domain.exception.RoleNotFoundException;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.RoleDomain;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of ManageUserUseCase.
 * Handles all user management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManageUserUseCaseImpl implements ManageUserUseCase {

    private final UserRepositoryPort userRepository;
    private final RoleRepositoryPort roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDomain createTeacher(CreateTeacherCommand command) {
        log.debug("Creating teacher with email: {}", command.email());

        // Validate email uniqueness
        if (userRepository.existsByEmailIgnoreCase(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        // Get teacher role
        RoleDomain teacherRole = roleRepository.findByType(RoleType.TEACHER)
                .orElseThrow(() -> new RoleNotFoundException(RoleType.TEACHER));

        // Create user
        Set<RoleDomain> roles = new HashSet<>();
        roles.add(teacherRole);

        UserDomain user = UserDomain.builder()
                .email(command.email())
                .password(passwordEncoder.encode(command.password()))
                .firstName(command.firstName())
                .lastName(command.lastName())
                .phone(command.phone())
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        user.validate();
        UserDomain savedUser = userRepository.save(user);

        log.info("Teacher created successfully: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Override
    @Transactional
    public UserDomain updateTeacher(UpdateTeacherCommand command) {
        log.debug("Updating teacher: id={}", command.teacherId());

        UserDomain user = userRepository.findById(command.teacherId())
                .orElseThrow(() -> new UserNotFoundException(command.teacherId()));

        // Verify user is a teacher
        if (!user.isTeacher()) {
            throw new IllegalArgumentException("User is not a teacher: " + command.teacherId());
        }

        // Update user
        UserDomain updatedUser = user.toBuilder()
                .firstName(command.firstName())
                .lastName(command.lastName())
                .phone(command.phone())
                .build();

        updatedUser.validate();
        UserDomain savedUser = userRepository.save(updatedUser);

        log.info("Teacher updated successfully: id={}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public void deleteTeacher(Long teacherId) {
        log.debug("Deleting teacher: id={}", teacherId);

        UserDomain user = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        // Verify user is a teacher
        if (!user.isTeacher()) {
            throw new IllegalArgumentException("User is not a teacher: " + teacherId);
        }

        userRepository.deleteById(teacherId);
        log.info("Teacher deleted successfully: id={}", teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDomain getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDomain getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> getUsersByRole(RoleType roleType) {
        return userRepository.findByRoleType(roleType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> getActiveUsersByRole(RoleType roleType) {
        return userRepository.findByRoleTypeAndStatus(roleType, UserStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> getAllTeachers() {
        return userRepository.findByRoleType(RoleType.TEACHER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> getActiveTeachers() {
        return userRepository.findByRoleTypeAndStatus(RoleType.TEACHER, UserStatus.ACTIVE);
    }

    @Override
    @Transactional
    public UserDomain updateProfile(UpdateProfileCommand command) {
        log.debug("Updating profile for user: id={}", command.userId());

        UserDomain user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        UserDomain updatedUser = user.toBuilder()
                .firstName(command.firstName())
                .lastName(command.lastName())
                .phone(command.phone())
                .build();

        updatedUser.validate();
        UserDomain savedUser = userRepository.save(updatedUser);

        log.info("Profile updated successfully: id={}", savedUser.getId());
        return savedUser;
    }

    @Override
    @Transactional
    public UserDomain changeUserStatus(Long userId, UserStatus status) {
        log.debug("Changing status for user: id={}, newStatus={}", userId, status);

        UserDomain user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        UserDomain updatedUser = user.toBuilder()
                .status(status)
                .build();

        UserDomain savedUser = userRepository.save(updatedUser);
        log.info("User status changed: id={}, status={}", savedUser.getId(), status);
        return savedUser;
    }

    @Override
    @Transactional
    public UserDomain assignRole(Long userId, RoleType roleType) {
        log.debug("Assigning role to user: userId={}, role={}", userId, roleType);

        UserDomain user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        RoleDomain role = roleRepository.findByType(roleType)
                .orElseThrow(() -> new RoleNotFoundException(roleType));

        // Check if user already has the role
        if (user.hasRole(roleType)) {
            log.debug("User already has role: userId={}, role={}", userId, roleType);
            return user;
        }

        // Add role
        Set<RoleDomain> newRoles = new HashSet<>(user.getRoles());
        newRoles.add(role);

        UserDomain updatedUser = user.toBuilder()
                .roles(newRoles)
                .build();

        UserDomain savedUser = userRepository.save(updatedUser);
        log.info("Role assigned to user: userId={}, role={}", savedUser.getId(), roleType);
        return savedUser;
    }

    @Override
    @Transactional
    public UserDomain removeRole(Long userId, RoleType roleType) {
        log.debug("Removing role from user: userId={}, role={}", userId, roleType);

        UserDomain user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user has the role
        if (!user.hasRole(roleType)) {
            log.debug("User does not have role: userId={}, role={}", userId, roleType);
            return user;
        }

        // Remove role
        Set<RoleDomain> newRoles = new HashSet<>(user.getRoles());
        newRoles.removeIf(r -> r.getType() == roleType);

        // Ensure user has at least one role
        if (newRoles.isEmpty()) {
            throw new IllegalStateException("Cannot remove last role from user");
        }

        UserDomain updatedUser = user.toBuilder()
                .roles(newRoles)
                .build();

        UserDomain savedUser = userRepository.save(updatedUser);
        log.info("Role removed from user: userId={}, role={}", savedUser.getId(), roleType);
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> searchUsersByName(String searchTerm) {
        return userRepository.searchByName(searchTerm);
    }
}
