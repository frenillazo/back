package com.acainfo.material.infrastructure.adapter.out.persistence.repository;

import com.acainfo.material.application.port.out.MaterialAiJobRepositoryPort;
import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.domain.model.MaterialAiJobStatus;
import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialAiJobJpaEntity;
import com.acainfo.material.infrastructure.mapper.MaterialAiJobPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing MaterialAiJobRepositoryPort.
 */
@Repository
@RequiredArgsConstructor
public class MaterialAiJobRepositoryAdapter implements MaterialAiJobRepositoryPort {

    private final JpaMaterialAiJobRepository jpaRepository;
    private final MaterialAiJobPersistenceMapper mapper;

    @Override
    public MaterialAiJob save(MaterialAiJob job) {
        MaterialAiJobJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(job));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MaterialAiJob> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public int failInterruptedJobs(String errorMessage) {
        return jpaRepository.failAllWithStatuses(
                List.of(MaterialAiJobStatus.PENDING, MaterialAiJobStatus.RUNNING),
                errorMessage,
                LocalDateTime.now());
    }
}
