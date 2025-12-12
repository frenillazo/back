package com.acainfo.reservation.application.port.in;

import com.acainfo.reservation.application.dto.GenerateReservationsCommand;
import com.acainfo.reservation.domain.model.SessionReservation;

import java.util.List;

/**
 * Use case for generating reservations when sessions are created.
 * Input port defining the contract for automatic reservation generation.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>Called automatically when sessions are generated from schedules</li>
 *   <li>Creates IN_PERSON reservations for all active enrollments in the group</li>
 *   <li>For REGULAR groups: first 24 students get IN_PERSON, rest get ONLINE if allowed</li>
 *   <li>For INTENSIVE groups: students choose mode when reserving</li>
 * </ul>
 */
public interface GenerateReservationsUseCase {

    /**
     * Generate reservations for all enrolled students when a session is created.
     *
     * @param command Generation data (sessionId, groupId)
     * @return List of created reservations
     */
    List<SessionReservation> generate(GenerateReservationsCommand command);
}
