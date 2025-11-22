package acainfo.back.attendance.application.services;

import acainfo.back.attendance.application.ports.in.UpdateAttendanceUseCase;
import acainfo.back.attendance.application.ports.out.AttendanceRepositoryPort;
import acainfo.back.attendance.domain.exception.AttendanceNotFoundException;
import acainfo.back.attendance.domain.exception.InvalidAttendanceOperationException;
import acainfo.back.attendance.domain.model.AttendanceDomain;
import acainfo.back.attendance.domain.model.AttendanceStatus;
import acainfo.back.shared.domain.exception.UserNotFoundException;
import acainfo.back.shared.infrastructure.adapters.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case Implementation: Update Attendance
 * Application layer - implements attendance modification operations
 *
 * Business Rules:
 * - Attendance can be modified within 7 days of recording
 * - Only AUSENTE status can be justified
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UpdateAttendanceUseCaseImpl implements UpdateAttendanceUseCase {

    private final AttendanceRepositoryPort attendanceRepository;
    private final UserRepository userRepository;

    @Override
    public AttendanceDomain updateAttendanceStatus(UpdateAttendanceStatusCommand command) {
        log.info("Updating attendance status for ID: {}", command.attendanceId());

        // 1. Fetch existing attendance
        AttendanceDomain attendance = attendanceRepository.findById(command.attendanceId())
            .orElseThrow(() -> new AttendanceNotFoundException(command.attendanceId()));

        // 2. Check if modification is allowed
        if (!attendance.canBeModified()) {
            throw InvalidAttendanceOperationException.modificationNotAllowed(
                command.attendanceId()
            );
        }

        // 3. Parse new status
        AttendanceStatus newStatus = parseAttendanceStatus(command.newStatus());

        // 4. Update status based on new status
        AttendanceDomain updated;
        switch (newStatus) {
            case PRESENTE:
                updated = attendance.markAsPresent();
                break;
            case AUSENTE:
                updated = attendance.markAsAbsent(command.notes());
                break;
            case TARDANZA:
                // For status update, we can't change to TARDANZA without minutes
                throw new IllegalArgumentException(
                    "Use markAsLate() operation to set TARDANZA status"
                );
            case JUSTIFICADO:
                // For status update, we can't change to JUSTIFICADO without justification details
                throw new IllegalArgumentException(
                    "Use justifyAbsence() operation to justify an absence"
                );
            default:
                throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        // 5. Update notes if provided
        if (command.notes() != null && !command.notes().isBlank()) {
            updated = updated.withNotes(command.notes());
        }

        // 6. Save
        AttendanceDomain saved = attendanceRepository.save(updated);
        log.info("Attendance status updated to {} for ID: {}", newStatus, saved.getId());

        return saved;
    }

    @Override
    public AttendanceDomain justifyAbsence(JustifyAbsenceCommand command) {
        log.info("Justifying absence for attendance ID: {}", command.attendanceId());

        // 1. Fetch existing attendance
        AttendanceDomain attendance = attendanceRepository.findById(command.attendanceId())
            .orElseThrow(() -> new AttendanceNotFoundException(command.attendanceId()));

        // 2. Validate user exists
        if (!userRepository.existsById(command.justifiedById())) {
            throw new UserNotFoundException(command.justifiedById());
        }

        // 3. Justify the absence (will throw exception if status cannot be justified)
        AttendanceDomain justified = attendance.justify(
            command.justifiedById(),
            command.justificationReason()
        );

        // 4. Save
        AttendanceDomain saved = attendanceRepository.save(justified);
        log.info("Absence justified for attendance ID: {}", saved.getId());

        return saved;
    }

    @Override
    public AttendanceDomain markAsLate(MarkAsLateCommand command) {
        log.info("Marking attendance as late for ID: {}", command.attendanceId());

        // 1. Fetch existing attendance
        AttendanceDomain attendance = attendanceRepository.findById(command.attendanceId())
            .orElseThrow(() -> new AttendanceNotFoundException(command.attendanceId()));

        // 2. Check if modification is allowed
        if (!attendance.canBeModified()) {
            throw InvalidAttendanceOperationException.modificationNotAllowed(
                command.attendanceId()
            );
        }

        // 3. Mark as late
        AttendanceDomain updated = attendance.markAsLate(
            command.minutesLate(),
            command.notes()
        );

        // 4. Save
        AttendanceDomain saved = attendanceRepository.save(updated);
        log.info("Attendance marked as late ({} minutes) for ID: {}",
            command.minutesLate(), saved.getId());

        return saved;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Parses attendance status string to enum.
     */
    private AttendanceStatus parseAttendanceStatus(String statusStr) {
        try {
            return AttendanceStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid attendance status: " + statusStr +
                ". Must be one of: PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO"
            );
        }
    }
}
