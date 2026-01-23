package com.acainfo.user.application.service;

import com.acainfo.user.application.port.in.ManageUserRolesUseCase;
import com.acainfo.user.application.port.out.RoleRepositoryPort;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user roles and status.
 * Implements business logic for role assignment/revocation and status updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleManagementService implements ManageUserRolesUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    @Transactional
    public User assignRole(Long userId, RoleType roleType) {
        log.info("Assigning role {} to user {}", roleType, userId);

        // Get user
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user already has the role
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> role.getType() == roleType);

        if (hasRole) {
            throw new IllegalArgumentException(
                    "User already has role: " + roleType
            );
        }

        // Get role entity
        Role role = roleRepositoryPort.findByType(roleType)
                .orElseThrow(() -> new IllegalStateException(
                        "Role not found: " + roleType
                ));

        // Add role to user
        user.getRoles().add(role);

        // Save and return
        User updatedUser = userRepositoryPort.save(user);
        log.info("Role {} assigned to user {} successfully", roleType, userId);

        return updatedUser;
    }

    @Override
    @Transactional
    public User revokeRole(Long userId, RoleType roleType) {
        log.info("Revoking role {} from user {}", roleType, userId);

        // Get user
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Check if user has the role
        boolean hasRole = user.getRoles().stream()
                .anyMatch(role -> role.getType() == roleType);

        if (!hasRole) {
            throw new IllegalArgumentException(
                    "User doesn't have role: " + roleType
            );
        }

        // Prevent removing last role
        if (user.getRoles().size() <= 1) {
            throw new IllegalArgumentException(
                    "Cannot remove last role from user. User must have at least one role."
            );
        }

        // Remove role
        user.getRoles().removeIf(role -> role.getType() == roleType);

        // Save and return
        User updatedUser = userRepositoryPort.save(user);
        log.info("Role {} revoked from user {} successfully", roleType, userId);

        return updatedUser;
    }

    @Override
    @Transactional
    public User updateStatus(Long userId, UserStatus status) {
        log.info("Updating status for user {} to {}", userId, status);

        // Get user
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Update status
        user.setStatus(status);

        // Save and return
        User updatedUser = userRepositoryPort.save(user);
        log.info("Status updated for user {} to {} successfully", userId, status);

        return updatedUser;
    }
}
