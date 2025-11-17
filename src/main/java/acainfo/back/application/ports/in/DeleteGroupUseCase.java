package acainfo.back.application.ports.in;

/**
 * Use case for deleting a group.
 */
public interface DeleteGroupUseCase {

    /**
     * Deletes a group from the system.
     * Can only delete groups with no enrollments (currentOccupancy = 0).
     *
     * @param id the group ID to delete
     * @throws acainfo.back.domain.exception.GroupNotFoundException if group not found
     * @throws IllegalStateException if group has enrolled students
     */
    void deleteGroup(Long id);

    /**
     * Cancels a group (soft delete).
     * Changes the status to CANCELADO.
     *
     * @param id the group ID to cancel
     * @throws acainfo.back.domain.exception.GroupNotFoundException if group not found
     */
    void cancelGroup(Long id);
}
