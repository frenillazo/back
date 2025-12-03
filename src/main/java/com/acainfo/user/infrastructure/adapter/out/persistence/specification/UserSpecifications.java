package com.acainfo.user.infrastructure.adapter.out.persistence.specification;

import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.UserStatus;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.RoleJpaEntity;
import com.acainfo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for UserJpaEntity (Criteria Builder).
 * Translates UserFilters (domain DTO) to JPA Specifications.
 *
 * This class encapsulates the JPA Criteria API complexity,
 * keeping the infrastructure layer isolated from domain layer.
 */
public class UserSpecifications {

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Domain filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<UserJpaEntity> withFilters(UserFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by exact email (case insensitive)
            if (filters.email() != null && !filters.email().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("email")),
                        filters.email().toLowerCase().trim()
                ));
            }

            // Filter by search term (search in email, firstName, lastName)
            if (filters.searchTerm() != null && !filters.searchTerm().isBlank()) {
                String searchPattern = "%" + filters.searchTerm().toLowerCase().trim() + "%";
                Predicate emailLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        searchPattern
                );
                Predicate firstNameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        searchPattern
                );
                Predicate lastNameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(emailLike, firstNameLike, lastNameLike));
            }

            // Filter by user status
            if (filters.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.status()));
            }

            // Filter by role type (join with roles)
            if (filters.roleType() != null) {
                Join<UserJpaEntity, RoleJpaEntity> rolesJoin = root.join("roles");
                predicates.add(criteriaBuilder.equal(rolesJoin.get("type"), filters.roleType()));
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find users by email (case insensitive).
     */
    public static Specification<UserJpaEntity> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("email")),
                    email.toLowerCase().trim()
            );
        };
    }

    /**
     * Specification to find users by status.
     */
    public static Specification<UserJpaEntity> hasStatus(UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Specification to find users by role type.
     */
    public static Specification<UserJpaEntity> hasRole(RoleType roleType) {
        return (root, query, criteriaBuilder) -> {
            if (roleType == null) {
                return criteriaBuilder.conjunction();
            }
            Join<UserJpaEntity, RoleJpaEntity> rolesJoin = root.join("roles");
            return criteriaBuilder.equal(rolesJoin.get("type"), roleType);
        };
    }

    /**
     * Specification to search users by term (email, firstName, lastName).
     */
    public static Specification<UserJpaEntity> searchByTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + searchTerm.toLowerCase().trim() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern)
            );
        };
    }
}
