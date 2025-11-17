package acainfo.back.application.services;

import acainfo.back.application.ports.in.CreateGroupUseCase;
import acainfo.back.application.ports.in.DeleteGroupUseCase;
import acainfo.back.application.ports.in.GetGroupUseCase;
import acainfo.back.application.ports.in.UpdateGroupUseCase;
import acainfo.back.application.ports.out.GroupRepositoryPort;
import acainfo.back.application.ports.out.SubjectRepositoryPort;
import acainfo.back.domain.exception.*;
import acainfo.back.domain.model.*;
import acainfo.back.infrastructure.adapters.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for group management.
 * Implements all group use cases with business logic and validations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GroupService implements
        CreateGroupUseCase,
        UpdateGroupUseCase,
        GetGroupUseCase,
        DeleteGroupUseCase {

    private static final int MAX_GROUPS_PER_SUBJECT = 3;

    private final GroupRepositoryPort groupRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final UserRepository userRepository;

    // ==================== CREATE ====================

    @Override
    public Group createGroup(Group group) {
        log.info("Creating new group for subject ID: {}", group.getSubject().getId());

        // Validate subject exists and is active
        validateSubjectActive(group.getSubject());

        // Validate maximum groups per subject
        validateMaxGroupsPerSubject(group.getSubject().getId());

        // Validate teacher if assigned
        if (group.getTeacher() != null) {
            validateTeacher(group.getTeacher());
        }

        // Validate max capacity is set
        if (group.getMaxCapacity() == null || group.getMaxCapacity() < 1) {
            throw new IllegalArgumentException("Max capacity must be specified and greater than 0");
        }

        // Set initial status if not set
        if (group.getStatus() == null) {
            group.setStatus(GroupStatus.ACTIVO);
        }

        // Initialize occupancy
        if (group.getCurrentOccupancy() == null) {
            group.setCurrentOccupancy(0);
        }

        Group savedGroup = groupRepository.save(group);
        log.info("Group created successfully with ID: {}", savedGroup.getId());

        return savedGroup;
    }

    // ==================== UPDATE ====================

    @Override
    public Group updateGroup(Long id, Group group) {
        log.info("Updating group with ID: {}", id);

        // Check if group exists
        Group existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        // Validate teacher if being changed
        if (group.getTeacher() != null &&
            !group.getTeacher().equals(existingGroup.getTeacher())) {
            validateTeacher(group.getTeacher());
        }

        // Update fields
        if (group.getType() != null) {
            existingGroup.setType(group.getType());
        }
        if (group.getPeriod() != null) {
            existingGroup.setPeriod(group.getPeriod());
        }
        if (group.getDescription() != null) {
            existingGroup.setDescription(group.getDescription());
        }

        // Update teacher if provided
        if (group.getTeacher() != null) {
            existingGroup.setTeacher(group.getTeacher());
        }

        // Update status if provided
        if (group.getStatus() != null) {
            existingGroup.setStatus(group.getStatus());
        }

        // Update max capacity if provided
        if (group.getMaxCapacity() != null && group.getMaxCapacity() > 0) {
            existingGroup.setMaxCapacity(group.getMaxCapacity());
        }

        Group updatedGroup = groupRepository.save(existingGroup);
        log.info("Group updated successfully: {}", updatedGroup.getId());

        return updatedGroup;
    }

    @Override
    public Group assignTeacher(Long groupId, Long teacherId) {
        log.info("Assigning teacher {} to group {}", teacherId, groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        validateTeacher(teacher);

        group.setTeacher(teacher);
        Group updatedGroup = groupRepository.save(group);

        log.info("Teacher assigned successfully to group {}", groupId);
        return updatedGroup;
    }

    // ==================== GET ====================

    @Override
    @Transactional(readOnly = true)
    public Group getGroupById(Long id) {
        log.debug("Fetching group by ID: {}", id);
        return groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getAllGroups() {
        log.debug("Fetching all groups");
        return groupRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getActiveGroups() {
        log.debug("Fetching active groups");
        return groupRepository.findByStatus(GroupStatus.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupsBySubject(Long subjectId) {
        log.debug("Fetching groups by subject ID: {}", subjectId);
        return groupRepository.findBySubjectId(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupsByTeacher(Long teacherId) {
        log.debug("Fetching groups by teacher ID: {}", teacherId);
        return groupRepository.findByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupsByStatus(GroupStatus status) {
        log.debug("Fetching groups by status: {}", status);
        return groupRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupsByType(GroupType type) {
        log.debug("Fetching groups by type: {}", type);
        return groupRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupsByPeriod(AcademicPeriod period) {
        log.debug("Fetching groups by period: {}", period);
        return groupRepository.findByPeriod(period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroupsWithAvailablePlaces() {
        log.debug("Fetching groups with available places");
        return groupRepository.findGroupsWithAvailablePlaces();
    }

    // ==================== DELETE ====================

    @Override
    public void deleteGroup(Long id) {
        log.info("Deleting group with ID: {}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        // Cannot delete group with enrolled students
        if (group.getCurrentOccupancy() > 0) {
            throw new IllegalStateException(
                "Cannot delete group with enrolled students. Current occupancy: " +
                group.getCurrentOccupancy()
            );
        }

        groupRepository.deleteById(id);
        log.info("Group deleted successfully: {}", id);
    }

    @Override
    public void cancelGroup(Long id) {
        log.info("Cancelling group with ID: {}", id);

        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        group.cancel();
        groupRepository.save(group);

        log.info("Group cancelled successfully: {}", id);
    }

    // ==================== FILTERING WITH SPECIFICATIONS ====================

    /**
     * Finds groups matching the given specification (dynamic filtering).
     *
     * @param spec the specification to filter by
     * @return list of matching groups
     */
    @Transactional(readOnly = true)
    public List<Group> findGroupsWithFilters(Specification<Group> spec) {
        log.debug("Finding groups with dynamic filters");
        return groupRepository.findAll(spec);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates that the subject exists and is active.
     */
    private void validateSubjectActive(Subject subject) {
        if (subject == null || subject.getId() == null) {
            throw new IllegalArgumentException("Subject is required");
        }

        Subject existingSubject = subjectRepository.findById(subject.getId())
                .orElseThrow(() -> new SubjectNotFoundException(subject.getId()));

        if (!existingSubject.isActive()) {
            throw new SubjectInactiveException(existingSubject.getId());
        }

        // Update the group's subject reference with the fetched entity
        subject.setStatus(existingSubject.getStatus());
        subject.setName(existingSubject.getName());
        subject.setCode(existingSubject.getCode());
    }

    /**
     * Validates that the subject doesn't exceed the maximum number of groups.
     */
    private void validateMaxGroupsPerSubject(Long subjectId) {
        long groupCount = groupRepository.countBySubjectId(subjectId);

        if (groupCount >= MAX_GROUPS_PER_SUBJECT) {
            throw new MaxGroupsPerSubjectException(subjectId);
        }
    }

    /**
     * Validates that the user is a teacher.
     */
    private void validateTeacher(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Teacher is required");
        }

        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException(user.getId()));

        if (!existingUser.hasRole(RoleType.TEACHER)) {
            throw new InvalidTeacherException(existingUser.getId());
        }
    }
}
