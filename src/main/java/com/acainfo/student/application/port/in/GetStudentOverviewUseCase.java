package com.acainfo.student.application.port.in;

import com.acainfo.student.application.dto.StudentOverviewResponse;

/**
 * Use case for retrieving aggregated student dashboard data.
 */
public interface GetStudentOverviewUseCase {

    /**
     * Get overview for a student with a specified upcoming sessions limit.
     *
     * @param studentId Student ID
     * @param upcomingSessionsLimit Maximum number of upcoming sessions to return
     * @return Aggregated overview response
     */
    StudentOverviewResponse getOverview(Long studentId, int upcomingSessionsLimit);

    /**
     * Get overview for a student with default upcoming sessions limit.
     *
     * @param studentId Student ID
     * @return Aggregated overview response
     */
    StudentOverviewResponse getOverview(Long studentId);
}
