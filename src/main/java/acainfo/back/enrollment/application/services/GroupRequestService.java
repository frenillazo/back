package acainfo.back.enrollment.application.services;

import acainfo.back.enrollment.application.ports.in.CreateGroupRequestUseCase;
import acainfo.back.enrollment.application.ports.in.SupportGroupRequestUseCase;
import acainfo.back.enrollment.application.ports.in.ManageGroupRequestUseCase;
import acainfo.back.enrollment.application.ports.in.GetGroupRequestUseCase;
import acainfo.back.enrollment.application.ports.out.GroupRequestRepositoryPort;
import acainfo.back.enrollment.domain.exception.DuplicateGroupRequestException;
import acainfo.back.enrollment.domain.exception.GroupRequestNotFoundException;
import acainfo.back.enrollment.domain.model.GroupRequestDomain;
import acainfo.back.enrollment.domain.model.GroupRequestStatus;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.exception.UserNotFoundException;
import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.SubjectDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for group request management.
 * Implements all group request use cases with business logic and validations.
 *
 * Business Rules:
 * 1. Only students can create and support group requests
 * 2. Only one pending request allowed per subject
 * 3. Minimum 8 supporters required for admin consideration
 * 4. Requester is automatically added as first supporter
 * 5. Only admins can approve/reject requests
 * 6. Once request is resolved, it cannot be modified
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GroupRequestService implements
        CreateGroupRequestUseCase,
        SupportGroupRequestUseCase,
        ManageGroupRequestUseCase,
        GetGroupRequestUseCase {

    private final GroupRequestRepositoryPort groupRequestRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final UserRepositoryPort userRepository;

    // ==================== CREATE ====================

    @Override
    public GroupRequestDomain createGroupRequest(Long subjectId, Long requesterId, String comments) {
        log.info("Creating group request for subject {} by student {}", subjectId, requesterId);

        // Validate student exists and has STUDENT role
        UserDomain requester = validateStudent(requesterId);

        // Validate subject exists
        SubjectDomain subject = validateSubject(subjectId);

        // Check if pending request already exists for this subject
        if (groupRequestRepository.existsPendingRequestBySubjectId(subjectId)) {
            throw new DuplicateGroupRequestException(subjectId);
        }

        // Create group request domain
        GroupRequestDomain groupRequest = GroupRequestDomain.builder()
                .subjectId(subject.getId())
                .requestedById(requester.getId())
                .status(GroupRequestStatus.PENDIENTE)
                .comments(comments)
                .requestedAt(java.time.LocalDateTime.now())
                .build();

        // Add requester as first supporter
        groupRequest = groupRequest.addSupporter(requester.getId());

        GroupRequestDomain savedRequest = groupRequestRepository.save(groupRequest);

        log.info("Group request created successfully with ID: {}", savedRequest.getId());

        // TODO: Send notification to admin if minimum supporters reached
        // if (savedRequest.hasMinimumSupporters()) {
        //     notificationService.notifyAdminGroupRequestReady(savedRequest);
        // }

        return savedRequest;
    }

    // ==================== SUPPORT ====================

    @Override
    @Transactional
    public GroupRequestDomain supportRequest(Long requestId, Long studentId) {
        log.info("Student {} supporting group request {}", studentId, requestId);

        // Validate student exists and has STUDENT role
        UserDomain student = validateStudent(studentId);

        // Get group request
        GroupRequestDomain groupRequest = groupRequestRepository.findById(requestId)
                .orElseThrow(() -> new GroupRequestNotFoundException(requestId));

        // Validate request is pending
        if (!groupRequest.isPending()) {
            throw new IllegalStateException("Only pending requests can receive new supporters");
        }

        // Check if already supporting
        if (groupRequest.isSupporter(student.getId())) {
            throw new IllegalStateException("Student is already supporting this request");
        }

        // Add supporter
        GroupRequestDomain updated = groupRequest.addSupporter(student.getId());
        GroupRequestDomain updatedRequest = groupRequestRepository.save(updated);

        log.info("Student {} added as supporter. Total supporters: {}",
            studentId, updatedRequest.getSupportersCount());

        // TODO: Notify admin if minimum reached
        // if (updatedRequest.hasMinimumSupporters()) {
        //     notificationService.notifyAdminGroupRequestReady(updatedRequest);
        // }

        return updatedRequest;
    }

    @Override
    @Transactional
    public GroupRequestDomain unsupportRequest(Long requestId, Long studentId) {
        log.info("Student {} removing support from group request {}", studentId, requestId);

        // Validate student
        UserDomain student = validateStudent(studentId);

        // Get group request
        GroupRequestDomain groupRequest = groupRequestRepository.findById(requestId)
                .orElseThrow(() -> new GroupRequestNotFoundException(requestId));

        // Validate request is pending
        if (!groupRequest.isPending()) {
            throw new IllegalStateException("Cannot remove support from resolved requests");
        }

        // Check if student is the requester (cannot remove themselves)
        if (groupRequest.isRequester(student.getId())) {
            throw new IllegalStateException("Requester cannot remove their support. Consider deleting the request instead.");
        }

        // Check if is supporter
        if (!groupRequest.isSupporter(student.getId())) {
            throw new IllegalStateException("Student is not supporting this request");
        }

        // Remove supporter
        GroupRequestDomain updated = groupRequest.removeSupporter(student.getId());
        GroupRequestDomain updatedRequest = groupRequestRepository.save(updated);

        log.info("Student {} removed as supporter. Total supporters: {}",
            studentId, updatedRequest.getSupportersCount());

        return updatedRequest;
    }

    // ==================== MANAGE (APPROVE/REJECT) ====================

    @Override
    @Transactional
    public GroupRequestDomain approveRequest(Long requestId, Long adminId) {
        log.info("Admin {} approving group request {}", adminId, requestId);

        // Validate admin
        validateAdmin(adminId);

        // Get group request
        GroupRequestDomain groupRequest = groupRequestRepository.findById(requestId)
                .orElseThrow(() -> new GroupRequestNotFoundException(requestId));

        // Validate has minimum supporters
        if (!groupRequest.hasMinimumSupporters()) {
            throw new IllegalStateException(
                String.format("Request does not have minimum supporters. Current: %d, Required: %d",
                    groupRequest.getSupportersCount(), GroupRequestDomain.MINIMUM_SUPPORTERS)
            );
        }

        // Approve request
        GroupRequestDomain approved = groupRequest.approve();
        GroupRequestDomain approvedRequest = groupRequestRepository.save(approved);

        log.info("Group request {} approved successfully", requestId);

        // TODO: Create new SubjectGroup for this subject
        // subjectGroupService.createGroupFromRequest(approvedRequest);

        // TODO: Notify requester and supporters
        // notificationService.notifyGroupRequestApproved(approvedRequest);

        return approvedRequest;
    }

    @Override
    @Transactional
    public GroupRequestDomain rejectRequest(Long requestId, Long adminId, String reason) {
        log.info("Admin {} rejecting group request {}", adminId, requestId);

        // Validate admin
        validateAdmin(adminId);

        // Validate reason provided
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        // Get group request
        GroupRequestDomain groupRequest = groupRequestRepository.findById(requestId)
                .orElseThrow(() -> new GroupRequestNotFoundException(requestId));

        // Reject request
        GroupRequestDomain rejected = groupRequest.reject(reason);
        GroupRequestDomain rejectedRequest = groupRequestRepository.save(rejected);

        log.info("Group request {} rejected. Reason: {}", requestId, reason);

        // TODO: Notify requester and supporters
        // notificationService.notifyGroupRequestRejected(rejectedRequest);

        return rejectedRequest;
    }

    // ==================== GET ====================

    @Override
    @Transactional(readOnly = true)
    public GroupRequestDomain getRequestById(Long id) {
        log.debug("Fetching group request by ID: {}", id);
        return groupRequestRepository.findById(id)
                .orElseThrow(() -> new GroupRequestNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupRequestDomain> getAllPendingRequests() {
        log.debug("Fetching all pending group requests");
        return groupRequestRepository.findByStatus(GroupRequestStatus.PENDIENTE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupRequestDomain> getRequestsBySubject(Long subjectId) {
        log.debug("Fetching group requests for subject: {}", subjectId);
        return groupRequestRepository.findBySubjectId(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupRequestDomain> getRequestsByRequester(Long studentId) {
        log.debug("Fetching group requests created by student: {}", studentId);
        return groupRequestRepository.findByRequesterId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupRequestDomain> getRequestsSupportedByStudent(Long studentId) {
        log.debug("Fetching group requests supported by student: {}", studentId);
        return groupRequestRepository.findRequestsSupportedByStudent(studentId);
    }

    @Transactional
    @Override
    public int getPendingRequestsByStudent(Long studentId) {
        log.debug("Counting pending group requests created by student: {}", studentId);
        return groupRequestRepository.countPendingByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentSupporter(Long requestId, Long studentId) {
        return groupRequestRepository.isStudentSupporter(requestId, studentId);
    }

    // ==================== PRIVATE VALIDATION METHODS ====================

    /**
     * Validates that the user exists and has STUDENT role.
     */
    private UserDomain validateStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID is required");
        }

        UserDomain student = userRepository.findById(studentId)
                .orElseThrow(() -> new UserNotFoundException(studentId));

        if (!student.hasRole(RoleType.STUDENT)) {
            throw new IllegalArgumentException(
                "User " + studentId + " is not a student. Only users with STUDENT role can create/support requests."
            );
        }

        if (!student.isActive()) {
            throw new IllegalStateException("Student account is not active");
        }

        return student;
    }

    /**
     * Validates that the user exists and has ADMIN role.
     */
    private UserDomain validateAdmin(Long adminId) {
        if (adminId == null) {
            throw new IllegalArgumentException("Admin ID is required");
        }

        UserDomain admin = userRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException(adminId));

        if (!admin.hasRole(RoleType.ADMIN)) {
            throw new IllegalArgumentException(
                "User " + adminId + " is not an admin. Only admins can approve/reject requests."
            );
        }
    }

    /**
     * Validates that the subject exists.
     */
    private SubjectDomain validateSubject(Long subjectId) {
        if (subjectId == null) {
            throw new IllegalArgumentException("Subject ID is required");
        }

        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException(subjectId));
    }
}
