package acainfo.back.subject.application.usecases;

import acainfo.back.subject.application.ports.in.DeleteSubjectUseCase;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.SubjectDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of DeleteSubjectUseCase
 * Handles deletion and archiving of subjects
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeleteSubjectUseCaseImpl implements DeleteSubjectUseCase {

    private final SubjectRepositoryPort subjectRepository;

    @Override
    public void deleteSubject(Long id) {
        log.info("Deleting subject with ID: {}", id);

        // Verify subject exists
        SubjectDomain subject = subjectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Subject not found with ID: {}", id);
                    return new SubjectNotFoundException(id);
                });

        // TODO: Check if subject has active groups
        // This would require a dependency on SubjectGroupRepositoryPort
        // For now, we just delete

        subjectRepository.deleteById(id);

        log.info("Subject deleted successfully: {}", subject.getCode());
    }

    @Override
    public void archiveSubject(Long id) {
        log.info("Archiving subject with ID: {}", id);

        // Load existing subject
        SubjectDomain subject = subjectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Subject not found with ID: {}", id);
                    return new SubjectNotFoundException(id);
                });

        // Use domain logic to archive
        subject.archive();

        // Save archived subject
        subjectRepository.save(subject);

        log.info("Subject archived successfully: {}", subject.getCode());
    }
}
