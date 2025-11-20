package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.Enrollment;

import java.util.Optional;

/**
 * Use case for processing waiting queue when a place becomes available.
 * This is typically triggered automatically when a student withdraws from a presential enrollment.
 */
public interface ProcessWaitingQueueUseCase {

    /**
     * Processes the waiting queue for a subject group.
     * If there are students waiting and places available, activates the first student in queue (FIFO).
     *
     * @param groupId the subject group ID
     * @return Optional containing the activated enrollment if any student was waiting, empty otherwise
     */
    Optional<Enrollment> processWaitingQueue(Long groupId);

    /**
     * Gets the position of a student in the waiting queue for a group.
     *
     * @param enrollmentId the enrollment ID
     * @return the position in queue (1-based), or -1 if not in waiting queue
     */
    int getQueuePosition(Long enrollmentId);

    /**
     * Gets the total number of students waiting for a group.
     *
     * @param groupId the subject group ID
     * @return the count of students in waiting queue
     */
    long getWaitingCount(Long groupId);
}
