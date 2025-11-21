package acainfo.back.shared.infrastructure.adapters.in.dto;

import acainfo.back.shared.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for student profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student profile information")
public class StudentProfileResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Email address", example = "estudiante@acainfo.com")
    private String email;

    @Schema(description = "First name", example = "María")
    private String firstName;

    @Schema(description = "Last name", example = "López")
    private String lastName;

    @Schema(description = "Full name", example = "María López")
    private String fullName;

    @Schema(description = "Phone number", example = "+34 612 345 678")
    private String phone;

    @Schema(description = "Account creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Last login date")
    private LocalDateTime lastLogin;

    @Schema(description = "Account status", example = "ACTIVE")
    private String status;

    public static StudentProfileResponse fromEntity(User user) {
        return StudentProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .createdAt(user.getCreatedAt())
            .lastLogin(user.getLastLogin())
            .status(user.getStatus().name())
            .build();
    }
}
