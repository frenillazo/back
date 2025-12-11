package com.acainfo.shared.factory;

import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.session.domain.model.Session;
import com.acainfo.session.domain.model.SessionMode;
import com.acainfo.session.domain.model.SessionStatus;
import com.acainfo.session.domain.model.SessionType;
import com.acainfo.session.infrastructure.adapter.out.persistence.entity.SessionJpaEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Factory for creating Session test data.
 * Provides fluent API for building Session domain and JPA entities with sensible defaults.
 */
public class SessionFactory {

    private Long id;
    private Long subjectId;
    private Long groupId;
    private Long scheduleId;
    private Classroom classroom;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;
    private SessionType type;
    private SessionMode mode;
    private LocalDate postponedToDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private SessionFactory() {
        // Default values
        this.id = 1L;
        this.subjectId = 1L;
        this.groupId = 1L;
        this.scheduleId = 1L;
        this.classroom = Classroom.AULA_PORTAL1;
        this.date = LocalDate.now().plusDays(1);
        this.startTime = LocalTime.of(9, 0);
        this.endTime = LocalTime.of(11, 0);
        this.status = SessionStatus.SCHEDULED;
        this.type = SessionType.REGULAR;
        this.mode = SessionMode.IN_PERSON;
        this.postponedToDate = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static SessionFactory builder() {
        return new SessionFactory();
    }

    public SessionFactory id(Long id) {
        this.id = id;
        return this;
    }

    public SessionFactory subjectId(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public SessionFactory groupId(Long groupId) {
        this.groupId = groupId;
        return this;
    }

    public SessionFactory scheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    public SessionFactory classroom(Classroom classroom) {
        this.classroom = classroom;
        return this;
    }

    public SessionFactory date(LocalDate date) {
        this.date = date;
        return this;
    }

    public SessionFactory startTime(LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public SessionFactory endTime(LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public SessionFactory status(SessionStatus status) {
        this.status = status;
        return this;
    }

    public SessionFactory type(SessionType type) {
        this.type = type;
        return this;
    }

    public SessionFactory mode(SessionMode mode) {
        this.mode = mode;
        return this;
    }

    public SessionFactory postponedToDate(LocalDate postponedToDate) {
        this.postponedToDate = postponedToDate;
        return this;
    }

    public SessionFactory createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public SessionFactory updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    // ========== Convenience Methods: Status ==========

    public SessionFactory scheduled() {
        this.status = SessionStatus.SCHEDULED;
        return this;
    }

    public SessionFactory inProgress() {
        this.status = SessionStatus.IN_PROGRESS;
        return this;
    }

    public SessionFactory completed() {
        this.status = SessionStatus.COMPLETED;
        return this;
    }

    public SessionFactory cancelled() {
        this.status = SessionStatus.CANCELLED;
        return this;
    }

    public SessionFactory postponed() {
        this.status = SessionStatus.POSTPONED;
        this.postponedToDate = this.date.plusDays(7);
        return this;
    }

    // ========== Convenience Methods: Type ==========

    public SessionFactory asRegular() {
        this.type = SessionType.REGULAR;
        return this;
    }

    public SessionFactory asExtra() {
        this.type = SessionType.EXTRA;
        this.scheduleId = null;
        return this;
    }

    public SessionFactory asScheduling() {
        this.type = SessionType.SCHEDULING;
        this.groupId = null;
        this.scheduleId = null;
        return this;
    }

    // ========== Convenience Methods: Mode ==========

    public SessionFactory inPerson() {
        this.mode = SessionMode.IN_PERSON;
        return this;
    }

    public SessionFactory online() {
        this.mode = SessionMode.ONLINE;
        return this;
    }

    public SessionFactory dual() {
        this.mode = SessionMode.DUAL;
        return this;
    }

    // ========== Convenience Methods: Classroom ==========

    public SessionFactory inAulaPortal1() {
        this.classroom = Classroom.AULA_PORTAL1;
        return this;
    }

    public SessionFactory inAulaPortal2() {
        this.classroom = Classroom.AULA_PORTAL2;
        return this;
    }

    public SessionFactory inAulaVirtual() {
        this.classroom = Classroom.AULA_VIRTUAL;
        return this;
    }

    // ========== Convenience Methods: Time ==========

    public SessionFactory morningSlot() {
        this.startTime = LocalTime.of(9, 0);
        this.endTime = LocalTime.of(11, 0);
        return this;
    }

    public SessionFactory afternoonSlot() {
        this.startTime = LocalTime.of(16, 0);
        this.endTime = LocalTime.of(18, 0);
        return this;
    }

    public SessionFactory eveningSlot() {
        this.startTime = LocalTime.of(18, 0);
        this.endTime = LocalTime.of(20, 0);
        return this;
    }

    public SessionFactory withTimeRange(int startHour, int startMinute, int endHour, int endMinute) {
        this.startTime = LocalTime.of(startHour, startMinute);
        this.endTime = LocalTime.of(endHour, endMinute);
        return this;
    }

    // ========== Convenience Methods: Date ==========

    public SessionFactory today() {
        this.date = LocalDate.now();
        return this;
    }

    public SessionFactory tomorrow() {
        this.date = LocalDate.now().plusDays(1);
        return this;
    }

    public SessionFactory nextWeek() {
        this.date = LocalDate.now().plusWeeks(1);
        return this;
    }

    public SessionFactory onDate(LocalDate date) {
        this.date = date;
        return this;
    }

    // ========== Build Methods ==========

    /**
     * Build domain Session entity.
     */
    public Session buildDomain() {
        return Session.builder()
                .id(id)
                .subjectId(subjectId)
                .groupId(groupId)
                .scheduleId(scheduleId)
                .classroom(classroom)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .type(type)
                .mode(mode)
                .postponedToDate(postponedToDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    /**
     * Build JPA Session entity.
     */
    public SessionJpaEntity buildJpaEntity() {
        return SessionJpaEntity.builder()
                .id(id)
                .subjectId(subjectId)
                .groupId(groupId)
                .scheduleId(scheduleId)
                .classroom(classroom)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .type(type)
                .mode(mode)
                .postponedToDate(postponedToDate)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // ========== Static Shortcuts ==========

    /**
     * Create a default scheduled regular session.
     */
    public static Session defaultSession() {
        return builder().buildDomain();
    }

    /**
     * Create a default JPA session.
     */
    public static SessionJpaEntity defaultSessionJpa() {
        return builder().buildJpaEntity();
    }

    /**
     * Create a scheduled regular session.
     */
    public static Session scheduledRegular() {
        return builder().asRegular().scheduled().buildDomain();
    }

    /**
     * Create an in-progress session.
     */
    public static Session inProgressSession() {
        return builder().inProgress().buildDomain();
    }

    /**
     * Create a completed session.
     */
    public static Session completedSession() {
        return builder().completed().buildDomain();
    }

    /**
     * Create a cancelled session.
     */
    public static Session cancelledSession() {
        return builder().cancelled().buildDomain();
    }

    /**
     * Create a postponed session.
     */
    public static Session postponedSession() {
        return builder().postponed().buildDomain();
    }

    /**
     * Create an extra session (no schedule).
     */
    public static Session extraSession() {
        return builder().asExtra().buildDomain();
    }

    /**
     * Create a scheduling session (no group, no schedule).
     */
    public static Session schedulingSession() {
        return builder().asScheduling().buildDomain();
    }

    /**
     * Create an online session.
     */
    public static Session onlineSession() {
        return builder().online().inAulaVirtual().buildDomain();
    }

    /**
     * Create a dual mode session.
     */
    public static Session dualSession() {
        return builder().dual().buildDomain();
    }

    /**
     * Create a session for a specific group.
     */
    public static Session forGroup(Long groupId) {
        return builder().groupId(groupId).buildDomain();
    }

    /**
     * Create a session for a specific subject.
     */
    public static Session forSubject(Long subjectId) {
        return builder().subjectId(subjectId).buildDomain();
    }

    /**
     * Create a session for a specific schedule.
     */
    public static Session forSchedule(Long scheduleId) {
        return builder().scheduleId(scheduleId).buildDomain();
    }

    /**
     * Create a session on a specific date.
     */
    public static Session onDate(LocalDate date) {
        return builder().date(date).buildDomain();
    }

    /**
     * Create a morning session tomorrow.
     */
    public static Session morningSessionTomorrow() {
        return builder()
                .tomorrow()
                .morningSlot()
                .inAulaPortal1()
                .buildDomain();
    }

    /**
     * Create an afternoon session tomorrow.
     */
    public static Session afternoonSessionTomorrow() {
        return builder()
                .tomorrow()
                .afternoonSlot()
                .inAulaPortal2()
                .buildDomain();
    }

    /**
     * Create an online scheduling session.
     */
    public static Session onlineSchedulingSession() {
        return builder()
                .asScheduling()
                .online()
                .inAulaVirtual()
                .buildDomain();
    }
}
