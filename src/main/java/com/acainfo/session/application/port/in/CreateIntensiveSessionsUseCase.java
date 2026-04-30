package com.acainfo.session.application.port.in;

import com.acainfo.session.application.dto.IntensiveSessionEntry;
import com.acainfo.session.domain.model.Session;

import java.util.List;

/**
 * Use case for creating, editing and deleting sessions of an intensive course.
 *
 * <p>Sessions of intensives are NOT generated from a {@code Schedule}; the admin
 * specifies arbitrary dates and times.</p>
 */
public interface CreateIntensiveSessionsUseCase {

    /**
     * Create multiple sessions for an intensive course in one transaction.
     */
    List<Session> createBulk(Long intensiveId, List<IntensiveSessionEntry> entries);

    /**
     * Create a single session for an intensive course.
     */
    Session createSingle(Long intensiveId, IntensiveSessionEntry entry);
}
