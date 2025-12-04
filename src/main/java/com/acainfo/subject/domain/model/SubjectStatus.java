package com.acainfo.subject.domain.model;

/**
 * Represents the lifecycle status of a subject.
 * This is a domain enum with no framework dependencies.
 */
public enum SubjectStatus {
    /**
     * Subject is active and available for group creation
     */
    ACTIVE,

    /**
     * Subject is temporarily inactive
     */
    INACTIVE,

    /**
     * Subject is archived and no longer in use
     */
    ARCHIVED
}
