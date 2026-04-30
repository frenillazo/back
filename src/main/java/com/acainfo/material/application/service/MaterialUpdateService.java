package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UpdateMaterialCommand;
import com.acainfo.material.application.port.in.UpdateMaterialUseCase;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.MaterialNotFoundException;
import com.acainfo.material.domain.model.Material;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for administrative material updates.
 * Implements {@link UpdateMaterialUseCase}.
 *
 * <p>Reactivation rules:</p>
 * <ul>
 *   <li>visibility false -> true: visibilityEnabledAt is reset to now()</li>
 *   <li>downloadDisabled true -> false: downloadEnabledAt is reset to now()</li>
 * </ul>
 * These timestamps are read by {@link MaterialAutoDisableService} to decide which materials
 * to deactivate after the configured threshold.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MaterialUpdateService implements UpdateMaterialUseCase {

    private final MaterialRepositoryPort materialRepository;

    @Override
    public Material updateMetadata(Long materialId, UpdateMaterialCommand command) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));

        if (command.name() != null && !command.name().isBlank()) {
            material.setName(command.name().trim());
        }
        if (command.description() != null) {
            // empty string allowed -> clears description
            material.setDescription(command.description().isBlank() ? null : command.description().trim());
        }

        LocalDateTime now = LocalDateTime.now();

        if (command.visible() != null && command.visible() != material.isVisible()) {
            material.setVisible(command.visible());
            if (command.visible()) {
                material.setVisibilityEnabledAt(now);
            }
        }

        if (command.downloadDisabled() != null && command.downloadDisabled() != material.isDownloadDisabled()) {
            material.setDownloadDisabled(command.downloadDisabled());
            if (!command.downloadDisabled()) {
                material.setDownloadEnabledAt(now);
            }
        }

        Material saved = materialRepository.save(material);
        log.info("Material {} updated by admin (visible={}, downloadDisabled={})",
                saved.getId(), saved.isVisible(), saved.isDownloadDisabled());
        return saved;
    }

    @Override
    public int batchSetDownloadDisabled(List<Long> ids, boolean disabled) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        LocalDateTime enabledAt = disabled ? null : LocalDateTime.now();
        int updated = materialRepository.batchUpdateDownloadDisabled(ids, disabled, enabledAt);
        log.info("Batch update downloadDisabled={} on {} materials (requested {})",
                disabled, updated, ids.size());
        return updated;
    }

    @Override
    public int batchSetVisibility(List<Long> ids, boolean visible) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        LocalDateTime enabledAt = visible ? LocalDateTime.now() : null;
        int updated = materialRepository.batchUpdateVisibility(ids, visible, enabledAt);
        log.info("Batch update visible={} on {} materials (requested {})",
                visible, updated, ids.size());
        return updated;
    }
}
