package com.acainfo.group.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when trying to enroll in a group that has reached its maximum capacity.
 */
public class GroupCapacityExceededException extends BusinessRuleException {

    public GroupCapacityExceededException(Long groupId, int maxCapacity) {
        super(String.format("Group %d has reached its maximum capacity of %d students", groupId, maxCapacity));
    }

    public GroupCapacityExceededException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "GROUP_CAPACITY_EXCEEDED";
    }
}
