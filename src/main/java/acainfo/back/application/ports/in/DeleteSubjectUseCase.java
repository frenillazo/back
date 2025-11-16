package acainfo.back.application.ports.in;

/**
 * Use case for deleting a subject.
 */
public interface DeleteSubjectUseCase {

    /**
     * Deletes a subject from the system.
     * Cannot delete a subject that has active groups.
     *
     * @param id the subject ID to delete
     * @throws acainfo.back.domain.exception.SubjectNotFoundException if subject not found
     * @throws acainfo.back.domain.exception.SubjectHasActiveGroupsException if subject has active groups
     */
    void deleteSubject(Long id);

    /**
     * Archives a subject (soft delete).
     * Changes the status to ARCHIVADO.
     *
     * @param id the subject ID to archive
     * @throws acainfo.back.domain.exception.SubjectNotFoundException if subject not found
     */
    void archiveSubject(Long id);
}
