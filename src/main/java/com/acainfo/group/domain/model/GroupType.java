package com.acainfo.group.domain.model;

/**
 * Type of subject group combining schedule type and academic period.
 * Determines the maximum capacity and session modality.
 */
public enum GroupType {
    /**
     * Regular group in first semester (Q1).
     * Max capacity: 24 students (mostly in-person sessions).
     */
    REGULAR_Q1,

    /**
     * Intensive group in first semester (Q1).
     * Max capacity: 50 students (dual/hybrid sessions).
     */
    INTENSIVE_Q1,

    /**
     * Regular group in second semester (Q2).
     * Max capacity: 24 students (mostly in-person sessions).
     */
    REGULAR_Q2,

    /**
     * Intensive group in second semester (Q2).
     * Max capacity: 50 students (dual/hybrid sessions).
     */
    INTENSIVE_Q2;

    /**
     * Check if this is a regular type (Q1 or Q2).
     */
    public boolean isRegular() {
        return this == REGULAR_Q1 || this == REGULAR_Q2;
    }

    /**
     * Check if this is an intensive type (Q1 or Q2).
     */
    public boolean isIntensive() {
        return this == INTENSIVE_Q1 || this == INTENSIVE_Q2;
    }

    /**
     * Check if this is a first semester type (Q1).
     */
    public boolean isFirstSemester() {
        return this == REGULAR_Q1 || this == INTENSIVE_Q1;
    }

    /**
     * Check if this is a second semester type (Q2).
     */
    public boolean isSecondSemester() {
        return this == REGULAR_Q2 || this == INTENSIVE_Q2;
    }
}
