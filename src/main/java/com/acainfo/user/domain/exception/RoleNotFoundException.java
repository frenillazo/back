package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;
import com.acainfo.user.domain.model.RoleType;

/**
 * Exception thrown when a role is not found.
 */
public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(Long roleId) {
        super("Rol no encontrado con id: " + roleId);
    }

    public RoleNotFoundException(RoleType roleType) {
        super("Rol no encontrado con tipo: " + roleType);
    }
}
