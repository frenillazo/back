package com.acainfo.user.infrastructure.adapter.out.persistence.specification;

import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for UserJpaEntity (Criteria Builder).
 * Translates UserFilters (domain DTO) to JPA Specifications.
 *
 * STUB: This is a temporary implementation. Will be fully implemented in Hito 1.6.
 */
public class UserSpecifications {

    /**
     * Build dynamic specification from filters.
     * STUB: Currently returns empty specification (no filtering).
     *
     * @param filters Domain filters
     * @return JPA Specification
     */
    public static Specification<UserJpaEntity> withFilters(UserFilters filters) {
        // TODO: Implement filtering logic in Hito 1.6
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }
}
