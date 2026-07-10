package com.acainfo.subject.infrastructure.adapter.out.persistence.repository;

import com.acainfo.subject.infrastructure.adapter.out.persistence.entity.SubjectInterestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Spring Data JPA repository for SubjectInterestJpaEntity.
 */
public interface JpaSubjectInterestRepository extends JpaRepository<SubjectInterestJpaEntity, Long> {

    boolean existsBySubjectIdAndStudentId(Long subjectId, Long studentId);

    void deleteBySubjectIdAndStudentId(Long subjectId, Long studentId);

    List<SubjectInterestJpaEntity> findByStudentId(Long studentId);

    /**
     * Aggregated interest per subject: [subjectId, count].
     */
    @Query("SELECT i.subjectId, COUNT(i) FROM SubjectInterestJpaEntity i GROUP BY i.subjectId")
    List<Object[]> countGroupedBySubject();
}
