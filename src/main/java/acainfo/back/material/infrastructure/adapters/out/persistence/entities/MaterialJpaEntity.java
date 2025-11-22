package acainfo.back.material.infrastructure.adapters.out.persistence.entities;

import acainfo.back.material.domain.model.MaterialType;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Entity for Material persistence
 * Infrastructure layer - handles database mapping only
 */
@Entity(name = "Material")
@Table(
    name = "materials",
    indexes = {
        @Index(name = "idx_material_group", columnList = "subject_group_id"),
        @Index(name = "idx_material_type", columnList = "type"),
        @Index(name = "idx_material_active", columnList = "is_active"),
        @Index(name = "idx_material_topic", columnList = "topic")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_group_id", nullable = false)
    private SubjectGroupJpaEntity subjectGroup;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType type;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    private String topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserJpaEntity uploadedBy;

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "requires_payment", nullable = false)
    private Boolean requiresPayment;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialJpaEntity)) return false;
        MaterialJpaEntity that = (MaterialJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
