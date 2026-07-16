package com.acainfo.enrollment.application.service;

import com.acainfo.security.cleanup.TokenCleanupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La expiración automática de solicitudes a las 48 h está APAGADA a propósito
 * (decisión del dueño, 16-jul-2026): caducaba solicitudes en silencio con un solo
 * admin que no entra a diario. Antes corría con {@code @Scheduled(fixedRate)}, que
 * ignora cualquier propiedad y no había forma de apagarlo por configuración.
 */
@SpringBootTest
@ActiveProfiles("test")
class EnrollmentExpirationSchedulingTest {

    @Autowired
    private ScheduledAnnotationBeanPostProcessor scheduledPostProcessor;

    @Test
    void laExpiracionDeSolicitudesNoQuedaProgramada() {
        assertThat(scheduledMethods())
                .noneMatch(method -> method.startsWith(EnrollmentExpirationService.class.getName()));
    }

    @Test
    void laPurgaDeTokensSiQuedaProgramada() {
        // Control: sin esto, el test de arriba pasaría igual si la inspección de
        // tareas no viera ninguna (pasó de verdad al escribirlo).
        assertThat(scheduledMethods())
                .anyMatch(method -> method.startsWith(TokenCleanupService.class.getName()));
    }

    /**
     * Métodos @Scheduled que Spring ha registrado de verdad, como
     * "paquete.Clase.metodo". Se leen del toString del runnable porque Spring lo
     * envuelve en un Task$OutcomeTrackingRunnable interno y no expone el método.
     */
    private Set<String> scheduledMethods() {
        Set<ScheduledTask> tasks = scheduledPostProcessor.getScheduledTasks();
        return tasks.stream()
                .map(task -> task.getTask().getRunnable().toString())
                .collect(Collectors.toSet());
    }
}
