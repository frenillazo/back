package com.acainfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the AcaInfo training center management system.
 *
 * This application uses Hexagonal Architecture with the following structure:
 * - domain: Pure Java domain models (no framework dependencies)
 * - application: Use cases and business logic
 * - infrastructure: Adapters for persistence, REST, external services
 */
@SpringBootApplication
public class AcaInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcaInfoApplication.class, args);
    }

}
