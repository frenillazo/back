package com.acainfo.intensive.infrastructure.adapter.in.rest;

import com.acainfo.intensive.application.dto.IntensiveFilters;
import com.acainfo.intensive.application.port.in.CreateIntensiveUseCase;
import com.acainfo.intensive.application.port.in.DeleteIntensiveUseCase;
import com.acainfo.intensive.application.port.in.GetIntensiveUseCase;
import com.acainfo.intensive.application.port.in.UpdateIntensiveUseCase;
import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.intensive.domain.model.IntensiveStatus;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.BulkCreateIntensiveSessionsRequest;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.CreateIntensiveRequest;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.IntensiveResponse;
import com.acainfo.intensive.infrastructure.adapter.in.rest.dto.UpdateIntensiveRequest;
import com.acainfo.intensive.infrastructure.mapper.IntensiveRestMapper;
import com.acainfo.session.application.dto.IntensiveSessionEntry;
import com.acainfo.session.application.port.in.CreateIntensiveSessionsUseCase;
import com.acainfo.session.domain.model.Session;
import com.acainfo.shared.application.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Intensive courses.
 *
 * Endpoints under {@code /api/intensives}.
 *
 * Security:
 * <ul>
 *   <li>GET (list, byId) → authenticated users</li>
 *   <li>POST, PATCH, DELETE → ADMIN only</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/intensives")
@RequiredArgsConstructor
@Slf4j
public class IntensiveController {

    private final CreateIntensiveUseCase createIntensiveUseCase;
    private final UpdateIntensiveUseCase updateIntensiveUseCase;
    private final GetIntensiveUseCase getIntensiveUseCase;
    private final DeleteIntensiveUseCase deleteIntensiveUseCase;
    private final IntensiveRestMapper intensiveRestMapper;
    private final IntensiveResponseEnricher intensiveResponseEnricher;
    private final CreateIntensiveSessionsUseCase createIntensiveSessionsUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IntensiveResponse> create(@Valid @RequestBody CreateIntensiveRequest request) {
        log.info("REST: Creating intensive for subject {}", request.getSubjectId());
        Intensive created = createIntensiveUseCase.create(intensiveRestMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(intensiveResponseEnricher.enrich(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<IntensiveResponse> getById(@PathVariable Long id) {
        Intensive intensive = getIntensiveUseCase.getById(id);
        return ResponseEntity.ok(intensiveResponseEnricher.enrich(intensive));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<IntensiveResponse>> list(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) IntensiveStatus status,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        IntensiveFilters filters = new IntensiveFilters(
                subjectId, teacherId, status, searchTerm, page, size, sortBy, sortDirection
        );
        Page<Intensive> result = getIntensiveUseCase.findWithFilters(filters);
        Page<IntensiveResponse> enriched = intensiveResponseEnricher.enrichPage(result);
        return ResponseEntity.ok(PageResponse.of(enriched));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IntensiveResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIntensiveRequest request
    ) {
        Intensive updated = updateIntensiveUseCase.update(id, intensiveRestMapper.toCommand(request));
        return ResponseEntity.ok(intensiveResponseEnricher.enrich(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteIntensiveUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<IntensiveResponse> cancel(@PathVariable Long id) {
        Intensive cancelled = deleteIntensiveUseCase.cancel(id);
        return ResponseEntity.ok(intensiveResponseEnricher.enrich(cancelled));
    }

    /**
     * Bulk create intensive sessions: a list of {date, startTime, endTime, classroom}.
     * Each entry's date must be within [intensive.startDate, intensive.endDate].
     */
    @PostMapping("/{id}/sessions/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Long>> createSessionsBulk(
            @PathVariable Long id,
            @Valid @RequestBody BulkCreateIntensiveSessionsRequest request
    ) {
        log.info("REST: Bulk creating {} sessions for intensive {}",
                request.getEntries().size(), id);

        List<IntensiveSessionEntry> entries = request.getEntries().stream()
                .map(e -> new IntensiveSessionEntry(e.getDate(), e.getStartTime(),
                        e.getEndTime(), e.getClassroom()))
                .toList();

        List<Session> created = createIntensiveSessionsUseCase.createBulk(id, entries);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(created.stream().map(Session::getId).toList());
    }

    /**
     * Single intensive session creation. Convenience over the bulk endpoint.
     */
    @PostMapping("/{id}/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createSession(
            @PathVariable Long id,
            @Valid @RequestBody BulkCreateIntensiveSessionsRequest.Entry entry
    ) {
        log.info("REST: Creating intensive session for intensive {} on {}", id, entry.getDate());
        Session created = createIntensiveSessionsUseCase.createSingle(
                id,
                new IntensiveSessionEntry(entry.getDate(), entry.getStartTime(),
                        entry.getEndTime(), entry.getClassroom())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created.getId());
    }
}
