package com.acainfo.session.domain.model;

import lombok.*;

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
    private Long subjectId; //Nullable en JPA, toda sesión está asociada a una asignatura salvo cuando -type == SCHEDULING-
    private Long scheduleId; //Nullable en JPA, una session puede existir sin un schedule -type != REGULAR-, cada schedule da lugar a sesiones tipo REGULAR que pueden ser canceladas o postpuestas
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionStatus status;
    private SessionType type;
    private SessionMode mode;
    private LocalDate postponedToDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Boolean isProgramada(){
        return status == SessionStatus.PROGRAMADA;
    }

    public Boolean isEnCurso(){
        return status == SessionStatus.EN_CURSO;
    }

    public Boolean isCompletada(){
        return status == SessionStatus.COMPLETADA;
    }

    public Boolean isCancelada(){
        return status == SessionStatus.CANCELADA;
    }

    public Boolean isPostpuesta(){
        return status == SessionStatus.POSTPUESTA;
    }

    public Boolean isRegular(){
        return type == SessionType.REGULAR;
    }

    public Boolean isRecuperacion(){
        return type == SessionType.POSTPONED;
    }

    public Boolean isExtra(){
        return type == SessionType.EXTRA;
    }

    public Boolean isHorarios(){
        return type == SessionType.SCHEDULING;
    }

    public Boolean isPhysical(){
        return mode == SessionMode.PRESENCIAL;
    }

    public Boolean isOnline(){
        return mode == SessionMode.ONLINE;
    }

    public Boolean isDual(){
        return mode == SessionMode.DUAL;
    }
}
