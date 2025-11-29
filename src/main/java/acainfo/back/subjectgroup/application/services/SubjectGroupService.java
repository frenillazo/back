package acainfo.back.subjectgroup.application.services;

import acainfo.back.user.domain.exception.InvalidTeacherException;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.User;
import acainfo.back.subjectgroup.application.ports.in.CreateGroupUseCase;
import acainfo.back.subjectgroup.application.ports.in.DeleteGroupUseCase;
import acainfo.back.subjectgroup.application.ports.in.GetGroupUseCase;
import acainfo.back.subjectgroup.application.ports.in.UpdateGroupUseCase;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.user.infrastructure.adapters.out.UserRepository;
import acainfo.back.subject.domain.exception.SubjectInactiveException;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.exception.MaxGroupsPerSubjectException;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for subjectGroup management.
 * Implements all subjectGroup use cases with business logic and validations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectGroupService implements
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
    public SubjectGroup createGroup(SubjectGroup subjectGroup) {
        log.info("Creating new subjectGroup for subject ID: {}", subjectGroup.getSubject().getId());

        // Validate subject exists and is active
        validateSubjectActive(subjectGroup.getSubject());

        // Validate maximum groups per subject
        validateMaxGroupsPerSubject(subjectGroup.getSubject().getId());

        // Validate teacher if assigned
        if (subjectGroup.getTeacher() != null) {
            validateTeacher(subjectGroup.getTeacher());
        }

        // Validate max capacity is set
        if (subjectGroup.getMaxCapacity() == null || subjectGroup.getMaxCapacity() < 1) {
            throw new IllegalArgumentException("Max capacity must be specified and greater than 0");
        }

        // Set initial status if not set
        if (subjectGroup.getStatus() == null) {
            subjectGroup.setStatus(GroupStatus.ACTIVO);
        }

        // Initialize occupancy
        if (subjectGroup.getCurrentOccupancy() == null) {
            subjectGroup.setCurrentOccupancy(0);
        }

        SubjectGroup savedSubjectGroup = groupRepository.save(subjectGroup);
        log.info("SubjectGroup created successfully with ID: {}", savedSubjectGroup.getId());

        return savedSubjectGroup;
    }

    // ==================== UPDATE ====================

    @Override
    public SubjectGroup updateGroup(Long id, SubjectGroup subjectGroup) {
        log.info("Updating subjectGroup with ID: {}", id);

        // Check if subjectGroup exists
        SubjectGroup existingSubjectGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        // Validate teacher if being changed
        if (subjectGroup.getTeacher() != null &&
            !subjectGroup.getTeacher().equals(existingSubjectGroup.getTeacher())) {
            validateTeacher(subjectGroup.getTeacher());
        }

        // Update fields
        if (subjectGroup.getType() != null) {
            existingSubjectGroup.setType(subjectGroup.getType());
        }
        if (subjectGroup.getPeriod() != null) {
            existingSubjectGroup.setPeriod(subjectGroup.getPeriod());
        }
        if (subjectGroup.getDescription() != null) {
            existingSubjectGroup.setDescription(subjectGroup.getDescription());
        }

        // Update teacher if provided
        if (subjectGroup.getTeacher() != null) {
            existingSubjectGroup.setTeacher(subjectGroup.getTeacher());
        }

        // Update status if provided
        if (subjectGroup.getStatus() != null) {
            existingSubjectGroup.setStatus(subjectGroup.getStatus());
        }

        // Update max capacity if provided
        if (subjectGroup.getMaxCapacity() != null && subjectGroup.getMaxCapacity() > 0) {
            existingSubjectGroup.setMaxCapacity(subjectGroup.getMaxCapacity());
        }

        SubjectGroup updatedSubjectGroup = groupRepository.save(existingSubjectGroup);
        log.info("SubjectGroup updated successfully: {}", updatedSubjectGroup.getId());

        return updatedSubjectGroup;
    }

    @Override
    public SubjectGroup assignTeacher(Long groupId, Long teacherId) {
        log.info("Assigning teacher {} to subjectGroup {}", teacherId, groupId);

        SubjectGroup subjectGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new UserNotFoundException(teacherId));

        validateTeacher(teacher);

        subjectGroup.setTeacher(teacher);
        SubjectGroup updatedSubjectGroup = groupRepository.save(subjectGroup);

        log.info("Teacher assigned successfully to subjectGroup {}", groupId);
        return updatedSubjectGroup;
    }

    // ==================== GET ====================

    @Override
    @Transactional(readOnly = true)
    public SubjectGroup getGroupById(Long id) {
        log.debug("Fetching subjectGroup by ID: {}", id);
        return groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getAllGroups() {
        log.debug("Fetching all groups");
        return groupRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getActiveGroups() {
        log.debug("Fetching active groups");
        return groupRepository.findByStatus(GroupStatus.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getGroupsBySubject(Long subjectId) {
        log.debug("Fetching groups by subject ID: {}", subjectId);
        return groupRepository.findBySubjectId(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getGroupsByTeacher(Long teacherId) {
        log.debug("Fetching groups by teacher ID: {}", teacherId);
        return groupRepository.findByTeacherId(teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getGroupsByStatus(GroupStatus status) {
        log.debug("Fetching groups by status: {}", status);
        return groupRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getGroupsByType(GroupType type) {
        log.debug("Fetching groups by type: {}", type);
        return groupRepository.findByType(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getGroupsByPeriod(AcademicPeriod period) {
        log.debug("Fetching groups by period: {}", period);
        return groupRepository.findByPeriod(period);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectGroup> getGroupsWithAvailablePlaces() {
        log.debug("Fetching groups with available places");
        return groupRepository.findGroupsWithAvailablePlaces();
    }

    // ==================== DELETE ====================

    @Override
    public void deleteGroup(Long id) {
        log.info("Deleting subjectGroup with ID: {}", id);

        SubjectGroup subjectGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        // Cannot delete subjectGroup with enrolled students
        if (subjectGroup.getCurrentOccupancy() > 0) {
            throw new IllegalStateException(
                "Cannot delete subjectGroup with enrolled students. Current occupancy: " +
                subjectGroup.getCurrentOccupancy()
            );
        }

        groupRepository.deleteById(id);
        log.info("SubjectGroup deleted successfully: {}", id);
    }

    @Override
    public void cancelGroup(Long id) {
        log.info("Cancelling subjectGroup with ID: {}", id);

        SubjectGroup subjectGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        subjectGroup.cancel();
        groupRepository.save(subjectGroup);

        log.info("SubjectGroup cancelled successfully: {}", id);
    }

    // ==================== FILTERING WITH SPECIFICATIONS ====================

    /**
     * Finds groups matching the given specification (dynamic filtering).
     *
     * @param spec the specification to filter by
     * @return list of matching groups
     */
    @Transactional(readOnly = true)
    public List<SubjectGroup> findGroupsWithFilters(Specification<SubjectGroup> spec) {
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

        // Update the subjectGroup's subject reference with the fetched entity
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
