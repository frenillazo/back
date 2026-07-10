package com.acainfo.course.application.port.out;

import com.acainfo.course.application.dto.CourseFilters;
import com.acainfo.course.domain.model.Course;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Course persistence.
 * Defines the contract for Course repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface CourseRepositoryPort {

    /**
     * Save or update a group.
     *
     * @param group Domain group to persist
     * @return Persisted group with ID
     */
    Course save(Course group);

    /**
     * Find group by ID.
     *
     * @param id Group ID
     * @return Optional containing the group if found
     */
    Optional<Course> findById(Long id);

    /**
     * Find groups with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of groups matching filters
     */
    Page<Course> findWithFilters(CourseFilters filters);

    /**
     * Delete a group by ID.
     *
     * @param id Group ID
     */
    void delete(Long id);

    /**
     * Find all groups.
     *
     * @return List of all groups
     */
    List<Course> findAll();

    /**
     * Find groups by a list of IDs.
     *
     * @param ids List of group IDs
     * @return List of groups found
     */
    List<Course> findByIds(List<Long> ids);

    /**
     * Count active groups (OPEN or CLOSED) for a teacher.
     * Used to validate teacher deletion.
     *
     * @param teacherId Teacher ID
     * @return Count of active groups
     */
    long countActiveGroupsByTeacherId(Long teacherId);

    /**
     * Count active groups (OPEN or CLOSED) for a subject.
     * Used to validate subject archiving.
     *
     * @param subjectId Subject ID
     * @return Count of active groups
     */
    long countActiveGroupsBySubjectId(Long subjectId);

    /**
     * Count all groups for a subject (regardless of status).
     * Used for generating sequential group names.
     *
     * @param subjectId Subject ID
     * @return Total count of groups
     */
    long countAllBySubjectId(Long subjectId);

    /**
     * Find group by ID with a pessimistic write lock.
     * Locks the row for the duration of the transaction, preventing
     * concurrent modifications (used for enrollment approval and waiting list promotion).
     *
     * @param id Group ID
     * @return Optional containing the group if found
     */
    Optional<Course> findByIdForUpdate(Long id);
}
