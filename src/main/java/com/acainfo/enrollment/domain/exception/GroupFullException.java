package com.acainfo.enrollment.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when trying to enroll in a full group without waiting list option.
 */
public class GroupFullException extends BusinessRuleException {

    public GroupFullException(Long groupId) {
        super("Group " + groupId + " is full and has no available seats");
    }

    @Override
    public String getErrorCode() {
        return "GROUP_FULL";
    }
}
