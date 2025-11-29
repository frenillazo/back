package acainfo.back.enrollment.domain.model;

import acainfo.back.user.domain.model.User;
import acainfo.back.subject.domain.model.Subject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a request to create a new group for a subject.
 * Students can request new groups if they gather minimum supporters (default: 8 students).
 *
 * Business rules:
 * - A student creates a request for a specific subject
 * - Other students can support the request
 * - Minimum 8 supporters required for admin consideration
 * - Admin can approve or reject the request
 * - Once approved, a new group is created for that subject
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
public class GroupRequest {

    public static final int MINIMUM_SUPPORTERS = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The subject for which a new group is requested
     */
    @NotNull(message = "Subject is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * The student who created the request
     */
    @NotNull(message = "Requester is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

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
    private Set<User> supporters = new HashSet<>();

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

    // ==================== BUSINESS METHODS ====================

    /**
     * Add a student as a supporter
     */
    public void addSupporter(User student) {
        if (this.supporters == null) {
            this.supporters = new HashSet<>();
        }
        this.supporters.add(student);
    }

    /**
     * Remove a student from supporters
     */
    public void removeSupporter(User student) {
        if (this.supporters != null) {
            this.supporters.remove(student);
        }
    }

    /**
     * Check if has minimum supporters required
     */
    public boolean hasMinimumSupporters() {
        return getSupportersCount() >= MINIMUM_SUPPORTERS;
    }

    /**
     * Get the count of supporters
     */
    public int getSupportersCount() {
        return (supporters != null) ? supporters.size() : 0;
    }

    /**
     * Approve the request
     */
    public void approve() {
        if (this.status != GroupRequestStatus.PENDIENTE) {
            throw new IllegalStateException("Only pending requests can be approved");
        }
        this.status = GroupRequestStatus.APROBADA;
        this.resolvedAt = LocalDateTime.now();
        this.rejectionReason = null; // Clear rejection reason if any
    }

    /**
     * Reject the request with a reason
     */
    public void reject(String reason) {
        if (this.status != GroupRequestStatus.PENDIENTE) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        this.status = GroupRequestStatus.RECHAZADA;
        this.resolvedAt = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    /**
     * Check if the student is a supporter
     */
    public boolean isSupporter(User student) {
        return supporters != null && supporters.contains(student);
    }

    /**
     * Check if the student is the requester
     */
    public boolean isRequester(User student) {
        return requestedBy != null && requestedBy.equals(student);
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if request is pending
     */
    public boolean isPending() {
        return this.status == GroupRequestStatus.PENDIENTE;
    }

    /**
     * Check if request is approved
     */
    public boolean isApproved() {
        return this.status == GroupRequestStatus.APROBADA;
    }

    /**
     * Check if request is rejected
     */
    public boolean isRejected() {
        return this.status == GroupRequestStatus.RECHAZADA;
    }

    /**
     * Check if request is resolved (approved or rejected)
     */
    public boolean isResolved() {
        return this.status.isResolved();
    }

    /**
     * Get percentage of supporters progress (towards minimum)
     */
    public double getSupportersProgress() {
        return (getSupportersCount() * 100.0) / MINIMUM_SUPPORTERS;
    }

    /**
     * Get how many more supporters are needed
     */
    public int getSupportersNeeded() {
        int needed = MINIMUM_SUPPORTERS - getSupportersCount();
        return Math.max(0, needed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupRequest)) return false;
        GroupRequest that = (GroupRequest) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "GroupRequest{" +
                "id=" + id +
                ", subject=" + (subject != null ? subject.getCode() : "null") +
                ", requestedBy=" + (requestedBy != null ? requestedBy.getEmail() : "null") +
                ", supporters=" + getSupportersCount() +
                ", status=" + status +
                ", requestedAt=" + requestedAt +
                '}';
    }
}
