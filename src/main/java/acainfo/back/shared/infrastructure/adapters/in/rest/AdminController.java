package acainfo.back.shared.infrastructure.adapters.in.rest;

import acainfo.back.shared.application.services.UserService;
import acainfo.back.shared.domain.model.RoleType;
import acainfo.back.shared.infrastructure.adapters.in.dto.CreateTeacherRequest;
import acainfo.back.shared.infrastructure.adapters.in.dto.ErrorResponse;
import acainfo.back.shared.infrastructure.adapters.in.dto.MessageResponse;
import acainfo.back.shared.infrastructure.adapters.in.dto.UpdateTeacherRequest;
import acainfo.back.shared.infrastructure.adapters.in.dto.UserResponse;
import acainfo.back.shared.infrastructure.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Administración", description = "Endpoints para administradores (gestión de profesores y usuarios)")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    private final UserService userService;

    @Operation(
            summary = "Listar todos los profesores",
            description = "Obtiene una lista de todos los usuarios con rol TEACHER. Solo accesible por administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de profesores obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllTeachers() {
        log.info("GET /api/admin/teachers");
        List<UserResponse> teachers = userService.getUsersByRole(RoleType.TEACHER);
        return ResponseEntity.ok(teachers);
    }

    @Operation(
            summary = "Obtener profesor por ID",
            description = "Obtiene los detalles de un profesor específico por su ID. Solo accesible por administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profesor encontrado",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profesor no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getTeacherById(@PathVariable Long id) {
        log.info("GET /api/admin/teachers/{}", id);
        UserResponse teacher = userService.getUserById(id);
        return ResponseEntity.ok(teacher);
    }

    @Operation(
            summary = "Crear nuevo profesor",
            description = "Crea un nuevo usuario con rol TEACHER. Permite asignar permisos personalizados. Solo accesible por administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Profesor creado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        log.info("POST /api/admin/teachers - Email: {}", request.getEmail());
        UserResponse teacher = userService.createTeacher(request, currentUser.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(teacher);
    }

    @Operation(
            summary = "Actualizar profesor",
            description = "Actualiza los datos de un profesor existente. Permite modificar información personal, estado y permisos. Solo accesible por administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profesor actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profesor no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeacherRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        log.info("PUT /api/admin/teachers/{}", id);
        UserResponse teacher = userService.updateTeacher(id, request, currentUser.getUser());
        return ResponseEntity.ok(teacher);
    }

    @Operation(
            summary = "Eliminar profesor",
            description = "Elimina un profesor (soft delete - cambia estado a INACTIVE). Solo accesible por administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profesor eliminado exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Profesor no encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No autorizado (requiere rol ADMIN)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteTeacher(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        log.info("DELETE /api/admin/teachers/{}", id);
        userService.deleteTeacher(id, currentUser.getUser());
        return ResponseEntity.ok(MessageResponse.success("Teacher deleted successfully"));
    }
}
