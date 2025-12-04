package com.acainfo.subject.domain.model;

/**
 * Represents the academic degree to which a subject belongs.
 * This is a domain enum with no framework dependencies.
 */
public enum Degree {
    INGENIERIA_INFORMATICA("Ingeniería Informática"),
    INGENIERIA_SOFTWARE("Ingeniería de Software"),
    CIENCIAS_COMPUTACION("Ciencias de la Computación");

    private final String displayName;

    Degree(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
