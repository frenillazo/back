package com.acainfo.subject.domain.model;

import lombok.*;

import java.time.LocalDateTime;

/**
 * SubjectInterest domain entity (anemic model with Lombok).
 *
 * <p>Minimal "me interesa" record: one row per (subject, student). Replaces the old
 * GroupRequest flow — no approval workflow, no supporters, no expiration. The admin
 * uses the aggregated counts to decide when to open a new course for a subject.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
@ToString
public class SubjectInterest {

    private Long id;
    private Long subjectId;
    private Long studentId;
    private LocalDateTime createdAt;
}
