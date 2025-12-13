package com.acainfo.payment.infrastructure.adapter.in.rest.dto;

/**
 * REST request DTO for cancelling a payment.
 */
public record CancelPaymentRequest(
        String reason
) {
}
