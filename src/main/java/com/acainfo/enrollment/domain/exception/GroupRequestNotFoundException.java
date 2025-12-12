package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a group request is not found.
 */
public class GroupRequestNotFoundException extends NotFoundException {

    public GroupRequestNotFoundException(Long id) {
        super("Group request not found with ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "GROUP_REQUEST_NOT_FOUND";
    }
}
