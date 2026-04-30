package com.acainfo.intensive.domain.model;

/**
 * Status of an intensive course.
 *
 * <ul>
 *   <li>OPEN: Open for enrollments and active</li>
 *   <li>CLOSED: Closed for new enrollments but ongoing</li>
 *   <li>CANCELLED: Cancelled, won't take place</li>
 * </ul>
 */
public enum IntensiveStatus {
    OPEN,
    CLOSED,
    CANCELLED
}
