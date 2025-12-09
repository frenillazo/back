package com.acainfo.session.domain.model;

public enum SessionType {
    REGULAR, //Sesión instanciada desde un Schedule
    EXTRA, //Sesión que se crea ajena a los schedules para repasar, hacer un simulacro de examen o corregir alguna tarea de forma extraordinaria
    SCHEDULING //Tipo de reunión online de ocurrencia única cuyo objetivo es acordar horarios con los alumnos al configurar un grupo nuevo
}
