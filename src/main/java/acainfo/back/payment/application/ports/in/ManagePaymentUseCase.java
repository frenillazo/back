package acainfo.back.payment.application.ports.in;

import acainfo.back.payment.domain.model.PaymentDomain;
import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.domain.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Use case port for managing payments.
 */
public interface ManagePaymentUseCase {

    record CreatePaymentCommand(
            Long studentId,
            BigDecimal amount,
            PaymentType paymentType,
            LocalDate dueDate,
            String description,
            String academicPeriod
    ) {}

    record ProcessPaymentCommand(
            Long paymentId,
            String stripePaymentId,
            Long processedByUserId
    ) {}

    PaymentDomain createPayment(CreatePaymentCommand command);

    PaymentDomain processPayment(ProcessPaymentCommand command);

    PaymentDomain cancelPayment(Long paymentId, Long processedByUserId, String reason);

    PaymentDomain refundPayment(Long paymentId, Long processedByUserId, String reason);

    PaymentDomain getPaymentById(Long paymentId);

    List<PaymentDomain> getPaymentsByStudent(Long studentId);

    List<PaymentDomain> getOverduePayments(Long studentId);

    List<PaymentDomain> getPendingPayments(Long studentId);

    List<PaymentDomain> getAllOverduePayments();

    void generateMonthlyPayments();

    void checkAndMarkOverduePayments();

    boolean hasOverduePayments(Long studentId);

    List<PaymentDomain> getPaymentsByStatus(PaymentStatus status);

    List<PaymentDomain> getPaymentsByAcademicPeriod(String period);

    BigDecimal calculateRevenueBetween(LocalDate startDate, LocalDate endDate);

    BigDecimal calculateTotalPendingByStudent(Long studentId);

    List<Long> getStudentsWithOverduePayments();
}
