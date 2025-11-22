package acainfo.back.subjectgroup.application.usecases;

import acainfo.back.shared.domain.exception.InvalidTeacherException;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.exception.SubjectInactiveException;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subjectgroup.application.ports.in.CreateGroupUseCase;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.MaxGroupsPerSubjectException;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CreateGroupUseCase
 * Handles subjectGroup creation with business validations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateGroupUseCaseImpl implements CreateGroupUseCase {

    private static final int MAX_GROUPS_PER_SUBJECT = 3;

    private final GroupRepositoryPort groupRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final UserRepository userRepository;

    @Override
    public SubjectGroupDomain createGroup(SubjectGroupDomain subjectGroup) {
        log.info("Creating new subjectGroup for subject ID: {}", subjectGroup.getSubjectId());

        // Validate subject exists and is active
        validateSubjectActive(subjectGroup.getSubjectId());

        // Validate maximum groups per subject
        validateMaxGroupsPerSubject(subjectGroup.getSubjectId());

        // Validate teacher if assigned
        if (subjectGroup.getTeacherId() != null) {
            validateTeacher(subjectGroup.getTeacherId());
        }

        SubjectGroupDomain savedSubjectGroup = groupRepository.save(subjectGroup);
        log.info("SubjectGroup created successfully with ID: {}", savedSubjectGroup.getId());

        return savedSubjectGroup;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates that the subject exists and is active.
     */
    private void validateSubjectActive(Long subjectId) {
        if (subjectId == null) {
            throw new IllegalArgumentException("Subject ID is required");
        }

        SubjectDomain subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException(subjectId));

        if (!subject.isActive()) {
            throw new SubjectInactiveException(subjectId);
        }
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
    private void validateTeacher(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Teacher ID is required");
        }

        User teacher = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!teacher.hasRole(RoleType.TEACHER)) {
            throw new InvalidTeacherException(userId);
        }
    }
}
