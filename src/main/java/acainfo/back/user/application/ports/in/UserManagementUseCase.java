// ============================================
// ports/in/UserManagementUseCase.java
// ============================================
package acainfo.back.user.application.ports.in;

import acainfo.back.user.domain.model.RoleType;
import acainfo.back.user.domain.model.User;
import acainfo.back.user.infrastructure.adapters.in.dto.CreateTeacherRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.UpdateTeacherRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.UserResponse;

import java.util.List;

public interface UserManagementUseCase {
    List<UserResponse> getUsersByRole(RoleType roleType);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    UserResponse createTeacher(CreateTeacherRequest request, User currentUser);
    UserResponse updateTeacher(Long teacherId, UpdateTeacherRequest request, User currentUser);
    void deleteTeacher(Long teacherId, User currentUser);
}