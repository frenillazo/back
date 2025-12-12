package com.acainfo.enrollment.application.port.out;

import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.group.domain.model.GroupType;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port for GroupRequest persistence.
 * Defines the contract for GroupRequest repository operations.
 * Implementations will be in infrastructure layer (adapters).
 */
public interface GroupRequestRepositoryPort {

    /**
     * Save or update a group request.
     *
     * @param groupRequest Domain group request to persist
     * @return Persisted group request with ID
     */
    GroupRequest save(GroupRequest groupRequest);

    /**
     * Find group request by ID.
     *
     * @param id Group request ID
     * @return Optional containing the group request if found
     */
    Optional<GroupRequest> findById(Long id);

    /**
     * Find group requests with dynamic filters (Criteria Builder).
     *
     * @param filters Filter criteria
     * @return Page of group requests matching filters
     */
    Page<GroupRequest> findWithFilters(GroupRequestFilters filters);

    /**
     * Find all group requests for a subject.
     *
     * @param subjectId Subject ID
     * @return List of group requests
     */
    List<GroupRequest> findBySubjectId(Long subjectId);

    /**
     * Find all group requests by requester.
     *
     * @param requesterId Requester ID
     * @return List of group requests
     */
    List<GroupRequest> findByRequesterId(Long requesterId);

    /**
     * Find group requests by status.
     *
     * @param status Group request status
     * @return List of group requests
     */
    List<GroupRequest> findByStatus(GroupRequestStatus status);

    /**
     * Find pending group requests for a subject with a specific type.
     *
     * @param subjectId Subject ID
     * @param type Requested group type
     * @return List of pending group requests
     */
    List<GroupRequest> findPendingBySubjectIdAndType(Long subjectId, GroupType type);

    /**
     * Find expired pending requests (for scheduled cleanup).
     *
     * @param dateTime Reference datetime
     * @return List of expired pending requests
     */
    List<GroupRequest> findExpiredPendingRequests(LocalDateTime dateTime);

    /**
     * Delete a group request by ID.
     *
     * @param id Group request ID
     */
    void delete(Long id);
}
