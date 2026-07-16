package com.acainfo.enrollment.application.service;

import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling automatic expiration of pending enrollment requests.
 * Enrollments that are not approved or rejected within 48 hours are expired.
 *
 * <p>APAGADO a propósito (16-jul-2026, decisión del dueño): con un solo admin que
 * no entra a diario, un plazo automático de 48 h perdía alumnos en silencio — ni
 * el alumno conocía el plazo ni el admin tenía aviso de solicitudes esperando.
 * La solicitud vive ahora hasta que el admin decide. Para reactivarlo, poner un
 * cron en {@code app.enrollment.expiration.cron} (y volver a contar el plazo al
 * alumno en la UI, que ya no lo menciona).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentExpirationService {

    private static final int EXPIRATION_HOURS = 48; // 2 days

    private final EnrollmentRepositoryPort enrollmentRepositoryPort;

    /**
     * Scheduled job to expire pending enrollments older than 48 hours.
     * Disabled by default ({@code cron=-}); still invocable manually.
     */
    @Scheduled(cron = "${app.enrollment.expiration.cron:-}")
    @Transactional
    public void expirePendingEnrollments() {
        log.info("Running enrollment expiration job...");

        List<Enrollment> expiredEnrollments = enrollmentRepositoryPort.findExpiredPendingEnrollments(EXPIRATION_HOURS);

        if (expiredEnrollments.isEmpty()) {
            log.debug("No expired enrollments found");
            return;
        }

        log.info("Found {} pending enrollments to expire", expiredEnrollments.size());

        for (Enrollment enrollment : expiredEnrollments) {
            try {
                enrollment.setStatus(EnrollmentStatus.EXPIRED);
                enrollment.setRejectedAt(LocalDateTime.now());
                enrollment.setRejectionReason("Solicitud expirada por falta de respuesta en 48 horas");
                enrollmentRepositoryPort.save(enrollment);

                log.info("Expired enrollment {} for student {} in group {}",
                        enrollment.getId(), enrollment.getStudentId(), enrollment.getCourseId());
            } catch (Exception e) {
                log.error("Failed to expire enrollment {}: {}", enrollment.getId(), e.getMessage());
            }
        }

        log.info("Enrollment expiration job completed. Expired {} enrollments", expiredEnrollments.size());
    }
}
