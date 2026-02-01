package com.acainfo.enrollment.infrastructure.adapter.in.rest.dto;

import lombok.*;

/**
 * DTO for subject interest summary.
 * Shows how many students are interested in each subject.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectInterestSummary {
    private Long subjectId;
    private String subjectName;
    private String subjectCode;
    private String degreeName;
    private Integer interestedCount;
}
