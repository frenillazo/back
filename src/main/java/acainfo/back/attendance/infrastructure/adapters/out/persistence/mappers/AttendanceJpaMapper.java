package acainfo.back.attendance.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.infrastructure.adapters.out.persistence.entities.AttendanceJpaEntity;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.EnrollmentJpaEntity;
import acainfo.back.session.infrastructure.adapters.out.persistence.entities.SessionJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Mapper
 * Converts: AttendanceDomain ↔ AttendanceJpaEntity
 *
 * Responsibility: Bridge domain and persistence layers
 * - Domain uses IDs for relationships (decoupled)
 * - JPA uses entity references (persistence requirement)
 */
@Component
public class AttendanceJpaMapper {

    /**
     * Converts JPA Entity → Domain
     * Extracts IDs from entity references
     */
    public AttendanceDomain toDomain(AttendanceJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return AttendanceDomain.builder()
                .id(jpa.getId())
                .sessionId(jpa.getSession() != null ? jpa.getSession().getId() : null)
                .enrollmentId(jpa.getEnrollment() != null ? jpa.getEnrollment().getId() : null)
                .status(jpa.getStatus())
                .recordedAt(jpa.getRecordedAt())
                .recordedById(jpa.getRecordedBy() != null ? jpa.getRecordedBy().getId() : null)
                .notes(jpa.getNotes())
                .minutesLate(jpa.getMinutesLate())
                .justifiedAt(jpa.getJustifiedAt())
                .justifiedById(jpa.getJustifiedBy() != null ? jpa.getJustifiedBy().getId() : null)
                .createdAt(jpa.getCreatedAt())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Converts Domain → JPA Entity (for new entities)
     * Requires fetched entity references
     */
    public AttendanceJpaEntity toJpaEntity(
            AttendanceDomain domain,
            SessionJpaEntity session,
            EnrollmentJpaEntity enrollment,
            UserJpaEntity recordedBy,
            UserJpaEntity justifiedBy
    ) {
        if (domain == null) {
            return null;
        }

        return AttendanceJpaEntity.builder()
                .id(domain.getId())
                .session(session)
                .enrollment(enrollment)
                .status(domain.getStatus())
                .recordedAt(domain.getRecordedAt())
                .recordedBy(recordedBy)
                .notes(domain.getNotes())
                .minutesLate(domain.getMinutesLate())
                .justifiedAt(domain.getJustifiedAt())
                .justifiedBy(justifiedBy)
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Updates existing JPA Entity from Domain
     * Used for updates - preserves immutable fields (session, enrollment, recordedBy, recordedAt, createdAt)
     */
    public void updateJpaEntity(AttendanceJpaEntity jpa, AttendanceDomain domain, UserJpaEntity justifiedBy) {
        if (jpa == null || domain == null) {
            return;
        }

        // Update mutable fields only
        jpa.setStatus(domain.getStatus());
        jpa.setNotes(domain.getNotes());
        jpa.setMinutesLate(domain.getMinutesLate());
        jpa.setJustifiedAt(domain.getJustifiedAt());
        jpa.setJustifiedBy(justifiedBy);
        jpa.setUpdatedAt(domain.getUpdatedAt());

        // Do NOT update: id, session, enrollment, recordedBy, recordedAt, createdAt
    }
}
