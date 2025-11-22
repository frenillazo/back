package acainfo.back.payment.infrastructure.adapters.in.dto;

import acainfo.back.payment.domain.model.PaymentDomain;
import acainfo.back.user.application.ports.out.UserRepositoryPort;
import acainfo.back.user.domain.model.UserDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert PaymentDomain to PaymentResponse DTO.
 * Fetches user information when needed.
 */
@Component
@RequiredArgsConstructor
public class PaymentResponseMapper {

    private final UserRepositoryPort userRepository;

    public PaymentResponse toResponse(PaymentDomain payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .invoiceNumber(payment.getInvoiceNumber())
                .description(payment.getDescription())
                .academicPeriod(payment.getAcademicPeriod())
                .daysOverdue(payment.getDaysOverdue())
                .blocksAccess(payment.blocksAccess())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt());

        // Fetch and add student info if studentId exists
        if (payment.getStudentId() != null) {
            userRepository.findById(payment.getStudentId())
                    .ifPresent(student -> builder.student(
                            PaymentResponse.StudentBasicInfo.builder()
                                    .id(student.getId())
                                    .email(student.getEmail())
                                    .firstName(student.getFirstName())
                                    .lastName(student.getLastName())
                                    .fullName(student.getFullName())
                                    .build()
                    ));
        }

        // Fetch and add processedBy info if processedBy exists
        if (payment.getProcessedBy() != null) {
            userRepository.findById(payment.getProcessedBy())
                    .ifPresent(processedBy -> builder.processedBy(
                            PaymentResponse.ProcessedByInfo.builder()
                                    .id(processedBy.getId())
                                    .email(processedBy.getEmail())
                                    .fullName(processedBy.getFullName())
                                    .build()
                    ));
        }

        return builder.build();
    }
}
