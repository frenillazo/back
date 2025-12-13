# API Reference

Base URL: `http://localhost:8080/api`

## Autenticación

Todos los endpoints (excepto login/register) requieren header:
```
Authorization: Bearer <jwt_token>
```

---

## Auth (`/api/auth`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/auth/register` | Registro de usuario | Público |
| POST | `/auth/login` | Login (devuelve JWT + RefreshToken) | Público |
| POST | `/auth/refresh` | Renovar JWT con RefreshToken | Autenticado |
| POST | `/auth/logout` | Invalidar RefreshToken actual | Autenticado |
| POST | `/auth/logout/all` | Invalidar todos los RefreshTokens | Autenticado |

### Request/Response Examples

**POST /auth/register**
```json
{
  "email": "student@example.com",
  "password": "password123",
  "firstName": "Juan",
  "lastName": "García"
}
```

**POST /auth/login**
```json
// Request
{ "email": "student@example.com", "password": "password123" }

// Response
{
  "accessToken": "eyJhbG...",
  "refreshToken": "abc123...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

## Users (`/api/users`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/users/profile` | Perfil del usuario autenticado | Autenticado |
| PUT | `/users/profile` | Actualizar nombre | Autenticado |
| PUT | `/users/profile/password` | Cambiar contraseña | Autenticado |

---

## Admin (`/api/admin`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/admin/users` | Listar usuarios con filtros | ADMIN |
| GET | `/admin/users/{id}` | Obtener usuario por ID | ADMIN |
| PUT | `/admin/users/{id}/block` | Bloquear usuario | ADMIN |
| PUT | `/admin/users/{id}/unblock` | Desbloquear usuario | ADMIN |

---

## Teachers (`/api/teachers`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/teachers` | Crear profesor | ADMIN |
| GET | `/teachers` | Listar profesores | ADMIN |
| GET | `/teachers/{id}` | Obtener profesor | ADMIN |
| PUT | `/teachers/{id}` | Actualizar profesor | ADMIN |
| DELETE | `/teachers/{id}` | Eliminar profesor | ADMIN |

---

## Subjects (`/api/subjects`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/subjects` | Crear asignatura | ADMIN |
| GET | `/subjects` | Listar con filtros | Autenticado |
| GET | `/subjects/{id}` | Obtener por ID | Autenticado |
| GET | `/subjects/code/{code}` | Obtener por código | Autenticado |
| PUT | `/subjects/{id}` | Actualizar | ADMIN |
| DELETE | `/subjects/{id}` | Eliminar | ADMIN |
| POST | `/subjects/{id}/archive` | Archivar | ADMIN |

### Query Parameters (GET /subjects)
- `name`, `code`, `degree`, `status`, `searchTerm`
- `page`, `size`, `sortBy`, `sortDirection`

---

## Groups (`/api/groups`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/groups` | Crear grupo | ADMIN |
| GET | `/groups` | Listar con filtros | Autenticado |
| GET | `/groups/{id}` | Obtener por ID | Autenticado |
| PUT | `/groups/{id}` | Actualizar | ADMIN |
| DELETE | `/groups/{id}` | Eliminar | ADMIN |
| POST | `/groups/{id}/cancel` | Cancelar grupo | ADMIN |

### Query Parameters (GET /groups)
- `subjectId`, `teacherId`, `type`, `status`
- `page`, `size`, `sortBy`, `sortDirection`

---

## Schedules (`/api/schedules`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/schedules` | Crear horario | ADMIN |
| GET | `/schedules` | Listar con filtros | Autenticado |
| GET | `/schedules/{id}` | Obtener por ID | Autenticado |
| GET | `/schedules/group/{groupId}` | Horarios de un grupo | Autenticado |
| PUT | `/schedules/{id}` | Actualizar | ADMIN |
| DELETE | `/schedules/{id}` | Eliminar | ADMIN |

---

## Sessions (`/api/sessions`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/sessions` | Crear sesión manual | ADMIN, TEACHER |
| GET | `/sessions` | Listar con filtros | Autenticado |
| GET | `/sessions/{id}` | Obtener por ID | Autenticado |
| GET | `/sessions/group/{groupId}` | Sesiones de un grupo | Autenticado |
| GET | `/sessions/subject/{subjectId}` | Sesiones de asignatura | Autenticado |
| GET | `/sessions/schedule/{scheduleId}` | Sesiones de horario | Autenticado |
| PUT | `/sessions/{id}` | Actualizar | ADMIN, TEACHER |
| DELETE | `/sessions/{id}` | Eliminar | ADMIN, TEACHER |

### Lifecycle

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/sessions/{id}/cancel` | Cancelar sesión | ADMIN, TEACHER |
| POST | `/sessions/{id}/complete` | Marcar completada | ADMIN, TEACHER |
| POST | `/sessions/{id}/postpone` | Posponer sesión | ADMIN, TEACHER |
| POST | `/sessions/{id}/start` | Iniciar sesión | ADMIN, TEACHER |

### Generation

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/sessions/generate` | Generar desde horarios | ADMIN |

---

## Enrollments (`/api/enrollments`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/enrollments` | Inscribirse en grupo | ADMIN, STUDENT |
| GET | `/enrollments` | Listar con filtros | Autenticado |
| GET | `/enrollments/{id}` | Obtener por ID | Autenticado |
| GET | `/enrollments/student/{studentId}` | Inscripciones activas | Autenticado |
| GET | `/enrollments/group/{groupId}` | Inscripciones del grupo | Autenticado |
| DELETE | `/enrollments/{id}` | Retirarse | ADMIN, STUDENT |
| PUT | `/enrollments/{id}/change-group` | Cambiar de grupo | ADMIN, STUDENT |

