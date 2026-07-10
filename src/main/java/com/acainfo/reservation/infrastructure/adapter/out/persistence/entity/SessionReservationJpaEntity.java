package com.acainfo.reservation.infrastructure.adapter.out.persistence.entity;

import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for SessionReservation persistence.
 * Maps to 'session_reservations' table in database.
 */
@Entity
@Table(
    name = "session_reservations",
    indexes = {
        @Index(name = "idx_reservation_student_id", columnList = "student_id"),
        @Index(name = "idx_reservation_session_id", columnList = "session_id"),
        @Index(name = "idx_reservation_enrollment_id", columnList = "enrollment_id"),
        @Index(name = "idx_reservation_status", columnList = "status"),
        @Index(name = "idx_reservation_mode", columnList = "mode"),
        @Index(name = "idx_reservation_student_session", columnList = "student_id, session_id"),
        @Index(name = "idx_reservation_session_status_mode", columnList = "session_id, status, mode")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_reservation_student_session",
            columnNames = {"student_id", "session_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SessionReservationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private ReservationMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ==================== Audit Fields ====================

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
