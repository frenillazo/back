package com.acainfo.schedule.domain.model;

import lombok.Getter;

/**
 * Classroom enumeration.
 * The center has only 3 fixed classrooms:
 * - AULA_PORTAL1: Physical classroom 1 (24 seats)
 * - AULA_PORTAL2: Physical classroom 2 (24 seats)
 * - AULA_VIRTUAL: Virtual classroom (unlimited capacity)
 */
@Getter
public enum Classroom {
    AULA_PORTAL1("aula_portal1", "Aula Portal 1", 24, true),
    AULA_PORTAL2("aula_portal2", "Aula Portal 2", 24, true),
    AULA_VIRTUAL("aulavirtual", "Aula Virtual", null, false);

    private final String code;
    private final String displayName;
    private final Integer capacity;  // null = unlimited
    private final boolean isPhysical;

    Classroom(String code, String displayName, Integer capacity, boolean isPhysical) {
        this.code = code;
        this.displayName = displayName;
        this.capacity = capacity;
        this.isPhysical = isPhysical;
    }
}
