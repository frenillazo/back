package com.acainfo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies that the full Spring application context starts
 * with the 'test' profile (H2 in-memory, ddl-auto=create-drop, data-test.sql seed).
 *
 * If this test fails, every other integration test will fail too:
 * fix context startup first.
 */
@SpringBootTest
@ActiveProfiles("test")
class AcaInfoApplicationSmokeTest {

    @Test
    void contextLoads() {
        // Intentionally empty: the test passes if the application context starts.
    }
}
