package acainfo.back.application.ports.out;

import acainfo.back.domain.model.Degree;
import acainfo.back.domain.model.Subject;
import acainfo.back.domain.model.SubjectStatus;

import java.util.List;
import java.util.Optional;

/**
 * Port for subject repository operations.
 * This interface defines the contract for subject persistence.
 */
public interface SubjectRepositoryPort {

    /**
     * Saves a subject (create or update).
     *
     * @param subject the subject to save
     * @return the saved subject
     */
    Subject save(Subject subject);

    /**
     * Finds a subject by ID.
     *
     * @param id the subject ID
     * @return Optional containing the subject if found
     */
    Optional<Subject> findById(Long id);

    /**
     * Finds a subject by code.
     *
     * @param code the subject code
     * @return Optional containing the subject if found
     */
    Optional<Subject> findByCode(String code);

    /**
     * Checks if a subject exists by code.
     *
     * @param code the subject code
     * @return true if exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Checks if a subject exists by code excluding a specific ID.
     * Useful for update validation.
     *
     * @param code the subject code
     * @param excludeId the ID to exclude
     * @return true if exists, false otherwise
     */
    boolean existsByCodeAndIdNot(String code, Long excludeId);

    /**
     * Finds all subjects.
     *
     * @return list of all subjects
     */
    List<Subject> findAll();

    /**
     * Finds subjects by status.
     *
     * @param status the subject status
     * @return list of subjects
     */
    List<Subject> findByStatus(SubjectStatus status);

    /**
     * Finds subjects by degree.
     *
     * @param degree the degree
     * @return list of subjects
     */
    List<Subject> findByDegree(Degree degree);

    /**
     * Finds subjects by degree and year.
     *
     * @param degree the degree
     * @param year the academic year
     * @return list of subjects
     */
    List<Subject> findByDegreeAndYear(Degree degree, Integer year);

    /**
     * Searches subjects by code or name.
     *
     * @param searchTerm the search term
     * @return list of matching subjects
     */
    List<Subject> searchByCodeOrName(String searchTerm);

    /**
     * Deletes a subject by ID.
     *
     * @param id the subject ID
     */
    void deleteById(Long id);

    /**
     * Counts subjects by status.
     *
     * @param status the subject status
     * @return count of subjects
     */
    long countByStatus(SubjectStatus status);
}
