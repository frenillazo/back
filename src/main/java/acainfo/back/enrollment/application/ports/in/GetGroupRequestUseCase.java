package acainfo.back.enrollment.application.ports.in;

import acainfo.back.enrollment.domain.model.GroupRequestDomain;
import org.springframework.transaction.annotation.Transactional;

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
    GroupRequestDomain getRequestById(Long id);

    /**
     * Gets all pending group requests.
     *
     * @return list of pending group requests
     */
    List<GroupRequestDomain> getAllPendingRequests();

    /**
     * Gets group requests for a specific subject.
     *
     * @param subjectId the subject ID
     * @return list of group requests
     */
    List<GroupRequestDomain> getRequestsBySubject(Long subjectId);

    /**
     * Gets group requests created by a student.
     *
     * @param studentId the student ID
     * @return list of group requests
     */
    List<GroupRequestDomain> getRequestsByRequester(Long studentId);

    /**
     * Gets group requests supported by a student.
     *
     * @param studentId the student ID
     * @return list of group requests
     */
    List<GroupRequestDomain> getRequestsSupportedByStudent(Long studentId);

    /**
     * Gets the count of pending requests created by a student.
     *
     * @param studentId the student ID
     * @return count of pending requests
     */
    int getPendingRequestsByStudent(Long studentId);

    /**
     * Checks if a student supports a specific request.
     *
     * @param requestId the request ID
     * @param studentId the student ID
     * @return true if student supports, false otherwise
     */
    boolean isStudentSupporter(Long requestId, Long studentId);
}
