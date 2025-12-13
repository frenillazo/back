package com.acainfo.material.domain.model;

import java.util.Set;

/**
 * Allowed file types configuration.
 * Whitelist of file extensions that can be uploaded.
 */
public final class AllowedFileTypes {

    private AllowedFileTypes() {
        // Utility class
    }

    /**
     * Whitelisted file extensions (lowercase, without dot).
     */
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            // Documents
            "pdf", "docx", "txt", "md",
            // Code
            "java", "cpp", "c", "h", "py",
            // Archives
            "zip"
    );

    /**
     * Check if a file extension is allowed.
     */
    public static boolean isAllowed(String extension) {
        if (extension == null) return false;
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * Get extension from filename.
     */
    public static String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
