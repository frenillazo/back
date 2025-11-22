package acainfo.back.payment.application.ports.out;

import acainfo.back.payment.domain.model.PaymentDomain;
import acainfo.back.payment.domain.model.PaymentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port for Payment persistence operations.
 */
public interface PaymentRepositoryPort {

    PaymentDomain save(PaymentDomain payment);

    Optional<PaymentDomain> findById(Long id);

    List<PaymentDomain> findByStudentId(Long studentId);

    List<PaymentDomain> findByStudentIdAndStatus(Long studentId, PaymentStatus status);

    List<PaymentDomain> findOverduePayments();

    List<PaymentDomain> findByDueDateBefore(LocalDate date);

    List<PaymentDomain> findByStatus(PaymentStatus status);

    void deleteById(Long id);

    boolean existsByStripePaymentId(String stripePaymentId);

    Optional<PaymentDomain> findByStripePaymentId(String stripePaymentId);

    List<PaymentDomain> findAll();
}
