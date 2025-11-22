package acainfo.back.subject.application.usecases;

import acainfo.back.subject.application.ports.in.UpdateSubjectUseCase;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.exception.DuplicateSubjectCodeException;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.SubjectDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UpdateSubjectUseCase
 * Handles updates to existing subjects with validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UpdateSubjectUseCaseImpl implements UpdateSubjectUseCase {

    private final SubjectRepositoryPort subjectRepository;

    @Override
    public SubjectDomain updateSubject(Long id, SubjectDomain subject) {
        log.info("Updating subject with ID: {}", id);

        // Verify subject exists
        SubjectDomain existing = subjectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Subject not found with ID: {}", id);
                    return new SubjectNotFoundException(id);
                });

        // Business validation: check if new code conflicts with existing
        if (!existing.getCode().equals(subject.getCode()) &&
                subjectRepository.existsByCode(subject.getCode())) {
            log.warn("Attempt to update subject {} with duplicate code: {}",
                    id, subject.getCode());
            throw new DuplicateSubjectCodeException(subject.getCode());
        }

        // Create updated subject preserving ID and created date
        SubjectDomain updatedSubject = SubjectDomain.builder()
                .id(existing.getId())
                .code(subject.getCode())
                .name(subject.getName())
                .year(subject.getYear())
                .degree(subject.getDegree())
                .semester(subject.getSemester())
                .status(subject.getStatus())
                .description(subject.getDescription())
                .createdAt(existing.getCreatedAt())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        SubjectDomain savedSubject = subjectRepository.save(updatedSubject);

        log.info("Subject updated successfully: {}", savedSubject.getCode());

        return savedSubject;
    }
}
