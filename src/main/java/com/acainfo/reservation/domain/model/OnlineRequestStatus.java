package com.acainfo.reservation.domain.model;

/**
 * Status of a request to change from in-person to online attendance.
 * Only applicable for regular group sessions.
 */
public enum OnlineRequestStatus {

    /**
     * Request is pending teacher approval.
     */
    PENDING,

    /**
     * Request was approved by the teacher.
     */
    APPROVED,

    /**
     * Request was rejected by the teacher.
     */
    REJECTED
}
