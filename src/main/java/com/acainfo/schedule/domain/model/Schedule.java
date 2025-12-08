package com.acainfo.schedule.domain.model;

import lombok.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Schedule domain entity (POJO).
 * Represents a schedule for a group on a specific day and time.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class Schedule {

    private Long id;
    private Long groupId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Classroom classroom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Query methods

    /**/
    public boolean isPhysical(){
        return classroom == Classroom.AULA_PORTAL1 || classroom == Classroom.AULA_PORTAL2;
    }

    public boolean isOnline(){
        return classroom == Classroom.AULA_VIRTUAL;
    }

    public long getDurationMinutes(){
        return Duration.between(startTime, endTime).toMinutes();
    }

}
