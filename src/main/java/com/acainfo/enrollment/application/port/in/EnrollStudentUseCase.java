package com.acainfo.enrollment.application.port.in;

import com.acainfo.enrollment.application.dto.EnrollStudentCommand;
import com.acainfo.enrollment.domain.model.Enrollment;

/**
 * Use case for enrolling students in groups.
 * Input port defining the contract for student enrollment.
 *
 * <p>Business rules:</p>
 * <ul>
 *   <li>If group has available seats, student is enrolled as ACTIVE</li>
 *   <li>If group is full, student is added to WAITING_LIST (FIFO)</li>
 *   <li>One active enrollment per student per group</li>
 * </ul>
 */
public interface EnrollStudentUseCase {

    /**
     * Enroll a student in a group.
     * If the group is full, the student is added to the waiting list.
     *
     * @param command Enrollment data (studentId, groupId)
     * @return The created enrollment (ACTIVE or WAITING_LIST)
     * @throws com.acainfo.enrollment.domain.exception.AlreadyEnrolledException if student already enrolled
     * @throws com.acainfo.group.domain.exception.GroupNotFoundException if group not found
     * @throws com.acainfo.user.domain.exception.UserNotFoundException if student not found
     */
    Enrollment enroll(EnrollStudentCommand command);
}
