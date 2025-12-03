# AcaInfo - Sistema de Gestión Centro de Formación

Sistema de gestión para centro de formación de ingeniería con arquitectura hexagonal pura.

## 🏗️ Arquitectura

Este proyecto implementa **Arquitectura Hexagonal Pura** con módulos independientes siguiendo los principios de Domain-Driven Design (DDD).

### Estructura de Módulos

```
com.acainfo/
├── shared/                    # Configuración y elementos compartidos
├── security/                  # JWT, RefreshToken (infraestructura)
│
├── user/                      # 👤 Gestión de usuarios y roles
├── subject/                   # 📚 Gestión de asignaturas
├── group/                     # 👥 Gestión de grupos
├── schedule/                  # 📅 Gestión de horarios
├── session/                   # 🎓 Gestión de sesiones
├── enrollment/                # 📝 Inscripciones y cola de espera
├── attendance/                # ✅ Control de asistencia
├── material/                  # 📄 Materiales educativos
├── payment/                   # 💳 Gestión de pagos
└── student/                   # 🎒 Dashboard estudiante (agregación)
```

### Estructura Hexagonal por Módulo

```
módulo/
├── domain/                    # 🔵 NÚCLEO - Java Puro
│   ├── model/                 # Entidades POJO puras
│   ├── exception/             # Excepciones de dominio
│   └── validation/            # Reglas de negocio
│
├── application/               # 🟢 CASOS DE USO
│   ├── port/in/               # Use Cases (interfaces)
│   ├── port/out/              # Repository Ports (interfaces)
│   ├── service/               # Implementación lógica
│   ├── dto/                   # Commands y Queries
│   └── mapper/                # Mappers de aplicación
│
└── infrastructure/            # 🟠 ADAPTADORES
    ├── adapter/in/rest/       # Controllers + DTOs REST
    ├── adapter/out/persistence/
    │   ├── entity/            # Entidades JPA (*JpaEntity)
    │   ├── repository/        # JPA Repos + Adapters
    │   └── specification/     # Criteria Builder
    └── mapper/                # Mappers de infraestructura
```

### Principios Arquitectónicos

1. **Dominio Puro**: Entidades sin anotaciones de framework
2. **Separación JPA**: Entidades JPA separadas con sufijo `*JpaEntity`
3. **MapStruct**: Conversiones automáticas entre capas
4. **Ports & Adapters**: Interfaces definen contratos, adaptadores implementan
5. **Dependency Inversion**: El dominio no depende de nada

## 🚀 Stack Tecnológico

- **Backend**: Spring Boot 3.2.1, Java 21
- **Base de Datos**: PostgreSQL (producción), H2 (desarrollo/test)
- **Mapeo**: MapStruct 1.5.5.Final
- **Seguridad**: Spring Security 6 + JWT (io.jsonwebtoken 0.12.6)
- **Documentación**: SpringDoc OpenAPI 2.3.0
- **Build**: Maven 3.9.11
- **Contenedores**: Docker + Docker Compose

## 📋 Requisitos Previos

- Java 21
- Maven 3.9+
- Docker & Docker Compose (para PostgreSQL)

## 🛠️ Setup del Proyecto

### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd recursing-turing
```

### 2. Compilar el proyecto

```bash
mvn clean install
```

### 3. Ejecutar en modo desarrollo (H2)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación estará disponible en: http://localhost:8080

### 4. Ejecutar con PostgreSQL (Docker)

```bash
# Levantar PostgreSQL y pgAdmin
docker-compose up -d

# Ejecutar aplicación con perfil prod
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Acceso a servicios:
- **Aplicación**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console** (dev): http://localhost:8080/h2-console
- **pgAdmin**: http://localhost:5050 (admin@acainfo.com / admin)

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar con cobertura
mvn test jacoco:report

