package com.acainfo.shared.factory;

import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.group.infrastructure.adapter.out.persistence.entity.SubjectGroupJpaEntity;

import java.time.LocalDateTime;

/**
 * Factory for creating SubjectGroup test data.
 * Provides fluent API for building SubjectGroup domain and JPA entities with sensible defaults.
 */
public class GroupFactory {

    private Long id;
    private Long subjectId;
    private Long teacherId;
    private GroupType type;
    private GroupStatus status;
    private Integer currentEnrollmentCount;
    private Integer capacity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private GroupFactory() {
        // Default values
        this.id = 1L;
        this.subjectId = 1L;
        this.teacherId = 2L;
        this.type = GroupType.REGULAR_Q1;
        this.status = GroupStatus.OPEN;
        this.currentEnrollmentCount = 0;
        this.capacity = null;  // Use default based on type
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static GroupFactory builder() {
        return new GroupFactory();
    }

    public GroupFactory id(Long id) {
        this.id = id;
        return this;
    }

    public GroupFactory subjectId(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public GroupFactory teacherId(Long teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public GroupFactory type(GroupType type) {
        this.type = type;
        return this;
    }

    public GroupFactory status(GroupStatus status) {
        this.status = status;
        return this;
    }

    public GroupFactory currentEnrollmentCount(Integer currentEnrollmentCount) {
        this.currentEnrollmentCount = currentEnrollmentCount;
        return this;
    }

    public GroupFactory capacity(Integer capacity) {
        this.capacity = capacity;
        return this;
    }

    public GroupFactory createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public GroupFactory updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    // ========== Convenience Methods ==========

    public GroupFactory open() {
        this.status = GroupStatus.OPEN;
        return this;
    }

    public GroupFactory closed() {
        this.status = GroupStatus.CLOSED;
        return this;
    }

    public GroupFactory cancelled() {
        this.status = GroupStatus.CANCELLED;
        return this;
    }

    public GroupFactory asRegularQ1() {
        this.type = GroupType.REGULAR_Q1;
        return this;
    }

    public GroupFactory asRegularQ2() {
        this.type = GroupType.REGULAR_Q2;
        return this;
    }

    public GroupFactory asIntensiveQ1() {
        this.type = GroupType.INTENSIVE_Q1;
        return this;
    }

    public GroupFactory asIntensiveQ2() {
        this.type = GroupType.INTENSIVE_Q2;
        return this;
    }

    public GroupFactory full() {
        // Set enrollment count to max capacity based on type
        this.currentEnrollmentCount = (this.type != null && this.type.isIntensive())
                ? SubjectGroup.INTENSIVE_MAX_CAPACITY
                : SubjectGroup.REGULAR_MAX_CAPACITY;
        return this;
    }

    public GroupFactory withEnrollments(int count) {
        this.currentEnrollmentCount = count;
        return this;
    }

    public GroupFactory withCustomCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    // ========== Build Methods ==========

    /**
     * Build domain SubjectGroup entity.
     */
    public SubjectGroup buildDomain() {
        return SubjectGroup.builder()
                .id(id)
                .subjectId(subjectId)
                .teacherId(teacherId)
                .type(type)
                .status(status)
                .currentEnrollmentCount(currentEnrollmentCount)
                .capacity(capacity)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Build JPA SubjectGroup entity.
     */
    public SubjectGroupJpaEntity buildJpaEntity() {
        return SubjectGroupJpaEntity.builder()
                .id(id)
                .subjectId(subjectId)
                .teacherId(teacherId)
                .type(type)
                .status(status)
                .currentEnrollmentCount(currentEnrollmentCount)
                .capacity(capacity)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // ========== Static Shortcuts ==========

    /**
     * Create a default open regular group.
     */
    public static SubjectGroup defaultGroup() {
        return builder().buildDomain();
    }

    /**
     * Create a default JPA group.
     */
    public static SubjectGroupJpaEntity defaultGroupJpa() {
        return builder().buildJpaEntity();
    }

    /**
     * Create an open regular Q1 group.
     */
    public static SubjectGroup openRegularQ1() {
        return builder().asRegularQ1().open().buildDomain();
    }

    /**
     * Create an open intensive Q1 group.
     */
    public static SubjectGroup openIntensiveQ1() {
        return builder().asIntensiveQ1().open().buildDomain();
    }

    /**
     * Create an open regular Q2 group.
     */
    public static SubjectGroup openRegularQ2() {
        return builder().asRegularQ2().open().buildDomain();
    }

    /**
     * Create an open intensive Q2 group.
     */
    public static SubjectGroup openIntensiveQ2() {
        return builder().asIntensiveQ2().open().buildDomain();
    }

    /**
     * Create a closed group.
     */
    public static SubjectGroup closedGroup() {
        return builder().closed().buildDomain();
    }

    /**
     * Create a cancelled group.
     */
    public static SubjectGroup cancelledGroup() {
        return builder().cancelled().buildDomain();
    }

    /**
     * Create a full regular group (24 students).
     */
    public static SubjectGroup fullRegularGroup() {
        return builder().asRegularQ1().full().buildDomain();
    }

    /**
     * Create a full intensive group (50 students).
     */
    public static SubjectGroup fullIntensiveGroup() {
        return builder().asIntensiveQ1().full().buildDomain();
    }

    /**
     * Create a group with specific enrollments.
     */
    public static SubjectGroup withEnrollments(int count) {
        return builder().withEnrollments(count).buildDomain();
    }

    /**
     * Create a group for a specific subject.
     */
    public static SubjectGroup forSubject(Long subjectId) {
        return builder().subjectId(subjectId).buildDomain();
    }

    /**
     * Create a group with a specific teacher.
     */
    public static SubjectGroup withTeacher(Long teacherId) {
        return builder().teacherId(teacherId).buildDomain();
    }
}
