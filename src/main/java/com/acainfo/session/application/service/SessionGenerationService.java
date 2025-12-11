package com.acainfo.session.application.service;

import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.dto.GenerateSessionsCommand;
import com.acainfo.session.application.port.in.GenerateSessionsUseCase;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.exception.InvalidSessionStateException;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

        log.info("Generated {} sessions", savedSessions.size());
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
                                .mode(determineSessionMode(schedule))
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
}
