package com.acainfo.group.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

/**
 * Exception thrown when a group is not found.
 */
public class GroupNotFoundException extends NotFoundException {

    public GroupNotFoundException(Long id) {
        super("Group not found with ID: " + id);
    }

    @Override
    public String getErrorCode() {
        return "GROUP_NOT_FOUND";
    }
}
