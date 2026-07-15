package com.acainfo.material.application.dto;

import java.nio.file.Path;

/**
 * A GENERATE capture persisted to a tmp file while the job waits its turn
 * (the multipart InputStream dies when the request returns; byte arrays in
 * the queue would pin memory). The pipeline reads it and deletes the tmp dir.
 */
public record StoredCapture(
        Path path,
        String mimeType
) {
}
