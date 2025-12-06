package com.acainfo.shared.factory;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.schedule.infrastructure.adapter.out.persistence.entity.ScheduleJpaEntity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Factory for creating Schedule test data.
 * Provides fluent API for building Schedule domain and JPA entities with sensible defaults.
 */
public class ScheduleFactory {

    private Long id;
    private Long groupId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Classroom classroom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ScheduleFactory() {
        // Default values
        this.id = 1L;
        this.groupId = 1L;
        this.dayOfWeek = DayOfWeek.MONDAY;
        this.startTime = LocalTime.of(9, 0);
        this.endTime = LocalTime.of(11, 0);
        this.classroom = Classroom.AULA_PORTAL1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static ScheduleFactory builder() {
        return new ScheduleFactory();
    }

    public ScheduleFactory id(Long id) {
        this.id = id;
        return this;
    }

    public ScheduleFactory groupId(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    public ScheduleFactory dayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }

    public ScheduleFactory startTime(LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public ScheduleFactory endTime(LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public ScheduleFactory classroom(Classroom classroom) {
        this.classroom = classroom;
        return this;
    }

    public ScheduleFactory createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ScheduleFactory updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    // ========== Convenience Methods ==========

    public ScheduleFactory onMonday() {
        this.dayOfWeek = DayOfWeek.MONDAY;
        return this;
    }

    public ScheduleFactory onTuesday() {
        this.dayOfWeek = DayOfWeek.TUESDAY;
        return this;
    }

    public ScheduleFactory onWednesday() {
        this.dayOfWeek = DayOfWeek.WEDNESDAY;
        return this;
    }

    public ScheduleFactory onThursday() {
        this.dayOfWeek = DayOfWeek.THURSDAY;
        return this;
    }

    public ScheduleFactory onFriday() {
        this.dayOfWeek = DayOfWeek.FRIDAY;
        return this;
    }

    public ScheduleFactory inAulaPortal1() {
        this.classroom = Classroom.AULA_PORTAL1;
        return this;
    }

    public ScheduleFactory inAulaPortal2() {
        this.classroom = Classroom.AULA_PORTAL2;
        return this;
    }

    public ScheduleFactory inAulaVirtual() {
        this.classroom = Classroom.AULA_VIRTUAL;
        return this;
    }

    public ScheduleFactory morningSlot() {
        this.startTime = LocalTime.of(9, 0);
        this.endTime = LocalTime.of(11, 0);
        return this;
    }

    public ScheduleFactory afternoonSlot() {
        this.startTime = LocalTime.of(16, 0);
        this.endTime = LocalTime.of(18, 0);
        return this;
    }

    public ScheduleFactory eveningSlot() {
        this.startTime = LocalTime.of(18, 0);
        this.endTime = LocalTime.of(20, 0);
        return this;
    }

    public ScheduleFactory withTimeRange(int startHour, int startMinute, int endHour, int endMinute) {
        this.startTime = LocalTime.of(startHour, startMinute);
        this.endTime = LocalTime.of(endHour, endMinute);
        return this;
    }

    // ========== Build Methods ==========

    /**
     * Build domain Schedule entity.
     */
    public Schedule buildDomain() {
        return Schedule.builder()
                .id(id)
                .groupId(groupId)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .classroom(classroom)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Build JPA Schedule entity.
     */
    public ScheduleJpaEntity buildJpaEntity() {
        return ScheduleJpaEntity.builder()
                .id(id)
                .groupId(groupId)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .classroom(classroom)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // ========== Static Shortcuts ==========

    /**
     * Create a default schedule (Monday 9-11, Aula Portal 1).
     */
    public static Schedule defaultSchedule() {
        return builder().buildDomain();
    }

    /**
     * Create a default JPA schedule.
     */
    public static ScheduleJpaEntity defaultScheduleJpa() {
        return builder().buildJpaEntity();
    }

    /**
     * Create a schedule in Aula Portal 1.
     */
    public static Schedule inPortal1() {
        return builder().inAulaPortal1().buildDomain();
    }

    /**
     * Create a schedule in Aula Portal 2.
     */
    public static Schedule inPortal2() {
        return builder().inAulaPortal2().buildDomain();
    }

    /**
     * Create a virtual schedule.
     */
    public static Schedule virtual() {
        return builder().inAulaVirtual().buildDomain();
    }

    /**
     * Create a morning schedule (9-11).
     */
    public static Schedule morningSchedule() {
        return builder().morningSlot().buildDomain();
    }

    /**
     * Create an afternoon schedule (16-18).
     */
    public static Schedule afternoonSchedule() {
        return builder().afternoonSlot().buildDomain();
    }

    /**
     * Create an evening schedule (18-20).
     */
    public static Schedule eveningSchedule() {
        return builder().eveningSlot().buildDomain();
    }

    /**
     * Create a schedule for a specific group.
     */
    public static Schedule forGroup(Long groupId) {
        return builder().groupId(groupId).buildDomain();
    }

    /**
     * Create a schedule on a specific day.
     */
    public static Schedule onDay(DayOfWeek day) {
        return builder().dayOfWeek(day).buildDomain();
    }

    /**
     * Create a schedule with specific time range.
     */
    public static Schedule withTime(LocalTime start, LocalTime end) {
        return builder().startTime(start).endTime(end).buildDomain();
    }

    /**
     * Create a Monday morning schedule in Portal 1.
     */
    public static Schedule mondayMorningPortal1() {
        return builder()
                .onMonday()
                .morningSlot()
                .inAulaPortal1()
                .buildDomain();
    }

    /**
     * Create a Tuesday afternoon schedule in Portal 2.
     */
    public static Schedule tuesdayAfternoonPortal2() {
        return builder()
                .onTuesday()
                .afternoonSlot()
                .inAulaPortal2()
                .buildDomain();
    }

    /**
     * Create a conflicting schedule (same day, time, classroom as default).
     */
    public static Schedule conflictingWithDefault() {
        return builder()
                .id(2L)
                .groupId(2L)
                .onMonday()
                .morningSlot()
                .inAulaPortal1()
                .buildDomain();
    }
}
