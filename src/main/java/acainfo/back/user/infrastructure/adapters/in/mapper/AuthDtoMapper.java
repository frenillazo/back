package acainfo.back.user.infrastructure.adapters.in.mapper;

import acainfo.back.user.application.ports.in.AuthenticateUserUseCase;
import acainfo.back.user.infrastructure.adapters.in.dto.AuthResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.LoginRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.RegisterRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper for authentication DTOs and use case commands/responses.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthDtoMapper {

    public static AuthenticateUserUseCase.RegisterCommand toRegisterCommand(RegisterRequest request) {
        return new AuthenticateUserUseCase.RegisterCommand(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );
    }

    public static AuthenticateUserUseCase.LoginCommand toLoginCommand(LoginRequest request) {
        return new AuthenticateUserUseCase.LoginCommand(
                request.getEmail(),
                request.getPassword()
        );
    }

    public static AuthResponse toAuthResponse(AuthenticateUserUseCase.AuthResponse response) {
        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
                .id(response.userId())
                .email(response.email())
                .firstName(response.firstName())
                .lastName(response.lastName())
                .roles(response.roles())
                .build();

        return AuthResponse.builder()
                .accessToken(response.accessToken())
                .refreshToken(response.refreshToken())
                .tokenType("Bearer")
                .expiresIn(response.expiresIn())
                .user(userDto)
                .build();
    }
}
