package com.acainfo.shared.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Test configuration for JPA integration tests.
 * Enables component scanning for mappers and adapters.
 */
@TestConfiguration
@ComponentScan(basePackages = {
        "com.acainfo.user.infrastructure.mapper",
        "com.acainfo.user.infrastructure.adapter.out.persistence.repository"
})
public class TestJpaConfig {
}
