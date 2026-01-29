package com.acainfo.payment.infrastructure.adapter.in.rest;

import com.acainfo.enrollment.application.port.in.GetEnrollmentUseCase;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.group.application.port.in.GetGroupUseCase;
import com.acainfo.group.domain.model.SubjectGroup;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.PaymentResponse;
import com.acainfo.payment.infrastructure.adapter.in.rest.mapper.PaymentRestMapper;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Infrastructure service to enrich PaymentResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResponseEnricher {

    private final PaymentRestMapper paymentRestMapper;
    private final GetEnrollmentUseCase getEnrollmentUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Enrich a single payment with related entity data.
     *
     * @param payment the payment to enrich
     * @return enriched payment response
     */
    public PaymentResponse enrich(Payment payment) {
        // Fetch student
        User student = getUserProfileUseCase.getUserById(payment.getStudentId());

        // Fetch enrollment -> group -> subject
        Enrollment enrollment = getEnrollmentUseCase.getById(payment.getEnrollmentId());
        SubjectGroup group = getGroupUseCase.getById(enrollment.getGroupId());
        Subject subject = getSubjectUseCase.getById(group.getSubjectId());

        return paymentRestMapper.toEnrichedResponse(
                payment,
                student.getFullName(),
                student.getEmail(),
                subject.getName(),
                subject.getCode()
        );
    }

    /**
     * Enrich a list of payments with related entity data.
     * Optimized to batch-fetch related entities to minimize database queries.
     *
     * @param payments the payments to enrich
     * @return list of enriched payment responses
     */
    public List<PaymentResponse> enrichList(List<Payment> payments) {
        if (payments.isEmpty()) {
            return List.of();
        }

        // Collect unique IDs
        Set<Long> studentIds = payments.stream()
                .map(Payment::getStudentId)
                .collect(Collectors.toSet());

        Set<Long> enrollmentIds = payments.stream()
                .map(Payment::getEnrollmentId)
                .collect(Collectors.toSet());

        // Fetch students
        Map<Long, User> studentsById = studentIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Fetch enrollments
        Map<Long, Enrollment> enrollmentsById = enrollmentIds.stream()
                .map(getEnrollmentUseCase::getById)
                .collect(Collectors.toMap(Enrollment::getId, Function.identity()));

        // Collect group IDs from enrollments
        Set<Long> groupIds = enrollmentsById.values().stream()
                .map(Enrollment::getGroupId)
                .collect(Collectors.toSet());

        // Fetch groups
        Map<Long, SubjectGroup> groupsById = groupIds.stream()
                .map(getGroupUseCase::getById)
                .collect(Collectors.toMap(SubjectGroup::getId, Function.identity()));

        // Collect subject IDs from groups
        Set<Long> subjectIds = groupsById.values().stream()
                .map(SubjectGroup::getSubjectId)
                .collect(Collectors.toSet());

        // Fetch subjects
        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Build enriched responses
        return payments.stream()
                .map(payment -> {
                    User student = studentsById.get(payment.getStudentId());
                    Enrollment enrollment = enrollmentsById.get(payment.getEnrollmentId());
                    SubjectGroup group = groupsById.get(enrollment.getGroupId());
                    Subject subject = subjectsById.get(group.getSubjectId());

                    return paymentRestMapper.toEnrichedResponse(
                            payment,
                            student.getFullName(),
                            student.getEmail(),
                            subject.getName(),
                            subject.getCode()
                    );
                })
                .toList();
    }
}
