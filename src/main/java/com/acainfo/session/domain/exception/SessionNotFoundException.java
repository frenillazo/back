package com.acainfo.session.domain.exception;

import com.acainfo.shared.domain.exception.NotFoundException;

public class SessionNotFoundException extends NotFoundException {
    public SessionNotFoundException(Long sessionId) {
        super("Session not found with id: " + sessionId);
    }
}
