package com.acainfo.material.infrastructure.adapter.out.persistence.entity;

import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.domain.model.MaterialAiJobType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for MaterialAiJob persistence.
 * Maps to 'material_ai_jobs' table in database.
 */
@Entity
@Table(
        name = "material_ai_jobs",
        indexes = {
                @Index(name = "idx_material_ai_job_status", columnList = "status")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MaterialAiJobJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MaterialAiJobType type;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "source_material_id")
    private Long sourceMaterialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MaterialAiJobStatus status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "result_material_id")
    private Long resultMaterialId;

    @Column(name = "created_by_id", nullable = false)
    private Long createdById;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
