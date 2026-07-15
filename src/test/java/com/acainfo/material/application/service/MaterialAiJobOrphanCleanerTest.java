package com.acainfo.material.application.service;

import com.acainfo.material.application.port.out.MaterialAiJobRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialAiJobOrphanCleaner}.
 */
@ExtendWith(MockitoExtension.class)
class MaterialAiJobOrphanCleanerTest {

    @Mock
    private MaterialAiJobRepositoryPort jobRepository;

    @InjectMocks
    private MaterialAiJobOrphanCleaner cleaner;

    @Test
    void alArrancarMarcaComoFailedLosJobsPendingYRunning() {
        when(jobRepository.failInterruptedJobs(MaterialAiJobOrphanCleaner.ORPHAN_ERROR_MESSAGE))
                .thenReturn(2);

        cleaner.failOrphanJobs();

        verify(jobRepository).failInterruptedJobs("Interrumpido por reinicio del servidor");
    }
}
