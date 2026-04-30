package com.acainfo.material.infrastructure.adapter.out.persistence.repository;

import com.acainfo.material.application.dto.MaterialFilters;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialJpaEntity;
import com.acainfo.material.infrastructure.adapter.out.persistence.specification.MaterialSpecifications;
import com.acainfo.material.infrastructure.mapper.MaterialPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing MaterialRepositoryPort.
 * Bridges domain layer with JPA persistence.
 */
@Repository
@RequiredArgsConstructor
public class MaterialRepositoryAdapter implements MaterialRepositoryPort {

    private final JpaMaterialRepository jpaRepository;
    private final MaterialPersistenceMapper mapper;

    @Override
    public Material save(Material material) {
        MaterialJpaEntity entity = mapper.toJpaEntity(material);
        MaterialJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Material> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Material> findWithFilters(MaterialFilters filters) {
        Pageable pageable = createPageable(filters);

        Specification<MaterialJpaEntity> spec = MaterialSpecifications.fromFilters(
                filters.subjectId(),
                filters.uploadedById(),
                filters.fileExtension(),
                filters.searchTerm()
        );

        return jpaRepository.findAll(spec, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Material> findBySubjectId(Long subjectId) {
        return mapper.toDomainList(
                jpaRepository.findBySubjectIdOrderByUploadedAtDesc(subjectId)
        );
    }

    @Override
    public List<Material> findByUploadedById(Long uploadedById) {
        return mapper.toDomainList(
                jpaRepository.findByUploadedByIdOrderByUploadedAtDesc(uploadedById)
        );
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Material> findRecentBySubjectIds(List<Long> subjectIds, int days) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return List.of();
        }
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return mapper.toDomainList(
                jpaRepository.findRecentBySubjectIds(subjectIds, since)
        );
    }

    @Override
    public List<Material> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.toDomainList(jpaRepository.findAllById(ids));
    }

    @Override
    public int batchUpdateDownloadDisabled(List<Long> ids, boolean disabled, LocalDateTime enabledAt) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        // When disabled=true, enabledAt is ignored by the query but JPA forbids null params -> use a safe sentinel.
        LocalDateTime safe = enabledAt != null ? enabledAt : LocalDateTime.now();
        return jpaRepository.batchUpdateDownloadDisabled(ids, disabled, safe);
    }

    @Override
    public int batchUpdateVisibility(List<Long> ids, boolean visible, LocalDateTime enabledAt) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        LocalDateTime safe = enabledAt != null ? enabledAt : LocalDateTime.now();
        return jpaRepository.batchUpdateVisibility(ids, visible, safe);
    }

    @Override
    public List<Material> findExpiredActiveMaterials(int daysThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysThreshold);
        return mapper.toDomainList(jpaRepository.findExpiredActiveMaterials(threshold));
    }

    private Pageable createPageable(MaterialFilters filters) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(filters.sortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                filters.page(),
                filters.size(),
                Sort.by(direction, filters.sortBy())
        );
    }
}
