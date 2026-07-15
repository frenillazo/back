package com.acainfo.material.infrastructure.adapter.in.rest;

import com.acainfo.material.application.dto.AiImageInput;
import com.acainfo.material.application.dto.GenerateAiMaterialCommand;
import com.acainfo.material.application.dto.TranscribeAiMaterialCommand;
import com.acainfo.material.application.port.in.MaterialAiUseCase;
import com.acainfo.material.domain.exception.InvalidAiJobRequestException;
import com.acainfo.material.domain.model.MaterialAiJob;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.GenerateAiMaterialRequest;
import com.acainfo.material.infrastructure.adapter.in.rest.dto.MaterialAiJobResponse;
import com.acainfo.material.infrastructure.adapter.in.rest.mapper.MaterialAiRestMapper;
import com.acainfo.security.userdetails.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * REST controller for the AI LaTeX generator/transcriber (admin only).
 * Creating a job returns 201 immediately; the frontend polls GET /ai/jobs/{id}.
 * "Relaunch" is always a NEW job re-sending the original request: the job table
 * does not persist the captures (tmp, deleted) nor the parameters.
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialAiController {

    /** Formats the Anthropic API accepts as image blocks. */
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final MaterialAiUseCase materialAiUseCase;
    private final MaterialAiRestMapper mapper;

    /**
     * Launch a GENERATE job: captures of exercises worked in class ->
     * new practice exercises with solutions, published as a PDF material.
     * Requires ADMIN role.
     */
    @PostMapping(value = "/ai/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialAiJobResponse> generate(
            @Valid @RequestPart("metadata") GenerateAiMaterialRequest request,
            @RequestPart("images") List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        List<AiImageInput> imageInputs = new ArrayList<>();
        for (MultipartFile image : images) {
            String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                throw new InvalidAiJobRequestException(
                        "Formato de captura no soportado: " + contentType + " (usa JPEG, PNG, GIF o WebP)");
            }
            imageInputs.add(new AiImageInput(image.getBytes(), contentType));
        }

        MaterialAiJob job = materialAiUseCase.createGenerateJob(new GenerateAiMaterialCommand(
                request.subjectId(),
                request.folderId(),
                userDetails.getUserId(),
                request.exerciseCountOrDefault(),
                imageInputs));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(job));
    }

    /**
     * Launch a TRANSCRIBE job: an already-published whiteboard PDF ->
     * clean transcription published next to the original.
     * Requires ADMIN role.
     */
    @PostMapping("/{id}/ai/transcribe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialAiJobResponse> transcribe(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MaterialAiJob job = materialAiUseCase.createTranscribeJob(
                new TranscribeAiMaterialCommand(id, userDetails.getUserId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(job));
    }

    /**
     * Poll a job's state.
     * Requires ADMIN role.
     */
    @GetMapping("/ai/jobs/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MaterialAiJobResponse> getJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(mapper.toResponse(materialAiUseCase.getJob(jobId)));
    }
}
