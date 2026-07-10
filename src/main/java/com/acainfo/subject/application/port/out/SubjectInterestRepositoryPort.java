package com.acainfo.subject.application.port.out;

import com.acainfo.subject.domain.model.SubjectInterest;

import java.util.List;
import java.util.Map;

/**
 * Output port for SubjectInterest persistence.
 */
public interface SubjectInterestRepositoryPort {

    SubjectInterest save(SubjectInterest interest);

    boolean existsBySubjectIdAndStudentId(Long subjectId, Long studentId);

    void deleteBySubjectIdAndStudentId(Long subjectId, Long studentId);

    List<SubjectInterest> findByStudentId(Long studentId);

    /**
     * Aggregated interest per subject: subjectId → number of interested students.
     */
    Map<Long, Long> countBySubject();
}
