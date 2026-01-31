package com.acainfo.shared.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for email functionality.
 */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Getter
@Setter
public class EmailProperties {

    /**
     * Email address used as the sender (From field).
     */
    private String from = "noreply@acainfo.com";

    /**
     * Whether to mock email sending (log instead of actually sending).
     * Useful for development and testing.
     */
    private boolean mock = false;

    /**
     * Verification email settings.
     */
    private Verification verification = new Verification();

    @Getter
    @Setter
    public static class Verification {
        /**
         * Number of hours before verification token expires.
         */
        private int expirationHours = 24;

        /**
         * Base URL for verification links (e.g., http://localhost:5173/verify-email).
         */
        private String baseUrl = "http://localhost:5173/verify-email";
    }
}
