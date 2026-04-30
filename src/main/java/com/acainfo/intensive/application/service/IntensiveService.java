package com.acainfo.intensive.application.service;

import com.acainfo.intensive.application.dto.CreateIntensiveCommand;
import com.acainfo.intensive.application.dto.IntensiveFilters;
import com.acainfo.intensive.application.dto.UpdateIntensiveCommand;
import com.acainfo.intensive.application.port.in.CreateIntensiveUseCase;
import com.acainfo.intensive.application.port.in.DeleteIntensiveUseCase;
import com.acainfo.intensive.application.port.in.GetIntensiveUseCase;
import com.acainfo.intensive.application.port.in.UpdateIntensiveUseCase;
import com.acainfo.intensive.application.port.out.IntensiveRepositoryPort;
import com.acainfo.intensive.domain.exception.IntensiveNotFoundException;
import com.acainfo.intensive.domain.exception.InvalidIntensiveDataException;
import com.acainfo.intensive.domain.model.Intensive;
import com.acainfo.intensive.domain.model.IntensiveStatus;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.exception.SubjectNotFoundException;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.exception.UserNotFoundException;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementing all Intensive use cases (CRUD + cancel).
 *
 * <p>Mirrors the patterns of {@link com.acainfo.group.application.service.GroupService}
 * but specialised for intensive courses (no GroupType, no Schedule, has start/end dates).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntensiveService implements
        CreateIntensiveUseCase,
        GetIntensiveUseCase,
        UpdateIntensiveUseCase,
        DeleteIntensiveUseCase {

    private final IntensiveRepositoryPort intensiveRepository;
    private final SubjectRepositoryPort subjectRepository;
    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public Intensive create(CreateIntensiveCommand command) {
        log.info("Creating intensive for subject {}, teacher {}", command.subjectId(), command.teacherId());

        // 1. Validate subject
        Subject subject = subjectRepository.findById(command.subjectId())
                .orElseThrow(() -> new SubjectNotFoundException(command.subjectId()));

        // 2. Validate teacher
        User teacher = userRepository.findById(command.teacherId())
                .orElseThrow(() -> new UserNotFoundException(command.teacherId()));

        if (!teacher.isTeacher() && !teacher.isAdmin()) {
            throw new InvalidIntensiveDataException(
                    "User " + command.teacherId() + " is not a teacher or admin"
            );
        }

        // 3. Validate dates
        if (command.startDate() == null || command.endDate() == null) {
            throw new InvalidIntensiveDataException("startDate and endDate are required");
        }
        if (command.endDate().isBefore(command.startDate())) {
            throw new InvalidIntensiveDataException("endDate must be on or after startDate");
        }

        // 4. Validate capacity if customised
        if (command.capacity() != null) {
            if (command.capacity() < 1) {
                throw new InvalidIntensiveDataException("Capacity must be at least 1");
            }
        }

        String name = generateName(subject);

        Intensive intensive = Intensive.builder()
                .name(name)
                .subjectId(command.subjectId())
                .teacherId(command.teacherId())
                .status(IntensiveStatus.OPEN)
                .currentEnrollmentCount(0)
                .capacity(command.capacity())
                .pricePerHour(command.pricePerHour())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .build();

        Intensive saved = intensiveRepository.save(intensive);
        log.info("Intensive created: id={}, name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Intensive getById(Long id) {
        return intensiveRepository.findById(id)
                .orElseThrow(() -> new IntensiveNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Intensive> findWithFilters(IntensiveFilters filters) {
        return intensiveRepository.findWithFilters(filters);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Intensive> findAll() {
        return intensiveRepository.findAll();
    }

    @Override
    @Transactional
    public Intensive update(Long id, UpdateIntensiveCommand command) {
        log.info("Updating intensive {}", id);

        Intensive intensive = getById(id);

        if (command.capacity() != null) {
            if (command.capacity() < intensive.getCurrentEnrollmentCount()) {
                throw new InvalidIntensiveDataException(
                        String.format("Capacity cannot be lower than current enrollments (%d)",
                                intensive.getCurrentEnrollmentCount())
                );
            }
            intensive.setCapacity(command.capacity());
        }

        if (command.status() != null) {
            intensive.setStatus(command.status());
        }

        if (command.pricePerHour() != null) {
            intensive.setPricePerHour(command.pricePerHour());
        }

        // If updating dates, validate consistency
        LocalDate newStart = command.startDate() != null ? command.startDate() : intensive.getStartDate();
        LocalDate newEnd = command.endDate() != null ? command.endDate() : intensive.getEndDate();

        if (newEnd.isBefore(newStart)) {
            throw new InvalidIntensiveDataException("endDate must be on or after startDate");
        }
        intensive.setStartDate(newStart);
        intensive.setEndDate(newEnd);

        return intensiveRepository.save(intensive);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Intensive intensive = getById(id);

        if (intensive.getCurrentEnrollmentCount() > 0) {
            throw new InvalidIntensiveDataException(
                    "Cannot delete intensive with existing enrollments. Cancel it instead."
            );
        }

        intensiveRepository.delete(id);
        log.info("Intensive {} deleted", id);
    }

    @Override
    @Transactional
    public Intensive cancel(Long id) {
        Intensive intensive = getById(id);
        intensive.setStatus(IntensiveStatus.CANCELLED);
        Intensive saved = intensiveRepository.save(intensive);
        log.info("Intensive {} cancelled", id);
        return saved;
    }

    // ==================== Helpers ====================

    private String generateName(Subject subject) {
        long existing = intensiveRepository.countAllBySubjectId(subject.getId());
        String academicYear = calculateAcademicYear();
        return String.format("%s intensivo %d %s", subject.getName(), existing + 1, academicYear);
    }

    private String calculateAcademicYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int startYear = month >= 9 ? year : year - 1;
        int endYear = startYear + 1;
        return String.format("%02d-%02d", startYear % 100, endYear % 100);
    }
}
