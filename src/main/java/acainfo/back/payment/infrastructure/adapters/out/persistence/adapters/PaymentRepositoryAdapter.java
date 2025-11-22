package acainfo.back.payment.infrastructure.adapters.out.persistence.adapters;

import acainfo.back.payment.application.ports.out.PaymentRepositoryPort;
import acainfo.back.payment.domain.model.PaymentDomain;
import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import acainfo.back.payment.infrastructure.adapters.out.persistence.mappers.PaymentJpaMapper;
import acainfo.back.payment.infrastructure.adapters.out.persistence.repositories.PaymentJpaRepository;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentJpaRepository paymentJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final PaymentJpaMapper paymentMapper;

    @Override
    public PaymentDomain save(PaymentDomain payment) {
        PaymentJpaEntity entity = paymentMapper.toEntity(payment);
        PaymentJpaEntity saved = paymentJpaRepository.save(entity);
        return paymentMapper.toDomain(saved);
    }

    @Override
    public Optional<PaymentDomain> findById(Long id) {
        return paymentJpaRepository.findById(id)
                .map(paymentMapper::toDomain);
    }

    @Override
    public List<PaymentDomain> findByStudentId(Long studentId) {
        UserJpaEntity student = userJpaRepository.findById(studentId).orElse(null);
        if (student == null) return List.of();

        return paymentJpaRepository.findByStudent(student).stream()
                .map(paymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDomain> findByStudentIdAndStatus(Long studentId, PaymentStatus status) {
        UserJpaEntity student = userJpaRepository.findById(studentId).orElse(null);
        if (student == null) return List.of();

        return paymentJpaRepository.findByStudentAndStatus(student, status).stream()
                .map(paymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDomain> findOverduePayments() {
        LocalDate fiveDaysAgo = LocalDate.now().minusDays(5);
        return paymentJpaRepository.findOverduePayments(fiveDaysAgo).stream()
                .map(paymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDomain> findByDueDateBefore(LocalDate date) {
        return paymentJpaRepository.findByDueDateBefore(date).stream()
                .map(paymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDomain> findByStatus(PaymentStatus status) {
        return paymentJpaRepository.findByStatus(status).stream()
                .map(paymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        paymentJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByStripePaymentId(String stripePaymentId) {
        return paymentJpaRepository.existsByStripePaymentId(stripePaymentId);
    }

    @Override
    public Optional<PaymentDomain> findByStripePaymentId(String stripePaymentId) {
        return paymentJpaRepository.findByStripePaymentId(stripePaymentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public List<PaymentDomain> findAll() {
        return paymentJpaRepository.findAll().stream()
                .map(paymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDomain> findByAcademicPeriod(String period) {
        return paymentJpaRepository.findByAcademicPeriod(period).stream()
                .map(paymentMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateRevenueBetween(LocalDate startDate, LocalDate endDate) {
        return paymentJpaRepository.calculateRevenueBetween(startDate, endDate);
    }

    @Override
    public BigDecimal calculateTotalPendingByStudentId(Long studentId) {
        return paymentJpaRepository.calculateTotalPendingByStudentId(studentId);
    }

    @Override
    public List<Long> findStudentIdsWithOverduePayments() {
        return paymentJpaRepository.findStudentIdsWithOverduePayments();
    }
}
