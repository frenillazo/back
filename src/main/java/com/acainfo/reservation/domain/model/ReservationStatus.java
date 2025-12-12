package com.acainfo.reservation.domain.model;

/**
 * Status of a session reservation.
 */
public enum ReservationStatus {

    /**
     * Reservation is confirmed and active.
     */
    CONFIRMED,

    /**
     * Reservation was cancelled by the student.
     */
    CANCELLED
}
