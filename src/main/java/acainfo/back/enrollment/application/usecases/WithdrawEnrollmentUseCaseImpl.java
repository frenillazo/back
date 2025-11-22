package acainfo.back.enrollment.application.usecases;

import acainfo.back.enrollment.application.ports.in.ProcessWaitingQueueUseCase;
import acainfo.back.enrollment.application.ports.in.WithdrawEnrollmentUseCase;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.model.EnrollmentDomain;
import acainfo.back.subjectgroup.application.ports.out.SubjectGroupRepositoryPort;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of WithdrawEnrollmentUseCase
 * Handles withdrawing students from enrollments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawEnrollmentUseCaseImpl implements WithdrawEnrollmentUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;
    private final SubjectGroupRepositoryPort groupRepository;
    private final ProcessWaitingQueueUseCase processWaitingQueueUseCase;

    @Override
    @Transactional
    public void withdrawEnrollment(Long enrollmentId, String reason) {
        log.info("Withdrawing enrollment {}", enrollmentId);

        EnrollmentDomain enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        if (enrollment.isWithdrawn()) {
            throw new IllegalStateException("Enrollment is already withdrawn");
        }

        // Mark enrollment as withdrawn using domain method
        EnrollmentDomain withdrawn = enrollment.withdraw(reason);

        // Free place if was presential and active
        if (enrollment.occupiesPhysicalSpace()) {
            SubjectGroupDomain group = groupRepository.findById(enrollment.getSubjectGroupId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Group not found: " + enrollment.getSubjectGroupId()));

            SubjectGroupDomain updatedGroup = group.decrementOccupancy();
            groupRepository.save(updatedGroup);

            log.info("Group {} occupancy decremented to {}/{}",
                updatedGroup.getId(), updatedGroup.getCurrentOccupancy(), updatedGroup.getMaxCapacity());

            // Process waiting queue automatically
            processWaitingQueueUseCase.processWaitingQueue(updatedGroup.getId());
        }

        enrollmentRepository.save(withdrawn);
        log.info("Enrollment {} withdrawn successfully", enrollmentId);

        // TODO: Send notification
        // notificationService.notifyWithdrawal(enrollment);
    }
}
