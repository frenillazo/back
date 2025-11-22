package acainfo.back.user.infrastructure.adapters.in.rest;

import acainfo.back.user.application.ports.in.AuthenticateUserUseCase;
import acainfo.back.user.infrastructure.adapters.in.dto.AuthResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.LoginRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.MessageResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.RefreshTokenRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.RegisterRequest;
import acainfo.back.user.infrastructure.adapters.in.mapper.AuthDtoMapper;
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
import org.springframework.web.bind.annotation.*;

// TODO: ErrorResponse will be moved to a shared config module
import acainfo.back.config.dto.ErrorResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para registro, login, refresh tokens y logout")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema con rol STUDENT por defecto. Requiere email único y contraseña segura."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/auth/register - Email: {}", request.getEmail());

        AuthenticateUserUseCase.RegisterCommand command = AuthDtoMapper.toRegisterCommand(request);
        AuthenticateUserUseCase.AuthResponse response = authenticateUserUseCase.register(command);
        AuthResponse dto = AuthDtoMapper.toAuthResponse(response);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica un usuario con email y contraseña. Retorna access token y refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/auth/login - Email: {}", request.getEmail());

        AuthenticateUserUseCase.LoginCommand command = AuthDtoMapper.toLoginCommand(request);
        AuthenticateUserUseCase.AuthResponse response = authenticateUserUseCase.login(command);
        AuthResponse dto = AuthDtoMapper.toAuthResponse(response);

        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Refrescar access token",
            description = "Genera un nuevo access token usando un refresh token válido. El refresh token se mantiene igual."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /api/auth/refresh");

        AuthenticateUserUseCase.AuthResponse response = authenticateUserUseCase.refreshToken(request.getRefreshToken());
        AuthResponse dto = AuthDtoMapper.toAuthResponse(response);

        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Cerrar sesión",
            description = "Cierra la sesión del usuario y revoca todos sus refresh tokens.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout exitoso",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        log.info("POST /api/auth/logout");

        Long currentUserId = authenticateUserUseCase.getCurrentUserId();
        if (currentUserId != null) {
            authenticateUserUseCase.logout(currentUserId);
        }

        return ResponseEntity.ok(MessageResponse.success("Logged out successfully"));
    }

    @Operation(
            summary = "Revocar todos los tokens",
            description = "Revoca todos los refresh tokens del usuario autenticado. Útil para cerrar sesión en todos los dispositivos.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens revocados exitosamente",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/revoke-all")
    public ResponseEntity<MessageResponse> revokeAll() {
        log.info("POST /api/auth/revoke-all");

        Long currentUserId = authenticateUserUseCase.getCurrentUserId();
        if (currentUserId != null) {
            authenticateUserUseCase.logout(currentUserId);
        }

        return ResponseEntity.ok(MessageResponse.success("All tokens revoked successfully"));
    }
}
