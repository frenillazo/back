package acainfo.back.subject.application.usecases;

import acainfo.back.subject.application.ports.in.CreateSubjectUseCase;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.exception.DuplicateSubjectCodeException;
import acainfo.back.subject.domain.model.SubjectDomain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of CreateSubjectUseCase
 * Handles creation of new subjects with validation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateSubjectUseCaseImpl implements CreateSubjectUseCase {

    private final SubjectRepositoryPort subjectRepository;

    @Override
    public SubjectDomain createSubject(SubjectDomain subject) {
        log.info("Creating new subject with code: {}", subject.getCode());

        // Business validation: check if code already exists
        if (subjectRepository.existsByCode(subject.getCode())) {
            log.warn("Attempt to create subject with duplicate code: {}", subject.getCode());
            throw new DuplicateSubjectCodeException(subject.getCode());
        }

        // Save subject
        SubjectDomain savedSubject = subjectRepository.save(subject);

        log.info("Subject created successfully with ID: {} and code: {}",
                savedSubject.getId(), savedSubject.getCode());

        return savedSubject;
    }
}
