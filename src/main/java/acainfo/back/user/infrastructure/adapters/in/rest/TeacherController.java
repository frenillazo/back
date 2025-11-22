package acainfo.back.user.infrastructure.adapters.in.rest;

import acainfo.back.user.application.ports.in.AuthenticateUserUseCase;
import acainfo.back.user.application.ports.in.ManageUserUseCase;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.infrastructure.adapters.in.dto.UserResponse;
import acainfo.back.user.infrastructure.adapters.in.mapper.UserDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// TODO: ErrorResponse will be moved to a shared config module
import acainfo.back.shared.infrastructure.adapters.in.dto.ErrorResponse;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profesores", description = "Endpoints para profesores (perfil, horarios)")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherController {

    private final ManageUserUseCase manageUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    @Operation(
            summary = "Obtener perfil del profesor",
            description = "Obtiene el perfil completo del profesor autenticado con sus roles y permisos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol TEACHER)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UserResponse> getProfile() {
        Long currentUserId = authenticateUserUseCase.getCurrentUserId();
        log.info("GET /api/teachers/profile - User ID: {}", currentUserId);

        UserDomain user = manageUserUseCase.getUserById(currentUserId);
        UserResponse response = UserDtoMapper.toUserResponse(user);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener horario del profesor",
            description = "Obtiene el horario semanal del profesor autenticado. (Endpoint placeholder - se implementará en Fase 2)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Horario obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol TEACHER)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/schedule")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> getSchedule() {
        Long currentUserId = authenticateUserUseCase.getCurrentUserId();
        log.info("GET /api/teachers/schedule - User ID: {}", currentUserId);

        // TODO: Implementar en Fase 2 cuando tengamos las entidades de Subject, SubjectGroup, Session
        return ResponseEntity.ok("Schedule endpoint - To be implemented in Phase 2");
    }
}
