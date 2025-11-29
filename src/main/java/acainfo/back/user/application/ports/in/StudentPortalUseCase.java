// ============================================
// ports/in/StudentPortalUseCase.java
// ============================================
package acainfo.back.user.application.ports.in;

import acainfo.back.user.infrastructure.adapters.in.dto.StudentDashboardResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.StudentProfileResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.UpdateStudentProfileRequest;

public interface StudentPortalUseCase {
    StudentDashboardResponse getDashboard(Long studentId);
    StudentProfileResponse getProfile(Long studentId);
    StudentProfileResponse updateProfile(Long studentId, UpdateStudentProfileRequest request);
}