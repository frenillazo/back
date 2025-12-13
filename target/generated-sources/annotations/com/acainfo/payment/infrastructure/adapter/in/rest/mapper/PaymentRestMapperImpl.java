package com.acainfo.payment.infrastructure.adapter.in.rest.mapper;

import com.acainfo.payment.application.dto.CancelPaymentCommand;
import com.acainfo.payment.application.dto.GenerateMonthlyPaymentsCommand;
import com.acainfo.payment.application.dto.GeneratePaymentCommand;
import com.acainfo.payment.application.dto.MarkPaymentPaidCommand;
import com.acainfo.payment.domain.model.Payment;
import com.acainfo.payment.domain.model.PaymentStatus;
import com.acainfo.payment.domain.model.PaymentType;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.CancelPaymentRequest;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.GenerateMonthlyPaymentsRequest;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.GeneratePaymentRequest;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.MarkPaymentPaidRequest;
import com.acainfo.payment.infrastructure.adapter.in.rest.dto.PaymentResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-13T09:52:54+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Microsoft)"
)
@Component
public class PaymentRestMapperImpl implements PaymentRestMapper {

    @Override
    public GeneratePaymentCommand toCommand(GeneratePaymentRequest request) {
        if ( request == null ) {
            return null;
        }

        Long enrollmentId = null;
        PaymentType type = null;
        Integer billingMonth = null;
        Integer billingYear = null;

        enrollmentId = request.enrollmentId();
        type = request.type();
        billingMonth = request.billingMonth();
        billingYear = request.billingYear();

        GeneratePaymentCommand generatePaymentCommand = new GeneratePaymentCommand( enrollmentId, type, billingMonth, billingYear );

        return generatePaymentCommand;
    }

    @Override
    public GenerateMonthlyPaymentsCommand toCommand(GenerateMonthlyPaymentsRequest request) {
        if ( request == null ) {
            return null;
        }

        Integer billingMonth = null;
        Integer billingYear = null;

        billingMonth = request.billingMonth();
        billingYear = request.billingYear();

        GenerateMonthlyPaymentsCommand generateMonthlyPaymentsCommand = new GenerateMonthlyPaymentsCommand( billingMonth, billingYear );

        return generateMonthlyPaymentsCommand;
    }

    @Override
    public MarkPaymentPaidCommand toCommand(Long paymentId, MarkPaymentPaidRequest request) {
        if ( paymentId == null && request == null ) {
            return null;
        }

        String stripePaymentIntentId = null;
        if ( request != null ) {
            stripePaymentIntentId = request.stripePaymentIntentId();
        }
        Long paymentId1 = null;
        paymentId1 = paymentId;

        LocalDateTime paidAt = java.time.LocalDateTime.now();

        MarkPaymentPaidCommand markPaymentPaidCommand = new MarkPaymentPaidCommand( paymentId1, paidAt, stripePaymentIntentId );

        return markPaymentPaidCommand;
    }

    @Override
    public CancelPaymentCommand toCommand(Long paymentId, CancelPaymentRequest request) {
        if ( paymentId == null && request == null ) {
            return null;
        }

        String reason = null;
        if ( request != null ) {
            reason = request.reason();
        }
        Long paymentId1 = null;
        paymentId1 = paymentId;

        CancelPaymentCommand cancelPaymentCommand = new CancelPaymentCommand( paymentId1, reason );

        return cancelPaymentCommand;
    }

    @Override
    public PaymentResponse toResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        Long id = null;
        Long enrollmentId = null;
        Long studentId = null;
        PaymentType type = null;
        PaymentStatus status = null;
        BigDecimal amount = null;
        BigDecimal totalHours = null;
        BigDecimal pricePerHour = null;
        Integer billingMonth = null;
        Integer billingYear = null;
        LocalDate generatedAt = null;
        LocalDate dueDate = null;
        LocalDateTime paidAt = null;
        String description = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = payment.getId();
        enrollmentId = payment.getEnrollmentId();
        studentId = payment.getStudentId();
        type = payment.getType();
        status = payment.getStatus();
        amount = payment.getAmount();
        totalHours = payment.getTotalHours();
        pricePerHour = payment.getPricePerHour();
        billingMonth = payment.getBillingMonth();
        billingYear = payment.getBillingYear();
        generatedAt = payment.getGeneratedAt();
        dueDate = payment.getDueDate();
        paidAt = payment.getPaidAt();
        description = payment.getDescription();
        createdAt = payment.getCreatedAt();
        updatedAt = payment.getUpdatedAt();

        boolean isOverdue = payment.isOverdue();
        Integer daysOverdue = (int)(payment.getDaysOverdue());

        PaymentResponse paymentResponse = new PaymentResponse( id, enrollmentId, studentId, type, status, amount, totalHours, pricePerHour, billingMonth, billingYear, generatedAt, dueDate, paidAt, description, isOverdue, daysOverdue, createdAt, updatedAt );

        return paymentResponse;
    }

    @Override
    public List<PaymentResponse> toResponseList(List<Payment> payments) {
        if ( payments == null ) {
            return null;
        }

        List<PaymentResponse> list = new ArrayList<PaymentResponse>( payments.size() );
        for ( Payment payment : payments ) {
            list.add( toResponse( payment ) );
        }

        return list;
    }
}
