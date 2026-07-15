package com.acainfo.material.application.service;

import com.acainfo.course.application.port.in.GetCourseUseCase;
import com.acainfo.course.domain.model.Course;
import com.acainfo.enrollment.application.port.out.EnrollmentRepositoryPort;
import com.acainfo.enrollment.domain.model.Enrollment;
import com.acainfo.enrollment.domain.model.EnrollmentStatus;
import com.acainfo.material.application.port.out.MaterialRepositoryPort;
import com.acainfo.material.domain.model.Material;
import com.acainfo.user.application.port.out.UserRepositoryPort;
import com.acainfo.user.domain.model.Role;
import com.acainfo.user.domain.model.RoleType;
import com.acainfo.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link MaterialQueryService}.
 * The clock is fixed at 2026-02-15 → current academic year 2025 (course "2025-26").
 */
@ExtendWith(MockitoExtension.class)
class MaterialQueryServiceTest {

    private static final Long MATERIAL_ID = 10L;
    private static final Long USER_ID = 20L;
    private static final Long SUBJECT_ID = 30L;
    private static final Long COURSE_ID = 40L;
    private static final int CURRENT_ACADEMIC_YEAR = 2025;

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-02-15T12:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private MaterialRepositoryPort materialRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private EnrollmentRepositoryPort enrollmentRepository;

    @Mock
    private GetCourseUseCase getCourseUseCase;

    private MaterialQueryService service;

    @BeforeEach
    void setUp() {
        service = new MaterialQueryService(
                materialRepository, userRepository, enrollmentRepository, getCourseUseCase, FIXED_CLOCK);
    }

    private Material visibleMaterialOfYear(int academicYear) {
        return Material.builder()
                .id(MATERIAL_ID)
                .subjectId(SUBJECT_ID)
                .name("Apuntes tema 1")
                .academicYear(academicYear)
                .visible(true)
                .downloadDisabled(false)
                .build();
    }

    private User userWithRole(RoleType roleType) {
        return User.builder()
                .id(USER_ID)
                .email("user@acainfo.com")
                .roles(Set.of(Role.builder().type(roleType).build()))
                .build();
    }

    @Nested
    class CanDownload {

        @Test
        void deniesStudentDownloadOfPastYearMaterial() {
            when(materialRepository.findById(MATERIAL_ID))
                    .thenReturn(Optional.of(visibleMaterialOfYear(CURRENT_ACADEMIC_YEAR - 1)));
            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(userWithRole(RoleType.STUDENT)));

            assertThat(service.canDownload(MATERIAL_ID, USER_ID)).isFalse();
        }

        @Test
        void allowsAdminDownloadOfPastYearMaterial() {
            when(materialRepository.findById(MATERIAL_ID))
                    .thenReturn(Optional.of(visibleMaterialOfYear(CURRENT_ACADEMIC_YEAR - 1)));
            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(userWithRole(RoleType.ADMIN)));

            assertThat(service.canDownload(MATERIAL_ID, USER_ID)).isTrue();
        }

        @Test
        void allowsEnrolledStudentDownloadOfCurrentYearMaterial() {
            when(materialRepository.findById(MATERIAL_ID))
                    .thenReturn(Optional.of(visibleMaterialOfYear(CURRENT_ACADEMIC_YEAR)));
            when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(userWithRole(RoleType.STUDENT)));
            when(enrollmentRepository.findByStudentIdAndStatus(USER_ID, EnrollmentStatus.ACTIVE))
                    .thenReturn(List.of(Enrollment.builder()
                            .studentId(USER_ID)
                            .courseId(COURSE_ID)
                            .status(EnrollmentStatus.ACTIVE)
                            .build()));
            when(getCourseUseCase.getById(COURSE_ID))
                    .thenReturn(Course.builder().id(COURSE_ID).subjectId(SUBJECT_ID).build());

            assertThat(service.canDownload(MATERIAL_ID, USER_ID)).isTrue();
        }
    }
}
