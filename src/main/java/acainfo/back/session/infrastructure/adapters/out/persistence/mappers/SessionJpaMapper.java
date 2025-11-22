package acainfo.back.session.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.schedule.infrastructure.adapters.out.persistence.entities.ScheduleJpaEntity;
import acainfo.back.session.domain.model.SessionDomain;
import acainfo.back.session.infrastructure.adapters.out.persistence.entities.SessionJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Mapper
 * Converts: SessionDomain ↔ SessionJpaEntity
 *
 * Responsibility: Anti-corruption layer between domain and persistence
 */
@Component
@RequiredArgsConstructor
public class SessionJpaMapper {

    /**
     * Converts JPA Entity → Domain
     * Maps entity references to IDs
     */
    public SessionDomain toDomain(SessionJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return SessionDomain.builder()
                .id(jpa.getId())
                .subjectGroupId(jpa.getSubjectGroup() != null ? jpa.getSubjectGroup().getId() : null)
                .generatedFromScheduleId(jpa.getGeneratedFromSchedule() != null ? jpa.getGeneratedFromSchedule().getId() : null)
                .type(jpa.getType())
                .scheduledStart(jpa.getScheduledStart())
                .scheduledEnd(jpa.getScheduledEnd())
                .actualStart(jpa.getActualStart())
                .actualEnd(jpa.getActualEnd())
                .mode(jpa.getMode())
                .status(jpa.getStatus())
                .classroom(jpa.getClassroom())
                .zoomMeetingId(jpa.getZoomMeetingId())
                .cancellationReason(jpa.getCancellationReason())
                .postponementReason(jpa.getPostponementReason())
                .originalSessionId(jpa.getOriginalSessionId())
                .recoveryForSessionId(jpa.getRecoveryForSessionId())
                .notes(jpa.getNotes())
                .topicsCovered(jpa.getTopicsCovered())
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Converts Domain → JPA Entity (for updates)
     * Maps IDs to entity references
     * Note: Requires fetching related entities from repositories
     */
    public SessionJpaEntity toJpaEntity(SessionDomain domain,
                                        SubjectGroupJpaEntity subjectGroup,
                                        ScheduleJpaEntity schedule) {
        if (domain == null) {
            return null;
        }

        return SessionJpaEntity.builder()
                .id(domain.getId())
                .subjectGroup(subjectGroup)
                .generatedFromSchedule(schedule)
                .type(domain.getType())
                .scheduledStart(domain.getScheduledStart())
                .scheduledEnd(domain.getScheduledEnd())
                .actualStart(domain.getActualStart())
                .actualEnd(domain.getActualEnd())
                .mode(domain.getMode())
                .status(domain.getStatus())
                .classroom(domain.getClassroom())
                .zoomMeetingId(domain.getZoomMeetingId())
                .cancellationReason(domain.getCancellationReason())
                .postponementReason(domain.getPostponementReason())
                .originalSessionId(domain.getOriginalSessionId())
                .recoveryForSessionId(domain.getRecoveryForSessionId())
                .notes(domain.getNotes())
                .topicsCovered(domain.getTopicsCovered())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing JPA entity with domain data
     * Preserves entity references and immutable fields
     */
    public void updateJpaEntity(SessionJpaEntity jpa, SessionDomain domain) {
        if (jpa == null || domain == null) {
            return;
        }

        // Don't update: id, subjectGroup, generatedFromSchedule, createdAt
        // These are immutable or managed separately

        jpa.setType(domain.getType());
        jpa.setScheduledStart(domain.getScheduledStart());
        jpa.setScheduledEnd(domain.getScheduledEnd());
        jpa.setActualStart(domain.getActualStart());
        jpa.setActualEnd(domain.getActualEnd());
        jpa.setMode(domain.getMode());
        jpa.setStatus(domain.getStatus());
        jpa.setClassroom(domain.getClassroom());
        jpa.setZoomMeetingId(domain.getZoomMeetingId());
        jpa.setCancellationReason(domain.getCancellationReason());
        jpa.setPostponementReason(domain.getPostponementReason());
        jpa.setOriginalSessionId(domain.getOriginalSessionId());
        jpa.setRecoveryForSessionId(domain.getRecoveryForSessionId());
        jpa.setNotes(domain.getNotes());
        jpa.setTopicsCovered(domain.getTopicsCovered());
        jpa.setUpdatedAt(domain.getUpdatedAt());
    }
}
