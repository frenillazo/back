package acainfo.back.user.infrastructure.adapters.in.mapper;

import acainfo.back.user.application.ports.in.ManageUserUseCase;
import acainfo.back.user.domain.model.PermissionDomain;
import acainfo.back.user.domain.model.RoleDomain;
import acainfo.back.user.domain.model.UserDomain;
import acainfo.back.user.infrastructure.adapters.in.dto.CreateTeacherRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.UpdateTeacherRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.UserResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for user DTOs and domain models.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserDtoMapper {

    public static ManageUserUseCase.CreateTeacherCommand toCreateTeacherCommand(CreateTeacherRequest request) {
        return new ManageUserUseCase.CreateTeacherCommand(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );
    }

    public static ManageUserUseCase.UpdateTeacherCommand toUpdateTeacherCommand(Long teacherId, UpdateTeacherRequest request) {
        return new ManageUserUseCase.UpdateTeacherCommand(
                teacherId,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );
    }

    public static UserResponse toUserResponse(UserDomain user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .status(user.getStatus().name())
                .roles(mapRoles(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private static Set<UserResponse.RoleDto> mapRoles(Set<RoleDomain> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(role -> UserResponse.RoleDto.builder()
                        .id(role.getId())
                        .type(role.getType().name())
                        .name(role.getName())
                        .permissions(mapPermissions(role.getPermissions()))
                        .build())
                .collect(Collectors.toSet());
    }

    private static Set<String> mapPermissions(Set<PermissionDomain> permissions) {
        if (permissions == null) {
            return Collections.emptySet();
        }

        return permissions.stream()
                .map(PermissionDomain::getName)
                .collect(Collectors.toSet());
    }
}
