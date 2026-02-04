package com.acainfo.session.domain.exception;

import com.acainfo.shared.domain.exception.BusinessRuleException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Exception thrown when a session conflicts with another session of the same teacher.
 * Teacher conflicts are allowed only when both sessions are:
 * 1. Online (SessionMode.ONLINE)
 * 2. From the same subject
 */
public class TeacherSessionConflictException extends BusinessRuleException {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TeacherSessionConflictException(String teacherName, LocalDate date,
            LocalTime startTime, LocalTime endTime) {
        super(String.format(
                "El profesor %s ya tiene una sesión programada el %s entre %s y %s. " +
                "Solo se permiten solapamientos si ambas sesiones son online y de la misma asignatura.",
                teacherName,
                date.format(DATE_FORMATTER),
                startTime,
                endTime
        ));
    }

    public TeacherSessionConflictException(String message) {
        super(message);
    }

    @Override
    public String getErrorCode() {
        return "TEACHER_SESSION_CONFLICT";
    }
}
