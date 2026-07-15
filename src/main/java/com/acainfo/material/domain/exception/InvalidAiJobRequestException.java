package com.acainfo.material.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an AI LaTeX job request is invalid
 * (e.g. transcribing a material that is not a PDF, or no captures).
 */
public class InvalidAiJobRequestException extends BusinessRuleException {

    public InvalidAiJobRequestException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "INVALID_AI_JOB_REQUEST";
    }
}
