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
 *   <li>REGULAR: groupId and scheduleId required (typically auto-generated from Schedule)</li>
 *   <li>EXTRA: groupId required, scheduleId must be null</li>
 *   <li>SCHEDULING: subjectId required, groupId and scheduleId must be null</li>
 * </ul>
 */
public record CreateSessionCommand(
        SessionType type,
        Long subjectId,      // Required for SCHEDULING, derived for others
        Long groupId,        // Required for REGULAR/EXTRA, null for SCHEDULING
        Long scheduleId,     // Required for REGULAR, null for EXTRA/SCHEDULING
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
            Long groupId,
            Long scheduleId,
            Classroom classroom,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            SessionMode mode
    ) {
        return new CreateSessionCommand(
                SessionType.REGULAR,
                null, // subjectId derived from group
                groupId,
                scheduleId,
                classroom,
                date,
                startTime,
                endTime,
                mode
        );
    }

    /**
     * Factory method for creating an EXTRA session for a group.
     */
    public static CreateSessionCommand forExtraSession(
            Long groupId,
            Classroom classroom,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            SessionMode mode
    ) {
        return new CreateSessionCommand(
                SessionType.EXTRA,
                null, // subjectId derived from group
                groupId,
                null, // no schedule
                classroom,
                date,
                startTime,
                endTime,
                mode
        );
    }

    /**
     * Factory method for creating a SCHEDULING session for a subject.
     */
    public static CreateSessionCommand forSchedulingSession(
            Long subjectId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    ) {
        return new CreateSessionCommand(
                SessionType.SCHEDULING,
                subjectId,
                null, // no group yet
                null, // no schedule
                Classroom.AULA_VIRTUAL, // always online
                date,
                startTime,
                endTime,
                SessionMode.ONLINE // always online
        );
    }
}
