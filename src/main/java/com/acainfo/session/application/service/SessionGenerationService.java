package com.acainfo.session.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.reservation.application.dto.GenerateReservationsCommand;
import com.acainfo.reservation.application.port.in.GenerateReservationsUseCase;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.dto.GenerateSessionsCommand;
import com.acainfo.session.application.port.in.GenerateSessionsUseCase;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.exception.TeacherSessionConflictException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service implementing session generation from schedules.
 * Handles bulk creation of REGULAR sessions based on schedule patterns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionGenerationService implements GenerateSessionsUseCase {

    private final SessionRepositoryPort sessionRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final ScheduleRepositoryPort scheduleRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final GenerateReservationsUseCase generateReservationsUseCase;

    @Override
    @Transactional
    public List<Session> generate(GenerateSessionsCommand command) {
        log.info("Generating sessions: groupId={}, from={}, to={}",
                command.groupId(), command.startDate(), command.endDate());

        List<Session> sessionsToCreate = preview(command);

        if (sessionsToCreate.isEmpty()) {
            log.info("No sessions to generate");
            return List.of();
        }

        List<Session> savedSessions = sessionRepositoryPort.saveAll(sessionsToCreate);

        // Auto-generate reservations for all newly created sessions
        for (Session session : savedSessions) {
            generateReservationsUseCase.generate(
                    new GenerateReservationsCommand(session.getId(), session.getGroupId())
            );
        }

        log.info("Generated {} sessions with auto-reservations", savedSessions.size());
        return savedSessions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Session> preview(GenerateSessionsCommand command) {
        log.debug("Previewing session generation: groupId={}, from={}, to={}",
                command.groupId(), command.startDate(), command.endDate());

        if (command.groupId() == null) {
            throw new InvalidSessionStateException(
                    "Generation for all groups not yet implemented. Please specify a groupId."
            );
        }

        List<Schedule> schedules = scheduleRepositoryPort.findByGroupId(command.groupId());

        if (schedules.isEmpty()) {
            log.debug("No schedules found for groupId: {}", command.groupId());
            return List.of();
        }

        SubjectGroup group = groupRepositoryPort.findById(command.groupId())
                .orElseThrow(() -> new GroupNotFoundException(command.groupId()));

        List<Session> sessionsToCreate = new ArrayList<>();

        LocalDate currentDate = command.startDate();
        while (!currentDate.isAfter(command.endDate())) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            for (Schedule schedule : schedules) {
                if (schedule.getDayOfWeek() == dayOfWeek) {
                    if (!sessionRepositoryPort.existsByScheduleIdAndDate(schedule.getId(), currentDate)) {
                        SessionMode sessionMode = determineSessionMode(schedule);

                        // Check for teacher conflicts before adding the session
                        checkForTeacherConflicts(
                                group.getTeacherId(),
                                group.getSubjectId(),
                                currentDate,
                                schedule.getStartTime(),
                                schedule.getEndTime(),
                                sessionMode,
                                sessionsToCreate  // Also check against sessions being generated in this batch
                        );

                        Session session = Session.builder()
                                .subjectId(group.getSubjectId())
                                .groupId(command.groupId())
                                .scheduleId(schedule.getId())
                                .classroom(schedule.getClassroom())
                                .date(currentDate)
                                .startTime(schedule.getStartTime())
                                .endTime(schedule.getEndTime())
                                .status(SessionStatus.SCHEDULED)
                                .type(SessionType.REGULAR)
                                .mode(sessionMode)
                                .build();

                        sessionsToCreate.add(session);
                    }
                }
            }

            currentDate = currentDate.plusDays(1);
        }

        log.debug("Preview: {} sessions would be generated", sessionsToCreate.size());
        return sessionsToCreate;
    }

    private SessionMode determineSessionMode(Schedule schedule) {
        if (schedule.isOnline()) {
            return SessionMode.ONLINE;
        } else if (schedule.isPhysical()) {
            return SessionMode.IN_PERSON;
        }
        return SessionMode.DUAL;
    }

    /**
     * Check for teacher session conflicts.
     * A teacher can have overlapping sessions ONLY if:
     * 1. Both sessions are online (SessionMode.ONLINE)
     * 2. Both sessions are for the same subject
     *
     * @param teacherId The teacher's ID
     * @param subjectId The subject ID for the new session
     * @param date Session date
     * @param startTime Start time
     * @param endTime End time
     * @param mode Session mode (to check if online)
     * @param batchSessions Sessions being generated in the same batch (to check for internal conflicts)
     */
    private void checkForTeacherConflicts(
            Long teacherId,
            Long subjectId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            SessionMode mode,
            List<Session> batchSessions
    ) {
        boolean newSessionIsOnline = mode == SessionMode.ONLINE;

        // First, check against existing sessions in the database
        List<Session> existingSessions = sessionRepositoryPort.findByTeacherIdAndDate(teacherId, date);

        for (Session existing : existingSessions) {
            if (!timeOverlaps(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                continue; // No overlap, no conflict
            }

            boolean existingSessionIsOnline = existing.getMode() == SessionMode.ONLINE;
            boolean sameSubject = subjectId.equals(existing.getSubjectId());
            boolean bothOnline = newSessionIsOnline && existingSessionIsOnline;

            if (bothOnline && sameSubject) {
                continue; // Allowed overlap
            }

            // Conflict with existing session
            String teacherName = userRepositoryPort.findById(teacherId)
                    .map(user -> user.getFullName())
                    .orElse("ID " + teacherId);

            throw new TeacherSessionConflictException(
                    teacherName,
                    date,
                    existing.getStartTime(),
                    existing.getEndTime()
            );
        }

        // Second, check against sessions being generated in the same batch
        for (Session batchSession : batchSessions) {
            if (!batchSession.getDate().equals(date)) {
                continue; // Different date, no conflict
            }

            // Get teacher ID for batch session
            SubjectGroup batchGroup = groupRepositoryPort.findById(batchSession.getGroupId())
                    .orElse(null);
            if (batchGroup == null || !teacherId.equals(batchGroup.getTeacherId())) {
                continue; // Different teacher, no conflict
            }

            if (!timeOverlaps(startTime, endTime, batchSession.getStartTime(), batchSession.getEndTime())) {
                continue; // No overlap, no conflict
            }

            boolean batchSessionIsOnline = batchSession.getMode() == SessionMode.ONLINE;
            boolean sameSubject = subjectId.equals(batchSession.getSubjectId());
            boolean bothOnline = newSessionIsOnline && batchSessionIsOnline;

            if (bothOnline && sameSubject) {
                continue; // Allowed overlap
            }

            // Conflict with batch session
            String teacherName = userRepositoryPort.findById(teacherId)
                    .map(user -> user.getFullName())
                    .orElse("ID " + teacherId);

            throw new TeacherSessionConflictException(
                    teacherName,
                    date,
                    batchSession.getStartTime(),
                    batchSession.getEndTime()
            );
        }
    }

    /**
     * Check if two time ranges overlap.
     */
    private boolean timeOverlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
