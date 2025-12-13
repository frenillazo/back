package com.acainfo.material.infrastructure.adapter.out.persistence.specification;

import com.acainfo.material.infrastructure.adapter.out.persistence.entity.MaterialJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Material entity using Criteria Builder.
 * Enables dynamic query building with type-safe filters.
 */
public final class MaterialSpecifications {

    private MaterialSpecifications() {
        // Utility class
    }

    /**
     * Filter by subject ID.
     */
    public static Specification<MaterialJpaEntity> hasSubjectId(Long subjectId) {
        return (root, query, cb) -> subjectId == null ? null :
                cb.equal(root.get("subjectId"), subjectId);
    }

    /**
     * Filter by uploader ID.
     */
    public static Specification<MaterialJpaEntity> hasUploadedById(Long uploadedById) {
        return (root, query, cb) -> uploadedById == null ? null :
                cb.equal(root.get("uploadedById"), uploadedById);
    }

    /**
     * Filter by file extension.
     */
    public static Specification<MaterialJpaEntity> hasFileExtension(String fileExtension) {
        return (root, query, cb) -> fileExtension == null || fileExtension.isBlank() ? null :
                cb.equal(root.get("fileExtension"), fileExtension.toLowerCase());
    }

    /**
     * Search by name or description (case-insensitive).
     */
    public static Specification<MaterialJpaEntity> searchTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return null;
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("originalFilename")), pattern)
            );
        };
    }

    /**
     * Build a combined specification from MaterialFilters.
     */
    public static Specification<MaterialJpaEntity> fromFilters(
            Long subjectId,
            Long uploadedById,
            String fileExtension,
            String searchTerm) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (subjectId != null) {
                predicates.add(cb.equal(root.get("subjectId"), subjectId));
            }

            if (uploadedById != null) {
                predicates.add(cb.equal(root.get("uploadedById"), uploadedById));
            }

            if (fileExtension != null && !fileExtension.isBlank()) {
                predicates.add(cb.equal(root.get("fileExtension"), fileExtension.toLowerCase()));
            }

            if (searchTerm != null && !searchTerm.isBlank()) {
                String pattern = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("originalFilename")), pattern)
                ));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
