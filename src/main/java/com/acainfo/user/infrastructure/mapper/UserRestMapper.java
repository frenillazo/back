package com.acainfo.user.infrastructure.mapper;

import com.acainfo.user.application.dto.*;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for REST layer.
 * Converts between REST DTOs and Application DTOs/Domain entities.
 *
 * Maps:
 * - Request DTOs → Commands (application layer)
 * - Domain entities → Response DTOs
 * - AuthenticationResult → AuthResponse
 */
@Mapper(componentModel = "spring")
public interface UserRestMapper {

    // ==================== Request → Command ====================

    /**
     * Maps RegisterRequest (REST) to RegisterUserCommand (application).
     */
    RegisterUserCommand toRegisterUserCommand(RegisterRequest request);

    /**
     * Maps LoginRequest (REST) to AuthenticationCommand (application).
     */
    AuthenticationCommand toAuthenticationCommand(LoginRequest request);

    /**
     * Maps UpdateProfileRequest (REST) to UpdateUserCommand (application).
     */
    UpdateUserCommand toUpdateUserCommand(UpdateProfileRequest request);

    /**
     * Maps CreateTeacherRequest (REST) to CreateTeacherCommand (application).
     */
    CreateTeacherCommand toCreateTeacherCommand(CreateTeacherRequest request);

    /**
     * Maps UpdateTeacherRequest (REST) to UpdateTeacherCommand (application).
     */
    UpdateTeacherCommand toUpdateTeacherCommand(UpdateTeacherRequest request);

    // ==================== Domain → Response ====================

    /**
     * Maps User (domain) to UserResponse (REST).
     * Custom mapping for roles (Set<Role> → Set<String>).
     */
    @Mapping(target = "roles", expression = "java(mapRolesToStrings(user.getRoles()))")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    UserResponse toUserResponse(User user);

    /**
     * Maps User (domain) to TeacherResponse (REST).
     */
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    TeacherResponse toTeacherResponse(User user);

    /**
     * Maps AuthenticationResult (application) to AuthResponse (REST).
     * Custom mapping to include token metadata.
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn", expression = "java(getAccessTokenExpiration())")
    AuthResponse toAuthResponse(AuthenticationResult result);

    // ==================== Helper Methods ====================

    /**
     * Converts Set<Role> to Set<String> (role types as strings).
     */
    default Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getType().name())
                .collect(Collectors.toSet());
    }

    /**
     * Returns access token expiration in milliseconds (15 minutes).
     * This should match jwt.access-token-expiration in application.properties.
     */
    default Long getAccessTokenExpiration() {
        return 900000L; // 15 minutes in milliseconds
    }
}
