package com.acainfo.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT configuration properties.
 * Loaded from application.properties with prefix "jwt".
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens.
     * IMPORTANT: Must be at least 256 bits (32 characters) for HS256 algorithm.
     * In production, load from environment variable or secret manager.
     */
    private String secret = "your-256-bit-secret-key-change-this-in-production-minimum-32-characters";

    /**
     * Access token expiration time in milliseconds.
     * Default: 15 minutes (900000 ms).
     */
    private long accessTokenExpiration = 900000; // 15 min

    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days (604800000 ms).
     */
    private long refreshTokenExpiration = 604800000; // 7 days

    /**
     * Token prefix in Authorization header.
     * Default: "Bearer ".
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Authorization header name.
     */
    private String headerName = "Authorization";
}
