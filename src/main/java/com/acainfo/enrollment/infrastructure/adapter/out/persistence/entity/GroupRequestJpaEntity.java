package com.acainfo.enrollment.infrastructure.adapter.out.persistence.entity;

import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.group.domain.model.GroupType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA Entity for GroupRequest persistence.
 * Maps to 'group_requests' table in database.
 */
@Entity
@Table(
    name = "group_requests",
    indexes = {
        @Index(name = "idx_group_request_subject_id", columnList = "subject_id"),
        @Index(name = "idx_group_request_requester_id", columnList = "requester_id"),
        @Index(name = "idx_group_request_status", columnList = "status"),
        @Index(name = "idx_group_request_type", columnList = "requested_group_type"),
        @Index(name = "idx_group_request_subject_status", columnList = "subject_id, status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GroupRequestJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_group_type", nullable = false, length = 20)
    private GroupType requestedGroupType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GroupRequestStatus status = GroupRequestStatus.PENDING;

    @ElementCollection
    @CollectionTable(
        name = "group_request_supporters",
        joinColumns = @JoinColumn(name = "group_request_id")
    )
    @Column(name = "supporter_id")
    @Builder.Default
    private Set<Long> supporterIds = new HashSet<>();

    @Column(name = "justification", length = 500)
    private String justification;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_group_id")
    private Long createdGroupId;

    @Column(name = "admin_response", length = 500)
    private String adminResponse;

    @Column(name = "processed_by_admin_id")
    private Long processedByAdminId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
