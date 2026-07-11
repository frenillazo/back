package com.acainfo.enrollment.application.port.in;

/**
 * Use case invoked when a course stops being OPEN (CLOSED or CANCELLED):
 * its live enrollments must stop being "active".
 *
 * <p>Semantics (decided 11-jul-2026): closing a course means the course is over
 * for enrollment purposes — ACTIVE enrollments are marked COMPLETED (the student
 * took the course), and PENDING_APPROVAL / WAITING_LIST ones are marked EXPIRED
 * (they never got to take it). Future reservations of completed students are
 * cancelled.</p>
 */
public interface CloseCourseEnrollmentsUseCase {

    /**
     * Close all live enrollments of a course.
     *
     * @param courseId the course being closed/cancelled
     * @return number of enrollments transitioned
     */
    int closeAllForCourse(Long courseId);
}
