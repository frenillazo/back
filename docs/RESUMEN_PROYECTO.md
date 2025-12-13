# Resumen del Proyecto

## Sistema de Gestión para Centro de Formación

Backend para gestión académica de un centro de formación de ingeniería.

### Capacidad del Sistema
- 300-400 alumnos activos por cuatrimestre
- 2 aulas físicas × 24 plazas = 48 plazas presenciales
- 1 aula virtual (capacidad ilimitada)
- 4 profesores (2 con rol administrador)

---

## Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Framework | Spring Boot 3.2.1 |
| Lenguaje | Java 21 |
| Base de Datos | PostgreSQL (prod), H2 (dev/test) |
| Seguridad | Spring Security 6 + JWT |
| Mapeo | MapStruct 1.5.5 |
| Documentación | SpringDoc OpenAPI 2.3.0 |
| Build | Maven 3.9.11 |
| Contenedores | Docker + Docker Compose |

---

## Arquitectura

**Hexagonal (Ports & Adapters)** con módulos independientes.

```
módulo/
├── domain/                 # Núcleo - Java puro
│   ├── model/              # Entidades POJO (anémicas)
│   ├── exception/          # Excepciones de dominio
│   └── validation/         # Reglas de validación
├── application/            # Casos de uso
│   ├── port/in/            # Use Cases (interfaces)
│   ├── port/out/           # Repository Ports (interfaces)
│   ├── service/            # Implementación
│   ├── dto/                # Commands, Queries, Filters
│   └── mapper/             # Mappers de aplicación
└── infrastructure/         # Adaptadores
    ├── adapter/in/rest/    # Controllers + DTOs REST
    ├── adapter/out/persistence/
    │   ├── entity/         # Entidades JPA (*JpaEntity)
    │   ├── repository/     # JPA Repos + Adapters
    │   └── specification/  # Criteria Builder
    └── mapper/             # Mappers de persistencia
```

### Principios Clave
- **Dominio anémico**: POJOs con Lombok, lógica en servicios
- **Separación JPA**: Entidades JPA separadas con sufijo `*JpaEntity`
- **Referencias por ID**: Entre agregados se usa `Long` (no entidades)
- **DTOs como records**: Inmutables con constructor parametrizado

---

## Módulos

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| `user` | Usuarios, roles, autenticación | ✅ Completo |
| `security` | JWT, RefreshToken | ✅ Completo |
| `subject` | Asignaturas | ✅ Completo |
| `group` | Grupos de clase | ✅ Completo |
| `schedule` | Horarios semanales | ✅ Completo |
| `session` | Sesiones de clase | ✅ Completo |
| `enrollment` | Inscripciones, cola de espera | ✅ Completo |
| `reservation` | Reservas, asistencia | ✅ Completo |
| `material` | Materiales educativos | ✅ Completo |
| `payment` | Pagos, control de morosos | ✅ Completo |
| `student` | Dashboard estudiante | ✅ Completo |
| `shared` | Excepciones base, DTOs comunes | ✅ Completo |

---

## Ejecución

### Desarrollo (H2)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Producción (PostgreSQL)
```bash
docker-compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Tests
```bash
./mvnw test
```

---

## Documentación API

Swagger UI disponible en: `http://localhost:8080/swagger-ui.html`

---

## Estructura de Paquetes

```
com.acainfo/
├── AcaInfoApplication.java
├── shared/
├── security/
├── user/
├── subject/
├── group/
├── schedule/
├── session/
├── enrollment/
├── reservation/
├── material/
├── payment/
└── student/
```
