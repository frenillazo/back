package acainfo.back.subjectgroup.application.ports.out;

import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroup;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Port for subjectGroup repository operations.
 * This interface defines the contract for subjectGroup persistence.
 */
public interface GroupRepositoryPort {

    /**
     * Saves a subjectGroup (create or update).
     *
     * @param subjectGroup the subjectGroup to save
     * @return the saved subjectGroup
     */
    SubjectGroup save(SubjectGroup subjectGroup);

    /**
     * Finds a subjectGroup by ID.
     *
     * @param id the subjectGroup ID
     * @return Optional containing the subjectGroup if found
     */
    Optional<SubjectGroup> findById(Long id);

    /**
     * Finds all groups.
     *
     * @return list of all groups
     */
    List<SubjectGroup> findAll();

    /**
     * Finds groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return list of groups
     */
    List<SubjectGroup> findBySubjectId(Long subjectId);

    /**
     * Finds groups by teacher ID.
     *
     * @param teacherId the teacher ID
     * @return list of groups
     */
    List<SubjectGroup> findByTeacherId(Long teacherId);

    /**
     * Finds groups by status.
     *
     * @param status the subjectGroup status
     * @return list of groups
     */
    List<SubjectGroup> findByStatus(GroupStatus status);

    /**
     * Finds groups by type.
     *
     * @param type the subjectGroup type
     * @return list of groups
     */
    List<SubjectGroup> findByType(GroupType type);

    /**
     * Finds groups by period.
     *
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroup> findByPeriod(AcademicPeriod period);


    /**
     * Finds groups with available places.
     *
     * @return list of groups with available places
     */
    List<SubjectGroup> findGroupsWithAvailablePlaces();

    /**
     * Finds active groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return list of active groups
     */
    List<SubjectGroup> findActiveBySubjectId(Long subjectId);

    /**
     * Counts groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return count of groups
     */
    long countBySubjectId(Long subjectId);

    /**
     * Counts active groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return count of active groups
     */
    long countActiveGroupsBySubjectId(Long subjectId);

    /**
     * Counts groups by status.
     *
     * @param status the subjectGroup status
     * @return count of groups
     */
    long countByStatus(GroupStatus status);

    /**
     * Checks if a subject has any active groups.
     *
     * @param subjectId the subject ID
     * @return true if has active groups, false otherwise
     */
    boolean hasActiveGroups(Long subjectId);

    /**
     * Deletes a subjectGroup by ID.
     *
     * @param id the subjectGroup ID
     */
    void deleteById(Long id);

    /**
     * Checks if a subjectGroup exists by ID.
     *
     * @param id the subjectGroup ID
     * @return true if exists, false otherwise
     */
    Boolean existsById(Long id);

    /**
     * Finds groups matching the given specification (dynamic filtering).
     *
     * @param spec the specification to filter by
     * @return list of matching groups
     */
    List<SubjectGroup> findAll(Specification<SubjectGroup> spec);
}
