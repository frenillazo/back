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

/**
 * Service implementing group use cases.
 * Contains business logic and validations for group operations.
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

    @Override
    @Transactional
    public SubjectGroup create(CreateGroupCommand command) {
        log.info("Creating group for subject: {}, teacher: {}, type: {}",
                command.subjectId(), command.teacherId(), command.type());

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

        // Validate custom capacity if provided
        if (command.capacity() != null) {
            int maxCapacity = command.type().isIntensive()
                    ? SubjectGroup.INTENSIVE_MAX_CAPACITY
                    : SubjectGroup.REGULAR_MAX_CAPACITY;

            if (command.capacity() < 1 || command.capacity() > maxCapacity) {
                throw new InvalidGroupDataException(
                        String.format("Capacity must be between 1 and %d for type %s",
                                maxCapacity, command.type())
                );
            }
        }

        // Create group
        SubjectGroup group = SubjectGroup.builder()
                .subjectId(command.subjectId())
                .teacherId(command.teacherId())
                .type(command.type())
                .status(GroupStatus.OPEN)
                .currentEnrollmentCount(0)
                .capacity(command.capacity())        // null = use default
                .pricePerHour(command.pricePerHour()) // null = use default (15€/hour)
                .build();

        SubjectGroup savedGroup = groupRepositoryPort.save(group);

        // Increment subject's group count
        subject.setCurrentGroupCount(subject.getCurrentGroupCount() + 1);
        subjectRepositoryPort.save(subject);

        log.info("Group created successfully: ID {}, Subject: {}, Type: {}",
                savedGroup.getId(), command.subjectId(), command.type());

        return savedGroup;
    }

    @Override
    @Transactional
    public SubjectGroup update(Long id, UpdateGroupCommand command) {
        log.info("Updating group with ID: {}", id);

        SubjectGroup group = getById(id);

        // Update capacity if provided
        if (command.capacity() != null) {
            int maxCapacity = group.isIntensive()
                    ? SubjectGroup.INTENSIVE_MAX_CAPACITY
                    : SubjectGroup.REGULAR_MAX_CAPACITY;

            if (command.capacity() < group.getCurrentEnrollmentCount()) {
                throw new InvalidGroupDataException(
                        String.format("Capacity cannot be less than current enrollments (%d)",
                                group.getCurrentEnrollmentCount())
                );
            }

            if (command.capacity() > maxCapacity) {
                throw new InvalidGroupDataException(
                        String.format("Capacity cannot exceed %d for type %s",
                                maxCapacity, group.getType())
                );
            }

            group.setCapacity(command.capacity());
        }

        // Update status if provided
        if (command.status() != null) {
            group.setStatus(command.status());
        }

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
        log.debug("Finding groups with filters: subjectId={}, teacherId={}, type={}, status={}",
                filters.subjectId(), filters.teacherId(), filters.type(), filters.status());
        return groupRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting group with ID: {}", id);

        SubjectGroup group = getById(id);

        // Check if group has enrollments
        if (group.getCurrentEnrollmentCount() > 0) {
            throw new InvalidGroupDataException(
                    "Cannot delete group with existing enrollments. Cancel it instead."
            );
        }

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
}
