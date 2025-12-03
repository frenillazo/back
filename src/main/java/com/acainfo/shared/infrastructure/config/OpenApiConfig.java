package com.acainfo.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 * Configures Swagger UI and API documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:AcaInfo API}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AcaInfo - Sistema de Gestión Centro de Formación")
                        .version("1.0.0")
                        .description("""
                                API REST para gestión de centro de formación de ingeniería.

                                ## Características principales:
                                - Gestión de usuarios (estudiantes, profesores, administradores)
                                - Autenticación JWT con refresh tokens
                                - Gestión académica (asignaturas, grupos, horarios)
                                - Control de asistencia
                                - Sistema de inscripciones y cola de espera
                                - Gestión de materiales educativos
                                - Sistema de pagos (Stripe)

                                ## Arquitectura:
                                - Hexagonal Pura (Ports & Adapters)
                                - Domain-Driven Design
                                - Spring Boot 3.2.1 + Java 21

                                ## Autenticación:
                                1. Registrarse en `/api/auth/register`
                                2. Login en `/api/auth/login` (retorna accessToken y refreshToken)
                                3. Incluir el accessToken en el header Authorization: Bearer {token}
                                4. Renovar token en `/api/auth/refresh` cuando expire
                                """)
                        .contact(new Contact()
                                .name("AcaInfo Team")
                                .email("info@acainfo.com")
                                .url("https://acainfo.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.acainfo.com")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                JWT token obtenido del endpoint /api/auth/login.

                                                Formato: Bearer {token}

                                                Ejemplo: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
                                                """)));
    }
}
