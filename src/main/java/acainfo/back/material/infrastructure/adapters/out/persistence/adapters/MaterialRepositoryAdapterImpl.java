package acainfo.back.material.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.material.application.ports.out.MaterialRepositoryPort;
import acainfo.back.material.domain.model.MaterialDomain;
import acainfo.back.material.domain.model.MaterialType;
import acainfo.back.material.infrastructure.adapters.out.persistence.entities.MaterialJpaEntity;
import acainfo.back.material.infrastructure.adapters.out.persistence.mappers.MaterialJpaMapper;
import acainfo.back.material.infrastructure.adapters.out.persistence.repositories.MaterialJpaRepository;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.entities.SubjectGroupJpaEntity;
import acainfo.back.subjectgroup.infrastructure.adapters.out.persistence.repositories.SubjectGroupJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository Adapter Implementation
 * Implements MaterialRepositoryPort using JPA infrastructure
 */
@Component
@RequiredArgsConstructor
public class MaterialRepositoryAdapterImpl implements MaterialRepositoryPort {

    private final MaterialJpaRepository jpaRepository;
    private final MaterialJpaMapper mapper;
    private final SubjectGroupJpaRepository subjectGroupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MaterialDomain save(MaterialDomain material) {
        // Fetch related entities
        SubjectGroupJpaEntity subjectGroup = material.getSubjectGroupId() != null
                ? subjectGroupRepository.findById(material.getSubjectGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("SubjectGroup not found: " + material.getSubjectGroupId()))
                : null;

        User uploadedBy = material.getUploadedById() != null
                ? userRepository.findById(material.getUploadedById())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + material.getUploadedById()))
                : null;

        MaterialJpaEntity jpaEntity;

        if (material.getId() != null) {
            // Update existing
            jpaEntity = jpaRepository.findById(material.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Material not found: " + material.getId()));
            mapper.updateJpaEntity(jpaEntity, material);
        } else {
            // Create new
            jpaEntity = mapper.toJpaEntity(material, subjectGroup, uploadedBy);
        }

        MaterialJpaEntity saved = jpaRepository.save(jpaEntity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialDomain> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> findBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId) {
        return jpaRepository.findActiveBySubjectGroupId(subjectGroupId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> findBySubjectGroupId(Long subjectGroupId) {
        return jpaRepository.findBySubjectGroupId(subjectGroupId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> findBySubjectGroupIdAndTypeAndIsActiveTrue(Long subjectGroupId, MaterialType type) {
        return jpaRepository.findActiveBySubjectGroupId(subjectGroupId).stream()
                .filter(m -> m.getType() == type)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> findBySubjectGroupIdAndTopicAndIsActiveTrue(Long subjectGroupId, String topic) {
        return jpaRepository.findBySubjectGroupIdAndTopic(subjectGroupId, topic).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> findByUploadedById(Long uploaderId) {
        return jpaRepository.findByUploadedById(uploaderId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndIsActiveTrue(Long id) {
        return jpaRepository.findById(id)
                .map(MaterialJpaEntity::getIsActive)
                .orElse(false);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId) {
        return jpaRepository.countActiveBySubjectGroupId(subjectGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialDomain> findByTypeAndIsActiveTrue(MaterialType type) {
        return jpaRepository.findByType(type).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctTopicsBySubjectGroupId(Long subjectGroupId) {
        return jpaRepository.findDistinctTopicsBySubjectGroupId(subjectGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFilePath(String filePath) {
        return jpaRepository.existsByFilePath(filePath);
    }

    @Override
    @Transactional(readOnly = true)
    public Long calculateTotalSizeBySubjectGroupId(Long subjectGroupId) {
        return jpaRepository.calculateTotalSizeBySubjectGroupId(subjectGroupId);
    }
}
