package com.acainfo.intensive.infrastructure.adapter.in.rest;

import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.IntensiveResponse;
import com.acainfo.intensive.infrastructure.mapper.IntensiveRestMapper;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enriches IntensiveResponse with subject + teacher names (denormalised view).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntensiveResponseEnricher {

    private final IntensiveRestMapper intensiveRestMapper;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;

    public IntensiveResponse enrich(Intensive intensive) {
        Subject subject = getSubjectUseCase.getById(intensive.getSubjectId());
        User teacher = getUserProfileUseCase.getUserById(intensive.getTeacherId());

        return intensiveRestMapper.toEnrichedResponse(
                intensive,
                subject.getName(),
                subject.getCode(),
                teacher.getFullName()
        );
    }

    public List<IntensiveResponse> enrichList(List<Intensive> intensives) {
        if (intensives.isEmpty()) return List.of();

        Set<Long> subjectIds = intensives.stream().map(Intensive::getSubjectId).collect(Collectors.toSet());
        Set<Long> teacherIds = intensives.stream().map(Intensive::getTeacherId).collect(Collectors.toSet());

        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));
        Map<Long, User> teachersById = teacherIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return intensives.stream()
                .map(i -> {
                    Subject s = subjectsById.get(i.getSubjectId());
                    User t = teachersById.get(i.getTeacherId());
                    return intensiveRestMapper.toEnrichedResponse(
                            i, s.getName(), s.getCode(), t.getFullName()
                    );
                })
                .toList();
    }

    public Page<IntensiveResponse> enrichPage(Page<Intensive> page) {
        List<IntensiveResponse> enriched = enrichList(page.getContent());
        Map<Long, IntensiveResponse> byId = enriched.stream()
                .collect(Collectors.toMap(IntensiveResponse::getId, Function.identity()));
        return page.map(i -> byId.get(i.getId()));
    }
}
