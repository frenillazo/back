package com.acainfo.reservation.infrastructure.adapter.in.rest;

import com.acainfo.reservation.infrastructure.adapter.in.rest.dto.ReservationResponse;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enriches ReservationResponse DTOs with student name and email.
 * Follows the same pattern as EnrollmentResponseEnricher.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationResponseEnricher {

    private final GetUserProfileUseCase getUserProfileUseCase;

    /**
     * Enrich a single reservation response with student data.
     */
    public ReservationResponse enrich(ReservationResponse response) {
        User student = getUserProfileUseCase.getUserById(response.getStudentId());
        response.setStudentName(student.getFullName());
        response.setStudentEmail(student.getEmail());
        return response;
    }

    /**
     * Enrich a list of reservation responses with student data.
     * Batch-fetches users to avoid N+1 queries.
     */
    public List<ReservationResponse> enrichList(List<ReservationResponse> responses) {
        if (responses.isEmpty()) {
            return responses;
        }

        Set<Long> studentIds = responses.stream()
                .map(ReservationResponse::getStudentId)
                .collect(Collectors.toSet());

        Map<Long, User> usersById = studentIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        responses.forEach(response -> {
            User student = usersById.get(response.getStudentId());
            if (student != null) {
                response.setStudentName(student.getFullName());
                response.setStudentEmail(student.getEmail());
            }
        });

        return responses;
    }
}
