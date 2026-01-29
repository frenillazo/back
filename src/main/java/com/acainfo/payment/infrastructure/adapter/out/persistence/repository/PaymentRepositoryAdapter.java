package com.acainfo.payment.infrastructure.adapter.out.persistence.repository;

import com.acainfo.payment.application.dto.PaymentFilters;
import com.acainfo.payment.application.port.out.PaymentRepositoryPort;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import com.acainfo.payment.infrastructure.adapter.out.persistence.entity.PaymentJpaEntity;
import com.acainfo.payment.infrastructure.adapter.out.persistence.specification.PaymentSpecifications;
import com.acainfo.payment.infrastructure.mapper.PaymentPersistenceMapper;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing PaymentRepositoryPort.
 * Bridges domain layer with JPA persistence.
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private static final int DAYS_UNTIL_OVERDUE = 5;

    private final JpaPaymentRepository jpaRepository;
    private final PaymentPersistenceMapper mapper;
    private final GetUserProfileUseCase getUserProfileUseCase;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = mapper.toJpaEntity(payment);
        PaymentJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<Payment> saveAll(List<Payment> payments) {
        List<PaymentJpaEntity> entities = mapper.toJpaEntityList(payments);
        List<PaymentJpaEntity> saved = jpaRepository.saveAll(entities);
        return mapper.toDomainList(saved);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Payment> findWithFilters(PaymentFilters filters) {
        Pageable pageable = createPageable(filters);

        // If filtering by studentEmail, first find matching student IDs
        List<Long> studentIdsFromEmail = null;
        if (filters.studentEmail() != null && !filters.studentEmail().isBlank()) {
            studentIdsFromEmail = getUserProfileUseCase.findIdsByEmailContaining(filters.studentEmail());
            if (studentIdsFromEmail.isEmpty()) {
                // No students match the email filter, return empty page
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }
        }

        Specification<PaymentJpaEntity> spec = PaymentSpecifications.fromFilters(
                filters.studentId(),
                filters.enrollmentId(),
                filters.status(),
                filters.type(),
                filters.billingMonth(),
                filters.billingYear(),
                null, // dueDateFrom
                null, // dueDateTo
                filters.isOverdue(),
                DAYS_UNTIL_OVERDUE
        );

        // Add studentIds filter if searching by email
        if (studentIdsFromEmail != null) {
            spec = spec.and(PaymentSpecifications.hasStudentIdIn(studentIdsFromEmail));
        }

        return jpaRepository.findAll(spec, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<Payment> findByStudentId(Long studentId) {
        return mapper.toDomainList(
                jpaRepository.findByStudentIdOrderByDueDateDesc(studentId)
        );
    }

    @Override
    public List<Payment> findByEnrollmentId(Long enrollmentId) {
        return mapper.toDomainList(
                jpaRepository.findByEnrollmentIdOrderByBillingYearDescBillingMonthDesc(enrollmentId)
        );
    }

    @Override
    public List<Payment> findByStudentIdAndStatus(Long studentId, PaymentStatus status) {
        return mapper.toDomainList(
                jpaRepository.findByStudentIdAndStatusOrderByDueDateAsc(studentId, status)
        );
    }

    @Override
    public Optional<Payment> findByEnrollmentIdAndBillingPeriod(Long enrollmentId, Integer billingMonth, Integer billingYear) {
        // For billing period lookup, we check any type (typically MONTHLY or INITIAL)
        Specification<PaymentJpaEntity> spec = Specification
                .where(PaymentSpecifications.hasEnrollmentId(enrollmentId))
                .and(PaymentSpecifications.hasBillingMonth(billingMonth))
                .and(PaymentSpecifications.hasBillingYear(billingYear));

        return jpaRepository.findOne(spec)
                .map(mapper::toDomain);
    }

    @Override
    public List<Payment> findByEnrollmentIdAndType(Long enrollmentId, PaymentType type) {
        Specification<PaymentJpaEntity> spec = Specification
                .where(PaymentSpecifications.hasEnrollmentId(enrollmentId))
                .and(PaymentSpecifications.hasType(type));

        return mapper.toDomainList(
                jpaRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "billingYear", "billingMonth"))
        );
    }

    @Override
    public boolean existsByEnrollmentIdAndBillingPeriod(Long enrollmentId, Integer billingMonth, Integer billingYear) {
        // Check for any type at this period
        return jpaRepository.existsByEnrollmentIdAndBillingMonthAndBillingYearAndType(
                enrollmentId, billingMonth, billingYear, PaymentType.MONTHLY) ||
                jpaRepository.existsByEnrollmentIdAndBillingMonthAndBillingYearAndType(
                        enrollmentId, billingMonth, billingYear, PaymentType.INITIAL);
    }

    @Override
    public boolean existsIntensivePaymentByEnrollmentId(Long enrollmentId) {
        return jpaRepository.existsByEnrollmentIdAndBillingMonthAndBillingYearAndType(
                enrollmentId, null, null, PaymentType.INTENSIVE_FULL) ||
                !findByEnrollmentIdAndType(enrollmentId, PaymentType.INTENSIVE_FULL).isEmpty();
    }

    @Override
    public List<Payment> findOverdueByStudentId(Long studentId, LocalDate today) {
        LocalDate overdueDate = today.minusDays(DAYS_UNTIL_OVERDUE);
        return mapper.toDomainList(
                jpaRepository.findOverduePaymentsByStudentId(studentId, PaymentStatus.PENDING, overdueDate)
        );
    }

    @Override
    public List<Payment> findAllOverdue(LocalDate today) {
        LocalDate overdueDate = today.minusDays(DAYS_UNTIL_OVERDUE);
        return mapper.toDomainList(
                jpaRepository.findOverduePayments(PaymentStatus.PENDING, overdueDate)
        );
    }

    @Override
    public boolean hasOverduePayments(Long studentId, LocalDate today) {
        LocalDate overdueDate = today.minusDays(DAYS_UNTIL_OVERDUE);
        return jpaRepository.hasOverduePayments(studentId, PaymentStatus.PENDING, overdueDate);
    }

    @Override
    public long countPendingByStudentId(Long studentId) {
        Specification<PaymentJpaEntity> spec = Specification
                .where(PaymentSpecifications.hasStudentId(studentId))
                .and(PaymentSpecifications.hasStatus(PaymentStatus.PENDING));

        return jpaRepository.count(spec);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    private Pageable createPageable(PaymentFilters filters) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(filters.sortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                filters.page(),
                filters.size(),
                Sort.by(direction, filters.sortBy())
        );
    }
}
