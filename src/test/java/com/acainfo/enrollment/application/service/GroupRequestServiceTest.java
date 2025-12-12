package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.dto.CreateGroupRequestCommand;
import com.acainfo.enrollment.application.dto.GroupRequestFilters;
import com.acainfo.enrollment.application.dto.ProcessGroupRequestCommand;
import com.acainfo.enrollment.application.port.out.GroupRequestRepositoryPort;
import com.acainfo.enrollment.domain.exception.AlreadySupporterException;
import com.acainfo.enrollment.domain.exception.GroupRequestNotFoundException;
import com.acainfo.enrollment.domain.exception.InsufficientSupportersException;
import com.acainfo.enrollment.domain.exception.InvalidGroupRequestStateException;
import com.acainfo.enrollment.domain.model.GroupRequest;
import com.acainfo.enrollment.domain.model.GroupRequestStatus;
import com.acainfo.group.application.port.in.CreateGroupUseCase;
import com.acainfo.group.domain.model.GroupType;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.shared.factory.EnrollmentFactory;
import com.acainfo.shared.factory.GroupFactory;
import com.acainfo.shared.factory.SubjectFactory;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GroupRequestService with Mockito.
 * Tests group request creation, support, and admin processing logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GroupRequestService Tests")
class GroupRequestServiceTest {

    @Mock
    private GroupRequestRepositoryPort groupRequestRepositoryPort;

    @Mock
    private SubjectRepositoryPort subjectRepositoryPort;

    @Mock
    private CreateGroupUseCase createGroupUseCase;

    @InjectMocks
    private GroupRequestService groupRequestService;

    @Nested
    @DisplayName("Create Group Request Tests")
    class CreateGroupRequestTests {

        @Test
        @DisplayName("Should create group request with requester as first supporter")
        void create_WithValidCommand_CreatesRequest() {
            // Given
            CreateGroupRequestCommand command = new CreateGroupRequestCommand(
                    1L, 100L, GroupType.REGULAR_Q1, "Need more availability"
            );

            when(subjectRepositoryPort.findById(1L))
                    .thenReturn(Optional.of(SubjectFactory.defaultSubject()));
            when(groupRequestRepositoryPort.save(any(GroupRequest.class)))
                    .thenAnswer(invocation -> {
                        GroupRequest gr = invocation.getArgument(0);
                        return gr.toBuilder().id(1L).build();
                    });

            // When
            GroupRequest result = groupRequestService.create(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSubjectId()).isEqualTo(1L);
            assertThat(result.getRequesterId()).isEqualTo(100L);
            assertThat(result.getRequestedGroupType()).isEqualTo(GroupType.REGULAR_Q1);
            assertThat(result.getStatus()).isEqualTo(GroupRequestStatus.PENDING);
            assertThat(result.getSupporterIds()).contains(100L); // Requester is first supporter
            assertThat(result.getJustification()).isEqualTo("Need more availability");
            assertThat(result.getExpiresAt()).isNotNull();
            verify(groupRequestRepositoryPort).save(any(GroupRequest.class));
        }

