package com.acainfo.user.infrastructure.adapter.in.rest;

import com.acainfo.security.terms.TermsAcceptanceService;
import com.acainfo.security.userdetails.CustomUserDetails;
import com.acainfo.user.application.port.in.AuthenticateUserUseCase;
import com.acainfo.user.application.port.in.LogoutUseCase;
import com.acainfo.user.application.port.in.RefreshTokenUseCase;
import com.acainfo.user.application.port.in.RegisterUserUseCase;
import com.acainfo.user.application.port.in.RequestPasswordResetUseCase;
import com.acainfo.user.application.port.in.ResendVerificationUseCase;
import com.acainfo.user.application.port.in.ResetPasswordUseCase;
import com.acainfo.user.application.port.in.VerifyEmailUseCase;
import com.acainfo.user.domain.model.User;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.MessageResponse;
import com.acainfo.user.infrastructure.adapter.in.rest.dto.RefreshTokenRequest;
import com.acainfo.user.infrastructure.mapper.UserRestMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Pure unit tests for the logout endpoints of {@link AuthController}.
 *
 * <p>Covers the jul-2026 audit bug #5: logout must not fail when the frontend
 * sends no body, and logout/all must actually revoke all user tokens.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerLogoutTest {

    private static final Long USER_ID = 42L;

    @Mock
    private RegisterUserUseCase registerUserUseCase;
    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;
    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;
    @Mock
    private LogoutUseCase logoutUseCase;
    @Mock
    private VerifyEmailUseCase verifyEmailUseCase;
    @Mock
    private ResendVerificationUseCase resendVerificationUseCase;
    @Mock
    private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;
    @Mock
    private UserRestMapper userRestMapper;
    @Mock
    private TermsAcceptanceService termsAcceptanceService;

    @InjectMocks
    private AuthController authController;

    private CustomUserDetails userDetails() {
        User user = User.builder()
                .id(USER_ID)
                .email("student@acainfo.com")
                .build();
        return new CustomUserDetails(user);
    }

    @Test
    void logout_withRefreshToken_revokesIt() {
        ResponseEntity<MessageResponse> response =
                authController.logout(new RefreshTokenRequest("refresh-token-123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(logoutUseCase).logout("refresh-token-123");
    }

    @Test
    void logout_withoutBody_returnsOkWithoutRevoking() {
        ResponseEntity<MessageResponse> response = authController.logout(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verifyNoInteractions(logoutUseCase);
    }

    @Test
    void logout_withBlankToken_returnsOkWithoutRevoking() {
        ResponseEntity<MessageResponse> response =
                authController.logout(new RefreshTokenRequest("   "));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verifyNoInteractions(logoutUseCase);
    }

    @Test
    void logoutAllDevices_authenticated_revokesAllUserTokens() {
        ResponseEntity<MessageResponse> response =
                authController.logoutAllDevices(userDetails());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(logoutUseCase).logoutAllDevices(USER_ID);
    }

    @Test
    void logoutAllDevices_withoutPrincipal_returnsUnauthorized() {
        ResponseEntity<MessageResponse> response = authController.logoutAllDevices(null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(logoutUseCase);
    }
}
