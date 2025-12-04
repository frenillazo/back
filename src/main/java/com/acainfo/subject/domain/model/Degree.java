package com.acainfo.subject.domain.model;

import lombok.Getter;

/**
 * Represents the academic degree to which a subject belongs.
 * This is a domain enum with no framework dependencies.
 */
@Getter
public enum Degree {
    INGENIERIA_INFORMATICA("Ingeniería Informática"),
    INGENIERIA_INDUSTRIAL("Ingeniería Industrial");

    private final String displayName;

    Degree(String displayName) {
        this.displayName = displayName;
    }

}
