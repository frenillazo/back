package com.acainfo.user.application.port.in;

import com.acainfo.user.domain.model.User;

public interface GetUserProfileUseCase {
    User getUserById(Long userId);
    User getUserByEmail(String email);
}
