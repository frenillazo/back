package com.acainfo.user.application.port.in;

import com.acainfo.user.application.dto.CreateTeacherCommand;
import com.acainfo.user.application.dto.UpdateTeacherCommand;
import com.acainfo.user.application.dto.UserFilters;
import com.acainfo.user.domain.model.User;
import org.springframework.data.domain.Page;

public interface ManageTeachersUseCase {
    User createTeacher(CreateTeacherCommand command);
    User updateTeacher(Long teacherId, UpdateTeacherCommand command);
    void deleteTeacher(Long teacherId);
    Page<User> getTeachers(UserFilters filters);
    User getTeacherById(Long teacherId);
}
