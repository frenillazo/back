package com.acainfo.session.domain.model;

import com.acainfo.schedule.domain.model.Classroom;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Session {

    private Long id;
    private Long subjectId;
    private Long scheduleId; //Nullable en JPA, una session puede existir sin un schedule salvo cuando -type != REGULAR-, cada schedule da lugar a sesiones tipo REGULAR que pueden ser canceladas o postpuestas
    private Classroom classroom; //Lo coge del schedule referenfiado por el id, pero si no es de type REGULAR se rellena manualmente por el administrador
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;
    private SessionType type;
    private SessionMode mode;
    private LocalDate postponedToDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isProgramada(){
        return status == SessionStatus.PROGRAMADA;
    }

    public boolean isEnCurso(){
        return status == SessionStatus.EN_CURSO;
    }

    public boolean isCompletada(){
        return status == SessionStatus.COMPLETADA;
    }

    public boolean isCancelada(){
        return status == SessionStatus.CANCELADA;
    }

    public boolean isPostpuesta(){
        return status == SessionStatus.POSTPUESTA;
    }

    public boolean isRegular() { return type == SessionType.REGULAR; }

    public boolean isExtra() { return type == SessionType.EXTRA; }

    public boolean isScheduling() { return type == SessionType.SCHEDULING; }

    public boolean isPresencial() { return mode == SessionMode.PRESENCIAL; }

    public boolean isOnline() { return mode == SessionMode.ONLINE; }

    public boolean isDual() { return mode == SessionMode.DUAL; }

    public long getDurationMinutes(){
        return Duration.between(startTime, endTime).toMinutes();
    }
}
