package com.acainfo.shared.e2e;

import com.acainfo.group.application.dto.CreateGroupCommand;
import com.acainfo.group.application.port.in.CreateGroupUseCase;
import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.schedule.application.dto.CreateScheduleCommand;
import com.acainfo.schedule.application.port.in.CreateScheduleUseCase;
import com.acainfo.schedule.domain.model.Classroom;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.subject.application.dto.CreateSubjectCommand;
import com.acainfo.subject.application.port.in.CreateSubjectUseCase;
import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.domain.model.User;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Helper class for creating test data in E2E tests.
 * Provides methods to create domain entities for testing scenarios.
 */
@Component
public class TestDataHelper {

    private final CreateSubjectUseCase createSubjectUseCase;
    private final CreateGroupUseCase createGroupUseCase;
    private final CreateScheduleUseCase createScheduleUseCase;
    private final TestAuthHelper authHelper;

    // Counters for unique codes/names
    private int subjectCounter = 0;

    public TestDataHelper(
            CreateSubjectUseCase createSubjectUseCase,
            CreateGroupUseCase createGroupUseCase,
            CreateScheduleUseCase createScheduleUseCase,
            TestAuthHelper authHelper) {
        this.createSubjectUseCase = createSubjectUseCase;
        this.createGroupUseCase = createGroupUseCase;
        this.createScheduleUseCase = createScheduleUseCase;
        this.authHelper = authHelper;
    }

    // ===========================================
    // Subject Creation Methods
    // ===========================================

    /**
     * Create a subject with specific code, name and degree.
     */
    public Subject createSubject(String code, String name, Degree degree) {
        CreateSubjectCommand command = new CreateSubjectCommand(code, name, degree);
        return createSubjectUseCase.create(command);
    }

    /**
     * Create a subject with auto-generated unique code.
     */
    public Subject createSubject(String name, Degree degree) {
        String code = generateSubjectCode();
        return createSubject(code, name, degree);
    }

    /**
     * Create a default subject (Programación I, Ingeniería Informática).
     */
    public Subject createDefaultSubject() {
        String code = generateSubjectCode();
        return createSubject(code, "Programación " + subjectCounter, Degree.INGENIERIA_INFORMATICA);
    }

    /**
     * Create a subject for Ingeniería Informática.
     */
    public Subject createInformaticaSubject(String name) {
        String code = generateSubjectCode();
        return createSubject(code, name, Degree.INGENIERIA_INFORMATICA);
    }

    /**
     * Create a subject for Ingeniería Industrial.
     */
    public Subject createIndustrialSubject(String name) {
        String code = generateSubjectCode();
        return createSubject(code, name, Degree.INGENIERIA_INDUSTRIAL);
    }

    // ===========================================
    // Group Creation Methods
    // ===========================================

    /**
     * Create a group with specific parameters.
     */
    public SubjectGroup createGroup(Long subjectId, Long teacherId, GroupType type, Integer capacity) {
        CreateGroupCommand command = new CreateGroupCommand(subjectId, teacherId, type, capacity);
        return createGroupUseCase.create(command);
    }

    /**
     * Create a group with default capacity.
     */
    public SubjectGroup createGroup(Long subjectId, Long teacherId, GroupType type) {
        return createGroup(subjectId, teacherId, type, null);
    }

    /**
     * Create a regular Q1 group.
     */
    public SubjectGroup createRegularQ1Group(Long subjectId, Long teacherId) {
        return createGroup(subjectId, teacherId, GroupType.REGULAR_Q1);
    }

    /**
     * Create an intensive Q1 group.
     */
    public SubjectGroup createIntensiveQ1Group(Long subjectId, Long teacherId) {
        return createGroup(subjectId, teacherId, GroupType.INTENSIVE_Q1);
    }

    /**
     * Create a regular Q2 group.
     */
    public SubjectGroup createRegularQ2Group(Long subjectId, Long teacherId) {
        return createGroup(subjectId, teacherId, GroupType.REGULAR_Q2);
    }

    /**
     * Create an intensive Q2 group.
     */
    public SubjectGroup createIntensiveQ2Group(Long subjectId, Long teacherId) {
        return createGroup(subjectId, teacherId, GroupType.INTENSIVE_Q2);
    }

    // ===========================================
    // Schedule Creation Methods
    // ===========================================

    /**
     * Create a schedule with specific parameters.
     */
    public Schedule createSchedule(Long groupId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, Classroom classroom) {
        CreateScheduleCommand command = new CreateScheduleCommand(groupId, dayOfWeek, startTime, endTime, classroom);
        return createScheduleUseCase.create(command);
    }

    /**
     * Create a morning schedule (9:00 - 11:00) in Aula Portal 1.
     */
    public Schedule createMorningSchedule(Long groupId, DayOfWeek dayOfWeek) {
        return createSchedule(groupId, dayOfWeek, LocalTime.of(9, 0), LocalTime.of(11, 0), Classroom.AULA_PORTAL1);
    }

    /**
     * Create an afternoon schedule (16:00 - 18:00) in Aula Portal 1.
     */
    public Schedule createAfternoonSchedule(Long groupId, DayOfWeek dayOfWeek) {
        return createSchedule(groupId, dayOfWeek, LocalTime.of(16, 0), LocalTime.of(18, 0), Classroom.AULA_PORTAL1);
    }

    /**
     * Create an evening schedule (18:00 - 20:00) in Aula Portal 2.
     */
    public Schedule createEveningSchedule(Long groupId, DayOfWeek dayOfWeek) {
        return createSchedule(groupId, dayOfWeek, LocalTime.of(18, 0), LocalTime.of(20, 0), Classroom.AULA_PORTAL2);
    }

    /**
     * Create a virtual schedule.
     */
    public Schedule createVirtualSchedule(Long groupId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return createSchedule(groupId, dayOfWeek, startTime, endTime, Classroom.AULA_VIRTUAL);
    }

    /**
     * Create a Monday morning schedule.
     */
    public Schedule createMondayMorningSchedule(Long groupId) {
        return createMorningSchedule(groupId, DayOfWeek.MONDAY);
    }

    /**
     * Create a Tuesday afternoon schedule.
     */
    public Schedule createTuesdayAfternoonSchedule(Long groupId) {
        return createAfternoonSchedule(groupId, DayOfWeek.TUESDAY);
    }

    // ===========================================
    // Composite Creation Methods
    // ===========================================

    /**
     * Create a complete test scenario: Subject + Teacher + Group.
     * Returns the created group.
     */
    public SubjectGroup createSubjectWithGroup(String adminToken) {
        // Create subject
        Subject subject = createDefaultSubject();

        // Create teacher
        User teacher = authHelper.createTeacher(adminToken, authHelper.uniqueTeacherEmail(), "Test", "Teacher");

        // Create group
        return createRegularQ1Group(subject.getId(), teacher.getId());
    }

    /**
     * Create a complete test scenario: Subject + Teacher + Group + Schedule.
     * Returns the created schedule.
     */
    public Schedule createFullScheduleScenario(String adminToken) {
        SubjectGroup group = createSubjectWithGroup(adminToken);
        return createMondayMorningSchedule(group.getId());
    }

    // ===========================================
    // Utility Methods
    // ===========================================

    /**
     * Generate unique subject code (e.g., TST001, TST002).
     */
    public String generateSubjectCode() {
        subjectCounter++;
        return String.format("TST%03d", subjectCounter);
    }

    /**
     * Reset counters (useful for test isolation).
     */
    public void resetCounters() {
        subjectCounter = 0;
    }
}
