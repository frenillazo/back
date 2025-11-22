package acainfo.back.enrollment.application.usecases;

import acainfo.back.enrollment.application.ports.in.ChangeGroupUseCase;
import acainfo.back.enrollment.application.ports.in.EnrollStudentUseCase;
import acainfo.back.enrollment.application.ports.in.WithdrawEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of ChangeGroupUseCase
 * Handles changing a student's enrollment to a different group
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeGroupUseCaseImpl implements ChangeGroupUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;
    private final GroupRepositoryPort groupRepository;
    private final WithdrawEnrollmentUseCase withdrawEnrollmentUseCase;
    private final EnrollStudentUseCase enrollStudentUseCase;

    @Override
    @Transactional
    public EnrollmentDomain changeGroup(Long enrollmentId, Long newGroupId) {
        log.info("Changing enrollment {} to new group {}", enrollmentId, newGroupId);

        EnrollmentDomain currentEnrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        SubjectGroupDomain newGroup = groupRepository.findById(newGroupId)
                .orElseThrow(() -> new GroupNotFoundException(newGroupId));

        SubjectGroupDomain currentGroup = groupRepository.findById(currentEnrollment.getSubjectGroupId())
                .orElseThrow(() -> new GroupNotFoundException(currentEnrollment.getSubjectGroupId()));

        // Validate same subject
        if (!currentGroup.getSubjectId().equals(newGroup.getSubjectId())) {
            throw new IllegalArgumentException(
                "Cannot change to a group of a different subject. " +
                "Current subject ID: " + currentGroup.getSubjectId() +
                ", New group subject ID: " + newGroup.getSubjectId()
            );
        }

        // Withdraw from current group
        withdrawEnrollmentUseCase.withdrawEnrollment(enrollmentId, "Cambio de grupo");

        // Enroll in new group
        EnrollmentDomain newEnrollment = enrollStudentUseCase.enrollStudent(
            currentEnrollment.getStudentId(),
            newGroupId
        );

        log.info("Student {} changed from group {} to group {} successfully",
            currentEnrollment.getStudentId(),
            currentEnrollment.getSubjectGroupId(),
            newGroupId);

        return newEnrollment;
    }
}
