package com.acainfo.reservation.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

/**
 * Exception thrown when an online request already exists for a reservation.
 */
public class OnlineRequestAlreadyExistsException extends BusinessRuleException {

    public OnlineRequestAlreadyExistsException(Long reservationId) {
        super("An online attendance request already exists for reservation " + reservationId);
    }

    @Override
    public String getErrorCode() {
        return "ONLINE_REQUEST_ALREADY_EXISTS";
    }
}
