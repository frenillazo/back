package acainfo.back.subject.infrastructure.adapters.out.persistence.entities;

import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for Subject persistence
 * Contains ONLY persistence-related annotations and mappings
 * NO business logic
 */
@Entity
@Table(
    name = "subjects",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_subject_code", columnNames = "code")
    },
    indexes = {
        @Index(name = "idx_subject_degree", columnList = "degree"),
        @Index(name = "idx_subject_year", columnList = "year"),
        @Index(name = "idx_subject_semester", columnList = "semester"),
        @Index(name = "idx_subject_status", columnList = "status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Integer year; // 1-4

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Degree degree;

    @Column(nullable = false)
    private Integer semester; // 1 or 2

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubjectStatus status = SubjectStatus.ACTIVO;

    @Column(length = 1000)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // NO business logic methods - only Lombok getters/setters
}
