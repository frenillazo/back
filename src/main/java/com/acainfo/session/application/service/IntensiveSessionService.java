package com.acainfo.session.application.service;

import com.acainfo.intensive.application.port.out.IntensiveRepositoryPort;
import com.acainfo.intensive.domain.exception.IntensiveNotFoundException;
import com.acainfo.intensive.domain.exception.InvalidIntensiveDataException;
import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.application.dto.IntensiveSessionEntry;
import com.acainfo.session.application.port.in.CreateIntensiveSessionsUseCase;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Service for creating sessions of an intensive course.
 *
 * <p>Sessions of intensives:</p>
 * <ul>
 *   <li>Are not derived from a {@code Schedule} (no recurrence).</li>
 *   <li>Have {@code groupId=null}, {@code scheduleId=null}, {@code intensiveId=X}.</li>
 *   <li>{@link SessionType#INTENSIVE}.</li>
 *   <li>Their date must fall within {@code intensive.startDate..endDate} (inclusive).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntensiveSessionService implements CreateIntensiveSessionsUseCase {

    private final IntensiveRepositoryPort intensiveRepository;
    private final SessionRepositoryPort sessionRepository;

    @Override
    @Transactional
    public List<Session> createBulk(Long intensiveId, List<IntensiveSessionEntry> entries) {
        log.info("Bulk creating {} sessions for intensive {}",
                entries == null ? 0 : entries.size(), intensiveId);

        if (entries == null || entries.isEmpty()) {
            return List.of();
        }

        Intensive intensive = intensiveRepository.findById(intensiveId)
                .orElseThrow(() -> new IntensiveNotFoundException(intensiveId));

        if (!intensive.isOpen() && !intensive.isClosed()) {
            throw new InvalidIntensiveDataException(
                    "Cannot add sessions to a CANCELLED intensive (id=" + intensiveId + ")"
            );
        }

        List<Session> toSave = new ArrayList<>();
        for (IntensiveSessionEntry entry : entries) {
            toSave.add(buildSession(intensive, entry));
        }

        List<Session> saved = sessionRepository.saveAll(toSave);
        log.info("Created {} intensive sessions for intensive {}", saved.size(), intensiveId);
        return saved;
    }

    @Override
    @Transactional
    public Session createSingle(Long intensiveId, IntensiveSessionEntry entry) {
        return createBulk(intensiveId, List.of(entry)).get(0);
    }

    // ==================== Helpers ====================

    private Session buildSession(Intensive intensive, IntensiveSessionEntry entry) {
        validateEntry(intensive, entry);

        SessionMode mode = determineMode(entry.classroom());

        return Session.builder()
                .subjectId(intensive.getSubjectId())
                .groupId(null)
                .intensiveId(intensive.getId())
                .scheduleId(null)
                .classroom(entry.classroom())
                .date(entry.date())
                .startTime(entry.startTime())
                .endTime(entry.endTime())
                .status(SessionStatus.SCHEDULED)
                .type(SessionType.INTENSIVE)
                .mode(mode)
                .build();
    }

    private void validateEntry(Intensive intensive, IntensiveSessionEntry entry) {
        if (entry.date() == null || entry.startTime() == null
                || entry.endTime() == null || entry.classroom() == null) {
            throw new InvalidSessionStateException(
                    "Intensive session requires date, startTime, endTime and classroom"
            );
        }
        if (!entry.startTime().isBefore(entry.endTime())) {
            throw new InvalidSessionStateException("startTime must be before endTime");
        }
        if (!intensive.containsDate(entry.date())) {
            throw new InvalidSessionStateException(
                    "Session date " + entry.date() + " is outside intensive range ["
                            + intensive.getStartDate() + ", " + intensive.getEndDate() + "]"
            );
        }
    }

    private SessionMode determineMode(Classroom classroom) {
        return switch (classroom) {
            case AULA_VIRTUAL -> SessionMode.ONLINE;
            case AULA_PORTAL1, AULA_PORTAL2 -> SessionMode.IN_PERSON;
        };
    }
}
