package com.acainfo.user.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;
import com.acainfo.user.domain.model.RoleType;

/**
 * Exception thrown when a role is not found.
 */
public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(Long roleId) {
        super("Role not found with id: " + roleId);
    }

    public RoleNotFoundException(RoleType roleType) {
        super("Role not found with type: " + roleType);
    }
}
