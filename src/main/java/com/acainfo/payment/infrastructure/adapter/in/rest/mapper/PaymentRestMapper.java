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

    @Mapping(target = "isOverdue", expression = "java(payment.isOverdue())")
    @Mapping(target = "daysOverdue", expression = "java(payment.getDaysOverdue())")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
