package com.acainfo.reservation.infrastructure.adapter.out.persistence.specification;

import com.acainfo.reservation.application.dto.ReservationFilters;
import com.acainfo.reservation.domain.model.AttendanceStatus;
import com.acainfo.reservation.domain.model.OnlineRequestStatus;
import com.acainfo.reservation.domain.model.ReservationMode;
import com.acainfo.reservation.domain.model.ReservationStatus;
import com.acainfo.reservation.infrastructure.adapter.out.persistence.entity.SessionReservationJpaEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for SessionReservationJpaEntity (Criteria Builder).
 * Translates ReservationFilters (application DTO) to JPA Specifications.
 */
public class ReservationSpecifications {

    private ReservationSpecifications() {
        // Utility class
    }

    /**
     * Build dynamic specification from filters.
     * Combines all filter predicates with AND logic.
     *
     * @param filters Application filters
     * @return JPA Specification with combined predicates
     */
    public static Specification<SessionReservationJpaEntity> withFilters(ReservationFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by studentId
            if (filters.studentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("studentId"), filters.studentId()));
            }

            // Filter by sessionId
            if (filters.sessionId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("sessionId"), filters.sessionId()));
            }

            // Filter by enrollmentId
            if (filters.enrollmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enrollmentId"), filters.enrollmentId()));
            }

            // Filter by status
            if (filters.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filters.status()));
            }

            // Filter by mode
            if (filters.mode() != null) {
                predicates.add(criteriaBuilder.equal(root.get("mode"), filters.mode()));
            }

            // Filter by online request status
            if (filters.onlineRequestStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("onlineRequestStatus"), filters.onlineRequestStatus()));
            }

            // Filter by attendance status
            if (filters.attendanceStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("attendanceStatus"), filters.attendanceStatus()));
            }

            // Filter by hasAttendanceRecorded
            if (filters.hasAttendanceRecorded() != null) {
                if (filters.hasAttendanceRecorded()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("attendanceStatus")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("attendanceStatus")));
                }
            }

            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification to find reservations by studentId.
     */
    public static Specification<SessionReservationJpaEntity> hasStudentId(Long studentId) {
        return (root, query, criteriaBuilder) -> {
            if (studentId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("studentId"), studentId);
        };
    }

    /**
     * Specification to find reservations by sessionId.
     */
    public static Specification<SessionReservationJpaEntity> hasSessionId(Long sessionId) {
        return (root, query, criteriaBuilder) -> {
            if (sessionId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("sessionId"), sessionId);
        };
    }

    /**
     * Specification to find reservations by status.
     */
    public static Specification<SessionReservationJpaEntity> hasStatus(ReservationStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * Specification to find reservations by mode.
     */
    public static Specification<SessionReservationJpaEntity> hasMode(ReservationMode mode) {
        return (root, query, criteriaBuilder) -> {
            if (mode == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("mode"), mode);
        };
    }

    /**
     * Specification to find reservations by online request status.
     */
    public static Specification<SessionReservationJpaEntity> hasOnlineRequestStatus(OnlineRequestStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("onlineRequestStatus"), status);
        };
    }

    /**
     * Specification to find reservations by attendance status.
     */
    public static Specification<SessionReservationJpaEntity> hasAttendanceStatus(AttendanceStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("attendanceStatus"), status);
        };
    }

    /**
     * Specification to find confirmed reservations.
     */
    public static Specification<SessionReservationJpaEntity> isConfirmed() {
        return hasStatus(ReservationStatus.CONFIRMED);
    }

    /**
     * Specification to find in-person reservations.
     */
    public static Specification<SessionReservationJpaEntity> isInPerson() {
        return hasMode(ReservationMode.IN_PERSON);
    }

    /**
     * Specification to find online reservations.
     */
    public static Specification<SessionReservationJpaEntity> isOnline() {
        return hasMode(ReservationMode.ONLINE);
    }

    /**
     * Specification to find reservations with pending online requests.
     */
    public static Specification<SessionReservationJpaEntity> hasPendingOnlineRequest() {
        return hasOnlineRequestStatus(OnlineRequestStatus.PENDING);
    }

    /**
     * Specification to find reservations without attendance recorded.
     */
    public static Specification<SessionReservationJpaEntity> attendanceNotRecorded() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("attendanceStatus"));
    }

    /**
     * Specification to find reservations with attendance recorded.
     */
    public static Specification<SessionReservationJpaEntity> attendanceRecorded() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotNull(root.get("attendanceStatus"));
    }
}
