package com.acainfo.payment.infrastructure.adapter.in.rest.dto;

/**
 * REST request DTO for marking a payment as paid.
 */
public record MarkPaymentPaidRequest(
        String stripePaymentIntentId
) {
}
