package com.acainfo.reservation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * SessionReservation domain entity - Anemic model with Lombok.
 * Represents a student's reservation for a specific session, including attendance tracking.
 *
 * <p>This unified entity handles:</p>
 * <ul>
 *   <li>Session reservation (pre-session): who has a seat in which session</li>
 *   <li>Online attendance request (pre-session): request to attend online instead of in-person</li>
 *   <li>Attendance tracking (post-session): whether the student showed up or not</li>
 * </ul>
 *
 * <p>Business rules (enforced in application services):</p>
 * <ul>
 *   <li>Auto-generated for enrolled students when sessions are created from schedules</li>
 *   <li>Students can switch to another session of the same subject (different group) if seats available</li>
 *   <li>In-person attendance limited to classroom capacity (typically 24)</li>
 *   <li>Online attendance requests require 6+ hours advance notice and teacher approval (regular groups only)</li>
 *   <li>Intensive groups: single group per subject, reserve as in-person or online directly</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class SessionReservation {

    private Long id;

    /**
     * Reference to the student (User with STUDENT role).
     */
    private Long studentId;

    /**
     * Reference to the session this reservation is for.
     */
    private Long sessionId;

    /**
     * Reference to the student's original enrollment.
     * Kept for traceability even when attending a different group's session.
     */
    private Long enrollmentId;

    // ==================== Reservation Fields (Pre-Session) ====================

    /**
     * Mode of attendance: IN_PERSON or ONLINE.
     */
    private ReservationMode mode;

    /**
     * Status of the reservation.
     */
    private ReservationStatus status;

    /**
     * When the reservation was created.
     */
    private LocalDateTime reservedAt;

    /**
     * When the reservation was cancelled (if applicable).
     */
    private LocalDateTime cancelledAt;

    // ==================== Online Request Fields (Pre-Session, Regular Groups) ====================

    /**
     * Status of online attendance request.
     * Null if no request has been made (student attending in-person as scheduled).
     * Only applicable for regular group sessions.
     */
    private OnlineRequestStatus onlineRequestStatus;

    /**
     * When the online request was submitted.
     */
    private LocalDateTime onlineRequestedAt;

    /**
     * When the online request was processed (approved/rejected).
     */
    private LocalDateTime onlineRequestProcessedAt;

    /**
     * Teacher who processed the online request.
     */
    private Long onlineRequestProcessedById;

    // ==================== Attendance Fields (Post-Session) ====================

    /**
     * Actual attendance status after the session.
     * Null until attendance is recorded.
     */
    private AttendanceStatus attendanceStatus;

    /**
     * When attendance was recorded.
     */
    private LocalDateTime attendanceRecordedAt;

    /**
     * User who recorded the attendance (teacher or admin).
     */
    private Long attendanceRecordedById;

    // ==================== Audit Fields ====================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Reservation Query Methods ====================

    /**
     * Check if reservation is confirmed.
     */
    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    /**
     * Check if reservation is cancelled.
     */
    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }

    /**
     * Check if attending in person.
     */
    public boolean isInPerson() {
        return mode == ReservationMode.IN_PERSON;
    }

    /**
     * Check if attending online.
     */
    public boolean isOnline() {
        return mode == ReservationMode.ONLINE;
    }

    // ==================== Online Request Query Methods ====================

    /**
     * Check if an online request has been made.
     */
    public boolean hasOnlineRequest() {
        return onlineRequestStatus != null;
    }

    /**
     * Check if online request is pending.
     */
    public boolean isOnlineRequestPending() {
        return onlineRequestStatus == OnlineRequestStatus.PENDING;
    }

    /**
     * Check if online request was approved.
     */
    public boolean isOnlineRequestApproved() {
        return onlineRequestStatus == OnlineRequestStatus.APPROVED;
    }

    /**
     * Check if online request was rejected.
     */
    public boolean isOnlineRequestRejected() {
        return onlineRequestStatus == OnlineRequestStatus.REJECTED;
    }

    // ==================== Attendance Query Methods ====================

    /**
     * Check if attendance has been recorded.
     */
    public boolean hasAttendanceRecorded() {
        return attendanceStatus != null;
    }

    /**
     * Check if student was present.
     */
    public boolean wasPresent() {
        return attendanceStatus == AttendanceStatus.PRESENT;
    }

    /**
     * Check if student was absent.
     */
    public boolean wasAbsent() {
        return attendanceStatus == AttendanceStatus.ABSENT;
    }

    // ==================== Computed Properties ====================

    /**
     * Check if this is a reservation for a different group than the enrollment.
     * Useful to identify cross-group attendance.
     */
    public boolean isCrossGroupReservation() {
        // This will be determined by comparing session's groupId with enrollment's groupId
        // Logic implemented in application service since it requires loading related entities
        return false; // Placeholder - actual logic in service layer
    }

    /**
     * Check if reservation can be cancelled.
     */
    public boolean canBeCancelled() {
        return isConfirmed() && !hasAttendanceRecorded();
    }

    /**
     * Check if online request can be submitted.
     * Must be in-person reservation without existing request.
     */
    public boolean canRequestOnline() {
        return isConfirmed() && isInPerson() && !hasOnlineRequest();
    }
}
