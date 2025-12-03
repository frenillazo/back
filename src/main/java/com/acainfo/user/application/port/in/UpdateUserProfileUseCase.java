package com.acainfo.user.application.port.in;

import com.acainfo.user.application.dto.UpdateUserCommand;
import com.acainfo.user.domain.model.User;

public interface UpdateUserProfileUseCase {
    User updateProfile(Long userId, UpdateUserCommand command);
    void changePassword(Long userId, String currentPassword, String newPassword);
}
