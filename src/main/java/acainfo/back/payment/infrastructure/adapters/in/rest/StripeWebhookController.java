package acainfo.back.payment.infrastructure.adapters.in.rest;

import acainfo.back.payment.application.services.PaymentService;
import acainfo.back.payment.application.services.StripeService;
import acainfo.back.payment.domain.model.Payment;
import acainfo.back.payment.infrastructure.adapters.out.PaymentRepository;
import acainfo.back.shared.domain.model.User;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling Stripe webhook events.
 * Stripe will send HTTP POST requests to this endpoint when payment events occur.
 *
 * Important: This endpoint should NOT require authentication as it's called by Stripe servers.
 * Security is handled via webhook signature verification.
 *
 * Webhook URL (configure in Stripe dashboard):
 * https://yourdomain.com/api/webhooks/stripe
 *
 * Events to listen for:
 * - payment_intent.succeeded: Payment completed successfully
 * - payment_intent.payment_failed: Payment failed
 * - payment_intent.canceled: Payment canceled
 */
@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
@Hidden // Hide from Swagger documentation as it's for external use
public class StripeWebhookController {

    private final StripeService stripeService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    /**
     * Handle Stripe webhook events.
     *
     * @param payload Raw webhook payload from Stripe
     * @param signature Stripe signature header for verification
     * @return HTTP 200 to acknowledge receipt
     */
    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        log.info("Received Stripe webhook event");

        try {
            // 1. Verify webhook signature
            boolean isValid = stripeService.verifyWebhookSignature(payload, signature);
            if (!isValid) {
                log.error("Invalid Stripe webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // 2. Parse event (simplified for now - real implementation would use Stripe SDK)
            // TODO: When integrating real Stripe SDK, use Event.PARSER.parse(payload)
            log.info("Webhook payload received (mock processing): {}", payload.substring(0, Math.min(100, payload.length())));

            // 3. Handle event based on type
            // In real implementation:
            // Event event = Event.PARSER.parse(payload);
            // switch (event.getType()) {
            //     case "payment_intent.succeeded":
            //         handlePaymentSucceeded(event);
            //         break;
            //     case "payment_intent.payment_failed":
            //         handlePaymentFailed(event);
            //         break;
            //     case "payment_intent.canceled":
            //         handlePaymentCanceled(event);
            //         break;
            // }

            // Mock success response
            log.info("Stripe webhook processed successfully");
            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Webhook processing failed");
        }
    }

    /**
     * Handle successful payment event from Stripe.
     * Updates the payment record to PAGADO status.
     */
    private void handlePaymentSucceeded(String paymentIntentId) {
        log.info("Processing payment_intent.succeeded for: {}", paymentIntentId);

        try {
            // Find payment by Stripe payment ID
            Payment payment = paymentRepository.findByStripePaymentId(paymentIntentId)
                .orElseThrow(() -> new IllegalStateException(
                    "Payment not found for Stripe payment ID: " + paymentIntentId));

            // Mark payment as paid (use system user as processor)
            User systemUser = null; // TODO: Create system user or handle differently
            paymentService.processPayment(payment.getId(), paymentIntentId, systemUser);

            log.info("Payment {} marked as paid via webhook", payment.getId());

        } catch (Exception e) {
            log.error("Error handling payment_intent.succeeded for {}", paymentIntentId, e);
            // Don't throw - Stripe will retry the webhook
        }
    }

    /**
     * Handle failed payment event from Stripe.
     */
    private void handlePaymentFailed(String paymentIntentId) {
        log.warn("Processing payment_intent.payment_failed for: {}", paymentIntentId);

        try {
            // Find payment and possibly notify student
            Payment payment = paymentRepository.findByStripePaymentId(paymentIntentId)
                .orElseThrow(() -> new IllegalStateException(
                    "Payment not found for Stripe payment ID: " + paymentIntentId));

            log.info("Payment {} failed via Stripe", payment.getId());
            // TODO: Send notification to student about failed payment

        } catch (Exception e) {
            log.error("Error handling payment_intent.payment_failed for {}", paymentIntentId, e);
        }
    }

    /**
     * Handle canceled payment event from Stripe.
     */
    private void handlePaymentCanceled(String paymentIntentId) {
        log.info("Processing payment_intent.canceled for: {}", paymentIntentId);

        try {
            // Find payment and possibly update status
            Payment payment = paymentRepository.findByStripePaymentId(paymentIntentId)
                .orElseThrow(() -> new IllegalStateException(
                    "Payment not found for Stripe payment ID: " + paymentIntentId));

            // Cancel the payment
            User systemUser = null; // TODO: Create system user
            paymentService.cancelPayment(payment.getId(), "Canceled via Stripe", systemUser);

            log.info("Payment {} canceled via webhook", payment.getId());

        } catch (Exception e) {
            log.error("Error handling payment_intent.canceled for {}", paymentIntentId, e);
        }
    }

    /**
     * Health check endpoint for webhook configuration testing.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Stripe webhook endpoint is healthy");
    }
}
