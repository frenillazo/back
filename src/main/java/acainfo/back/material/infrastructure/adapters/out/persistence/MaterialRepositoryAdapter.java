package acainfo.back.material.infrastructure.adapters.out.persistence;

import acainfo.back.material.application.ports.out.MaterialRepositoryPort;
import acainfo.back.material.domain.model.Material;
import acainfo.back.material.domain.model.MaterialType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements MaterialRepositoryPort using JPA.
 * Bridges the domain layer with the persistence infrastructure.
 */
@Component
@RequiredArgsConstructor
public class MaterialRepositoryAdapter implements MaterialRepositoryPort {

    private final JpaMaterialRepository jpaMaterialRepository;

    @Override
    public Material save(Material material) {
        return jpaMaterialRepository.save(material);
    }

    @Override
    public Optional<Material> findById(Long id) {
        return jpaMaterialRepository.findById(id);
    }

    @Override
    public List<Material> findBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId) {
        return jpaMaterialRepository.findBySubjectGroupIdAndIsActiveTrue(subjectGroupId);
    }

    @Override
    public List<Material> findBySubjectGroupId(Long subjectGroupId) {
        return jpaMaterialRepository.findBySubjectGroupId(subjectGroupId);
    }

    @Override
    public List<Material> findBySubjectGroupIdAndTypeAndIsActiveTrue(Long subjectGroupId, MaterialType type) {
        return jpaMaterialRepository.findBySubjectGroupIdAndTypeAndIsActiveTrue(subjectGroupId, type);
    }

    @Override
    public List<Material> findBySubjectGroupIdAndTopicAndIsActiveTrue(Long subjectGroupId, String topic) {
        return jpaMaterialRepository.findBySubjectGroupIdAndTopicAndIsActiveTrue(subjectGroupId, topic);
    }

    @Override
    public List<Material> findByUploadedById(Long uploaderId) {
        return jpaMaterialRepository.findByUploadedById(uploaderId);
    }

    @Override
    public boolean existsByIdAndIsActiveTrue(Long id) {
        return jpaMaterialRepository.existsByIdAndIsActiveTrue(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaMaterialRepository.deleteById(id);
    }

    @Override
    public long countBySubjectGroupIdAndIsActiveTrue(Long subjectGroupId) {
        return jpaMaterialRepository.countBySubjectGroupIdAndIsActiveTrue(subjectGroupId);
    }

    @Override
    public List<Material> findByTypeAndIsActiveTrue(MaterialType type) {
        return jpaMaterialRepository.findByTypeAndIsActiveTrue(type);
    }
}
