package com.acainfo.intensive.infrastructure.adapter.out.persistence.specification;

import com.acainfo.intensive.application.dto.IntensiveFilters;
import com.acainfo.intensive.infrastructure.adapter.out.persistence.entity.IntensiveJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Criteria specifications for IntensiveJpaEntity.
 */
public class IntensiveSpecifications {

    public static Specification<IntensiveJpaEntity> withFilters(IntensiveFilters filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.subjectId() != null) {
                predicates.add(cb.equal(root.get("subjectId"), filters.subjectId()));
            }
            if (filters.teacherId() != null) {
                predicates.add(cb.equal(root.get("teacherId"), filters.teacherId()));
            }
            if (filters.status() != null) {
                predicates.add(cb.equal(root.get("status"), filters.status()));
            }
            if (filters.searchTerm() != null && !filters.searchTerm().isBlank()) {
                String pattern = "%" + filters.searchTerm().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
