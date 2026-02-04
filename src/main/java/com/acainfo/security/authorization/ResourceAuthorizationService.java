package com.acainfo.security.authorization;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.session.application.port.out.SessionRepositoryPort;
import com.acainfo.session.domain.model.Session;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for checking resource-level authorization.
 * Used by controllers to verify if a user can access specific resources.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceAuthorizationService {

    private final EnrollmentRepositoryPort enrollmentRepositoryPort;
    private final SessionRepositoryPort sessionRepositoryPort;

    /**
     * Check if a student can access a specific session.
     * Students can access sessions from groups they are enrolled in (active or pending).
     *
     * @param user The requesting user
     * @param sessionId The session ID to check
     * @return true if the user can access the session
     */
    public boolean canAccessSession(User user, Long sessionId) {
        // Admin and teachers can access any session
        if (user.isAdmin() || user.isTeacher()) {
            return true;
        }

        // Students can only access sessions from their enrolled groups
        if (user.isStudent()) {
            Session session = sessionRepositoryPort.findById(sessionId).orElse(null);
            if (session == null) {
                return false;
            }

            // Check if student is enrolled in the session's group
            Long groupId = session.getGroupId();
            if (groupId != null) {
                return isStudentEnrolledInGroup(user.getId(), groupId);
            }

            // For sessions without a group (e.g., SCHEDULING type), check subject enrollment
            Long subjectId = session.getSubjectId();
            if (subjectId != null) {
                return isStudentEnrolledInSubject(user.getId(), subjectId);
            }
        }

        return false;
    }

    /**
     * Check if a student can access sessions for a specific group.
     *
     * @param user The requesting user
     * @param groupId The group ID to check
     * @return true if the user can access the group's sessions
     */
    public boolean canAccessGroupSessions(User user, Long groupId) {
        // Admin and teachers can access any group's sessions
        if (user.isAdmin() || user.isTeacher()) {
            return true;
        }

        // Students can only access sessions from their enrolled groups
        if (user.isStudent()) {
            return isStudentEnrolledInGroup(user.getId(), groupId);
        }

        return false;
    }

    /**
     * Check if a student can access sessions for a specific subject.
     *
     * @param user The requesting user
     * @param subjectId The subject ID to check
     * @return true if the user can access the subject's sessions
     */
    public boolean canAccessSubjectSessions(User user, Long subjectId) {
        // Admin and teachers can access any subject's sessions
        if (user.isAdmin() || user.isTeacher()) {
            return true;
        }

        // Students can only access sessions from subjects they're enrolled in
        if (user.isStudent()) {
            return isStudentEnrolledInSubject(user.getId(), subjectId);
        }

        return false;
    }

    /**
     * Get the set of group IDs that a student is enrolled in.
     *
     * @param studentId The student's user ID
     * @return Set of group IDs
     */
    public Set<Long> getStudentEnrolledGroupIds(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepositoryPort.findByStudentIdAndStatusIn(
                studentId,
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.WAITING_LIST, EnrollmentStatus.PENDING_APPROVAL)
        );
        return enrollments.stream()
                .map(Enrollment::getGroupId)
                .collect(Collectors.toSet());
    }

    private boolean isStudentEnrolledInGroup(Long studentId, Long groupId) {
        return enrollmentRepositoryPort.existsActiveOrWaitingOrPendingEnrollment(studentId, groupId);
    }

    private boolean isStudentEnrolledInSubject(Long studentId, Long subjectId) {
        // Get all student's enrollments and check if any are for the subject
        List<Enrollment> enrollments = enrollmentRepositoryPort.findByStudentIdAndStatusIn(
                studentId,
                List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.WAITING_LIST, EnrollmentStatus.PENDING_APPROVAL)
        );

        // We need to check if any enrollment's group belongs to the subject
        // This requires loading the groups - for now, we'll do this in the service
        // A more efficient approach would be a direct query
        for (Enrollment enrollment : enrollments) {
            Session session = sessionRepositoryPort.findByGroupId(enrollment.getGroupId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (session != null && session.getSubjectId().equals(subjectId)) {
                return true;
            }
        }

        return false;
    }
}
