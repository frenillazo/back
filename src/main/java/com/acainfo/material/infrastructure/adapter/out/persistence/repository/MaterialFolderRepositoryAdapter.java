package com.acainfo.material.infrastructure.adapter.out.persistence.repository;

import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialFolderJpaEntity;
import com.acainfo.material.infrastructure.mapper.MaterialFolderPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing MaterialFolderRepositoryPort.
 */
@Repository
@RequiredArgsConstructor
public class MaterialFolderRepositoryAdapter implements MaterialFolderRepositoryPort {

    private final JpaMaterialFolderRepository jpaRepository;
    private final MaterialFolderPersistenceMapper mapper;

    @Override
    public MaterialFolder save(MaterialFolder folder) {
        MaterialFolderJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(folder));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MaterialFolder> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<MaterialFolder> findBySubjectId(Long subjectId) {
        return mapper.toDomainList(jpaRepository.findBySubjectIdOrderByPositionAscNameAsc(subjectId));
    }

    @Override
    public List<MaterialFolder> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapper.toDomainList(jpaRepository.findAllById(ids));
    }

    @Override
    public boolean existsBySubjectIdAndName(Long subjectId, String name) {
        return jpaRepository.existsBySubjectIdAndName(subjectId, name);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }
}
