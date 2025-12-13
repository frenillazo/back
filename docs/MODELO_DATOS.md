# Modelo de Datos

## Diagrama de Relaciones

```
User (1) ──────< Role (N)
  │
  │ teacherId
  ▼
SubjectGroup (N) >────── Subject (1)
  │
  │ groupId
  ▼
Schedule (N) ──────> Classroom
  │
  │ scheduleId
  ▼
Session (N) ──────> Classroom
  │
  │ sessionId
  ▼
SessionReservation (N) <────── Enrollment (1)
                                    │
                                    │ enrollmentId
                                    ▼
                               Payment (N)
```

---

## Entidades Principales

### User
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| email | String | Único, login |
| password | String | BCrypt |
| firstName | String | |
| lastName | String | |
| status | UserStatus | ACTIVE, BLOCKED, PENDING_ACTIVATION |
| roles | Set<Role> | Relación N:M |

### Role
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| type | RoleType | ADMIN, TEACHER, STUDENT |

### Subject (Asignatura)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| code | String | Único (ej: "PROG1") |
| name | String | |
| description | String | |
| degree | Degree | GRADO, MASTER |
| status | SubjectStatus | ACTIVE, ARCHIVED |
| weeklyHours | BigDecimal | Horas semanales |
| pricePerHour | BigDecimal | Precio €/hora |

### SubjectGroup (Grupo)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| subjectId | Long | FK → Subject |
| teacherId | Long | FK → User |
| type | GroupType | REGULAR_Q1, INTENSIVE_Q1, REGULAR_Q2, INTENSIVE_Q2 |
| status | GroupStatus | OPEN, CLOSED, CANCELLED |
| capacity | Integer | Capacidad custom (null = default) |
| currentEnrollmentCount | Integer | Contador de inscritos |

### Schedule (Horario)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| groupId | Long | FK → SubjectGroup |
| dayOfWeek | DayOfWeek | MONDAY-SUNDAY |
| startTime | LocalTime | |
| endTime | LocalTime | |
| classroom | Classroom | Enum |

### Session (Sesión de clase)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| subjectId | Long | FK → Subject |
| groupId | Long | FK → SubjectGroup (nullable para SCHEDULING) |
| scheduleId | Long | FK → Schedule (nullable para EXTRA) |
| date | LocalDate | |
| startTime | LocalTime | |
| endTime | LocalTime | |
| classroom | Classroom | |
| status | SessionStatus | SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED |
| type | SessionType | REGULAR, EXTRA, SCHEDULING |
| mode | SessionMode | IN_PERSON, ONLINE, DUAL |

### Enrollment (Inscripción)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| studentId | Long | FK → User |
| groupId | Long | FK → SubjectGroup |
| status | EnrollmentStatus | ACTIVE, WAITING_LIST, WITHDRAWN, COMPLETED |
| waitingListPosition | Integer | Posición en cola (null si no aplica) |
| pricePerHour | BigDecimal | Precio personalizado |
| enrolledAt | LocalDateTime | |

### SessionReservation (Reserva/Asistencia)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| studentId | Long | FK → User |
| sessionId | Long | FK → Session |
| enrollmentId | Long | FK → Enrollment |
| mode | ReservationMode | IN_PERSON, ONLINE |
| status | ReservationStatus | CONFIRMED, CANCELLED |
| onlineRequestStatus | OnlineRequestStatus | PENDING, APPROVED, REJECTED |
| attendanceStatus | AttendanceStatus | PRESENT, ABSENT, JUSTIFIED_ABSENCE |

### Payment (Pago)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| enrollmentId | Long | FK → Enrollment |
| studentId | Long | FK → User (denormalizado) |
| type | PaymentType | INITIAL, MONTHLY, INTENSIVE_FULL |
| status | PaymentStatus | PENDING, PAID, CANCELLED |
| amount | BigDecimal | |
| totalHours | BigDecimal | |
| pricePerHour | BigDecimal | Snapshot del precio |
| billingMonth | Integer | 1-12 |
| billingYear | Integer | |
| dueDate | LocalDate | |
| paidAt | LocalDateTime | |

### Material
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| subjectId | Long | FK → Subject |
| uploadedById | Long | FK → User |
| name | String | |
| description | String | |
| originalFilename | String | |
| storedFilename | String | UUID |
| fileExtension | String | pdf, java, cpp, h |
| mimeType | String | |
| fileSize | Long | bytes |

---

## Enumeraciones

### UserStatus
- `ACTIVE` - Cuenta activa
- `BLOCKED` - Bloqueada por admin o impago
- `PENDING_ACTIVATION` - Pendiente de activar

### RoleType
- `ADMIN` - Administrador
- `TEACHER` - Profesor
- `STUDENT` - Estudiante

### GroupType
- `REGULAR_Q1` - Regular cuatrimestre 1
- `REGULAR_Q2` - Regular cuatrimestre 2
- `INTENSIVE_Q1` - Intensivo cuatrimestre 1
- `INTENSIVE_Q2` - Intensivo cuatrimestre 2

### GroupStatus
- `OPEN` - Abierto para inscripciones
- `CLOSED` - Cerrado
- `CANCELLED` - Cancelado

### SessionStatus
- `SCHEDULED` - Programada
- `IN_PROGRESS` - En curso
- `COMPLETED` - Completada
- `CANCELLED` - Cancelada
- `POSTPONED` - Pospuesta

### SessionType
- `REGULAR` - Generada desde horario
- `EXTRA` - Sesión adicional manual
- `SCHEDULING` - Reunión para acordar horarios

### SessionMode
- `IN_PERSON` - Presencial
- `ONLINE` - Online
- `DUAL` - Híbrida

### EnrollmentStatus
- `ACTIVE` - Inscripción activa
- `WAITING_LIST` - En cola de espera
- `WITHDRAWN` - Retirado
- `COMPLETED` - Completado

### ReservationMode
- `IN_PERSON` - Asistirá presencialmente
- `ONLINE` - Asistirá online

### ReservationStatus
- `CONFIRMED` - Confirmada
- `CANCELLED` - Cancelada

### AttendanceStatus
- `PRESENT` - Asistió
- `ABSENT` - Faltó
- `JUSTIFIED_ABSENCE` - Ausencia justificada
- `NOT_RECORDED` - No registrada

### PaymentType
- `INITIAL` - Pago inicial (proporcional)
- `MONTHLY` - Mensualidad
- `INTENSIVE_FULL` - Pago único intensivo

### PaymentStatus
- `PENDING` - Pendiente
- `PAID` - Pagado
- `CANCELLED` - Cancelado

### Classroom
- `AULA_PORTAL1` - Aula física 1 (24 plazas)
- `AULA_PORTAL2` - Aula física 2 (24 plazas)
- `AULA_VIRTUAL` - Aula virtual (ilimitada)

### Degree
- `GRADO` - Grado universitario
- `MASTER` - Máster
