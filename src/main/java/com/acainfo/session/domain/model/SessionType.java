package com.acainfo.session.domain.model;

public enum SessionType {
    REGULAR,    // Sesión instanciada desde un Schedule del curso
    EXTRA       // Sesión extraordinaria (sin schedule) para repasar, examen, etc.
}
