package acainfo.back.subjectgroup.application.usecases;

import acainfo.back.shared.domain.exception.InvalidTeacherException;
import acainfo.back.shared.domain.exception.UserNotFoundException;
import acainfo.back.shared.domain.model.RoleType;
import acainfo.back.shared.domain.model.User;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import acainfo.back.subjectgroup.application.ports.in.UpdateGroupUseCase;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of UpdateGroupUseCase
 * Handles subjectGroup update operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UpdateGroupUseCaseImpl implements UpdateGroupUseCase {

    private final GroupRepositoryPort groupRepository;
    private final UserRepository userRepository;

    @Override
    public SubjectGroupDomain updateGroup(Long id, SubjectGroupDomain subjectGroup) {
        log.info("Updating subjectGroup with ID: {}", id);

        // Check if subjectGroup exists
        SubjectGroupDomain existingSubjectGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        // Validate teacher if being changed
        if (subjectGroup.getTeacherId() != null &&
            !subjectGroup.getTeacherId().equals(existingSubjectGroup.getTeacherId())) {
            validateTeacher(subjectGroup.getTeacherId());
        }

        // Build updated subjectGroup preserving ID and non-updatable fields
        SubjectGroupDomain updated = SubjectGroupDomain.builder()
                .id(existingSubjectGroup.getId())
                .subjectId(existingSubjectGroup.getSubjectId()) // Subject cannot change
                .teacherId(subjectGroup.getTeacherId() != null ?
                          subjectGroup.getTeacherId() : existingSubjectGroup.getTeacherId())
                .type(subjectGroup.getType() != null ?
                     subjectGroup.getType() : existingSubjectGroup.getType())
                .period(subjectGroup.getPeriod() != null ?
                       subjectGroup.getPeriod() : existingSubjectGroup.getPeriod())
                .status(subjectGroup.getStatus() != null ?
                       subjectGroup.getStatus() : existingSubjectGroup.getStatus())
                .maxCapacity(subjectGroup.getMaxCapacity() != null && subjectGroup.getMaxCapacity() > 0 ?
                            subjectGroup.getMaxCapacity() : existingSubjectGroup.getMaxCapacity())
                .currentOccupancy(existingSubjectGroup.getCurrentOccupancy()) // Don't allow direct modification
                .description(subjectGroup.getDescription() != null ?
                            subjectGroup.getDescription() : existingSubjectGroup.getDescription())
                .createdAt(existingSubjectGroup.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        SubjectGroupDomain savedSubjectGroup = groupRepository.save(updated);
        log.info("SubjectGroup updated successfully: {}", savedSubjectGroup.getId());

        return savedSubjectGroup;
    }

    @Override
    public SubjectGroupDomain assignTeacher(Long groupId, Long teacherId) {
        log.info("Assigning teacher {} to subjectGroup {}", teacherId, groupId);

        SubjectGroupDomain subjectGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        validateTeacher(teacherId);

        // Build updated subjectGroup with new teacher
        SubjectGroupDomain updated = SubjectGroupDomain.builder()
                .id(subjectGroup.getId())
                .subjectId(subjectGroup.getSubjectId())
                .teacherId(teacherId)
                .type(subjectGroup.getType())
                .period(subjectGroup.getPeriod())
                .status(subjectGroup.getStatus())
                .maxCapacity(subjectGroup.getMaxCapacity())
                .currentOccupancy(subjectGroup.getCurrentOccupancy())
                .description(subjectGroup.getDescription())
                .createdAt(subjectGroup.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        SubjectGroupDomain savedSubjectGroup = groupRepository.save(updated);

        log.info("Teacher assigned successfully to subjectGroup {}", groupId);
        return savedSubjectGroup;
    }

    // ==================== PRIVATE HELPER METHODS ====================

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
