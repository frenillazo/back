package com.acainfo.enrollment.domain.exception;

import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when a group request doesn't have enough supporters for approval.
 */
public class InsufficientSupportersException extends BusinessRuleException {

    public InsufficientSupportersException(int currentCount) {
        super("Insufficient supporters for approval. Current: " + currentCount +
              ", Required: " + GroupRequest.MIN_SUPPORTERS_FOR_APPROVAL);
    }

    @Override
    public String getErrorCode() {
        return "INSUFFICIENT_SUPPORTERS";
    }
}
