package com.acainfo.material.application.service;

import com.acainfo.material.application.dto.UpdateMaterialFolderCommand;
import com.acainfo.material.application.port.in.MaterialFolderUseCase;
import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.exception.DuplicateFolderNameException;
import com.acainfo.material.domain.exception.MaterialFolderNotFoundException;
import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for material folder management.
 * Implements {@link MaterialFolderUseCase}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MaterialFolderService implements MaterialFolderUseCase {

    private final MaterialFolderRepositoryPort folderRepository;
    private final MaterialRepositoryPort materialRepository;
    private final GetSubjectUseCase getSubjectUseCase;

    @Override
    @Transactional(readOnly = true)
    public List<MaterialFolder> getBySubjectId(Long subjectId) {
        return folderRepository.findBySubjectId(subjectId);
    }

    @Override
    public MaterialFolder create(Long subjectId, String name) {
        // Throws SubjectNotFoundException if the subject does not exist
        getSubjectUseCase.getById(subjectId);

        String trimmedName = name.trim();
        if (folderRepository.existsBySubjectIdAndName(subjectId, trimmedName)) {
            throw new DuplicateFolderNameException(trimmedName);
        }

        // Append at the end of the subject's folders
        int position = folderRepository.findBySubjectId(subjectId).stream()
                .mapToInt(MaterialFolder::getPosition)
                .max()
                .orElse(-1) + 1;

        MaterialFolder saved = folderRepository.save(MaterialFolder.builder()
                .subjectId(subjectId)
                .name(trimmedName)
                .position(position)
                .build());

        log.info("Material folder created: id={}, name='{}', subject={}",
                saved.getId(), saved.getName(), saved.getSubjectId());
        return saved;
    }

    @Override
    public MaterialFolder update(Long folderId, UpdateMaterialFolderCommand command) {
        MaterialFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new MaterialFolderNotFoundException(folderId));

        if (command.name() != null && !command.name().isBlank()) {
            String trimmedName = command.name().trim();
            if (!trimmedName.equals(folder.getName())
                    && folderRepository.existsBySubjectIdAndName(folder.getSubjectId(), trimmedName)) {
                throw new DuplicateFolderNameException(trimmedName);
            }
            folder.setName(trimmedName);
        }

        if (command.position() != null) {
            folder.setPosition(command.position());
        }

        MaterialFolder saved = folderRepository.save(folder);
        log.info("Material folder updated: id={}, name='{}', position={}",
                saved.getId(), saved.getName(), saved.getPosition());
        return saved;
    }

    @Override
    public void delete(Long folderId) {
        MaterialFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new MaterialFolderNotFoundException(folderId));

        // Send materials back to the subject root BEFORE deleting: in dev/test the
        // schema comes from Hibernate without the FK, so the prod-only
        // ON DELETE SET NULL is just a safety net, not the mechanism.
        int moved = materialRepository.clearFolderId(folderId);
        folderRepository.delete(folderId);

        log.info("Material folder deleted: id={}, name='{}', materialsMovedToRoot={}",
                folderId, folder.getName(), moved);
    }
}
