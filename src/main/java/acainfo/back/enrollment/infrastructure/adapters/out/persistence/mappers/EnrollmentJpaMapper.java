package acainfo.back.enrollment.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.EnrollmentJpaEntity;
import acainfo.back.shared.domain.model.User;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Mapper
 * Converts: EnrollmentDomain ↔ EnrollmentJpaEntity
 *
 * Responsibility: Bridge domain and persistence layers
 * - Domain uses IDs for relationships (decoupled)
 * - JPA uses entity references (persistence requirement)
 */
@Component
public class EnrollmentJpaMapper {

    /**
     * Converts JPA Entity → Domain
     * Extracts IDs from entity references
     */
    public EnrollmentDomain toDomain(EnrollmentJpaEntity jpa) {
        if (jpa == null) {
            return null;
        }

        return EnrollmentDomain.builder()
                .id(jpa.getId())
                .studentId(jpa.getStudent() != null ? jpa.getStudent().getId() : null)
                .subjectGroupId(jpa.getSubjectGroup() != null ? jpa.getSubjectGroup().getId() : null)
                .status(jpa.getStatus())
                .attendanceMode(jpa.getAttendanceMode())
                .enrollmentDate(jpa.getEnrollmentDate())
                .withdrawalDate(jpa.getWithdrawalDate())
                .withdrawalReason(jpa.getWithdrawalReason())
                .updatedAt(jpa.getUpdatedAt())
                .build();
    }

    /**
     * Converts Domain → JPA Entity (for new entities)
     * Requires fetched entity references
     */
    public EnrollmentJpaEntity toJpaEntity(
            EnrollmentDomain domain,
            User student,
            SubjectGroupJpaEntity subjectGroup
    ) {
        if (domain == null) {
            return null;
        }

        return EnrollmentJpaEntity.builder()
                .id(domain.getId())
                .student(student)
                .subjectGroup(subjectGroup)
                .status(domain.getStatus())
                .attendanceMode(domain.getAttendanceMode())
                .enrollmentDate(domain.getEnrollmentDate())
                .withdrawalDate(domain.getWithdrawalDate())
                .withdrawalReason(domain.getWithdrawalReason())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Updates existing JPA Entity from Domain
     * Used for updates - preserves immutable fields (student, subjectGroup, enrollmentDate)
     */
    public void updateJpaEntity(EnrollmentJpaEntity jpa, EnrollmentDomain domain) {
        if (jpa == null || domain == null) {
            return;
        }

        // Update mutable fields only
        jpa.setStatus(domain.getStatus());
        jpa.setAttendanceMode(domain.getAttendanceMode());
        jpa.setWithdrawalDate(domain.getWithdrawalDate());
        jpa.setWithdrawalReason(domain.getWithdrawalReason());
        jpa.setUpdatedAt(domain.getUpdatedAt());

        // Do NOT update: id, student, subjectGroup, enrollmentDate
    }
}
