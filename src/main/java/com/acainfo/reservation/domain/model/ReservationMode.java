package com.acainfo.reservation.domain.model;

/**
 * Mode of attendance for a session reservation.
 */
public enum ReservationMode {

    /**
     * Student will attend in person (limited to classroom capacity, typically 24).
     */
    IN_PERSON,

    /**
     * Student will attend online.
     */
    ONLINE
}
