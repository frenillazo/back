package com.acainfo.user.application.port.in;

import com.acainfo.user.application.dto.AuthenticationCommand;
import com.acainfo.user.application.dto.AuthenticationResult;

public interface AuthenticateUserUseCase {
    AuthenticationResult authenticate(AuthenticationCommand command);
}
