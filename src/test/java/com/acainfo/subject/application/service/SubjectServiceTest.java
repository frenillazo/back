package com.acainfo.subject.application.service;

import com.acainfo.course.application.port.out.CourseRepositoryPort;
import com.acainfo.subject.application.dto.UpdateSubjectCommand;
import com.acainfo.subject.application.port.out.SubjectRepositoryPort;
import com.acainfo.subject.domain.model.Degree;
import com.acainfo.subject.domain.model.Subject;
import com.acainfo.subject.domain.model.SubjectStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link SubjectService}.
 */
@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    private static final Long SUBJECT_ID = 1L;

    @Mock
    private SubjectRepositoryPort subjectRepositoryPort;

    @Mock
    private CourseRepositoryPort courseRepositoryPort;

    @InjectMocks
    private SubjectService subjectService;

    private Subject subjectWithYear(Integer year) {
        return Subject.builder()
                .id(SUBJECT_ID)
                .code("ING101")
                .name("Programación I")
                .degree(Degree.INGENIERIA_INFORMATICA)
                .year(year)
                .status(SubjectStatus.ACTIVE)
                .build();
    }

    @Nested
    class Update {

        private Subject stubSubject(Integer year) {
            Subject subject = subjectWithYear(year);
            when(subjectRepositoryPort.findById(SUBJECT_ID)).thenReturn(Optional.of(subject));
            when(subjectRepositoryPort.save(any(Subject.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            return subject;
        }

        @Test
        void updatesYearWhenProvided() {
            stubSubject(1);

            Subject updated = subjectService.update(
                    SUBJECT_ID, new UpdateSubjectCommand(null, 3, null, null));

            assertThat(updated.getYear()).isEqualTo(3);
        }

        @Test
        void keepsYearWhenNeitherYearNorClearYearProvided() {
            stubSubject(2);

            Subject updated = subjectService.update(
                    SUBJECT_ID, new UpdateSubjectCommand("Nuevo nombre", null, null, null));

            assertThat(updated.getYear()).isEqualTo(2);
            assertThat(updated.getName()).isEqualTo("Nuevo nombre");
        }

        @Test
        void clearsYearWhenClearYearIsTrue() {
            stubSubject(2);

            Subject updated = subjectService.update(
                    SUBJECT_ID, new UpdateSubjectCommand(null, null, true, null));

            assertThat(updated.getYear()).isNull();
        }

        @Test
        void clearYearTakesPrecedenceOverYear() {
            stubSubject(2);

            Subject updated = subjectService.update(
                    SUBJECT_ID, new UpdateSubjectCommand(null, 4, true, null));

            assertThat(updated.getYear()).isNull();
        }

        @Test
        void keepsYearWhenClearYearIsFalse() {
            stubSubject(2);

            Subject updated = subjectService.update(
                    SUBJECT_ID, new UpdateSubjectCommand(null, null, false, null));

            assertThat(updated.getYear()).isEqualTo(2);
        }
    }
}
