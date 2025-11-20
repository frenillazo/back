package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.GroupRequest;

import java.util.List;

/**
 * Use case for retrieving group request information.
 */
public interface GetGroupRequestUseCase {

    /**
     * Gets a group request by ID.
     *
     * @param id the group request ID
     * @return the group request
     */
    GroupRequest getRequestById(Long id);

    /**
     * Gets all pending group requests.
     *
     * @return list of pending group requests
     */
    List<GroupRequest> getAllPendingRequests();

    /**
     * Gets group requests for a specific subject.
     *
     * @param subjectId the subject ID
     * @return list of group requests
     */
    List<GroupRequest> getRequestsBySubject(Long subjectId);

    /**
     * Gets group requests created by a student.
     *
     * @param studentId the student ID
     * @return list of group requests
     */
    List<GroupRequest> getRequestsByRequester(Long studentId);

    /**
     * Gets group requests supported by a student.
     *
     * @param studentId the student ID
     * @return list of group requests
     */
    List<GroupRequest> getRequestsSupportedByStudent(Long studentId);

    /**
     * Checks if a student supports a specific request.
     *
     * @param requestId the request ID
     * @param studentId the student ID
     * @return true if student supports, false otherwise
     */
    boolean isStudentSupporter(Long requestId, Long studentId);
}
