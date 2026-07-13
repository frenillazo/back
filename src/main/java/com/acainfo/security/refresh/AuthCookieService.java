package com.acainfo.security.refresh;

import com.acainfo.security.jwt.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Construye la cookie httpOnly que transporta el refresh token.
 *
 * <p>El refresh token vive SOLO en esta cookie: no viaja en el body de la
 * respuesta ni es accesible por JavaScript, lo que lo protege frente a XSS.
 * Se limita al path {@code /api/auth} para que el navegador solo la envíe a
 * los endpoints de refresh y logout.</p>
 */
@Component
public class AuthCookieService {

    /** Nombre de la cookie del refresh token (compartido con el controller). */
    public static final String REFRESH_COOKIE_NAME = "refreshToken";

    private static final String COOKIE_PATH = "/api/auth";

    private final JwtProperties jwtProperties;
    private final boolean secure;
    private final String sameSite;

    public AuthCookieService(
            JwtProperties jwtProperties,
            @Value("${app.auth.refresh-cookie.secure:true}") boolean secure,
            @Value("${app.auth.refresh-cookie.same-site:Lax}") String sameSite) {
        this.jwtProperties = jwtProperties;
        this.secure = secure;
        this.sameSite = sameSite;
    }

    /** Cookie con el refresh token, con la misma vida que el token en BD. */
    public ResponseCookie buildRefreshCookie(String refreshToken) {
        return baseBuilder(refreshToken)
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()))
                .build();
    }

    /** Cookie que borra el refresh token del navegador (logout). */
    public ResponseCookie buildClearRefreshCookie() {
        return baseBuilder("")
                .maxAge(0)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseBuilder(String value) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(COOKIE_PATH);
    }
}
