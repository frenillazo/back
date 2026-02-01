package com.acainfo.user.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * Result of a batch activation operation.
 */
public record ActivationResult(
        int totalProcessed,
        int activated,
        int skipped,
        List<String> errors
) {
}
