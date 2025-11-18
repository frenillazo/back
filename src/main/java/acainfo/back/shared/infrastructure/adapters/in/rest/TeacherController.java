package acainfo.back.shared.infrastructure.adapters.in.rest;

import acainfo.back.shared.application.services.UserService;
import acainfo.back.shared.infrastructure.adapters.in.dto.ErrorResponse;
import acainfo.back.shared.infrastructure.adapters.in.dto.UserResponse;
import acainfo.back.shared.infrastructure.config.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profesores", description = "Endpoints para profesores (perfil, horarios)")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherController {

    private final UserService userService;

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
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("GET /api/teachers/profile - User: {}", currentUser.getUsername());
        UserResponse profile = userService.getUserById(currentUser.getUser().getId());
        return ResponseEntity.ok(profile);
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
    public ResponseEntity<?> getSchedule(@AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("GET /api/teachers/schedule - User: {}", currentUser.getUsername());
        // TODO: Implementar en Fase 2 cuando tengamos las entidades de Subject, SubjectGroup, Session
        return ResponseEntity.ok("Schedule endpoint - To be implemented in Phase 2");
    }
}
