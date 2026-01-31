package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * Result of a batch deactivation operation.
 */
public record DeactivationResult(
        int totalProcessed,
        int deactivated,
        int skipped,
        List<String> errors
) {
}
