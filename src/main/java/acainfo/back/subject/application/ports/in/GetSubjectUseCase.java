package acainfo.back.subject.application.ports.in;

import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.Subject;
import acainfo.back.subject.domain.model.SubjectStatus;

import java.util.List;

/**
 * Use case for retrieving subjects.
 */
public interface GetSubjectUseCase {

    /**
     * Gets a subject by its ID.
     *
     * @param id the subject ID
     * @return the subject
     * @throws SubjectNotFoundException if not found
     */
    Subject getSubjectById(Long id);

    /**
     * Gets a subject by its code.
     *
     * @param code the subject code
     * @return the subject
     * @throws SubjectNotFoundException if not found
     */
    Subject getSubjectByCode(String code);

    /**
     * Gets all subjects.
     *
     * @return list of all subjects
     */
    List<Subject> getAllSubjects();

    /**
     * Gets all active subjects.
     *
     * @return list of active subjects
     */
    List<Subject> getActiveSubjects();

    /**
     * Gets subjects by degree.
     *
     * @param degree the degree
     * @return list of subjects
     */
    List<Subject> getSubjectsByDegree(Degree degree);

    /**
     * Gets subjects by status.
     *
     * @param status the subject status
     * @return list of subjects
     */
    List<Subject> getSubjectsByStatus(SubjectStatus status);

    /**
     * Gets subjects by degree and year.
     *
     * @param degree the degree
     * @param year the academic year
     * @return list of subjects
     */
    List<Subject> getSubjectsByDegreeAndYear(Degree degree, Integer year);

    /**
     * Searches subjects by code or name.
     *
     * @param searchTerm the search term
     * @return list of matching subjects
     */
    List<Subject> searchSubjects(String searchTerm);
}
