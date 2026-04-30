package com.acainfo.group.application.service;

import com.acainfo.group.application.dto.CreateGroupCommand;
import com.acainfo.group.application.dto.GroupFilters;
import com.acainfo.group.application.dto.UpdateGroupCommand;
import com.acainfo.group.application.port.in.CreateGroupUseCase;
import com.acainfo.group.application.port.in.DeleteGroupUseCase;
import com.acainfo.group.application.port.in.GetGroupUseCase;
import com.acainfo.group.application.port.in.UpdateGroupUseCase;
import com.acainfo.group.application.port.out.GroupRepositoryPort;
import com.acainfo.group.domain.exception.GroupNotFoundException;
import com.acainfo.group.domain.exception.InvalidGroupDataException;
import com.acainfo.group.domain.model.GroupStatus;
import com.acainfo.group.domain.model.SubjectGroup;
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
 * Service implementing group use cases.
 * Contains business logic and validations for regular group operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService implements
        CreateGroupUseCase,
        UpdateGroupUseCase,
        GetGroupUseCase,
        DeleteGroupUseCase {

    private final GroupRepositoryPort groupRepositoryPort;
    private final SubjectRepositoryPort subjectRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ScheduleRepositoryPort scheduleRepositoryPort;
    private final SessionRepositoryPort sessionRepositoryPort;

    @Override
    @Transactional
    public SubjectGroup create(CreateGroupCommand command) {
        log.info("Creating group for subject: {}, teacher: {}",
                command.subjectId(), command.teacherId());

        // Validate that subject exists
        Subject subject = subjectRepositoryPort.findById(command.subjectId())
                .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));

        // Validate that teacher exists and is actually a teacher
        User teacher = userRepositoryPort.findById(command.teacherId())
                .orElseThrow(() -> new UserNotFoundException(command.teacherId()));

        if (!teacher.isTeacher() && !teacher.isAdmin()) {
            throw new InvalidGroupDataException(
                    "User " + command.teacherId() + " is not a teacher or admin"
            );
        }

        // Validate dates
        if (command.startDate() == null || command.endDate() == null) {
            throw new InvalidGroupDataException("startDate and endDate are required");
        }
        if (command.endDate().isBefore(command.startDate())) {
            throw new InvalidGroupDataException("endDate must be on or after startDate");
        }

        // Validate custom capacity if provided
        if (command.capacity() != null && command.capacity() < 1) {
            throw new InvalidGroupDataException("Capacity must be at least 1");
        }

        // Generate group name automatically
        String groupName = generateGroupName(subject);

        // Create group
        SubjectGroup group = SubjectGroup.builder()
                .name(groupName)
                .subjectId(command.subjectId())
                .teacherId(command.teacherId())
                .status(GroupStatus.OPEN)
                .currentEnrollmentCount(0)
                .capacity(command.capacity())        // null = use default
                .pricePerHour(command.pricePerHour()) // null = use default (15€/hour)
                .startDate(command.startDate())
                .endDate(command.endDate())
                .build();

        SubjectGroup savedGroup = groupRepositoryPort.save(group);

        // Increment subject's group count
        subject.setCurrentGroupCount(subject.getCurrentGroupCount() + 1);
        subjectRepositoryPort.save(subject);

        log.info("Group created successfully: ID {}, Subject: {}", savedGroup.getId(), command.subjectId());
        return savedGroup;
    }

    @Override
    @Transactional
    public SubjectGroup update(Long id, UpdateGroupCommand command) {
        log.info("Updating group with ID: {}", id);

        SubjectGroup group = getById(id);

        if (command.capacity() != null) {
            if (command.capacity() < group.getCurrentEnrollmentCount()) {
                throw new InvalidGroupDataException(
                        String.format("Capacity cannot be less than current enrollments (%d)",
                                group.getCurrentEnrollmentCount())
                );
            }
            group.setCapacity(command.capacity());
        }

        if (command.status() != null) {
            group.setStatus(command.status());
        }

        if (command.pricePerHour() != null) {
            group.setPricePerHour(command.pricePerHour());
        }

        // Validate dates if updated
        LocalDate newStart = command.startDate() != null ? command.startDate() : group.getStartDate();
        LocalDate newEnd = command.endDate() != null ? command.endDate() : group.getEndDate();

        if (newEnd.isBefore(newStart)) {
            throw new InvalidGroupDataException("endDate must be on or after startDate");
        }
        group.setStartDate(newStart);
        group.setEndDate(newEnd);

        SubjectGroup updatedGroup = groupRepositoryPort.save(group);
        log.info("Group updated successfully: ID {}", id);

        return updatedGroup;
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectGroup getById(Long id) {
        log.debug("Getting group by ID: {}", id);
        return groupRepositoryPort.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectGroup> findWithFilters(GroupFilters filters) {
        log.debug("Finding groups with filters: subjectId={}, teacherId={}, status={}",
                filters.subjectId(), filters.teacherId(), filters.status());
        return groupRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting group with ID: {}", id);

        SubjectGroup group = getById(id);

        if (group.getCurrentEnrollmentCount() > 0) {
            throw new InvalidGroupDataException(
                    "Cannot delete group with existing enrollments. Cancel it instead."
            );
        }

        // Delete all sessions and schedules associated with this group
        List<Schedule> schedules = scheduleRepositoryPort.findByGroupId(id);
        for (Schedule schedule : schedules) {
            sessionRepositoryPort.deleteByScheduleId(schedule.getId());
            scheduleRepositoryPort.delete(schedule.getId());
        }
        log.info("Deleted {} schedules and their associated sessions for group ID: {}", schedules.size(), id);

        // Decrement subject's group count
        Subject subject = subjectRepositoryPort.findById(group.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException(group.getSubjectId()));

        subject.setCurrentGroupCount(Math.max(0, subject.getCurrentGroupCount() - 1));
        subjectRepositoryPort.save(subject);

        groupRepositoryPort.delete(id);
        log.info("Group deleted successfully: ID {}", id);
    }

    @Override
    @Transactional
    public SubjectGroup cancel(Long id) {
        log.info("Cancelling group with ID: {}", id);

        SubjectGroup group = getById(id);
        group.setStatus(GroupStatus.CANCELLED);

        SubjectGroup cancelledGroup = groupRepositoryPort.save(group);
        log.info("Group cancelled successfully: ID {}", id);

        return cancelledGroup;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Generate group name automatically.
     * Format: "[subjectName] grupo N YY-YY"
     * Example: "Álgebra grupo 1 25-26"
     */
    private String generateGroupName(Subject subject) {
        long existingCount = groupRepositoryPort.countAllBySubjectId(subject.getId());
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
