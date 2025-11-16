package acainfo.back.infrastructure.adapters.out;

import acainfo.back.application.ports.out.SubjectRepositoryPort;
import acainfo.back.domain.model.Degree;
import acainfo.back.domain.model.Subject;
import acainfo.back.domain.model.SubjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements SubjectRepositoryPort using Spring Data JPA.
 * This adapter bridges the application layer with the infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class SubjectRepositoryAdapter implements SubjectRepositoryPort {

    private final SubjectRepository subjectRepository;

    @Override
    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }

    @Override
    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    @Override
    public Optional<Subject> findByCode(String code) {
        return subjectRepository.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return subjectRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long excludeId) {
        // Check if exists and is not the excluded ID
        return subjectRepository.findByCode(code)
                .map(subject -> !subject.getId().equals(excludeId))
                .orElse(false);
    }

    @Override
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Override
    public List<Subject> findByStatus(SubjectStatus status) {
        return subjectRepository.findByStatus(status);
    }

    @Override
    public List<Subject> findByDegree(Degree degree) {
        return subjectRepository.findByDegree(degree);
    }

    @Override
    public List<Subject> findByDegreeAndYear(Degree degree, Integer year) {
        return subjectRepository.findByDegreeAndYear(degree, year);
    }

    @Override
    public List<Subject> searchByCodeOrName(String searchTerm) {
        return subjectRepository.searchByCodeOrName(searchTerm);
    }

    @Override
    public void deleteById(Long id) {
        subjectRepository.deleteById(id);
    }

    @Override
    public long countByStatus(SubjectStatus status) {
        return subjectRepository.countByStatus(status);
    }
}
