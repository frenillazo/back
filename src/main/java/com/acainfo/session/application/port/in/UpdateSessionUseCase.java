package com.acainfo.session.application.port.in;

import com.acainfo.session.application.dto.UpdateSessionCommand;
import com.acainfo.session.domain.model.Session;

/**
 * Use case for updating sessions.
 * Input port defining the contract for session updates.
 */
public interface UpdateSessionUseCase {

    /**
     * Update an existing session.
     *
     * @param id Session ID
     * @param command Update data
     * @return The updated session
     * @throws com.acainfo.session.domain.exception.SessionNotFoundException if not found
     * @throws com.acainfo.session.domain.exception.InvalidSessionStateException if session cannot be updated
     */
    Session update(Long id, UpdateSessionCommand command);
}
