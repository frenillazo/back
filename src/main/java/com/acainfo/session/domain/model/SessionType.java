package com.acainfo.session.domain.model;

public enum SessionType {
    REGULAR,    // Sesión instanciada desde un Schedule de un grupo regular
    EXTRA,      // Sesión extraordinaria (sin schedule) para repasar, examen, etc.
    SCHEDULING, // Reunión online previa a la creación del grupo, para acordar horarios
    INTENSIVE   // Sesión puntual de un curso intensivo (sin schedule, fechas/horas arbitrarias)
}
