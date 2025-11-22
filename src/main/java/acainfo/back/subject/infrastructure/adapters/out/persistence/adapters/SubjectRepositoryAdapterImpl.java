package acainfo.back.subject.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.subject.application.ports.out.SubjectRepositoryPort;
import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectDomain;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import acainfo.back.subject.infrastructure.adapters.out.persistence.mappers.SubjectJpaMapper;
import acainfo.back.subject.infrastructure.adapters.out.persistence.repositories.SubjectJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Infrastructure Adapter for Subject persistence
 * Implements SubjectRepositoryPort
 * Uses SubjectJpaMapper to convert between Domain and JPA entities
 * Delegates to SubjectJpaRepository for database operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubjectRepositoryAdapterImpl implements SubjectRepositoryPort {

    private final SubjectJpaRepository jpaRepository;
    private final SubjectJpaMapper mapper;

    @Override
    public SubjectDomain save(SubjectDomain subject) {
        log.debug("Saving subject: {}", subject.getCode());

        SubjectJpaEntity jpaEntity = mapper.toJpaEntity(subject);
        SubjectJpaEntity saved = jpaRepository.save(jpaEntity);

        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SubjectDomain> findById(Long id) {
        log.debug("Finding subject by ID: {}", id);

        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SubjectDomain> findByCode(String code) {
        log.debug("Finding subject by code: {}", code);

        return jpaRepository.findByCode(code)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        log.debug("Checking existence by code: {}", code);

        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long excludeId) {
        log.debug("Checking existence by code: {} excluding ID: {}", code, excludeId);

        return jpaRepository.existsByCodeAndIdNot(code, excludeId);
    }

    @Override
    public List<SubjectDomain> findAll() {
        log.debug("Finding all subjects");

        return mapper.toDomains(jpaRepository.findAll());
    }

    @Override
    public List<SubjectDomain> findByStatus(SubjectStatus status) {
        log.debug("Finding subjects by status: {}", status);

        return mapper.toDomains(jpaRepository.findByStatus(status));
    }

    @Override
    public List<SubjectDomain> findByDegree(Degree degree) {
        log.debug("Finding subjects by degree: {}", degree);

        return mapper.toDomains(jpaRepository.findByDegree(degree));
    }

    @Override
    public List<SubjectDomain> findByDegreeAndYear(Degree degree, Integer year) {
        log.debug("Finding subjects by degree: {} and year: {}", degree, year);

        return mapper.toDomains(jpaRepository.findByDegreeAndYear(degree, year));
    }

    @Override
    public List<SubjectDomain> searchByCodeOrName(String searchTerm) {
        log.debug("Searching subjects by code or name: {}", searchTerm);

        return mapper.toDomains(jpaRepository.searchByCodeOrName(searchTerm));
    }

    @Override
    public void deleteById(Long id) {
        log.debug("Deleting subject by ID: {}", id);

        jpaRepository.deleteById(id);
    }

    @Override
    public long countByStatus(SubjectStatus status) {
        log.debug("Counting subjects by status: {}", status);

        return jpaRepository.countByStatus(status);
    }
}
