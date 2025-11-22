package acainfo.back.payment.infrastructure.adapters.out.persistence.mappers;

import acainfo.back.payment.domain.model.PaymentDomain;
import acainfo.back.payment.infrastructure.adapters.out.persistence.entities.PaymentJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.entities.UserJpaEntity;
import acainfo.back.user.infrastructure.adapters.out.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentJpaMapper {

    private final UserJpaRepository userRepository;

    public PaymentDomain toDomain(PaymentJpaEntity entity) {
        if (entity == null) return null;

        return PaymentDomain.builder()
                .id(entity.getId())
                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .amount(entity.getAmount())
                .paymentType(entity.getPaymentType())
                .status(entity.getStatus())
                .dueDate(entity.getDueDate())
                .paidDate(entity.getPaidDate())
                .stripePaymentId(entity.getStripePaymentId())
                .invoiceNumber(entity.getInvoiceNumber())
                .description(entity.getDescription())
                .academicPeriod(entity.getAcademicPeriod())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .processedBy(entity.getProcessedBy() != null ? entity.getProcessedBy().getId() : null)
                .version(entity.getVersion())
                .build();
    }

    public PaymentJpaEntity toEntity(PaymentDomain domain) {
        if (domain == null) return null;

        UserJpaEntity student = domain.getStudentId() != null
            ? userRepository.findById(domain.getStudentId()).orElse(null)
            : null;

        UserJpaEntity processedBy = domain.getProcessedBy() != null
            ? userRepository.findById(domain.getProcessedBy()).orElse(null)
            : null;

        return PaymentJpaEntity.builder()
                .id(domain.getId())
                .student(student)
                .amount(domain.getAmount())
                .paymentType(domain.getPaymentType())
                .status(domain.getStatus())
                .dueDate(domain.getDueDate())
                .paidDate(domain.getPaidDate())
                .stripePaymentId(domain.getStripePaymentId())
                .invoiceNumber(domain.getInvoiceNumber())
                .description(domain.getDescription())
                .academicPeriod(domain.getAcademicPeriod())
                .processedBy(processedBy)
                .version(domain.getVersion())
                .build();
    }
}
