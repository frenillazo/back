package com.acainfo.course.application.service;

import com.acainfo.course.application.dto.CreateCourseCommand;
import com.acainfo.course.application.dto.CourseFilters;
import com.acainfo.course.application.dto.UpdateCourseCommand;
import com.acainfo.course.application.port.in.CreateCourseUseCase;
import com.acainfo.course.application.port.in.DeleteCourseUseCase;
import com.acainfo.course.application.port.in.GetCourseUseCase;
import com.acainfo.course.application.port.in.UpdateCourseUseCase;
import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.course.domain.exception.CourseNotFoundException;
import com.acainfo.course.domain.exception.InvalidCourseDataException;
import com.acainfo.course.domain.model.CourseStatus;
import com.acainfo.course.domain.model.Course;
import com.acainfo.enrollment.application.port.in.CloseCourseEnrollmentsUseCase;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.schedule.application.port.out.ScheduleRepositoryPort;
import com.acainfo.schedule.domain.model.Schedule;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementing course use cases.
 * Contains business logic and validations for course operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService implements
        CreateCourseUseCase,
        UpdateCourseUseCase,
        GetCourseUseCase,
        DeleteCourseUseCase {

    private final CourseRepositoryPort courseRepositoryPort;
    private final SubjectRepositoryPort subjectRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ScheduleRepositoryPort scheduleRepositoryPort;
    private final SessionRepositoryPort sessionRepositoryPort;
    private final EnrollmentRepositoryPort enrollmentRepositoryPort;
    private final CloseCourseEnrollmentsUseCase closeCourseEnrollmentsUseCase;

    @Override
    @Transactional
    public Course create(CreateCourseCommand command) {
        log.info("Creating course for subject: {}, teacher: {}",
                command.subjectId(), command.teacherId());

        // Validate that subject exists
        Subject subject = subjectRepositoryPort.findById(command.subjectId())
                .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));

        // Teacher is optional; when provided it must exist and be a teacher/admin
        if (command.teacherId() != null) {
            User teacher = userRepositoryPort.findById(command.teacherId())
                    .orElseThrow(() -> new UserNotFoundException(command.teacherId()));

            if (!teacher.isTeacher() && !teacher.isAdmin()) {
                throw new InvalidCourseDataException(
                        "User " + command.teacherId() + " is not a teacher or admin"
                );
            }
        }

        // Validate dates
        if (command.startDate() == null || command.endDate() == null) {
            throw new InvalidCourseDataException("startDate and endDate are required");
        }
        if (command.endDate().isBefore(command.startDate())) {
            throw new InvalidCourseDataException("endDate must be on or after startDate");
        }

        // Validate capacity if provided (null = unlimited, virtual/dual course)
        if (command.capacity() != null && command.capacity() < 1) {
            throw new InvalidCourseDataException("Capacity must be at least 1");
        }

        // Generate course name automatically
        String courseName = generateCourseName(subject);

        Course course = Course.builder()
                .name(courseName)
                .subjectId(command.subjectId())
                .teacherId(command.teacherId())
                .status(CourseStatus.OPEN)
                .capacity(command.capacity())          // null = unlimited
                .pricePerMonth(command.pricePerMonth()) // informative only
                .startDate(command.startDate())
                .endDate(command.endDate())
                .build();

        Course savedCourse = courseRepositoryPort.save(course);

        // Increment subject's course count
        subject.setCurrentGroupCount(subject.getCurrentGroupCount() + 1);
        subjectRepositoryPort.save(subject);

        log.info("Course created successfully: ID {}, Subject: {}", savedCourse.getId(), command.subjectId());
        return savedCourse;
    }

    @Override
    @Transactional
    public Course update(Long id, UpdateCourseCommand command) {
        log.info("Updating course with ID: {}", id);

        Course course = getById(id);

        if (command.capacity() != null) {
            long activeEnrollments = enrollmentRepositoryPort.countActiveByCourseId(id);
            if (command.capacity() < activeEnrollments) {
                throw new InvalidCourseDataException(
                        String.format("Capacity cannot be less than current enrollments (%d)",
                                activeEnrollments)
                );
            }
            course.setCapacity(command.capacity());
        }

        if (command.status() != null) {
            boolean closingCourse = course.isOpen() && command.status() != CourseStatus.OPEN;
            course.setStatus(command.status());
            if (closingCourse) {
                closeCourseEnrollmentsUseCase.closeAllForCourse(id);
            }
        }

        if (command.pricePerMonth() != null) {
            course.setPricePerMonth(command.pricePerMonth());
        }

        if (command.teacherId() != null) {
            User teacher = userRepositoryPort.findById(command.teacherId())
                    .orElseThrow(() -> new UserNotFoundException(command.teacherId()));
            if (!teacher.isTeacher() && !teacher.isAdmin()) {
                throw new InvalidCourseDataException(
                        "User " + command.teacherId() + " is not a teacher or admin"
                );
            }
            course.setTeacherId(command.teacherId());
        }

        // Validate dates if updated
        LocalDate newStart = command.startDate() != null ? command.startDate() : course.getStartDate();
        LocalDate newEnd = command.endDate() != null ? command.endDate() : course.getEndDate();

        if (newEnd.isBefore(newStart)) {
            throw new InvalidCourseDataException("endDate must be on or after startDate");
        }
        course.setStartDate(newStart);
        course.setEndDate(newEnd);

        Course updatedCourse = courseRepositoryPort.save(course);
        log.info("Course updated successfully: ID {}", id);

        return updatedCourse;
    }

    @Override
    @Transactional(readOnly = true)
    public Course getById(Long id) {
        log.debug("Getting course by ID: {}", id);
        return courseRepositoryPort.findById(id)
                .orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> findWithFilters(CourseFilters filters) {
        log.debug("Finding courses with filters: subjectId={}, teacherId={}, status={}",
                filters.subjectId(), filters.teacherId(), filters.status());
        return courseRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting course with ID: {}", id);

        Course course = getById(id);

        long activeEnrollments = enrollmentRepositoryPort.countActiveByCourseId(id);
        if (activeEnrollments > 0) {
            throw new InvalidCourseDataException(
                    "Cannot delete course with existing enrollments. Cancel it instead."
            );
        }

        // Delete all sessions and schedules associated with this course
        List<Schedule> schedules = scheduleRepositoryPort.findByCourseId(id);
        for (Schedule schedule : schedules) {
            sessionRepositoryPort.deleteByScheduleId(schedule.getId());
            scheduleRepositoryPort.delete(schedule.getId());
        }
        log.info("Deleted {} schedules and their associated sessions for course ID: {}", schedules.size(), id);

        // Decrement subject's course count
        Subject subject = subjectRepositoryPort.findById(course.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException(course.getSubjectId()));

        subject.setCurrentGroupCount(Math.max(0, subject.getCurrentGroupCount() - 1));
        subjectRepositoryPort.save(subject);

        courseRepositoryPort.delete(id);
        log.info("Course deleted successfully: ID {}", id);
    }

    @Override
    @Transactional
    public Course cancel(Long id) {
        log.info("Cancelling course with ID: {}", id);

        Course course = getById(id);
        boolean wasOpen = course.isOpen();
        course.setStatus(CourseStatus.CANCELLED);

        Course cancelledCourse = courseRepositoryPort.save(course);
        if (wasOpen) {
            closeCourseEnrollmentsUseCase.closeAllForCourse(id);
        }
        log.info("Course cancelled successfully: ID {}", id);

        return cancelledCourse;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Generate course name automatically.
     * Format: "[subjectName] grupo N YY-YY"
     * Example: "Álgebra grupo 1 25-26"
     */
    private String generateCourseName(Subject subject) {
        long existingCount = courseRepositoryPort.countAllBySubjectId(subject.getId());
        String academicYear = calculateAcademicYear();
        return String.format("%s grupo %d %s",
                subject.getName(),
                existingCount + 1,
                academicYear
        );
    }

    /**
     * Calculate academic year in format "YY-YY". Sep-Dec → year starts; Jan-Aug → previous year started.
     */
    private String calculateAcademicYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int startYear = month >= 9 ? year : year - 1;
        int endYear = startYear + 1;
        return String.format("%02d-%02d", startYear % 100, endYear % 100);
    }
}
