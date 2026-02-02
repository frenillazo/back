package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for rejecting an enrollment.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectEnrollmentRequest {

    /**
     * Optional reason for rejection.
     */
    private String reason;
}
