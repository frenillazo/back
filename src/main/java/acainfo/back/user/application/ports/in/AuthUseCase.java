// ============================================
// ports/in/AuthUseCase.java
// ============================================
package acainfo.back.user.application.ports.in;

import acainfo.back.user.infrastructure.adapters.in.dto.AuthResponse;
import acainfo.back.user.infrastructure.adapters.in.dto.LoginRequest;
import acainfo.back.user.infrastructure.adapters.in.dto.RegisterRequest;

public interface AuthUseCase {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    void revokeAllTokens();
}