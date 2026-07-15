package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UpdateMaterialCommand;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.model.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialUpdateService}.
 */
@ExtendWith(MockitoExtension.class)
class MaterialUpdateServiceTest {

    private static final Long MATERIAL_ID = 10L;

    @Mock
    private MaterialRepositoryPort materialRepository;

    @InjectMocks
    private MaterialUpdateService service;

    @BeforeEach
    void setUp() {
        Material material = Material.builder()
                .id(MATERIAL_ID)
                .subjectId(30L)
                .name("Examen 2024")
                .academicYear(2024)
                .visible(true)
                .downloadDisabled(false)
                .build();
        when(materialRepository.findById(MATERIAL_ID)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updatesAcademicYearWhenProvided() {
        Material updated = service.updateMetadata(
                MATERIAL_ID, new UpdateMaterialCommand(null, null, null, null, 2025));

        assertThat(updated.getAcademicYear()).isEqualTo(2025);
    }

    @Test
    void keepsAcademicYearWhenNotProvided() {
        Material updated = service.updateMetadata(
                MATERIAL_ID, new UpdateMaterialCommand("Examen práctica", null, null, null, null));

        assertThat(updated.getAcademicYear()).isEqualTo(2024);
        assertThat(updated.getName()).isEqualTo("Examen práctica");
    }
}
