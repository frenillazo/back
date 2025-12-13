# Reglas de Negocio

## Capacidades

### Aulas
| Aula | Tipo | Capacidad |
|------|------|-----------|
| AULA_PORTAL1 | Física | 24 plazas |
| AULA_PORTAL2 | Física | 24 plazas |
| AULA_VIRTUAL | Virtual | Ilimitada |

### Grupos
| Tipo | Capacidad Default | Descripción |
|------|-------------------|-------------|
| REGULAR | 24 | Clases regulares cuatrimestrales |
| INTENSIVE | 50 | Intensivos (mayor flexibilidad online) |

- Una asignatura puede tener **múltiples grupos del mismo tipo**
- No hay límite de grupos por asignatura
- La capacidad es customizable dentro del límite del tipo

---

## Inscripciones

### Flujo de Inscripción
1. Estudiante solicita inscripción en grupo
2. Si hay plazas → Estado `ACTIVE`
3. Si no hay plazas → Estado `WAITING_LIST` (cola FIFO)
4. Al liberarse plaza → Primer estudiante de cola es promovido automáticamente

### Cola de Espera
- Orden FIFO (First In, First Out)
- `waitingListPosition` indica posición en cola
- Al retirarse un estudiante, las posiciones se reajustan
- Promoción automática al liberarse plaza

### Cambio de Grupo
- Solo entre grupos de la **misma asignatura**
- Mantiene historial de inscripción original
- Sujeto a disponibilidad del grupo destino

---

## Sesiones

### Tipos de Sesión
| Tipo | scheduleId | groupId | Descripción |
|------|------------|---------|-------------|
| REGULAR | ✓ | ✓ | Generada automáticamente desde horario |
| EXTRA | ✗ | ✓ | Sesión adicional manual |
| SCHEDULING | ✗ | ✗ | Reunión para acordar horarios |

### Estados y Transiciones
```
SCHEDULED ──┬──► IN_PROGRESS ──► COMPLETED
            │
            ├──► CANCELLED
            │
            └──► POSTPONED ──► SCHEDULED (nueva fecha)
```

### Generación Automática
- Se generan desde los `Schedule` configurados
- Evita duplicados (verifica `scheduleId + date`)
- Rango de fechas configurable

---

## Reservas y Asistencia

### Modos de Reserva
- `IN_PERSON`: Ocupa plaza física en aula
- `ONLINE`: No ocupa plaza física

### Solicitud Online
1. Estudiante solicita cambiar a online (`PENDING`)
2. Profesor aprueba/rechaza (`APPROVED`/`REJECTED`)
3. Si aprobada, la reserva cambia a modo `ONLINE`

### Control de Asistencia
- Individual: Por reserva específica
- Masivo: Todas las reservas de una sesión
- Estados: `PRESENT`, `ABSENT`, `JUSTIFIED_ABSENCE`

---

## Pagos

### Tipos de Pago
| Tipo | Grupos | Descripción |
|------|--------|-------------|
| INITIAL | REGULAR | Pago proporcional al inscribirse (días restantes del mes) |
| MONTHLY | REGULAR | Mensualidad completa (1º de cada mes) |
| INTENSIVE_FULL | INTENSIVE | Pago único al inscribirse |

### Cálculo de Importe
```
amount = totalHours × pricePerHour
```
- `pricePerHour` se copia del `Enrollment` al generar el pago
- Permite precios personalizados por estudiante

### Fecha de Vencimiento
- `dueDate = generatedAt + 5 días`
- Pagos vencidos: `status = PENDING` AND `dueDate < hoy`

### Bloqueo por Impago
Estudiantes con pagos vencidos (+5 días) **NO pueden**:
- Descargar materiales
- Ver horarios/sesiones detallados
- Hacer reservas
- Recibir notificaciones

**SÍ pueden**:
- Ver su perfil
- Ver información pública
- Ver oferta académica y precios

### Verificación de Acceso
```
canAccessResources = !hasOverduePayments
```

---

## Materiales

### Tipos Permitidos
- `.pdf` - Documentos
- `.java` - Código Java
- `.cpp` - Código C++
- `.h` - Headers C/C++

### Control de Acceso para Descarga
1. Admin/Teacher → Acceso total
2. Estudiante → Requiere:
   - Inscripción activa en la asignatura
   - Sin pagos vencidos

### Almacenamiento
- Archivos guardados con UUID como nombre
- Metadatos en base de datos
- Path: `uploads/materials/{uuid}.{ext}`

---

## Usuarios y Roles

### Roles
| Rol | Permisos |
|-----|----------|
| ADMIN | Todo |
| TEACHER | Gestionar sesiones propias, asistencia, materiales |
| STUDENT | Inscribirse, reservar, descargar (con pagos al día) |

### Estados de Cuenta
- `ACTIVE`: Puede operar normalmente
- `BLOCKED`: Acceso denegado (manual o por impago)
- `PENDING_ACTIVATION`: Registro incompleto

---

## Horarios

### Restricciones
- Un grupo puede tener múltiples horarios (distintos días)
- Detección de conflictos por aula + día + hora
- Horario define: día semana, hora inicio/fin, aula

### Conflictos
Se verifica que no exista otro horario con:
- Mismo `classroom`
- Mismo `dayOfWeek`
- Solapamiento de horas

---

## Cuatrimestres

Los tipos de grupo indican el cuatrimestre:
- `*_Q1`: Primer cuatrimestre (Sept-Ene)
- `*_Q2`: Segundo cuatrimestre (Feb-Jun)

Esto afecta:
- Generación de sesiones (rango de fechas)
- Generación de pagos mensuales
- Reportes y estadísticas
