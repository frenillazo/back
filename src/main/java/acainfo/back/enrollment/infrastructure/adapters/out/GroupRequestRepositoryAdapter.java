package acainfo.back.enrollment.infrastructure.adapters.out;

import acainfo.back.enrollment.application.ports.out.GroupRequestRepositoryPort;
import acainfo.back.enrollment.domain.model.GroupRequest;
import acainfo.back.enrollment.domain.model.GroupRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements GroupRequestRepositoryPort using Spring Data JPA.
 * This adapter bridges the application layer with the infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class GroupRequestRepositoryAdapter implements GroupRequestRepositoryPort {

    private final GroupRequestRepository groupRequestRepository;

    @Override
    public GroupRequest save(GroupRequest groupRequest) {
        return groupRequestRepository.save(groupRequest);
    }

    @Override
    public Optional<GroupRequest> findById(Long id) {
        return groupRequestRepository.findById(id);
    }

    @Override
    public List<GroupRequest> findAll() {
        return groupRequestRepository.findAll();
    }

    @Override
    public List<GroupRequest> findBySubjectId(Long subjectId) {
        return groupRequestRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<GroupRequest> findByRequesterId(Long requesterId) {
        return groupRequestRepository.findByRequesterId(requesterId);
    }

    @Override
    public List<GroupRequest> findByStatus(GroupRequestStatus status) {
        return groupRequestRepository.findByStatus(status);
    }

    @Override
    public List<GroupRequest> findPendingBySubjectId(Long subjectId) {
        return groupRequestRepository.findPendingBySubjectId(subjectId);
    }

    @Override
    public Optional<GroupRequest> findPendingRequestBySubjectId(Long subjectId) {
        return groupRequestRepository.findPendingRequestBySubjectId(subjectId);
    }

    @Override
    public boolean existsPendingRequestBySubjectId(Long subjectId) {
        return groupRequestRepository.existsPendingRequestBySubjectId(subjectId);
    }

    @Override
    public List<GroupRequest> findRequestsSupportedByStudent(Long studentId) {
        return groupRequestRepository.findRequestsSupportedByStudent(studentId);
    }

    @Override
    public boolean isStudentSupporter(Long requestId, Long studentId) {
        return groupRequestRepository.isStudentSupporter(requestId, studentId);
    }

    @Override
    public int countPendingByStudentId(Long studentId) {
        return groupRequestRepository.countPendingByStudentId(studentId);
    }

    @Override
    public void deleteById(Long id) {
        groupRequestRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return groupRequestRepository.existsById(id);
    }
}
