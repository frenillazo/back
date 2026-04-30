package com.acainfo.intensive.application.port.out;

import com.acainfo.intensive.application.dto.IntensiveFilters;
import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.intensive.domain.model.IntensiveStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Intensive persistence.
 */
public interface IntensiveRepositoryPort {

    Intensive save(Intensive intensive);

    Optional<Intensive> findById(Long id);

    /**
     * Pessimistic-lock variant for capacity-sensitive operations
     * (enrollment approval, waiting list promotion).
     */
    Optional<Intensive> findByIdForUpdate(Long id);

    Page<Intensive> findWithFilters(IntensiveFilters filters);

    List<Intensive> findAll();

    List<Intensive> findByStatus(IntensiveStatus status);

    void delete(Long id);

    long countActiveBySubjectId(Long subjectId);

    long countActiveByTeacherId(Long teacherId);

    long countAllBySubjectId(Long subjectId);
}
