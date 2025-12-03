package com.acainfo.user.application.port.in;

import com.acainfo.user.application.dto.AuthenticationResult;

public interface RefreshTokenUseCase {
    AuthenticationResult refreshToken(String refreshToken);
}
