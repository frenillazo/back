package com.acainfo.shared.infrastructure.config;

import com.acainfo.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security configuration.
 * Includes JWT authentication filter.
 *
 * This configuration provides:
 * - Password encoding with BCrypt
 * - Authentication manager for manual authentication
 * - CORS configuration for frontend integration
 * - Public endpoints for authentication
 * - Stateless session management with JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Whether to enable Swagger UI publicly.
     * Set to false in production to restrict API documentation access.
     * Default: true (for development)
     */
    @Value("${app.swagger.enabled:true}")
    private boolean swaggerEnabled;

    /**
     * Password encoder using BCrypt hashing algorithm.
     * Uses default strength (10 rounds).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager for manual authentication.
     * Used by AuthService to authenticate users programmatically.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Security filter chain configuration.
     *
     * Configures:
     * - CSRF disabled (stateless REST API)
     * - CORS enabled
     * - Stateless sessions with JWT
     * - Public endpoints for auth
     * - All other endpoints require authentication
     * - JWT authentication filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless REST API)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session (JWT-based, no server-side sessions)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> {
                        // Build list of public endpoints
                        List<String> publicEndpoints = new ArrayList<>(List.of(
                                "/api/auth/**",
                                "/api/public/**",
                                "/actuator/health"
                        ));

                        // Only expose Swagger publicly if enabled (typically in dev)
                        if (swaggerEnabled) {
                            publicEndpoints.add("/v3/api-docs/**");
                            publicEndpoints.add("/swagger-ui/**");
                            publicEndpoints.add("/swagger-ui.html");
                        }

                        // Public endpoints (authentication)
                        auth.requestMatchers(publicEndpoints.toArray(new String[0])).permitAll();

                        // All other endpoints require authentication
                        auth.anyRequest().authenticated();
                })

                // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration.
     * Allows requests from frontend (localhost:3000, 4200 for development).
     *
     * Configure allowed origins, methods, headers, and credentials.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins (frontend URLs)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",  // React default
                "http://localhost:4200",  // Angular default
                "http://localhost:8080",   // Same origin
                "http://localhost:5173"
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allowed headers (all)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
