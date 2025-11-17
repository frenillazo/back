package acainfo.back.application.ports.out;

import acainfo.back.domain.model.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Port for group repository operations.
 * This interface defines the contract for group persistence.
 */
public interface GroupRepositoryPort {

    /**
     * Saves a group (create or update).
     *
     * @param group the group to save
     * @return the saved group
     */
    Group save(Group group);

    /**
     * Finds a group by ID.
     *
     * @param id the group ID
     * @return Optional containing the group if found
     */
    Optional<Group> findById(Long id);

    /**
     * Finds all groups.
     *
     * @return list of all groups
     */
    List<Group> findAll();

    /**
     * Finds groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return list of groups
     */
    List<Group> findBySubjectId(Long subjectId);

    /**
     * Finds groups by teacher ID.
     *
     * @param teacherId the teacher ID
     * @return list of groups
     */
    List<Group> findByTeacherId(Long teacherId);

    /**
     * Finds groups by status.
     *
     * @param status the group status
     * @return list of groups
     */
    List<Group> findByStatus(GroupStatus status);

    /**
     * Finds groups by type.
     *
     * @param type the group type
     * @return list of groups
     */
    List<Group> findByType(GroupType type);

    /**
     * Finds groups by period.
     *
     * @param period the academic period
     * @return list of groups
     */
    List<Group> findByPeriod(AcademicPeriod period);


    /**
     * Finds groups with available places.
     *
     * @return list of groups with available places
     */
    List<Group> findGroupsWithAvailablePlaces();

    /**
     * Finds active groups by subject ID.
     *
     * @param subjectId the subject ID
     * @return list of active groups
     */
    List<Group> findActiveBySubjectId(Long subjectId);

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
     * @param status the group status
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
     * Deletes a group by ID.
     *
     * @param id the group ID
     */
    void deleteById(Long id);

    /**
     * Checks if a group exists by ID.
     *
     * @param id the group ID
     * @return true if exists, false otherwise
     */
    Boolean existsById(Long id);

    /**
     * Finds groups matching the given specification (dynamic filtering).
     *
     * @param spec the specification to filter by
     * @return list of matching groups
     */
    List<Group> findAll(Specification<Group> spec);
}
