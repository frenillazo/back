package acainfo.back.enrollment.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.enrollment.domain.model.GroupRequestDomain;
import acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities.GroupRequestJpaEntity;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import acainfo.back.subject.infrastructure.adapters.out.persistence.repositories.SubjectJpaRepository;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper between GroupRequestDomain and GroupRequestJpaEntity.
 * Handles conversion of IDs to/from JPA entities.
 */
@Component
@RequiredArgsConstructor
public class GroupRequestJpaMapper {

    private final UserJpaRepository userRepository;
    private final SubjectJpaRepository subjectRepository;

    /**
     * Convert JPA entity to domain model.
     */
    public GroupRequestDomain toDomain(GroupRequestJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        // Convert supporters to IDs
        Set<Long> supporterIds = null;
        if (entity.getSupporters() != null) {
            supporterIds = entity.getSupporters().stream()
                    .map(UserJpaEntity::getId)
                    .collect(Collectors.toSet());
        }

        return GroupRequestDomain.builder()
                .id(entity.getId())
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .requestedById(entity.getRequestedBy() != null ? entity.getRequestedBy().getId() : null)
                .supporterIds(supporterIds)
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .resolvedAt(entity.getResolvedAt())
                .rejectionReason(entity.getRejectionReason())
                .comments(entity.getComments())
                .build();
    }

    /**
     * Convert domain model to JPA entity.
     * Loads related entities from repositories.
     */
    public GroupRequestJpaEntity toEntity(GroupRequestDomain domain) {
        if (domain == null) {
            return null;
        }

        // Load subject entity
        SubjectJpaEntity subject = domain.getSubjectId() != null
                ? subjectRepository.findById(domain.getSubjectId()).orElse(null)
                : null;

        // Load requester entity
        UserJpaEntity requestedBy = domain.getRequestedById() != null
                ? userRepository.findById(domain.getRequestedById()).orElse(null)
                : null;

        // Load supporter entities
        Set<UserJpaEntity> supporters = null;
        if (domain.getSupporterIds() != null && !domain.getSupporterIds().isEmpty()) {
            supporters = userRepository.findAllById(domain.getSupporterIds()).stream()
                    .collect(Collectors.toSet());
        }

        return GroupRequestJpaEntity.builder()
                .id(domain.getId())
                .subject(subject)
                .requestedBy(requestedBy)
                .supporters(supporters != null ? supporters : new java.util.HashSet<>())
                .status(domain.getStatus())
                .requestedAt(domain.getRequestedAt())
                .resolvedAt(domain.getResolvedAt())
                .rejectionReason(domain.getRejectionReason())
                .comments(domain.getComments())
                .build();
    }

    /**
     * Update an existing entity with data from domain model.
     * Preserves the entity's managed state.
     */
    public void updateEntity(GroupRequestJpaEntity entity, GroupRequestDomain domain) {
        if (entity == null || domain == null) {
            return;
        }

        // Update status
        entity.setStatus(domain.getStatus());
        entity.setResolvedAt(domain.getResolvedAt());
        entity.setRejectionReason(domain.getRejectionReason());
        entity.setComments(domain.getComments());

        // Update supporters if changed
        if (domain.getSupporterIds() != null) {
            Set<UserJpaEntity> supporters = userRepository.findAllById(domain.getSupporterIds())
                    .stream()
                    .collect(Collectors.toSet());
            entity.setSupporters(supporters);
        }

        // Note: subject and requestedBy are not updated as they shouldn't change
    }
}
