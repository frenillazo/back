package com.acainfo.shared.factory;

import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.domain.model.SubjectStatus;
import com.acainfo.subject.infrastructure.adapter.out.persistence.entity.SubjectJpaEntity;

import java.time.LocalDateTime;

/**
 * Factory for creating Subject test data.
 * Provides fluent API for building Subject domain and JPA entities with sensible defaults.
 */
public class SubjectFactory {

    private Long id;
    private String code;
    private String name;
    private Degree degree;
    private SubjectStatus status;
    private Integer currentGroupCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private SubjectFactory() {
        // Default values
        this.id = 1L;
        this.code = "ING101";
        this.name = "Programación I";
        this.degree = Degree.INGENIERIA_INFORMATICA;
        this.status = SubjectStatus.ACTIVE;
        this.currentGroupCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static SubjectFactory builder() {
        return new SubjectFactory();
    }

    public SubjectFactory id(Long id) {
        this.id = id;
        return this;
    }

    public SubjectFactory code(String code) {
        this.code = code;
        return this;
    }

    public SubjectFactory name(String name) {
        this.name = name;
        return this;
    }

    public SubjectFactory degree(Degree degree) {
        this.degree = degree;
        return this;
    }

    public SubjectFactory status(SubjectStatus status) {
        this.status = status;
        return this;
    }

    public SubjectFactory currentGroupCount(Integer currentGroupCount) {
        this.currentGroupCount = currentGroupCount;
        return this;
    }

    public SubjectFactory createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SubjectFactory updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    // ========== Convenience Methods ==========

    public SubjectFactory active() {
        this.status = SubjectStatus.ACTIVE;
        return this;
    }

    public SubjectFactory inactive() {
        this.status = SubjectStatus.INACTIVE;
        return this;
    }

    public SubjectFactory archived() {
        this.status = SubjectStatus.ARCHIVED;
        return this;
    }

    public SubjectFactory withMaxGroups() {
        this.currentGroupCount = 3; // MAX_GROUPS_PER_SUBJECT
        return this;
    }

    public SubjectFactory withNoGroups() {
        this.currentGroupCount = 0;
        return this;
    }

    public SubjectFactory asIngenieriaInformatica() {
        this.degree = Degree.INGENIERIA_INFORMATICA;
        return this;
    }

    public SubjectFactory asIngenieriaSoftware() {
        this.degree = Degree.INGENIERIA_SOFTWARE;
        return this;
    }

    public SubjectFactory asCienciasComputacion() {
        this.degree = Degree.CIENCIAS_COMPUTACION;
        return this;
    }

    // ========== Build Methods ==========

    /**
     * Build domain Subject entity.
     */
    public Subject buildDomain() {
        return Subject.builder()
                .id(id)
                .code(code)
                .name(name)
                .degree(degree)
                .status(status)
                .currentGroupCount(currentGroupCount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Build JPA Subject entity.
     */
    public SubjectJpaEntity buildJpaEntity() {
        return SubjectJpaEntity.builder()
                .id(id)
                .code(code)
                .name(name)
                .degree(degree)
                .status(status)
                .currentGroupCount(currentGroupCount)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // ========== Static Shortcuts ==========

    /**
     * Create a default active subject (Programación I).
     */
    public static Subject defaultSubject() {
        return builder().buildDomain();
    }

    /**
     * Create a default JPA subject.
     */
    public static SubjectJpaEntity defaultSubjectJpa() {
        return builder().buildJpaEntity();
    }

    /**
     * Create a subject with specific code.
     */
    public static Subject withCode(String code) {
        return builder().code(code).buildDomain();
    }

    /**
     * Create an inactive subject.
     */
    public static Subject inactiveSubject() {
        return builder().inactive().buildDomain();
    }

    /**
     * Create an archived subject.
     */
    public static Subject archivedSubject() {
        return builder().archived().buildDomain();
    }

    /**
     * Create a subject with maximum groups (cannot create more).
     */
    public static Subject subjectWithMaxGroups() {
        return builder().withMaxGroups().buildDomain();
    }

    /**
     * Create a subject with no groups (can create groups).
     */
    public static Subject subjectWithNoGroups() {
        return builder().withNoGroups().buildDomain();
    }

    /**
     * Create a subject for a specific degree.
     */
    public static Subject forDegree(Degree degree) {
        return builder().degree(degree).buildDomain();
    }
}
