package acainfo.back.subjectgroup.application.ports.in;

import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;

/**
 * Use case for deleting a subjectGroup.
 */
public interface DeleteGroupUseCase {

    /**
     * Deletes a subjectGroup from the system.
     * Can only delete groups with no enrollments (currentOccupancy = 0).
     *
     * @param id the subjectGroup ID to delete
     * @throws GroupNotFoundException if subjectGroup not found
     * @throws IllegalStateException if subjectGroup has enrolled students
     */
    void deleteGroup(Long id);

    /**
     * Cancels a subjectGroup (soft delete).
     * Changes the status to CANCELADO.
     *
     * @param id the subjectGroup ID to cancel
     * @throws GroupNotFoundException if subjectGroup not found
     */
    void cancelGroup(Long id);
}
