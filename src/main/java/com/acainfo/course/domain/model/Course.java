package com.acainfo.course.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Course domain entity (anemic model with Lombok).
 *
 * <p>Unified model: a course is a group of students taking a subject, optionally with
 * a teacher, over a date range. Sessions are derived from {@code Schedule}s (weekly
 * recurrence) and bound by {@code [startDate, endDate]}. An "intensive" is simply a
 * course with a short date range — there is no separate type or entity.</p>
 *
 * <p>Capacity semantics: {@code capacity} is the number of physical seats (typically 24
 * for an on-site course). {@code null} means unlimited (virtual/dual courses) — no
 * enrollment cap and no waiting list.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Course {

    private Long id;
    private String name;                         // Auto-generated: "[subjectName] grupo N YY-YY"
    private Long subjectId;
    private Long teacherId;                      // Optional: null = not assigned yet
    private CourseStatus status;                 // OPEN, CLOSED, CANCELLED
    private BigDecimal pricePerMonth;            // Informative only (payments are handled outside the app)
    private Integer capacity;                    // null = unlimited (virtual/dual)

    private LocalDate startDate;                 // Inclusive — first day sessions can be generated
    private LocalDate endDate;                   // Inclusive — last day sessions can be generated

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Query Methods ====================

    public boolean isOpen() {
        return status == CourseStatus.OPEN;
    }

    public boolean isClosed() {
        return status == CourseStatus.CLOSED;
    }

    public boolean isCancelled() {
        return status == CourseStatus.CANCELLED;
    }

    /**
     * Whether this course has an enrollment cap.
     * Courses without capacity (virtual/dual) accept unlimited enrollments and never
     * use the waiting list. NOTE: occupancy itself is NOT stored on the course —
     * always compute it via {@code enrollmentRepositoryPort.countActiveByCourseId}.
     */
    public boolean hasCapacityLimit() {
        return capacity != null;
    }

    /**
     * Whether {@code date} is contained in the [startDate, endDate] range (both inclusive).
     */
    public boolean containsDate(LocalDate date) {
        if (date == null || startDate == null || endDate == null) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
