package acainfo.back.subject.infrastructure.adapters.out.persistence.repositories;

import acainfo.back.subject.domain.model.Degree;
import acainfo.back.subject.domain.model.SubjectStatus;
import acainfo.back.subject.infrastructure.adapters.out.persistence.entities.SubjectJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Subject persistence
 * Works with SubjectJpaEntity
 * Provides database operations
 */
@Repository
public interface SubjectJpaRepository extends JpaRepository<SubjectJpaEntity, Long> {

    /**
     * Find a subject by its unique code
     */
    Optional<SubjectJpaEntity> findByCode(String code);

    /**
     * Check if a subject exists with the given code
     */
    boolean existsByCode(String code);

    /**
     * Check if a subject exists with the given code, excluding a specific ID
     */
    boolean existsByCodeAndIdNot(String code, Long id);

    /**
     * Find all subjects by degree
     */
    List<SubjectJpaEntity> findByDegree(Degree degree);

    /**
     * Find all subjects by status
     */
    List<SubjectJpaEntity> findByStatus(SubjectStatus status);

    /**
     * Find all subjects by degree and year
     */
    List<SubjectJpaEntity> findByDegreeAndYear(Degree degree, Integer year);

    /**
     * Find all subjects by degree, year and semester
     */
    List<SubjectJpaEntity> findByDegreeAndYearAndSemester(Degree degree, Integer year, Integer semester);

    /**
     * Find all subjects by year
     */
    List<SubjectJpaEntity> findByYear(Integer year);

    /**
     * Find all subjects by semester
     */
    List<SubjectJpaEntity> findBySemester(Integer semester);

    /**
     * Find all active subjects by degree
     */
    @Query("SELECT s FROM SubjectJpaEntity s WHERE s.degree = :degree AND s.status = 'ACTIVO'")
    List<SubjectJpaEntity> findActiveByDegree(@Param("degree") Degree degree);

    /**
     * Find all active subjects by degree and year
     */
    @Query("SELECT s FROM SubjectJpaEntity s WHERE s.degree = :degree AND s.year = :year AND s.status = 'ACTIVO'")
    List<SubjectJpaEntity> findActiveByDegreeAndYear(@Param("degree") Degree degree, @Param("year") Integer year);

    /**
     * Count subjects by status
     */
    long countByStatus(SubjectStatus status);

    /**
     * Count subjects by degree
     */
    long countByDegree(Degree degree);

    /**
     * Search subjects by name containing (case insensitive)
     */
    @Query("SELECT s FROM SubjectJpaEntity s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SubjectJpaEntity> searchByName(@Param("name") String name);

    /**
     * Search subjects by code or name containing (case insensitive)
     */
    @Query("SELECT s FROM SubjectJpaEntity s WHERE LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<SubjectJpaEntity> searchByCodeOrName(@Param("search") String search);
}
