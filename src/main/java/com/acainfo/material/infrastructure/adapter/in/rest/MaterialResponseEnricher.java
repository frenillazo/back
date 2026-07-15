package com.acainfo.material.infrastructure.adapter.in.rest;

import com.acainfo.material.application.port.out.MaterialFolderRepositoryPort;
import com.acainfo.material.domain.model.Material;
import com.acainfo.material.domain.model.MaterialFolder;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialResponse;
import com.acainfo.material.infrastructure.adapter.in.rest.mapper.MaterialRestMapper;
import com.acainfo.subject.application.port.in.GetSubjectUseCase;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.user.application.port.in.GetUserProfileUseCase;
import com.acainfo.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Infrastructure service to enrich MaterialResponse with related entity data.
 * This service fetches data from other modules to build enriched responses,
 * reducing the number of API calls the frontend needs to make.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MaterialResponseEnricher {

    private final MaterialRestMapper materialRestMapper;
    private final GetSubjectUseCase getSubjectUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final MaterialFolderRepositoryPort materialFolderRepository;

    /**
     * Enrich a single material with related entity data.
     *
     * @param material the material to enrich
     * @return enriched material response
     */
    public MaterialResponse enrich(Material material) {
        Subject subject = getSubjectUseCase.getById(material.getSubjectId());
        User uploader = getUserProfileUseCase.getUserById(material.getUploadedById());
        String folderName = material.getFolderId() == null ? null :
                materialFolderRepository.findById(material.getFolderId())
                        .map(MaterialFolder::getName)
                        .orElse(null);

        return materialRestMapper.toEnrichedResponse(
                material,
                subject.getName(),
                uploader.getFullName(),
                folderName
        );
    }

    /**
     * Enrich a list of materials with related entity data.
     * Optimized to batch-fetch related entities to minimize database queries.
     *
     * @param materials the materials to enrich
     * @return list of enriched material responses
     */
    public List<MaterialResponse> enrichList(List<Material> materials) {
        if (materials.isEmpty()) {
            return List.of();
        }

        // Collect unique IDs
        Set<Long> subjectIds = materials.stream()
                .map(Material::getSubjectId)
                .collect(Collectors.toSet());

        Set<Long> uploaderIds = materials.stream()
                .map(Material::getUploadedById)
                .collect(Collectors.toSet());

        // Fetch subjects
        Map<Long, Subject> subjectsById = subjectIds.stream()
                .map(getSubjectUseCase::getById)
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        // Fetch uploaders
        Map<Long, User> uploadersById = uploaderIds.stream()
                .map(getUserProfileUseCase::getUserById)
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Batch-fetch folder names (folderId == null means subject root, no folder name)
        List<Long> folderIds = materials.stream()
                .map(Material::getFolderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> folderNamesById = materialFolderRepository.findAllByIds(folderIds).stream()
                .collect(Collectors.toMap(MaterialFolder::getId, MaterialFolder::getName));

        // Build enriched responses
        return materials.stream()
                .map(material -> {
                    Subject subject = subjectsById.get(material.getSubjectId());
                    User uploader = uploadersById.get(material.getUploadedById());
                    String folderName = material.getFolderId() == null ? null :
                            folderNamesById.get(material.getFolderId());

                    return materialRestMapper.toEnrichedResponse(
                            material,
                            subject.getName(),
                            uploader.getFullName(),
                            folderName
                    );
                })
                .toList();
    }
}
