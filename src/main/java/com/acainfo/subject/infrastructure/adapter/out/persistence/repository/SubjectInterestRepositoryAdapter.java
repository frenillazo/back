package com.acainfo.subject.infrastructure.adapter.out.persistence.repository;

import com.acainfo.subject.application.port.out.SubjectInterestRepositoryPort;
import com.acainfo.subject.domain.model.SubjectInterest;
import com.acainfo.subject.infrastructure.adapter.out.persistence.entity.SubjectInterestJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter implementing SubjectInterestRepositoryPort with Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class SubjectInterestRepositoryAdapter implements SubjectInterestRepositoryPort {

    private final JpaSubjectInterestRepository jpaRepository;

    @Override
    public SubjectInterest save(SubjectInterest interest) {
        SubjectInterestJpaEntity entity = SubjectInterestJpaEntity.builder()
                .id(interest.getId())
                .subjectId(interest.getSubjectId())
                .studentId(interest.getStudentId())
                .createdAt(interest.getCreatedAt())
                .build();
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public boolean existsBySubjectIdAndStudentId(Long subjectId, Long studentId) {
        return jpaRepository.existsBySubjectIdAndStudentId(subjectId, studentId);
    }

    @Override
    @Transactional
    public void deleteBySubjectIdAndStudentId(Long subjectId, Long studentId) {
        jpaRepository.deleteBySubjectIdAndStudentId(subjectId, studentId);
    }

    @Override
    public List<SubjectInterest> findByStudentId(Long studentId) {
        return jpaRepository.findByStudentId(studentId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Map<Long, Long> countBySubject() {
        return jpaRepository.countGroupedBySubject().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private SubjectInterest toDomain(SubjectInterestJpaEntity entity) {
        return SubjectInterest.builder()
                .id(entity.getId())
                .subjectId(entity.getSubjectId())
                .studentId(entity.getStudentId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
