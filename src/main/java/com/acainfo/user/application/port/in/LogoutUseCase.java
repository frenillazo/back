package com.acainfo.user.application.port.in;

public interface LogoutUseCase {
    void logout(String refreshToken);
    void logoutAllDevices(Long userId);
}