---

## Waiting Queue (`/api/waiting-queue`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/waiting-queue/group/{groupId}` | Cola de un grupo | Autenticado |
| GET | `/waiting-queue/student/{studentId}` | Colas del estudiante | Autenticado |
| DELETE | `/waiting-queue/{id}` | Salir de cola | ADMIN, STUDENT |

---

## Reservations (`/api/reservations`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/reservations` | Crear reserva | Autenticado |
| GET | `/reservations` | Listar con filtros | Autenticado |
| GET | `/reservations/{id}` | Obtener por ID | Autenticado |
| GET | `/reservations/session/{sessionId}` | Reservas de sesión | Autenticado |
| GET | `/reservations/student/{studentId}` | Reservas del estudiante | Autenticado |
| DELETE | `/reservations/{id}` | Cancelar reserva | Autenticado |
| PUT | `/reservations/{id}/switch-session` | Cambiar sesión | Autenticado |
| POST | `/reservations/generate` | Generar automáticamente | ADMIN |

### Online Requests

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/reservations/{id}/online-request` | Solicitar online | STUDENT |
| PUT | `/reservations/{id}/online-request/process` | Aprobar/rechazar | ADMIN, TEACHER |
| GET | `/online-requests/pending` | Solicitudes pendientes | ADMIN, TEACHER |

### Attendance

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| PUT | `/reservations/{id}/attendance` | Registrar asistencia individual | ADMIN, TEACHER |
| POST | `/sessions/{sessionId}/attendance` | Registrar asistencia masiva | ADMIN, TEACHER |
| GET | `/sessions/{sessionId}/attendance` | Listar asistencia de sesión | ADMIN, TEACHER |

---

## Materials (`/api/materials`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/materials` | Subir material (multipart) | ADMIN, TEACHER |
| GET | `/materials` | Listar con filtros | Autenticado |
| GET | `/materials/{id}` | Metadata del material | Autenticado |
| GET | `/materials/{id}/download` | Descargar archivo | Autenticado* |
| GET | `/materials/{id}/can-download` | Verificar permiso | Autenticado |
| GET | `/materials/subject/{subjectId}` | Materiales de asignatura | Autenticado |
| DELETE | `/materials/{id}` | Eliminar | ADMIN, TEACHER |

*Requiere inscripción activa y pagos al día

---

## Payments (`/api/payments`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/payments` | Listar con filtros | Autenticado |
| GET | `/payments/{id}` | Obtener por ID | Autenticado |
| GET | `/payments/student/{studentId}` | Pagos del estudiante | Autenticado |
| GET | `/payments/student/{studentId}/pending` | Pagos pendientes | Autenticado |
| GET | `/payments/student/{studentId}/overdue` | Pagos vencidos | Autenticado |
| GET | `/payments/student/{studentId}/access` | Estado de acceso | Autenticado |
| POST | `/payments/{id}/pay` | Marcar como pagado | Autenticado |

### Admin

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/payments/generate` | Generar pago inicial | ADMIN |
| POST | `/payments/generate-monthly` | Generar mensualidades | ADMIN |
| POST | `/payments/{id}/cancel` | Cancelar pago | ADMIN |
| GET | `/payments/overdue` | Todos los vencidos | ADMIN |

---

## Student Dashboard (`/api/student`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/student/overview` | Overview del estudiante autenticado | STUDENT |
| GET | `/student/{studentId}/overview` | Overview de cualquier estudiante | ADMIN |

### Query Parameters
- `upcomingSessionsLimit`: Número de próximas sesiones (default: 5, max: 20)

### Response Structure
```json
{
  "userId": 1,
  "fullName": "Juan García",
  "email": "juan@example.com",
  "activeEnrollments": [
    {
      "enrollmentId": 1,
      "groupId": 1,
      "subjectName": "Programación I",
      "subjectCode": "PROG1",
      "groupType": "REGULAR_Q1",
      "teacherName": "Prof. López",
      "enrolledAt": "2025-01-15T10:00:00"
    }
  ],
  "waitingListCount": 0,
  "upcomingSessions": [
    {
      "sessionId": 1,
      "groupId": 1,
      "subjectName": "Programación I",
      "subjectCode": "PROG1",
      "groupType": "REGULAR_Q1",
      "date": "2025-01-20",
      "startTime": "10:00",
      "endTime": "12:00",
      "classroom": "Aula Portal 1",
      "sessionStatus": "SCHEDULED",
      "hasReservation": true
    }
  ],
  "paymentStatus": {
    "canAccessResources": true,
    "hasOverduePayments": false,
    "pendingPaymentsCount": 1,
    "totalPendingAmount": 120.00,
    "nextDueDate": "2025-02-01"
  }
}
```

---

## Paginación

Endpoints que devuelven listas soportan paginación:

### Query Parameters
- `page`: Número de página (0-based, default: 0)
- `size`: Tamaño de página (default: 20)
- `sortBy`: Campo para ordenar
- `sortDirection`: ASC o DESC

### Response Structure
```json
{
  "content": [...],
  "pageNumber": 0,
  "totalPages": 5,
  "totalElements": 100,
  "size": 20,
  "first": true,
  "last": false
}
```

---

## Códigos de Error

| Código | Significado |
|--------|-------------|
| 400 | Bad Request - Validación fallida |
| 401 | Unauthorized - Token inválido o expirado |
| 403 | Forbidden - Sin permisos |
| 404 | Not Found - Recurso no encontrado |
| 409 | Conflict - Duplicado o estado inválido |
| 500 | Internal Server Error |

### Error Response
```json
{
  "timestamp": "2025-01-15T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email already exists",
  "path": "/api/auth/register"
}
```
