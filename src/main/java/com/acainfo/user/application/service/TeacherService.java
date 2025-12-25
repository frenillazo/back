package com.acainfo.user.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.user.application.dto.CreateTeacherCommand;
import com.acainfo.user.application.dto.UpdateTeacherCommand;
import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.application.port.in.ManageTeachersUseCase;
import com.acainfo.user.application.port.out.RoleRepositoryPort;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.DuplicateEmailException;
import com.acainfo.user.domain.exception.TeacherHasActiveGroupsException;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service implementing teacher management use cases.
 * Handles CRUD operations for teachers (ADMIN only).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService implements ManageTeachersUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createTeacher(CreateTeacherCommand command) {
        log.info("Creating new teacher with email: {}", command.email());

        // Validate email uniqueness
        String email = command.email().toLowerCase().trim();
        if (userRepositoryPort.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        // Validate password
        if (command.password() == null || command.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Get TEACHER role
        Role teacherRole = roleRepositoryPort.findByType(RoleType.TEACHER)
                .orElseThrow(() -> new IllegalStateException("TEACHER role not found in database"));

        // Create teacher user
        User teacher = User.builder()
                .email(email)
                .password(passwordEncoder.encode(command.password()))
                .firstName(command.firstName().trim())
                .lastName(command.lastName().trim())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(teacherRole))
                .build();

        User savedTeacher = userRepositoryPort.save(teacher);
        log.info("Teacher created successfully: {}", savedTeacher.getEmail());

        return savedTeacher;
    }

    @Override
    @Transactional
    public User updateTeacher(Long teacherId, UpdateTeacherCommand command) {
        log.info("Updating teacher ID: {}", teacherId);

        User teacher = userRepositoryPort.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        // Validate that user is actually a teacher
        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        // Update fields
        if (command.firstName() != null && !command.firstName().isBlank()) {
            teacher.setFirstName(command.firstName().trim());
        }
        if (command.lastName() != null && !command.lastName().isBlank()) {
            teacher.setLastName(command.lastName().trim());
        }

        User updatedTeacher = userRepositoryPort.save(teacher);
        log.info("Teacher updated successfully: {}", updatedTeacher.getEmail());

        return updatedTeacher;
    }

    @Override
    @Transactional
    public void deleteTeacher(Long teacherId) {
        log.info("Deleting teacher ID: {}", teacherId);

        User teacher = userRepositoryPort.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        // Validate that user is actually a teacher
        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        // Check if teacher has active groups (OPEN or CLOSED)
        long activeGroupsCount = groupRepositoryPort.countActiveGroupsByTeacherId(teacherId);
        if (activeGroupsCount > 0) {
            throw new TeacherHasActiveGroupsException(teacherId, activeGroupsCount);
        }

        // Soft delete: change status to BLOCKED
        teacher.setStatus(UserStatus.BLOCKED);
        userRepositoryPort.save(teacher);

        log.info("Teacher deleted (blocked) successfully: {}", teacher.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getTeachers(UserFilters filters) {
        log.info("Fetching teachers with filters");

        // Force roleType to TEACHER to ensure only teachers are returned
        UserFilters teacherFilters = new UserFilters(
                filters.email(),
                filters.searchTerm(),
                filters.status(),
                RoleType.TEACHER, // Force TEACHER role
                filters.page(),
                filters.size(),
                filters.sortBy(),
                filters.sortDirection()
        );

        return userRepositoryPort.findWithFilters(teacherFilters);
    }

    @Override
    @Transactional(readOnly = true)
    public User getTeacherById(Long teacherId) {
        log.info("Getting teacher by ID: {}", teacherId);

        User teacher = userRepositoryPort.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        // Validate that user is actually a teacher
        if (!teacher.isTeacher()) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        return teacher;
    }
}
