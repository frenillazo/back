package acainfo.back.material.domain.model;

import acainfo.back.material.domain.exception.InvalidFileTypeException;

/**
 * Enum representing the type of educational material file.
 * According to plan, supported file types are: PDF, Java, C++, and Header files.
 */
public enum MaterialType {
    PDF("application/pdf", ".pdf", "PDF Document"),
    JAVA("text/x-java", ".java", "Java Source Code"),
    CPP("text/x-c++src", ".cpp", "C++ Source Code"),
    HEADER("text/x-c++hdr", ".h", "C/C++ Header File");

    private final String mimeType;
    private final String extension;
    private final String displayName;

    MaterialType(String mimeType, String extension, String displayName) {
        this.mimeType = mimeType;
        this.extension = extension;
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Determines the material type from a file name.
     *
     * @param fileName the file name
     * @return the material type
     * @throws InvalidFileTypeException if the file type is not supported
     */
    public static MaterialType fromFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new InvalidFileTypeException("File name cannot be null or empty");
        }

        String lowerCase = fileName.toLowerCase();

        if (lowerCase.endsWith(".pdf")) return PDF;
        if (lowerCase.endsWith(".java")) return JAVA;
        if (lowerCase.endsWith(".cpp")) return CPP;
        if (lowerCase.endsWith(".h")) return HEADER;

        throw new InvalidFileTypeException("Unsupported file type: " + fileName +
            ". Allowed types: .pdf, .java, .cpp, .h");
    }

    /**
     * Check if this is a code file (not PDF).
     */
    public boolean isCode() {
        return this == JAVA || this == CPP || this == HEADER;
    }

    /**
     * Check if this is a PDF document.
     */
    public boolean isPdf() {
        return this == PDF;
    }
}