        @Test
        @DisplayName("Should throw exception when subject not found")
        void create_WhenSubjectNotFound_ThrowsException() {
            // Given
            CreateGroupRequestCommand command = new CreateGroupRequestCommand(
                    999L, 100L, GroupType.REGULAR_Q1, "Justification"
            );

            when(subjectRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupRequestService.create(command))
                    .isInstanceOf(SubjectNotFoundException.class);

            verify(groupRequestRepositoryPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Add Supporter Tests")
    class AddSupporterTests {

        @Test
        @DisplayName("Should add supporter to pending request")
        void addSupporter_ToPendingRequest_AddsSuccessfully() {
            // Given
            GroupRequest pendingRequest = EnrollmentFactory.defaultGroupRequest();
            pendingRequest = pendingRequest.toBuilder().id(1L).build();

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingRequest));
            when(groupRequestRepositoryPort.save(any(GroupRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            GroupRequest result = groupRequestService.addSupporter(1L, 200L);

            // Then
            assertThat(result.getSupporterIds()).contains(200L);
            verify(groupRequestRepositoryPort).save(any(GroupRequest.class));
        }

        @Test
        @DisplayName("Should throw exception when already supporter")
        void addSupporter_WhenAlreadySupporter_ThrowsException() {
            // Given
            GroupRequest pendingRequest = EnrollmentFactory.defaultGroupRequest();
            Long existingSupporterId = pendingRequest.getRequesterId(); // Requester is already supporter

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingRequest));

            // When & Then
            assertThatThrownBy(() -> groupRequestService.addSupporter(1L, existingSupporterId))
                    .isInstanceOf(AlreadySupporterException.class);

            verify(groupRequestRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request not pending")
        void addSupporter_WhenNotPending_ThrowsException() {
            // Given
            GroupRequest approvedRequest = EnrollmentFactory.approvedGroupRequest();
            approvedRequest = approvedRequest.toBuilder().id(1L).build();

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(approvedRequest));

            // When & Then
            assertThatThrownBy(() -> groupRequestService.addSupporter(1L, 200L))
                    .isInstanceOf(InvalidGroupRequestStateException.class)
                    .hasMessageContaining("non-pending");

            verify(groupRequestRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request not found")
        void addSupporter_WhenNotFound_ThrowsException() {
            // Given
            when(groupRequestRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupRequestService.addSupporter(999L, 200L))
                    .isInstanceOf(GroupRequestNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Remove Supporter Tests")
    class RemoveSupporterTests {

        @Test
        @DisplayName("Should remove supporter from pending request")
        void removeSupporter_FromPendingRequest_RemovesSuccessfully() {
            // Given
            GroupRequest pendingRequest = EnrollmentFactory.groupRequestWithMinSupporters(1L, 100L)
                    .toBuilder().id(1L).build();
            Long requesterId = pendingRequest.getRequesterId();
            Long supporterToRemove = pendingRequest.getSupporterIds().stream()
                    .filter(id -> !id.equals(requesterId))
                    .findFirst()
                    .orElseThrow();

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingRequest));
            when(groupRequestRepositoryPort.save(any(GroupRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            GroupRequest result = groupRequestService.removeSupporter(1L, supporterToRemove);

            // Then
            assertThat(result.getSupporterIds()).doesNotContain(supporterToRemove);
            verify(groupRequestRepositoryPort).save(any(GroupRequest.class));
        }

        @Test
        @DisplayName("Should throw exception when trying to remove requester")
        void removeSupporter_WhenRequester_ThrowsException() {
            // Given
            GroupRequest pendingRequest = EnrollmentFactory.defaultGroupRequest();
            Long requesterId = pendingRequest.getRequesterId();

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingRequest));

            // When & Then
            assertThatThrownBy(() -> groupRequestService.removeSupporter(1L, requesterId))
                    .isInstanceOf(InvalidGroupRequestStateException.class)
                    .hasMessageContaining("requester");

            verify(groupRequestRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request not pending")
        void removeSupporter_WhenNotPending_ThrowsException() {
            // Given
            GroupRequest rejectedRequest = EnrollmentFactory.rejectedGroupRequest();
            rejectedRequest = rejectedRequest.toBuilder().id(1L).build();

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(rejectedRequest));

            // When & Then
            assertThatThrownBy(() -> groupRequestService.removeSupporter(1L, 200L))
                    .isInstanceOf(InvalidGroupRequestStateException.class)
                    .hasMessageContaining("non-pending");
        }
    }

    @Nested
    @DisplayName("Approve Group Request Tests")
    class ApproveGroupRequestTests {

        @Test
        @DisplayName("Should approve request with minimum supporters and create group")
        void approve_WithMinSupporters_ApprovesAndCreatesGroup() {
            // Given
            GroupRequest pendingRequest = EnrollmentFactory.groupRequestWithMinSupporters(1L, 100L);
            pendingRequest = pendingRequest.toBuilder().id(1L).build();
            SubjectGroup createdGroup = GroupFactory.builder().id(10L).buildDomain();

            ProcessGroupRequestCommand command = new ProcessGroupRequestCommand(1L, 50L, "Approved - high demand");

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingRequest));
            when(createGroupUseCase.create(any())).thenReturn(createdGroup);
            when(groupRequestRepositoryPort.save(any(GroupRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            GroupRequest result = groupRequestService.approve(command);

            // Then
            assertThat(result.getStatus()).isEqualTo(GroupRequestStatus.APPROVED);
            assertThat(result.getCreatedGroupId()).isEqualTo(10L);
            assertThat(result.getAdminResponse()).isEqualTo("Approved - high demand");
            assertThat(result.getProcessedByAdminId()).isEqualTo(50L);
            assertThat(result.getProcessedAt()).isNotNull();
            verify(createGroupUseCase).create(any());
            verify(groupRequestRepositoryPort).save(any(GroupRequest.class));
        }

        @Test
        @DisplayName("Should throw exception when insufficient supporters")
        void approve_WithInsufficientSupporters_ThrowsException() {
            // Given
            GroupRequest pendingRequest = EnrollmentFactory.defaultGroupRequest(); // Only 1 supporter
            pendingRequest = pendingRequest.toBuilder().id(1L).build();

            ProcessGroupRequestCommand command = new ProcessGroupRequestCommand(1L, 50L, "Approved");

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingRequest));

            // When & Then
            assertThatThrownBy(() -> groupRequestService.approve(command))
                    .isInstanceOf(InsufficientSupportersException.class);

            verify(createGroupUseCase, never()).create(any());
            verify(groupRequestRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when request not pending")
        void approve_WhenNotPending_ThrowsException() {
            // Given
            GroupRequest approvedRequest = EnrollmentFactory.approvedGroupRequest();
            approvedRequest = approvedRequest.toBuilder().id(1L).build();

            ProcessGroupRequestCommand command = new ProcessGroupRequestCommand(1L, 50L, "Approved again");

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(approvedRequest));

            // When & Then
            assertThatThrownBy(() -> groupRequestService.approve(command))
                    .isInstanceOf(InvalidGroupRequestStateException.class)
                    .hasMessageContaining("pending");
        }

        @Test
        @DisplayName("Should throw exception when request not found")
        void approve_WhenNotFound_ThrowsException() {
            // Given
            ProcessGroupRequestCommand command = new ProcessGroupRequestCommand(999L, 50L, "Approved");

            when(groupRequestRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupRequestService.approve(command))
                    .isInstanceOf(GroupRequestNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Reject Group Request Tests")
    class RejectGroupRequestTests {

        @Test
        @DisplayName("Should reject pending request")
        void reject_WhenPending_RejectsSuccessfully() {
            // Given
            GroupRequest pendingRequest = EnrollmentFactory.defaultGroupRequest();
            pendingRequest = pendingRequest.toBuilder().id(1L).build();

            ProcessGroupRequestCommand command = new ProcessGroupRequestCommand(1L, 50L, "Not enough demand");

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingRequest));
            when(groupRequestRepositoryPort.save(any(GroupRequest.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            GroupRequest result = groupRequestService.reject(command);

            // Then
            assertThat(result.getStatus()).isEqualTo(GroupRequestStatus.REJECTED);
            assertThat(result.getAdminResponse()).isEqualTo("Not enough demand");
            assertThat(result.getProcessedByAdminId()).isEqualTo(50L);
            assertThat(result.getProcessedAt()).isNotNull();
            assertThat(result.getCreatedGroupId()).isNull();
            verify(groupRequestRepositoryPort).save(any(GroupRequest.class));
        }

        @Test
        @DisplayName("Should throw exception when request not pending")
        void reject_WhenNotPending_ThrowsException() {
            // Given
            GroupRequest approvedRequest = EnrollmentFactory.approvedGroupRequest();
            approvedRequest = approvedRequest.toBuilder().id(1L).build();

            ProcessGroupRequestCommand command = new ProcessGroupRequestCommand(1L, 50L, "Rejected");

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(approvedRequest));

            // When & Then
            assertThatThrownBy(() -> groupRequestService.reject(command))
                    .isInstanceOf(InvalidGroupRequestStateException.class)
                    .hasMessageContaining("pending");
        }

        @Test
        @DisplayName("Should throw exception when request not found")
        void reject_WhenNotFound_ThrowsException() {
            // Given
            ProcessGroupRequestCommand command = new ProcessGroupRequestCommand(999L, 50L, "Rejected");

            when(groupRequestRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupRequestService.reject(command))
                    .isInstanceOf(GroupRequestNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Group Request Tests")
    class GetGroupRequestTests {

        @Test
        @DisplayName("Should return group request by ID")
        void getById_WhenExists_ReturnsGroupRequest() {
            // Given
            GroupRequest groupRequest = EnrollmentFactory.defaultGroupRequest();
            groupRequest = groupRequest.toBuilder().id(1L).build();

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(groupRequest));

            // When
            GroupRequest result = groupRequestService.getById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when not found")
        void getById_WhenNotFound_ThrowsException() {
            // Given
            when(groupRequestRepositoryPort.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> groupRequestService.getById(999L))
                    .isInstanceOf(GroupRequestNotFoundException.class);
        }

        @Test
        @DisplayName("Should return group requests with filters")
        void findWithFilters_ReturnsFilteredResults() {
            // Given
            GroupRequest request1 = EnrollmentFactory.defaultGroupRequest();
            GroupRequestFilters filters = new GroupRequestFilters(1L, null, null, GroupRequestStatus.PENDING, 0, 20, "createdAt", "DESC");
            Page<GroupRequest> page = new PageImpl<>(List.of(request1));

            when(groupRequestRepositoryPort.findWithFilters(filters)).thenReturn(page);

            // When
            Page<GroupRequest> result = groupRequestService.findWithFilters(filters);

            // Then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return supporters for group request")
        void getSupporters_ReturnsSetOfSupporters() {
            // Given
            GroupRequest groupRequest = EnrollmentFactory.groupRequestWithMinSupporters(1L, 100L);
            groupRequest = groupRequest.toBuilder().id(1L).build();

            when(groupRequestRepositoryPort.findById(1L)).thenReturn(Optional.of(groupRequest));

            // When
            var result = groupRequestService.getSupporters(1L);

            // Then
            assertThat(result).hasSize(8); // MIN_SUPPORTERS_FOR_APPROVAL
            assertThat(result).contains(100L); // Requester
        }
    }
}
