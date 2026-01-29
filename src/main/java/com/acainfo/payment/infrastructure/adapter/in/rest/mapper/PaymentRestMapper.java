package com.acainfo.payment.infrastructure.adapter.in.rest.mapper;

import com.acainfo.payment.application.dto.CancelPaymentCommand;
import com.acainfo.payment.application.dto.GenerateMonthlyPaymentsCommand;
import com.acainfo.payment.application.dto.GeneratePaymentCommand;
import com.acainfo.payment.application.dto.MarkPaymentPaidCommand;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Payment REST layer.
 * Converts between REST DTOs and application layer DTOs/domain entities.
 */
@Mapper(componentModel = "spring")
public interface PaymentRestMapper {

    // ==================== Request to Command ====================

    GeneratePaymentCommand toCommand(GeneratePaymentRequest request);

    GenerateMonthlyPaymentsCommand toCommand(GenerateMonthlyPaymentsRequest request);

    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "paidAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "stripePaymentIntentId", source = "request.stripePaymentIntentId")
    MarkPaymentPaidCommand toCommand(Long paymentId, MarkPaymentPaidRequest request);

    @Mapping(target = "paymentId", source = "paymentId")
    @Mapping(target = "reason", source = "request.reason")
    CancelPaymentCommand toCommand(Long paymentId, CancelPaymentRequest request);

    // ==================== Domain to Response ====================

    /**
     * Convert Payment (Domain) to PaymentResponse (REST) without enriched data.
     * Use toEnrichedResponse for enriched responses.
     */
    @Mapping(target = "studentName", ignore = true)
    @Mapping(target = "studentEmail", ignore = true)
    @Mapping(target = "subjectName", ignore = true)
    @Mapping(target = "subjectCode", ignore = true)
    @Mapping(target = "isOverdue", expression = "java(payment.isOverdue())")
    @Mapping(target = "daysOverdue", expression = "java(payment.getDaysOverdue())")
    PaymentResponse toResponse(Payment payment);

    /**
     * Convert Payment (Domain) to PaymentResponse (REST) with enriched data.
     *
     * @param payment      the payment domain object
     * @param studentName  full name of the student
     * @param studentEmail email of the student
     * @param subjectName  name of the subject
     * @param subjectCode  code of the subject
     * @return enriched payment response
     */
    @Mapping(target = "studentName", source = "studentName")
    @Mapping(target = "studentEmail", source = "studentEmail")
    @Mapping(target = "subjectName", source = "subjectName")
    @Mapping(target = "subjectCode", source = "subjectCode")
    @Mapping(target = "id", source = "payment.id")
    @Mapping(target = "enrollmentId", source = "payment.enrollmentId")
    @Mapping(target = "studentId", source = "payment.studentId")
    @Mapping(target = "type", source = "payment.type")
    @Mapping(target = "status", source = "payment.status")
    @Mapping(target = "amount", source = "payment.amount")
    @Mapping(target = "totalHours", source = "payment.totalHours")
    @Mapping(target = "pricePerHour", source = "payment.pricePerHour")
    @Mapping(target = "billingMonth", source = "payment.billingMonth")
    @Mapping(target = "billingYear", source = "payment.billingYear")
    @Mapping(target = "generatedAt", source = "payment.generatedAt")
    @Mapping(target = "dueDate", source = "payment.dueDate")
    @Mapping(target = "paidAt", source = "payment.paidAt")
    @Mapping(target = "description", source = "payment.description")
    @Mapping(target = "createdAt", source = "payment.createdAt")
    @Mapping(target = "updatedAt", source = "payment.updatedAt")
    @Mapping(target = "isOverdue", expression = "java(payment.isOverdue())")
    @Mapping(target = "daysOverdue", expression = "java(payment.getDaysOverdue())")
    PaymentResponse toEnrichedResponse(
            Payment payment,
            String studentName,
            String studentEmail,
            String subjectName,
            String subjectCode
    );

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
