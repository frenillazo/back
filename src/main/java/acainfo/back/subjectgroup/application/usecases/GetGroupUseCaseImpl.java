package acainfo.back.subjectgroup.application.usecases;

import acainfo.back.subjectgroup.application.ports.in.GetGroupUseCase;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.AcademicPeriod;
import acainfo.back.subjectgroup.domain.model.GroupStatus;
import acainfo.back.subjectgroup.domain.model.GroupType;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GetGroupUseCase
 * Handles all subjectGroup retrieval operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetGroupUseCaseImpl implements GetGroupUseCase {

    private final GroupRepositoryPort groupRepository;

    @Override
    public SubjectGroupDomain getGroupById(Long id) {
        log.debug("Fetching subjectGroup by ID: {}", id);
        return groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));
    }

    @Override
    public List<SubjectGroupDomain> getAllGroups() {
        log.debug("Fetching all groups");
        return groupRepository.findAll();
    }

    @Override
    public List<SubjectGroupDomain> getActiveGroups() {
        log.debug("Fetching active groups");
        return groupRepository.findByStatus(GroupStatus.ACTIVO);
    }

    @Override
    public List<SubjectGroupDomain> getGroupsBySubject(Long subjectId) {
        log.debug("Fetching groups by subject ID: {}", subjectId);
        return groupRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<SubjectGroupDomain> getGroupsByTeacher(Long teacherId) {
        log.debug("Fetching groups by teacher ID: {}", teacherId);
        return groupRepository.findByTeacherId(teacherId);
    }

    @Override
    public List<SubjectGroupDomain> getGroupsByStatus(GroupStatus status) {
        log.debug("Fetching groups by status: {}", status);
        return groupRepository.findByStatus(status);
    }

    @Override
    public List<SubjectGroupDomain> getGroupsByType(GroupType type) {
        log.debug("Fetching groups by type: {}", type);
        return groupRepository.findByType(type);
    }

    @Override
    public List<SubjectGroupDomain> getGroupsByPeriod(AcademicPeriod period) {
        log.debug("Fetching groups by period: {}", period);
        return groupRepository.findByPeriod(period);
    }

    @Override
    public List<SubjectGroupDomain> getGroupsWithAvailablePlaces() {
        log.debug("Fetching groups with available places");
        return groupRepository.findGroupsWithAvailablePlaces();
    }
}
