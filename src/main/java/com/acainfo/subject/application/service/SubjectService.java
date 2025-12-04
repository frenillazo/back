package com.acainfo.subject.application.service;

import com.acainfo.subject.application.dto.CreateSubjectCommand;
import com.acainfo.subject.application.dto.SubjectFilters;
import com.acainfo.subject.application.dto.UpdateSubjectCommand;
import com.acainfo.subject.application.port.in.CreateSubjectUseCase;
import com.acainfo.subject.application.port.in.DeleteSubjectUseCase;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.application.port.in.UpdateSubjectUseCase;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.DuplicateSubjectCodeException;
import com.acainfo.subject.domain.exception.InvalidSubjectDataException;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.domain.model.SubjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementing subject use cases.
 * Contains business logic and validations for subject operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectService implements
        CreateSubjectUseCase,
        UpdateSubjectUseCase,
        GetSubjectUseCase,
        DeleteSubjectUseCase {

    private final SubjectRepositoryPort subjectRepositoryPort;

    // Business rules constants
    private static final String CODE_PATTERN = "^[A-Z]{3}\\d{3}$";

    @Override
    @Transactional
    public Subject create(CreateSubjectCommand command) {
        log.info("Creating subject with code: {}", command.code());

        // Normalize code (uppercase and trim)
        String normalizedCode = command.code().toUpperCase().trim();

        // Validate name
        if (command.name() == null || command.name().isBlank()) {
            throw new InvalidSubjectDataException("Name is required");
        }

        // Validate code format (3 letters + 3 digits, e.g., ING101)
        if (!isValidCode(normalizedCode)) {
            throw new InvalidSubjectDataException(
                    "Code must be 3 uppercase letters followed by 3 digits (e.g., ING101)"
            );
        }

        // Check if code already exists
        if (subjectRepositoryPort.existsByCode(normalizedCode)) {
            throw new DuplicateSubjectCodeException(normalizedCode);
        }

        // Create subject
        Subject subject = Subject.builder()
                .code(normalizedCode)
                .name(command.name().trim())
                .degree(command.degree())
                .status(SubjectStatus.ACTIVE)
                .currentGroupCount(0)
                .build();

        Subject savedSubject = subjectRepositoryPort.save(subject);
        log.info("Subject created successfully: {} - {}", savedSubject.getCode(), savedSubject.getName());

        return savedSubject;
    }

    @Override
    @Transactional
    public Subject update(Long id, UpdateSubjectCommand command) {
        log.info("Updating subject with ID: {}", id);

        Subject subject = getById(id);

        // Update name if provided
        if (command.name() != null && !command.name().isBlank()) {
            subject.setName(command.name().trim());
        }

        // Update status if provided
        if (command.status() != null) {
            subject.setStatus(command.status());
        }

        Subject updatedSubject = subjectRepositoryPort.save(subject);
        log.info("Subject updated successfully: {}", updatedSubject.getCode());

        return updatedSubject;
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getById(Long id) {
        log.debug("Getting subject by ID: {}", id);
        return subjectRepositoryPort.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getByCode(String code) {
        log.debug("Getting subject by code: {}", code);
        return subjectRepositoryPort.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException(code));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Subject> findWithFilters(SubjectFilters filters) {
        log.debug("Finding subjects with filters: searchTerm={}, degree={}, status={}",
                filters.searchTerm(), filters.degree(), filters.status());
        return subjectRepositoryPort.findWithFilters(filters);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting subject with ID: {}", id);

        Subject subject = getById(id);

        // Check if subject has groups
        if (subject.getCurrentGroupCount() > 0) {
            throw new InvalidSubjectDataException(
                    "Cannot delete subject with existing groups. Archive it instead."
            );
        }

        subjectRepositoryPort.delete(id);
        log.info("Subject deleted successfully: {}", subject.getCode());
    }

    @Override
    @Transactional
    public Subject archive(Long id) {
        log.info("Archiving subject with ID: {}", id);

        Subject subject = getById(id);
        subject.setStatus(SubjectStatus.ARCHIVED);
        Subject archivedSubject = subjectRepositoryPort.save(subject);

        log.info("Subject archived successfully: {}", archivedSubject.getCode());
        return archivedSubject;
    }

    // Private validation methods

    /**
     * Validate subject code format (3 letters + 3 digits).
     */
    private boolean isValidCode(String code) {
        return code != null && code.matches(CODE_PATTERN);
    }

}
