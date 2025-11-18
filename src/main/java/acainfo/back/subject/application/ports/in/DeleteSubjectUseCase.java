package acainfo.back.subject.application.ports.in;

import acainfo.back.subject.domain.exception.SubjectHasActiveGroupsException;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;

/**
 * Use case for deleting a subject.
 */
public interface DeleteSubjectUseCase {

    /**
     * Deletes a subject from the system.
     * Cannot delete a subject that has active groups.
     *
     * @param id the subject ID to delete
     * @throws SubjectNotFoundException if subject not found
     * @throws SubjectHasActiveGroupsException if subject has active groups
     */
    void deleteSubject(Long id);

    /**
     * Archives a subject (soft delete).
     * Changes the status to ARCHIVADO.
     *
     * @param id the subject ID to archive
     * @throws SubjectNotFoundException if subject not found
     */
    void archiveSubject(Long id);
}
