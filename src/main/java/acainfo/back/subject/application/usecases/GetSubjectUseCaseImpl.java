package acainfo.back.subject.application.usecases;

import acainfo.back.subject.application.ports.in.GetSubjectUseCase;
import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.exception.SubjectNotFoundException;
import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subject.domain.model.SubjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GetSubjectUseCase
 * Handles all retrieval operations for subjects
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetSubjectUseCaseImpl implements GetSubjectUseCase {

    private final SubjectRepositoryPort subjectRepository;

    @Override
    public SubjectDomain getSubjectById(Long id) {
        log.debug("Getting subject by ID: {}", id);

        return subjectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Subject not found with ID: {}", id);
                    return new SubjectNotFoundException(id);
                });
    }

    @Override
    public SubjectDomain getSubjectByCode(String code) {
        log.debug("Getting subject by code: {}", code);

        return subjectRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.error("Subject not found with code: {}", code);
                    return new SubjectNotFoundException("Subject not found with code: " + code);
                });
    }

    @Override
    public List<SubjectDomain> getAllSubjects() {
        log.debug("Getting all subjects");

        return subjectRepository.findAll();
    }

    @Override
    public List<SubjectDomain> getActiveSubjects() {
        log.debug("Getting active subjects");

        return subjectRepository.findByStatus(SubjectStatus.ACTIVO);
    }

    @Override
    public List<SubjectDomain> getSubjectsByDegree(Degree degree) {
        log.debug("Getting subjects by degree: {}", degree);

        return subjectRepository.findByDegree(degree);
    }

    @Override
    public List<SubjectDomain> getSubjectsByStatus(SubjectStatus status) {
        log.debug("Getting subjects by status: {}", status);

        return subjectRepository.findByStatus(status);
    }

    @Override
    public List<SubjectDomain> getSubjectsByDegreeAndYear(Degree degree, Integer year) {
        log.debug("Getting subjects by degree: {} and year: {}", degree, year);

        return subjectRepository.findByDegreeAndYear(degree, year);
    }

    @Override
    public List<SubjectDomain> searchSubjects(String searchTerm) {
        log.debug("Searching subjects with term: {}", searchTerm);

        return subjectRepository.searchByCodeOrName(searchTerm);
    }
}
