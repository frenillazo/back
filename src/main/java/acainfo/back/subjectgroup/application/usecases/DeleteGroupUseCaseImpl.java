package acainfo.back.subjectgroup.application.usecases;

import acainfo.back.subjectgroup.application.ports.in.DeleteGroupUseCase;
import acainfo.back.subjectgroup.application.ports.out.GroupRepositoryPort;
import acainfo.back.subjectgroup.domain.exception.GroupNotFoundException;
import acainfo.back.subjectgroup.domain.model.SubjectGroupDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of DeleteGroupUseCase
 * Handles subjectGroup deletion operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeleteGroupUseCaseImpl implements DeleteGroupUseCase {

    private final GroupRepositoryPort groupRepository;

    @Override
    public void deleteGroup(Long id) {
        log.info("Deleting subjectGroup with ID: {}", id);

        SubjectGroupDomain subjectGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        // Cannot delete subjectGroup with enrolled students
        if (subjectGroup.getCurrentOccupancy() > 0) {
            throw new IllegalStateException(
                "Cannot delete subjectGroup with enrolled students. Current occupancy: " +
                subjectGroup.getCurrentOccupancy()
            );
        }

        groupRepository.deleteById(id);
        log.info("SubjectGroup deleted successfully: {}", id);
    }

    @Override
    public void cancelGroup(Long id) {
        log.info("Cancelling subjectGroup with ID: {}", id);

        SubjectGroupDomain subjectGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFoundException(id));

        subjectGroup.cancel();
        groupRepository.save(subjectGroup);

        log.info("SubjectGroup cancelled successfully: {}", id);
    }
}
