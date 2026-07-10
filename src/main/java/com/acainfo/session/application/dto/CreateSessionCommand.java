package com.acainfo.session.application.dto;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionType;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command DTO for creating a session.
 *
 * <p>Field requirements by session type:</p>
 * <ul>
 *   <li>REGULAR: courseId and scheduleId required (typically auto-generated from Schedule)</li>
 *   <li>EXTRA: courseId required, scheduleId must be null</li>
 * </ul>
 */
public record CreateSessionCommand(
        SessionType type,
        Long subjectId,      // Derived from the course's subject
        Long courseId,       // Required
        Long scheduleId,     // Required for REGULAR, null for EXTRA
        Classroom classroom,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        SessionMode mode
) {
    /**
     * Factory method for creating a REGULAR session from a schedule.
     */
    public static CreateSessionCommand forRegularSession(
            Long courseId,
            Long scheduleId,
            Classroom classroom,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            SessionMode mode
    ) {
        return new CreateSessionCommand(
                SessionType.REGULAR,
                null, // subjectId derived from course
                courseId,
                scheduleId,
                classroom,
                date,
                startTime,
                endTime,
                mode
        );
    }

    /**
     * Factory method for creating an EXTRA session for a course.
     */
    public static CreateSessionCommand forExtraSession(
            Long courseId,
            Classroom classroom,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            SessionMode mode
    ) {
        return new CreateSessionCommand(
                SessionType.EXTRA,
                null, // subjectId derived from course
                courseId,
                null, // no schedule
                classroom,
                date,
                startTime,
                endTime,
                mode
        );
    }
}
