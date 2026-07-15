package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UploadMaterialCommand;
import com.acainfo.material.application.port.out.FileStoragePort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialUploadService}.
 * The clock is fixed at 2026-02-15 → current academic year 2025 (course "2025-26").
 */
@ExtendWith(MockitoExtension.class)
class MaterialUploadServiceTest {

    private static final Long SUBJECT_ID = 30L;
    private static final Long UPLOADER_ID = 1L;
    private static final int CURRENT_ACADEMIC_YEAR = 2025;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-02-15T12:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private MaterialRepositoryPort materialRepository;

    @Mock
    private FileStoragePort fileStorage;

    private MaterialUploadService service;

    @BeforeEach
    void setUp() {
        service = new MaterialUploadService(materialRepository, fileStorage, FIXED_CLOCK);
    }

    @Test
    void uploadAssignsCurrentAcademicYear() {
        when(fileStorage.store(any(InputStream.class), anyString(), anyLong(), any(MaterialCategory.class)))
                .thenReturn("subjects/30/doc.pdf");
        when(materialRepository.save(any(Material.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Material saved = service.upload(new UploadMaterialCommand(
                SUBJECT_ID,
                UPLOADER_ID,
                "Apuntes tema 1",
                null,
                "apuntes.pdf",
                "application/pdf",
                1024L,
                InputStream.nullInputStream(),
                MaterialCategory.TEORIA
        ));

        assertThat(saved.getAcademicYear()).isEqualTo(CURRENT_ACADEMIC_YEAR);
        assertThat(saved.isVisible()).isTrue();
    }
}
