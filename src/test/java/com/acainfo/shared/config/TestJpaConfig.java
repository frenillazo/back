package com.acainfo.shared.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for JPA integration tests.
 * Enables JPA repositories and entity scanning.
 */
@TestConfiguration
@EnableJpaRepositories(basePackages = "com.acainfo.**.infrastructure.adapter.out.persistence.repository")
@EntityScan(basePackages = "com.acainfo.**.infrastructure.adapter.out.persistence.entity")
@EnableJpaAuditing
public class TestJpaConfig {
}
