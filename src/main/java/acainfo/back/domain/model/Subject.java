package acainfo.back.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a subject/course in the training center.
 * Subjects are offered for different engineering degrees and years.
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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique code for the subject (e.g., "ING-101", "INF-201")
     */
    @NotBlank(message = "Subject code is required")
    @Size(min = 3, max = 20, message = "Subject code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z]{3}-\\d{3}$", message = "Subject code must follow pattern: XXX-999 (e.g., ING-101)")
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    /**
     * Name of the subject
     */
    @NotBlank(message = "Subject name is required")
    @Size(min = 3, max = 200, message = "Subject name must be between 3 and 200 characters")
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Academic year (1-4)
     */
    @NotNull(message = "Year is required")
    @Min(value = 1, message = "Year must be between 1 and 4")
    @Max(value = 4, message = "Year must be between 1 and 4")
    @Column(nullable = false)
    private Integer year;

    /**
     * Engineering degree this subject belongs to
     */
    @NotNull(message = "Degree is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Degree degree;

    /**
     * Semester (1 or 2)
     */
    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be 1 or 2")
    @Max(value = 2, message = "Semester must be 1 or 2")
    @Column(nullable = false)
    private Integer semester;

    /**
     * Current status of the subject
     */
    @NotNull(message = "Subject status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubjectStatus status = SubjectStatus.ACTIVO;

    /**
     * Optional description of the subject
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods

    /**
     * Checks if the subject is active
     */
    public boolean isActive() {
        return status == SubjectStatus.ACTIVO;
    }

    /**
     * Checks if the subject is inactive
     */
    public boolean isInactive() {
        return status == SubjectStatus.INACTIVO;
    }

    /**
     * Checks if the subject is archived
     */
    public boolean isArchived() {
        return status == SubjectStatus.ARCHIVADO;
    }

    /**
     * Gets the full name with code
     */
    public String getFullName() {
        return code + " - " + name;
    }

    /**
     * Activates the subject
     */
    public void activate() {
        this.status = SubjectStatus.ACTIVO;
    }

    /**
     * Deactivates the subject
     */
    public void deactivate() {
        this.status = SubjectStatus.INACTIVO;
    }

    /**
     * Archives the subject
     */
    public void archive() {
        this.status = SubjectStatus.ARCHIVADO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject)) return false;
        Subject subject = (Subject) o;
        return id != null && id.equals(subject.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", year=" + year +
                ", degree=" + degree +
                ", semester=" + semester +
                ", status=" + status +
                '}';
    }
}
