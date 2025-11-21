package acainfo.back.payment.infrastructure.adapters.in.dto;

import acainfo.back.payment.domain.model.Payment;
import acainfo.back.payment.domain.model.PaymentStatus;
import acainfo.back.payment.domain.model.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for payment responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment response information")
public class PaymentResponse {

    @Schema(description = "Payment ID", example = "1")
    private Long id;

    @Schema(description = "Student information")
    private StudentBasicInfo student;

    @Schema(description = "Payment amount in EUR", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Payment type", example = "MENSUAL")
    private PaymentType paymentType;

    @Schema(description = "Payment status", example = "PAGADO")
    private PaymentStatus status;

    @Schema(description = "Due date", example = "2024-02-01")
    private LocalDate dueDate;

    @Schema(description = "Paid date (if paid)", example = "2024-01-28")
    private LocalDate paidDate;

    @Schema(description = "Invoice number", example = "INV-20240128-1-1234")
    private String invoiceNumber;

    @Schema(description = "Payment description", example = "Cuota mensual enero 2024")
    private String description;

    @Schema(description = "Academic period", example = "2024-Q1")
    private String academicPeriod;

    @Schema(description = "Days overdue (negative if not yet due)", example = "3")
    private Long daysOverdue;

    @Schema(description = "Whether this payment blocks student access", example = "false")
    private Boolean blocksAccess;

    @Schema(description = "Payment creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "User who processed the payment (if applicable)")
    private ProcessedByInfo processedBy;

    /**
     * Nested DTO for basic student information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentBasicInfo {
        @Schema(description = "Student ID", example = "1")
        private Long id;

        @Schema(description = "Student email", example = "estudiante@acainfo.com")
        private String email;

        @Schema(description = "Student first name", example = "María")
        private String firstName;

        @Schema(description = "Student last name", example = "López")
        private String lastName;

        @Schema(description = "Student full name", example = "María López")
        private String fullName;
    }

    /**
     * Nested DTO for user who processed the payment.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessedByInfo {
        @Schema(description = "User ID", example = "2")
        private Long id;

        @Schema(description = "User email", example = "admin@acainfo.com")
        private String email;

        @Schema(description = "User full name", example = "Admin User")
        private String fullName;
    }

    /**
     * Converts a Payment entity to a PaymentResponse DTO.
     */
    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponseBuilder builder = PaymentResponse.builder()
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

        // Add student info
        if (payment.getStudent() != null) {
            var student = payment.getStudent();
            builder.student(StudentBasicInfo.builder()
                    .id(student.getId())
                    .email(student.getEmail())
                    .firstName(student.getFirstName())
                    .lastName(student.getLastName())
                    .fullName(student.getFullName())
                    .build());
        }

        // Add processed by info
        if (payment.getProcessedBy() != null) {
            var processedBy = payment.getProcessedBy();
            builder.processedBy(ProcessedByInfo.builder()
                    .id(processedBy.getId())
                    .email(processedBy.getEmail())
                    .fullName(processedBy.getFullName())
                    .build());
        }

        return builder.build();
    }
}
