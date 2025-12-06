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
    private Long groupId;           // Reference to SubjectGroup (aggregate independence)
    private DayOfWeek dayOfWeek;    // MONDAY, TUESDAY, etc.
    private LocalTime startTime;    // 09:00
    private LocalTime endTime;      // 11:00
    private Classroom classroom;    // Enum: AULA_PORTAL1, AULA_PORTAL2, AULA_VIRTUAL
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
