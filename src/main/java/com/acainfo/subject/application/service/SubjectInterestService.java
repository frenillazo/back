package com.acainfo.subject.application.service;

import com.acainfo.subject.application.dto.SubjectInterestSummary;
import com.acainfo.subject.application.port.in.SubjectInterestUseCase;
import com.acainfo.subject.application.port.out.SubjectInterestRepositoryPort;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.domain.model.SubjectInterest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Service implementing the minimal "me interesa" flow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectInterestService implements SubjectInterestUseCase {

    private final SubjectInterestRepositoryPort interestRepositoryPort;
    private final SubjectRepositoryPort subjectRepositoryPort;

    @Override
    @Transactional
    public void markInterest(Long subjectId, Long studentId) {
        subjectRepositoryPort.findById(subjectId)
                .orElseThrow(() -> new SubjectNotFoundException(subjectId));

        if (interestRepositoryPort.existsBySubjectIdAndStudentId(subjectId, studentId)) {
            return; // idempotent
        }

        interestRepositoryPort.save(SubjectInterest.builder()
                .subjectId(subjectId)
                .studentId(studentId)
                .createdAt(LocalDateTime.now())
                .build());
        log.info("Student {} marked interest in subject {}", studentId, subjectId);
    }

    @Override
    @Transactional
    public void removeInterest(Long subjectId, Long studentId) {
        interestRepositoryPort.deleteBySubjectIdAndStudentId(subjectId, studentId);
        log.info("Student {} removed interest in subject {}", studentId, subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasInterest(Long subjectId, Long studentId) {
        return interestRepositoryPort.existsBySubjectIdAndStudentId(subjectId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getInterestSubjectIds(Long studentId) {
        return interestRepositoryPort.findByStudentId(studentId).stream()
                .map(SubjectInterest::getSubjectId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectInterestSummary> getInterestSummary() {
        Map<Long, Long> counts = interestRepositoryPort.countBySubject();
        if (counts.isEmpty()) {
            return List.of();
        }

        List<Subject> subjects = subjectRepositoryPort.findByIds(counts.keySet().stream().toList());

        return subjects.stream()
                .map(subject -> new SubjectInterestSummary(
                        subject.getId(),
                        subject.getName(),
                        subject.getCode(),
                        counts.getOrDefault(subject.getId(), 0L)
                ))
                .sorted(Comparator.comparingLong(SubjectInterestSummary::interestedStudents).reversed())
                .toList();
    }
}
