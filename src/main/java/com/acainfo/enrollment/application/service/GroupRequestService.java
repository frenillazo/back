package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.dto.CreateGroupRequestCommand;
import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.application.dto.ProcessGroupRequestCommand;
import com.acainfo.enrollment.application.port.in.CreateGroupRequestUseCase;
import com.acainfo.enrollment.application.port.in.GetGroupRequestUseCase;
import com.acainfo.enrollment.application.port.in.ProcessGroupRequestUseCase;
import com.acainfo.enrollment.application.port.in.SupportGroupRequestUseCase;
import com.acainfo.enrollment.application.port.out.GroupRequestRepositoryPort;
import com.acainfo.enrollment.domain.exception.AlreadySupporterException;
import com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException;
import com.acainfo.enrollment.domain.exception.InsufficientSupportersException;
import com.acainfo.enrollment.domain.exception.InvalidGroupRequestStateException;
import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.group.application.dto.CreateGroupCommand;
import com.acainfo.group.application.port.in.CreateGroupUseCase;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Service implementing group request use cases.
 * Handles creation, support, and admin processing of group requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupRequestService implements
        CreateGroupRequestUseCase,
        SupportGroupRequestUseCase,
        ProcessGroupRequestUseCase,
        GetGroupRequestUseCase {

    private static final int DEFAULT_EXPIRATION_DAYS = 30;

    private final GroupRequestRepositoryPort groupRequestRepositoryPort;
    private final SubjectRepositoryPort subjectRepositoryPort;
    private final CreateGroupUseCase createGroupUseCase;

    // ==================== CreateGroupRequestUseCase ====================

    @Override
    @Transactional
    public GroupRequest create(CreateGroupRequestCommand command) {
        log.info("Creating group request for subject {} by requester {}",
                command.subjectId(), command.requesterId());

        // Validate subject exists
        subjectRepositoryPort.findById(command.subjectId())
                .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));

        // Create request with requester as first supporter
        Set<Long> supporters = new HashSet<>();
        supporters.add(command.requesterId());

        GroupRequest groupRequest = GroupRequest.builder()
                .subjectId(command.subjectId())
                .requesterId(command.requesterId())
                .requestedGroupType(command.requestedGroupType())
                .status(GroupRequestStatus.PENDING)
                .supporterIds(supporters)
                .justification(command.justification())
                .expiresAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRATION_DAYS))
                .build();

        GroupRequest savedRequest = groupRequestRepositoryPort.save(groupRequest);

        log.info("Group request created successfully: ID {}", savedRequest.getId());
        return savedRequest;
    }

    // ==================== SupportGroupRequestUseCase ====================

    @Override
    @Transactional
    public GroupRequest addSupporter(Long groupRequestId, Long studentId) {
        log.info("Adding supporter {} to group request {}", studentId, groupRequestId);

        GroupRequest groupRequest = getById(groupRequestId);

        if (!groupRequest.isPending()) {
            throw new InvalidGroupRequestStateException(
                    "Cannot add supporters to a non-pending request. Current status: " + groupRequest.getStatus()
            );
        }

        if (groupRequest.isSupporter(studentId)) {
            throw new AlreadySupporterException(studentId, groupRequestId);
        }

        groupRequest.getSupporterIds().add(studentId);
        GroupRequest savedRequest = groupRequestRepositoryPort.save(groupRequest);

        log.info("Supporter {} added to group request {}. Total supporters: {}",
                studentId, groupRequestId, savedRequest.getSupporterCount());

        return savedRequest;
    }

    @Override
    @Transactional
    public GroupRequest removeSupporter(Long groupRequestId, Long studentId) {
        log.info("Removing supporter {} from group request {}", studentId, groupRequestId);

        GroupRequest groupRequest = getById(groupRequestId);

        if (!groupRequest.isPending()) {
            throw new InvalidGroupRequestStateException(
                    "Cannot remove supporters from a non-pending request. Current status: " + groupRequest.getStatus()
            );
        }

        // Cannot remove the requester
        if (groupRequest.getRequesterId().equals(studentId)) {
            throw new InvalidGroupRequestStateException(
                    "Cannot remove the requester from supporters"
            );
        }

        groupRequest.getSupporterIds().remove(studentId);
        GroupRequest savedRequest = groupRequestRepositoryPort.save(groupRequest);

        log.info("Supporter {} removed from group request {}. Total supporters: {}",
                studentId, groupRequestId, savedRequest.getSupporterCount());

        return savedRequest;
    }

    // ==================== ProcessGroupRequestUseCase ====================

    @Override
    @Transactional
    public GroupRequest approve(ProcessGroupRequestCommand command) {
        log.info("Approving group request {} by admin {}", command.groupRequestId(), command.adminId());

        GroupRequest groupRequest = getById(command.groupRequestId());

        if (!groupRequest.isPending()) {
            throw new InvalidGroupRequestStateException(
                    "Can only approve pending requests. Current status: " + groupRequest.getStatus()
            );
        }

        if (!groupRequest.hasMinimumSupporters()) {
            throw new InsufficientSupportersException(groupRequest.getSupporterCount());
        }

        // Create the new group
        CreateGroupCommand createGroupCommand = new CreateGroupCommand(
                groupRequest.getSubjectId(),
                null, // teacherId will be assigned later by admin
                groupRequest.getRequestedGroupType(),
                null,  // default capacity
                null
        );

        SubjectGroup createdGroup = createGroupUseCase.create(createGroupCommand);

        // Update group request
        groupRequest.setStatus(GroupRequestStatus.APPROVED);
        groupRequest.setCreatedGroupId(createdGroup.getId());
        groupRequest.setAdminResponse(command.adminResponse());
        groupRequest.setProcessedByAdminId(command.adminId());
        groupRequest.setProcessedAt(LocalDateTime.now());

        GroupRequest savedRequest = groupRequestRepositoryPort.save(groupRequest);

        log.info("Group request {} approved. Created group ID: {}", command.groupRequestId(), createdGroup.getId());
        return savedRequest;
    }

    @Override
    @Transactional
    public GroupRequest reject(ProcessGroupRequestCommand command) {
        log.info("Rejecting group request {} by admin {}", command.groupRequestId(), command.adminId());

        GroupRequest groupRequest = getById(command.groupRequestId());

        if (!groupRequest.isPending()) {
            throw new InvalidGroupRequestStateException(
                    "Can only reject pending requests. Current status: " + groupRequest.getStatus()
            );
        }

        groupRequest.setStatus(GroupRequestStatus.REJECTED);
        groupRequest.setAdminResponse(command.adminResponse());
        groupRequest.setProcessedByAdminId(command.adminId());
        groupRequest.setProcessedAt(LocalDateTime.now());

        GroupRequest savedRequest = groupRequestRepositoryPort.save(groupRequest);

        log.info("Group request {} rejected", command.groupRequestId());
        return savedRequest;
    }

    // ==================== GetGroupRequestUseCase ====================

    @Override
    @Transactional(readOnly = true)
    public GroupRequest getById(Long id) {
        log.debug("Getting group request by ID: {}", id);
        return groupRequestRepositoryPort.findById(id)
                .orElseThrow(() -> new GroupRequestNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupRequest> findWithFilters(GroupRequestFilters filters) {
        log.debug("Finding group requests with filters: subjectId={}, requesterId={}, status={}",
                filters.subjectId(), filters.requesterId(), filters.status());
        return groupRequestRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getSupporters(Long groupRequestId) {
        log.debug("Getting supporters for group request: {}", groupRequestId);
        GroupRequest groupRequest = getById(groupRequestId);
        return groupRequest.getSupporterIds();
    }
}
