package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an online attendance request is made too late.
 * Requests must be submitted at least 6 hours before the session.
 */
public class OnlineRequestTooLateException extends BusinessRuleException {

    private static final int MINIMUM_HOURS_ADVANCE = 6;

    public OnlineRequestTooLateException(Long sessionId) {
        super("Online attendance request for session " + sessionId +
              " must be submitted at least " + MINIMUM_HOURS_ADVANCE + " hours in advance");
    }

    @Override
    public String getErrorCode() {
        return "ONLINE_REQUEST_TOO_LATE";
    }
}
