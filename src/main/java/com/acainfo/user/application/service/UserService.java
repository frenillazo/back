package com.acainfo.user.application.service;

import com.acainfo.user.application.dto.UpdateUserCommand;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.application.port.in.UpdateUserProfileUseCase;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.InvalidCredentialsException;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementing user profile use cases.
 * Handles profile retrieval and updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements
        GetUserProfileUseCase,
        UpdateUserProfileUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);
        return userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UpdateUserCommand command) {
        log.info("Updating profile for user ID: {}", userId);

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Update fields
        if (command.firstName() != null && !command.firstName().isBlank()) {
            user.setFirstName(command.firstName().trim());
        }
        if (command.lastName() != null && !command.lastName().isBlank()) {
            user.setLastName(command.lastName().trim());
        }

        User updatedUser = userRepositoryPort.save(user);
        log.info("Profile updated successfully for user: {}", updatedUser.getEmail());

        return updatedUser;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepositoryPort.save(user);

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findIdsByEmailContaining(String emailSearch) {
        if (emailSearch == null || emailSearch.isBlank()) {
            return List.of();
        }
        log.debug("Finding user IDs by email containing: {}", emailSearch);
        return userRepositoryPort.findIdsByEmailContaining(emailSearch);
    }
}
