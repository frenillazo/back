package acainfo.back.enrollment.application.ports.out;

import acainfo.back.enrollment.domain.model.GroupRequest;
import acainfo.back.enrollment.domain.model.GroupRequestStatus;

import java.util.List;
import java.util.Optional;

/**
 * Port for group request repository operations.
 * This interface defines the contract for group request persistence.
 */
public interface GroupRequestRepositoryPort {

    /**
     * Saves a group request (create or update).
     *
     * @param groupRequest the group request to save
     * @return the saved group request
     */
    GroupRequest save(GroupRequest groupRequest);

    /**
     * Finds a group request by ID.
     *
     * @param id the group request ID
     * @return Optional containing the group request if found
     */
    Optional<GroupRequest> findById(Long id);

    /**
     * Finds all group requests.
     *
     * @return list of all group requests
     */
    List<GroupRequest> findAll();

    /**
     * Finds group requests by subject ID.
     *
     * @param subjectId the subject ID
     * @return list of group requests
     */
    List<GroupRequest> findBySubjectId(Long subjectId);

    /**
     * Finds group requests by requester (student who created the request).
     *
     * @param requesterId the requester ID
     * @return list of group requests
     */
    List<GroupRequest> findByRequesterId(Long requesterId);

    /**
     * Finds group requests by status.
     *
     * @param status the request status
     * @return list of group requests
     */
    List<GroupRequest> findByStatus(GroupRequestStatus status);

    /**
     * Finds pending group requests for a subject.
     *
     * @param subjectId the subject ID
     * @return list of pending group requests
     */
    List<GroupRequest> findPendingBySubjectId(Long subjectId);

    /**
     * Finds a pending group request for a subject.
     * Only one pending request should exist per subject.
     *
     * @param subjectId the subject ID
     * @return Optional containing the pending request if found
     */
    Optional<GroupRequest> findPendingRequestBySubjectId(Long subjectId);

    /**
     * Checks if a pending request exists for a subject.
     *
     * @param subjectId the subject ID
     * @return true if pending request exists, false otherwise
     */
    boolean existsPendingRequestBySubjectId(Long subjectId);

    /**
     * Finds group requests where a student is a supporter.
     *
     * @param studentId the student ID
     * @return list of group requests
     */
    List<GroupRequest> findRequestsSupportedByStudent(Long studentId);

    /**
     * Checks if a student supports a specific request.
     *
     * @param requestId the request ID
     * @param studentId the student ID
     * @return true if student supports the request, false otherwise
     */
    boolean isStudentSupporter(Long requestId, Long studentId);

    /**
     * Deletes a group request by ID.
     *
     * @param id the group request ID
     */
    void deleteById(Long id);

    /**
     * Checks if a group request exists by ID.
     *
     * @param id the group request ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);
}
