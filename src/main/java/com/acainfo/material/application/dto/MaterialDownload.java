package com.acainfo.material.application.dto;

import java.io.InputStream;

/**
 * DTO containing material download data.
 *
 * @param filename Original filename for download
 * @param mimeType MIME type for Content-Type header
 * @param fileSize File size for Content-Length header
 * @param content File content as InputStream
 */
public record MaterialDownload(
        String filename,
        String mimeType,
        Long fileSize,
        InputStream content
) {
}