# Ver reporte de cobertura
open target/site/jacoco/index.html
```

## 📚 Documentación API

La documentación de la API está disponible mediante Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:
```
http://localhost:8080/v3/api-docs
```

## 🔒 Seguridad

El sistema utiliza JWT para autenticación:

1. **Registro**: `POST /api/auth/register`
2. **Login**: `POST /api/auth/login` (retorna JWT + RefreshToken)
3. **Refresh**: `POST /api/auth/refresh`
4. **Logout**: `POST /api/auth/logout`

### Roles del Sistema

- **ADMIN**: Administradores (gestión completa)
- **TEACHER**: Profesores (gestión académica)
- **STUDENT**: Estudiantes (consulta e inscripciones)

## 🗂️ Perfiles de Configuración

### Development (dev)
- Base de datos H2 en memoria
- SQL logging habilitado
- H2 Console habilitada
- Recreación de esquema en cada inicio

### Test (test)
- Base de datos H2 en memoria independiente
- Logging mínimo
- Configuración optimizada para tests

### Production (prod)
- PostgreSQL
- Variables de entorno para credenciales
- Logging en nivel INFO
- Pool de conexiones optimizado

## 🐳 Docker

### Levantar servicios

```bash
docker-compose up -d
```

### Detener servicios

```bash
docker-compose down
```

### Logs

```bash
docker-compose logs -f postgres
```

### Configuración PostgreSQL

- **Host**: localhost
- **Puerto**: 5432
- **Database**: formaciondb
- **Usuario**: postgres
- **Password**: postgres

## 📁 Estructura de Directorios

```
.
├── src/
│   ├── main/
│   │   ├── java/com/acainfo/
│   │   │   ├── AcaInfoApplication.java
│   │   │   ├── shared/           # Módulo compartido
│   │   │   ├── security/         # Módulo de seguridad
│   │   │   ├── user/             # Módulo de usuarios
│   │   │   ├── subject/          # Módulo de asignaturas
│   │   │   ├── group/            # Módulo de grupos
│   │   │   ├── schedule/         # Módulo de horarios
│   │   │   ├── session/          # Módulo de sesiones
│   │   │   ├── enrollment/       # Módulo de inscripciones
│   │   │   ├── attendance/       # Módulo de asistencia
│   │   │   ├── material/         # Módulo de materiales
│   │   │   ├── payment/          # Módulo de pagos
│   │   │   └── student/          # Dashboard estudiante
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-test.properties
│   │       └── application-prod.properties
│   └── test/
│       └── java/com/acainfo/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── CLAUDE.md                     # Plan de implementación
└── README.md
```

## 🚧 Estado del Proyecto

### ✅ Fase 0: Setup Inicial (Completada)
- [x] Proyecto Spring Boot configurado
- [x] MapStruct + Lombok configurados
- [x] Perfiles de configuración (dev, test, prod)
- [x] Módulo shared con excepciones base
- [x] Estructura hexagonal de módulos creada
- [x] Docker Compose configurado
- [x] Documentación básica

### 🔄 Próximas Fases
- [ ] Fase 1: Módulo User + Seguridad
- [ ] Fase 2: Gestión Académica (Subject, Group, Schedule)
- [ ] Fase 3: Gestión de Sesiones
- [ ] Fase 4: Inscripciones
- [ ] Fase 5: Control de Asistencia
- [ ] Fase 6: Materiales Educativos
- [ ] Fase 7: Sistema de Pagos
- [ ] Fase 8: Dashboard Estudiante
- [ ] Fase 9: Testing y Calidad
- [ ] Fase 10: Documentación y Deployment

## 📖 Convenciones de Código

### Dominio
- POJOs puros sin anotaciones de framework
- Constructores privados + métodos estáticos de creación
- Lógica de negocio en las entidades
- Sin dependencias externas

### Aplicación
- Interfaces de puertos (in/out)
- Servicios implementan casos de uso
- DTOs para Commands y Queries
- MapStruct para conversiones

### Infraestructura
- Entidades JPA con sufijo `*JpaEntity`
- Adapters implementan puertos
- Specifications para filtros dinámicos
- Lombok permitido (solo infraestructura)

## 🤝 Contribución

1. Seguir los principios de arquitectura hexagonal
2. Mantener el dominio puro (sin frameworks)
3. Escribir tests para cada capa
4. Documentar decisiones arquitectónicas importantes

## 📝 Licencia

[Especificar licencia]

## 👥 Contacto

[Información de contacto]
