package acainfo.back.enrollment.infrastructure.adapters.out.persistence.entities;

import acainfo.back.enrollment.domain.model.GroupRequestStatus;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA Entity for group requests persistence.
 * Contains only persistence-related annotations and fields.
 * Business logic resides in GroupRequestDomain.
 */
@Entity
@Table(
    name = "group_requests",
    indexes = {
        @Index(name = "idx_group_request_subject", columnList = "subject_id"),
        @Index(name = "idx_group_request_requester", columnList = "requested_by"),
        @Index(name = "idx_group_request_status", columnList = "status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The subject for which a new group is requested
     */
    @NotNull(message = "Subject is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private SubjectJpaEntity subject;

    /**
     * The student who created the request
     */
    @NotNull(message = "Requester is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private UserJpaEntity requestedBy;

    /**
     * Students who support this request (including the requester)
     * Many-to-many relationship to track all supporters
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_request_supporters",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @Builder.Default
    private Set<UserJpaEntity> supporters = new HashSet<>();

    /**
     * Current status of the request
     */
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GroupRequestStatus status = GroupRequestStatus.PENDIENTE;

    /**
     * When the request was created
     */
    @CreatedDate
    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    /**
     * When the request was resolved (approved or rejected)
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Reason for rejection (if rejected)
     */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    /**
     * Additional comments or notes about the request
     */
    @Column(length = 1000)
    private String comments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupRequestJpaEntity)) return false;
        GroupRequestJpaEntity that = (GroupRequestJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "GroupRequestJpaEntity{" +
                "id=" + id +
                ", subject=" + (subject != null ? subject.getCode() : "null") +
                ", requestedBy=" + (requestedBy != null ? requestedBy.getEmail() : "null") +
                ", supporters=" + (supporters != null ? supporters.size() : 0) +
                ", status=" + status +
                ", requestedAt=" + requestedAt +
                '}';
    }
}
