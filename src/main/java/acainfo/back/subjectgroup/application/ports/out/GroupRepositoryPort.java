package acainfo.back.subjectgroup.application.ports.out;

import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;

import java.util.List;
import java.util.Optional;

/**
 * Port for subjectGroup repository operations.
 * This interface defines the contract for subjectGroup persistence.
 * Works with SubjectGroupDomain (pure domain model)
 */
public interface GroupRepositoryPort {

    /**
     * Saves a subjectGroup (create or update).
     *
     * @param subjectGroup the subjectGroup to save
     * @return the saved subjectGroup
     */
    SubjectGroupDomain save(SubjectGroupDomain subjectGroup);

    /**
     * Finds a subjectGroup by ID.
     *
     * @param id the subjectGroup ID
     * @return Optional containing the subjectGroup if found
     */
    Optional<SubjectGroupDomain> findById(Long id);

    /**
     * Finds all groups.
     *
     * @return list of all groups
     */
    List<SubjectGroupDomain> findAll();

    /**
     * Finds groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return list of groups
     */
    List<SubjectGroupDomain> findBySubjectId(Long subjectId);

    /**
     * Finds groups by teacher ID.
     *
     * @param teacherId the teacher ID
     * @return list of groups
     */
    List<SubjectGroupDomain> findByTeacherId(Long teacherId);

    /**
     * Finds groups by status.
     *
     * @param status the subjectGroup status
     * @return list of groups
     */
    List<SubjectGroupDomain> findByStatus(GroupStatus status);

    /**
     * Finds groups by type.
     *
     * @param type the subjectGroup type
     * @return list of groups
     */
    List<SubjectGroupDomain> findByType(GroupType type);

    /**
     * Finds groups by period.
     *
     * @param period the academic period
     * @return list of groups
     */
    List<SubjectGroupDomain> findByPeriod(AcademicPeriod period);


    /**
     * Finds groups with available places.
     *
     * @return list of groups with available places
     */
    List<SubjectGroupDomain> findGroupsWithAvailablePlaces();

    /**
     * Finds active groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return list of active groups
     */
    List<SubjectGroupDomain> findActiveBySubjectId(Long subjectId);

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
}
