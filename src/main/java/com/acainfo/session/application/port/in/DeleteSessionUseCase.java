package com.acainfo.session.application.port.in;

/**
 * Use case for deleting sessions.
 * Input port defining the contract for session deletion.
 */
public interface DeleteSessionUseCase {

    /**
     * Delete a session (hard delete).
     * Only allowed for sessions that haven't started (SCHEDULED status).
     *
     * @param id Session ID
     * @throws com.acainfo.session.domain.exception.SessionNotFoundException if not found
     * @throws com.acainfo.session.domain.exception.InvalidSessionStateException if session cannot be deleted
     */
    void delete(Long id);
}
