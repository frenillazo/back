package com.acainfo.material.application.dto;

/**
 * One capture (photo/screenshot of worked exercises) fed to the GENERATE mode.
 *
 * @param data     Raw image bytes
 * @param mimeType Image MIME type (image/png, image/jpeg...)
 */
public record AiImageInput(
        byte[] data,
        String mimeType
) {
}
