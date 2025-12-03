package com.acainfo.user.application.port.in;

import com.acainfo.user.application.dto.RegisterUserCommand;
import com.acainfo.user.domain.model.User;

/**
 * Use case for user registration.
 * Input port defining the contract for registering new users.
 */
public interface RegisterUserUseCase {

    /**
     * Register a new user (student by default).
     *
     * @param command Registration data
     * @return Registered user
     */
    User register(RegisterUserCommand command);
}
