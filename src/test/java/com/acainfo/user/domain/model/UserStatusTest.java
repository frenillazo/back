package com.acainfo.user.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserStatus enum.
 */
@DisplayName("UserStatus Enum Tests")
class UserStatusTest {

    @Test
    @DisplayName("Should have ACTIVE status")
    void shouldHaveActiveStatus() {
        // When
        UserStatus status = UserStatus.ACTIVE;

        // Then
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should have PENDING_ACTIVATION status")
    void shouldHavePendingActivationStatus() {
        // When
        UserStatus status = UserStatus.PENDING_ACTIVATION;

        // Then
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("PENDING_ACTIVATION");
    }

    @Test
    @DisplayName("Should have BLOCKED status")
    void shouldHaveBlockedStatus() {
        // When
        UserStatus status = UserStatus.BLOCKED;

        // Then
        assertThat(status).isNotNull();
        assertThat(status.name()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("Should have exactly 4 statuses")
    void shouldHaveExactlyFourStatuses() {
        // When
        UserStatus[] statuses = UserStatus.values();

        // Then
        assertThat(statuses).hasSize(4);
    }

    @Test
    @DisplayName("Should be able to get status by name")
    void shouldGetStatusByName() {
        // When
        UserStatus active = UserStatus.valueOf("ACTIVE");
        UserStatus pending = UserStatus.valueOf("PENDING_ACTIVATION");
        UserStatus blocked = UserStatus.valueOf("BLOCKED");

        // Then
        assertThat(active).isEqualTo(UserStatus.ACTIVE);
        assertThat(pending).isEqualTo(UserStatus.PENDING_ACTIVATION);
        assertThat(blocked).isEqualTo(UserStatus.BLOCKED);
    }
}
