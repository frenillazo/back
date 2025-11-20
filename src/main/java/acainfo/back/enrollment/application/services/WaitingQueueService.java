package acainfo.back.enrollment.application.services;

import acainfo.back.enrollment.application.ports.in.ProcessWaitingQueueUseCase;
import acainfo.back.enrollment.application.ports.out.EnrollmentRepositoryPort;
import acainfo.back.enrollment.domain.exception.EnrollmentNotFoundException;
import acainfo.back.enrollment.domain.model.Enrollment;
import acainfo.back.enrollment.domain.model.EnrollmentStatus;
import acainfo.back.enrollment.domain.model.AttendanceMode;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for processing waiting queue when places become available in a group.
 *
 * Business Rules:
 * 1. Waiting queue is processed in FIFO order (First In First Out)
 * 2. Only students in EN_ESPERA status are eligible
 * 3. When a place is available, first student in queue is activated
 * 4. Enrollment changes from EN_ESPERA to ACTIVO
 * 5. Attendance mode is set to PRESENCIAL
 * 6. Group occupancy is incremented
 * 7. Student is notified (TODO)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WaitingQueueService implements ProcessWaitingQueueUseCase {

    private final EnrollmentRepositoryPort enrollmentRepository;
    private final GroupRepositoryPort groupRepository;

    /**
     * Processes the waiting queue for a subject group.
     * This method should be called automatically when a place becomes available
     * (e.g., when a student withdraws from a presential enrollment).
     *
     * @param groupId the subject group ID
     * @return Optional containing the activated enrollment if any, empty if no one was waiting
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Optional<Enrollment> processWaitingQueue(Long groupId) {
        log.info("Processing waiting queue for group {}", groupId);

        // 1. Validate group exists
        SubjectGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        // 2. Check if group has available places
        if (!group.hasAvailablePlaces()) {
            log.debug("Group {} has no available places. Current occupancy: {}/{}",
                groupId, group.getCurrentOccupancy(), group.getMaxCapacity());
            return Optional.empty();
        }

        // 3. Get waiting queue (ordered by enrollment date - FIFO)
        List<Enrollment> waitingQueue = enrollmentRepository
                .findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(groupId, EnrollmentStatus.EN_ESPERA);

        if (waitingQueue.isEmpty()) {
            log.debug("No students waiting for group {}", groupId);
            return Optional.empty();
        }

        // 4. Get first student in queue (FIFO)
        Enrollment nextInQueue = waitingQueue.get(0);

        log.info("Activating student {} from waiting queue for group {}. " +
                "Position in queue: 1/{}, Available places: {}",
            nextInQueue.getStudent().getId(),
            groupId,
            waitingQueue.size(),
            group.getAvailablePlaces());

        // 5. Activate enrollment
        nextInQueue.activate();
        nextInQueue.changeAttendanceMode(AttendanceMode.PRESENCIAL);

        // 6. Increment group occupancy
        group.incrementOccupancy();
        groupRepository.save(group);

        // 7. Save enrollment
        Enrollment activatedEnrollment = enrollmentRepository.save(nextInQueue);

        log.info("Student {} successfully activated from waiting queue. " +
                "New group occupancy: {}/{}. Remaining in queue: {}",
            nextInQueue.getStudent().getId(),
            group.getCurrentOccupancy(),
            group.getMaxCapacity(),
            waitingQueue.size() - 1);

        // 8. TODO: Send notification to student
        // notificationService.notifyPlaceAvailable(activatedEnrollment);

        // 9. If there are still available places and more students waiting, process next
        if (group.hasAvailablePlaces() && waitingQueue.size() > 1) {
            log.info("Group {} still has {} available places and {} students waiting. " +
                    "Processing next in queue...",
                groupId, group.getAvailablePlaces(), waitingQueue.size() - 1);
            // Recursively process next in queue
            processWaitingQueue(groupId);
        }

        return Optional.of(activatedEnrollment);
    }

    /**
     * Gets the position of a student in the waiting queue.
     *
     * @param enrollmentId the enrollment ID
     * @return position in queue (1-based), or -1 if not in waiting queue
     */
    @Override
    @Transactional(readOnly = true)
    public int getQueuePosition(Long enrollmentId) {
        log.debug("Getting queue position for enrollment {}", enrollmentId);

        // Get enrollment
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // Check if enrollment is in waiting status
        if (!enrollment.isWaiting()) {
            log.debug("Enrollment {} is not in waiting queue. Status: {}", enrollmentId, enrollment.getStatus());
            return -1;
        }

        // Get waiting queue for the group
        List<Enrollment> waitingQueue = enrollmentRepository
                .findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(
                    enrollment.getSubjectGroup().getId(),
                    EnrollmentStatus.EN_ESPERA
                );

        // Find position (1-based)
        for (int i = 0; i < waitingQueue.size(); i++) {
            if (waitingQueue.get(i).getId().equals(enrollmentId)) {
                int position = i + 1;
                log.debug("Enrollment {} is at position {} in waiting queue (total: {})",
                    enrollmentId, position, waitingQueue.size());
                return position;
            }
        }

        // Should not happen if enrollment is in EN_ESPERA status
        log.warn("Enrollment {} has EN_ESPERA status but not found in waiting queue", enrollmentId);
        return -1;
    }

    /**
     * Gets the total number of students waiting for a group.
     *
     * @param groupId the subject group ID
     * @return the count of students in waiting queue
     */
    @Override
    @Transactional(readOnly = true)
    public long getWaitingCount(Long groupId) {
        log.debug("Getting waiting count for group {}", groupId);

        long count = enrollmentRepository.countBySubjectGroupIdAndStatus(groupId, EnrollmentStatus.EN_ESPERA);

        log.debug("Group {} has {} students waiting", groupId, count);
        return count;
    }

    /**
     * Gets all students waiting for a group (for admin viewing).
     *
     * @param groupId the subject group ID
     * @return list of enrollments in waiting queue (ordered by enrollment date)
     */
    @Transactional(readOnly = true)
    public List<Enrollment> getWaitingQueue(Long groupId) {
        log.debug("Getting waiting queue for group {}", groupId);

        return enrollmentRepository.findBySubjectGroupIdAndStatusOrderByEnrollmentDateAsc(
            groupId,
            EnrollmentStatus.EN_ESPERA
        );
    }

    /**
     * Manually processes the entire waiting queue for a group.
     * This can be used by admins to process the queue after increasing group capacity.
     *
     * @param groupId the subject group ID
     * @return number of students activated from waiting queue
     */
    @Transactional
    public int processEntireQueue(Long groupId) {
        log.info("Processing entire waiting queue for group {}", groupId);

        int activatedCount = 0;
        Optional<Enrollment> activated;

        // Process queue until no more places or no more students waiting
        do {
            activated = processWaitingQueue(groupId);
            if (activated.isPresent()) {
                activatedCount++;
            }
        } while (activated.isPresent());

        log.info("Processed entire waiting queue for group {}. Students activated: {}", groupId, activatedCount);
        return activatedCount;
    }
}
