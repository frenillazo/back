package com.acainfo.security.terms;

import com.acainfo.security.userdetails.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST Controller for terms and conditions acceptance.
 * All endpoints require authentication (JWT).
 */
@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Terms & Conditions", description = "Terms and conditions acceptance endpoints")
public class TermsController {

    private final TermsAcceptanceService termsAcceptanceService;

    @GetMapping("/status")
    @Operation(
            summary = "Get terms acceptance status",
            description = "Check if the authenticated user has accepted the current terms version"
    )
    public ResponseEntity<Map<String, Object>> getStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        boolean accepted = termsAcceptanceService.hasAcceptedCurrentTerms(userId);
        String currentVersion = termsAcceptanceService.getCurrentTermsVersion();

        return ResponseEntity.ok(Map.of(
                "accepted", accepted,
                "currentVersion", currentVersion
        ));
    }

    @PostMapping("/accept")
    @Operation(
            summary = "Accept terms and conditions",
            description = "Record the user's acceptance of the current terms version"
    )
    public ResponseEntity<Map<String, Object>> acceptTerms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = userDetails.getUserId();
        String ipAddress = extractClientIp(request);

        TermsAcceptance acceptance = termsAcceptanceService.acceptTerms(userId, ipAddress);

        log.info("User {} accepted terms version {} from IP {}", userId, acceptance.getTermsVersion(), ipAddress);

        return ResponseEntity.ok(Map.of(
                "message", "Términos y condiciones aceptados correctamente",
                "version", acceptance.getTermsVersion(),
                "acceptedAt", acceptance.getAcceptedAt().toString()
        ));
    }

    /**
     * Extract the real client IP address.
     * Cloudflare sets CF-Connecting-IP and X-Forwarded-For headers.
     */
    private String extractClientIp(HttpServletRequest request) {
        // Cloudflare specific header (most reliable)
        String ip = request.getHeader("CF-Connecting-IP");
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        // Standard proxy header
        ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // X-Forwarded-For can contain multiple IPs: client, proxy1, proxy2
            return ip.split(",")[0].trim();
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }
}
