package com.acainfo.schedule.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Exception thrown when a schedule conflicts with another schedule of the same teacher.
 * Teacher conflicts are allowed only when both schedules are:
 * 1. Online (AULA_VIRTUAL)
 * 2. From the same subject
 */
public class TeacherScheduleConflictException extends BusinessRuleException {

    public TeacherScheduleConflictException(String teacherName, DayOfWeek dayOfWeek,
            LocalTime startTime, LocalTime endTime) {
        super(String.format(
                "El profesor %s ya tiene una sesión programada el %s entre %s y %s. " +
                "Solo se permiten solapamientos si ambas sesiones son online y de la misma asignatura.",
                teacherName,
                translateDayOfWeek(dayOfWeek),
                startTime,
                endTime
        ));
    }

    public TeacherScheduleConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "TEACHER_SCHEDULE_CONFLICT";
    }

    private static String translateDayOfWeek(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }
}
