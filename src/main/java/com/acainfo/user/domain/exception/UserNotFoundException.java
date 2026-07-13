package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long userId) {
        super("Usuario no encontrado con id: " + userId);
    }

    public UserNotFoundException(String email) {
        super("Usuario no encontrado con email: " + email);
    }
}
