package acainfo.back.infrastructure.adapters.out;

import acainfo.back.domain.model.Degree;
import acainfo.back.domain.model.Subject;
import acainfo.back.domain.model.SubjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Subject entity.
 * Provides CRUD operations, custom queries, and dynamic filtering with Specifications.
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    /**
     * Find a subject by its unique code
     * @param code the subject code
     * @return Optional containing the subject if found
     */
    Optional<Subject> findByCode(String code);

    /**
     * Check if a subject exists with the given code
     * @param code the subject code
     * @return true if exists, false otherwise
     */
    boolean existsByCode(String code);

    /**
     * Find all subjects by degree
     * @param degree the degree
     * @return list of subjects
     */
    List<Subject> findByDegree(Degree degree);

    /**
     * Find all subjects by status
     * @param status the subject status
     * @return list of subjects
     */
    List<Subject> findByStatus(SubjectStatus status);

    /**
     * Find all active subjects
     * @return list of active subjects
     */
    default List<Subject> findAllActive() {
        return findByStatus(SubjectStatus.ACTIVO);
    }

    /**
     * Find all subjects by degree and year
     * @param degree the degree
     * @param year the academic year
     * @return list of subjects
     */
    List<Subject> findByDegreeAndYear(Degree degree, Integer year);

    /**
     * Find all subjects by degree, year and semester
     * @param degree the degree
     * @param year the academic year
     * @param semester the semester
     * @return list of subjects
     */
    List<Subject> findByDegreeAndYearAndSemester(Degree degree, Integer year, Integer semester);

    /**
     * Find all subjects by year
     * @param year the academic year
     * @return list of subjects
     */
    List<Subject> findByYear(Integer year);

    /**
     * Find all subjects by semester
     * @param semester the semester
     * @return list of subjects
     */
    List<Subject> findBySemester(Integer semester);

    /**
     * Find all active subjects by degree
     * @param degree the degree
     * @return list of active subjects
     */
    @Query("SELECT s FROM Subject s WHERE s.degree = :degree AND s.status = 'ACTIVO'")
    List<Subject> findActiveByDegree(@Param("degree") Degree degree);

    /**
     * Find all active subjects by degree and year
     * @param degree the degree
     * @param year the academic year
     * @return list of active subjects
     */
    @Query("SELECT s FROM Subject s WHERE s.degree = :degree AND s.year = :year AND s.status = 'ACTIVO'")
    List<Subject> findActiveByDegreeAndYear(@Param("degree") Degree degree, @Param("year") Integer year);

    /**
     * Count subjects by status
     * @param status the subject status
     * @return count of subjects
     */
    long countByStatus(SubjectStatus status);

    /**
     * Count active subjects
     * @return count of active subjects
     */
    default long countActive() {
        return countByStatus(SubjectStatus.ACTIVO);
    }

    /**
     * Count subjects by degree
     * @param degree the degree
     * @return count of subjects
     */
    long countByDegree(Degree degree);

    /**
     * Search subjects by name containing (case insensitive)
     * @param name the name to search
     * @return list of matching subjects
     */
    @Query("SELECT s FROM Subject s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Subject> searchByName(@Param("name") String name);

    /**
     * Search subjects by code or name containing (case insensitive)
     * @param search the search term
     * @return list of matching subjects
     */
    @Query("SELECT s FROM Subject s WHERE LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Subject> searchByCodeOrName(@Param("search") String search);
}
