package acainfo.back.subject.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain Entity - Subject
 * Pure POJO without framework annotations
 * Contains ONLY business logic
 */
public class SubjectDomain {

    // ===== FIELDS =====

    private Long id;
    private String code;
    private String name;
    private Integer year; // 1-4
    private Degree degree;
    private Integer semester; // 1 or 2
    private SubjectStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== PRIVATE CONSTRUCTOR =====

    private SubjectDomain() {
        // Only accessible from Builder
    }

    // ===== BUILDER =====

    public static SubjectDomainBuilder builder() {
        return new SubjectDomainBuilder();
    }

    // ===== BUSINESS LOGIC =====

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
        if (isActive()) {
            throw new IllegalStateException("Subject is already active");
        }
        this.status = SubjectStatus.ACTIVO;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivates the subject
     */
    public void deactivate() {
        if (isInactive()) {
            throw new IllegalStateException("Subject is already inactive");
        }
        this.status = SubjectStatus.INACTIVO;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Archives the subject
     */
    public void archive() {
        if (isArchived()) {
            throw new IllegalStateException("Subject is already archived");
        }
        this.status = SubjectStatus.ARCHIVADO;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if subject belongs to a specific year
     */
    public boolean belongsToYear(Integer year) {
        return this.year.equals(year);
    }

    /**
     * Checks if subject belongs to a specific degree
     */
    public boolean belongsToDegree(Degree degree) {
        return this.degree == degree;
    }

    /**
     * Checks if subject belongs to a specific semester
     */
    public boolean belongsToSemester(Integer semester) {
        return this.semester.equals(semester);
    }

    // ===== GETTERS (NO SETTERS - Immutability) =====

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getYear() {
        return year;
    }

    public Degree getDegree() {
        return degree;
    }

    public Integer getSemester() {
        return semester;
    }

    public SubjectStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ===== EQUALS & HASHCODE =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectDomain that = (SubjectDomain) o;
        return Objects.equals(id, that.id) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
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

    // ===== BUILDER CLASS =====

    public static class SubjectDomainBuilder {
        private SubjectDomain subject = new SubjectDomain();

        public SubjectDomainBuilder id(Long id) {
            subject.id = id;
            return this;
        }

        public SubjectDomainBuilder code(String code) {
            subject.code = code;
            return this;
        }

        public SubjectDomainBuilder name(String name) {
            subject.name = name;
            return this;
        }

        public SubjectDomainBuilder year(Integer year) {
            subject.year = year;
            return this;
        }

        public SubjectDomainBuilder degree(Degree degree) {
            subject.degree = degree;
            return this;
        }

        public SubjectDomainBuilder semester(Integer semester) {
            subject.semester = semester;
            return this;
        }

        public SubjectDomainBuilder status(SubjectStatus status) {
            subject.status = status;
            return this;
        }

        public SubjectDomainBuilder description(String description) {
            subject.description = description;
            return this;
        }

        public SubjectDomainBuilder createdAt(LocalDateTime createdAt) {
            subject.createdAt = createdAt;
            return this;
        }

        public SubjectDomainBuilder updatedAt(LocalDateTime updatedAt) {
            subject.updatedAt = updatedAt;
            return this;
        }

        public SubjectDomain build() {
            validate();
            return subject;
        }

        private void validate() {
            if (subject.code == null || subject.code.isBlank()) {
                throw new IllegalArgumentException("Subject code is required");
            }
            if (subject.name == null || subject.name.isBlank()) {
                throw new IllegalArgumentException("Subject name is required");
            }
            if (subject.year == null || subject.year < 1 || subject.year > 4) {
                throw new IllegalArgumentException("Subject year must be between 1 and 4");
            }
            if (subject.degree == null) {
                throw new IllegalArgumentException("Subject degree is required");
            }
            if (subject.semester == null || (subject.semester != 1 && subject.semester != 2)) {
                throw new IllegalArgumentException("Subject semester must be 1 or 2");
            }
            if (subject.status == null) {
                subject.status = SubjectStatus.ACTIVO;
            }
            if (subject.createdAt == null) {
                subject.createdAt = LocalDateTime.now();
            }
        }
    }
}
