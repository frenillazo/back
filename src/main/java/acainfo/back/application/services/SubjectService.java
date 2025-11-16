package acainfo.back.application.services;

import acainfo.back.application.ports.in.CreateSubjectUseCase;
import acainfo.back.application.ports.in.DeleteSubjectUseCase;
import acainfo.back.application.ports.in.GetSubjectUseCase;
import acainfo.back.application.ports.in.UpdateSubjectUseCase;
import acainfo.back.application.ports.out.SubjectRepositoryPort;
import acainfo.back.domain.exception.DuplicateSubjectCodeException;
import acainfo.back.domain.exception.SubjectHasActiveGroupsException;
import acainfo.back.domain.exception.SubjectNotFoundException;
import acainfo.back.domain.model.Degree;
import acainfo.back.domain.model.Subject;
import acainfo.back.domain.model.SubjectStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for subject management.
 * Implements all subject use cases with business logic and validations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectService implements
        CreateSubjectUseCase,
        UpdateSubjectUseCase,
        GetSubjectUseCase,
        DeleteSubjectUseCase {

    private final SubjectRepositoryPort subjectRepository;

    // ==================== CREATE ====================

    @Override
    public Subject createSubject(Subject subject) {
        log.info("Creating new subject with code: {}", subject.getCode());

        // Validate unique code
        validateUniqueCode(subject.getCode());

        // Validate business rules
        validateSubjectData(subject);

        // Set initial status if not set
        if (subject.getStatus() == null) {
            subject.setStatus(SubjectStatus.ACTIVO);
        }

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Subject created successfully with ID: {}", savedSubject.getId());

        return savedSubject;
    }

    // ==================== UPDATE ====================

    @Override
    public Subject updateSubject(Long id, Subject subject) {
        log.info("Updating subject with ID: {}", id);

        // Check if subject exists
        Subject existingSubject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));

        // Validate unique code (if changed)
        if (!existingSubject.getCode().equals(subject.getCode())) {
            validateUniqueCodeForUpdate(subject.getCode(), id);
        }

        // Validate business rules
        validateSubjectData(subject);

        // Update fields
        existingSubject.setCode(subject.getCode());
        existingSubject.setName(subject.getName());
        existingSubject.setYear(subject.getYear());
        existingSubject.setDegree(subject.getDegree());
        existingSubject.setSemester(subject.getSemester());
        existingSubject.setDescription(subject.getDescription());

        // Status can be updated
        if (subject.getStatus() != null) {
            existingSubject.setStatus(subject.getStatus());
        }

        Subject updatedSubject = subjectRepository.save(existingSubject);
        log.info("Subject updated successfully: {}", updatedSubject.getCode());

        return updatedSubject;
    }

    // ==================== GET ====================

    @Override
    @Transactional(readOnly = true)
    public Subject getSubjectById(Long id) {
        log.debug("Fetching subject by ID: {}", id);
        return subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Subject getSubjectByCode(String code) {
        log.debug("Fetching subject by code: {}", code);
        return subjectRepository.findByCode(code)
                .orElseThrow(() -> new SubjectNotFoundException(code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getAllSubjects() {
        log.debug("Fetching all subjects");
        return subjectRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getActiveSubjects() {
        log.debug("Fetching active subjects");
        return subjectRepository.findByStatus(SubjectStatus.ACTIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByDegree(Degree degree) {
        log.debug("Fetching subjects by degree: {}", degree);
        return subjectRepository.findByDegree(degree);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByStatus(SubjectStatus status) {
        log.debug("Fetching subjects by status: {}", status);
        return subjectRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> getSubjectsByDegreeAndYear(Degree degree, Integer year) {
        log.debug("Fetching subjects by degree: {} and year: {}", degree, year);
        return subjectRepository.findByDegreeAndYear(degree, year);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Subject> searchSubjects(String searchTerm) {
        log.debug("Searching subjects with term: {}", searchTerm);
        return subjectRepository.searchByCodeOrName(searchTerm);
    }

    // ==================== DELETE ====================

    @Override
    public void deleteSubject(Long id) {
        log.info("Deleting subject with ID: {}", id);

        // Check if subject exists
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));

        // TODO: Check if subject has active groups (will be implemented in Hito 2.5)
        // For now, we'll allow deletion. This will be enhanced when Group entity is created.
        // validateNoActiveGroups(id);

        subjectRepository.deleteById(id);
        log.info("Subject deleted successfully: {}", subject.getCode());
    }

    @Override
    public void archiveSubject(Long id) {
        log.info("Archiving subject with ID: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new SubjectNotFoundException(id));

        subject.archive();
        subjectRepository.save(subject);

        log.info("Subject archived successfully: {}", subject.getCode());
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates that the subject code is unique.
     */
    private void validateUniqueCode(String code) {
        if (subjectRepository.existsByCode(code)) {
            throw new DuplicateSubjectCodeException(code);
        }
    }

    /**
     * Validates that the subject code is unique for update.
     */
    private void validateUniqueCodeForUpdate(String code, Long excludeId) {
        if (subjectRepository.existsByCodeAndIdNot(code, excludeId)) {
            throw new DuplicateSubjectCodeException(code);
        }
    }

    /**
     * Validates subject business rules.
     */
    private void validateSubjectData(Subject subject) {
        // Year validation (1-4)
        if (subject.getYear() < 1 || subject.getYear() > 4) {
            throw new IllegalArgumentException("Year must be between 1 and 4");
        }

        // Semester validation (1-2)
        if (subject.getSemester() < 1 || subject.getSemester() > 2) {
            throw new IllegalArgumentException("Semester must be 1 or 2");
        }

        // Degree validation
        if (subject.getDegree() == null) {
            throw new IllegalArgumentException("Degree is required");
        }

        // Code format validation (already validated by @Pattern annotation)
        if (subject.getCode() == null || subject.getCode().isBlank()) {
            throw new IllegalArgumentException("Subject code is required");
        }

        // Name validation
        if (subject.getName() == null || subject.getName().isBlank()) {
            throw new IllegalArgumentException("Subject name is required");
        }
    }

    /**
     * Validates that the subject has no active groups.
     * TODO: This will be implemented when Group entity is created (Hito 2.5)
     */
    private void validateNoActiveGroups(Long subjectId) {
        // This will be implemented when GroupRepository is available
        // For now, this is a placeholder

        // Future implementation:
        // long activeGroupCount = groupRepository.countActiveGroupsBySubjectId(subjectId);
        // if (activeGroupCount > 0) {
        //     throw new SubjectHasActiveGroupsException(subjectId, (int) activeGroupCount);
        // }

        log.warn("Active groups validation not yet implemented (pending Group entity)");
    }
}
