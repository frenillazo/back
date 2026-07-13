package com.acainfo.security.jwt;

import com.acainfo.shared.infrastructure.rest.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point para peticiones NO autenticadas a endpoints protegidos.
 *
 * <p>Devuelve <b>401</b> (no 403) cuando falta el token o es inválido/expirado,
 * de modo que el interceptor del front pueda disparar el refresh silencioso.
 * El 403 queda reservado para usuarios autenticados sin permisos (rol
 * insuficiente), gestionado por el {@code AccessDeniedHandler} por defecto.</p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "No autenticado o sesión expirada",
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getWriter(), body);
    }
}
