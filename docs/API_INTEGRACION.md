# API Integración - Guía para Frontend

## Configuración Base

### URL Base
```
Desarrollo: http://localhost:8080/api
Producción: https://api.acainfo.com/api
```

### Cliente HTTP (Axios)

```typescript
// shared/services/apiClient.ts
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptor: Añadir token a requests
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Interceptor: Manejar errores y refresh token
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // Si es 401 y no es retry, intentar refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        const { data } = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        })

        localStorage.setItem('accessToken', data.accessToken)
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`

        return apiClient(originalRequest)
      } catch (refreshError) {
        // Refresh falló, limpiar y redirigir a login
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)
```

---

## Autenticación

### Flujo de Login

```
1. POST /auth/login → { accessToken, refreshToken }
2. Guardar tokens en localStorage/store
3. Incluir accessToken en header de todas las requests
4. Cuando accessToken expira (401), usar refreshToken
5. Si refresh falla, redirigir a login
```

### Tipos

```typescript
// features/auth/types/auth.types.ts

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: 'Bearer'
  expiresIn: number
}

export interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  status: UserStatus
  roles: Role[]
  createdAt: string
  updatedAt: string
}

export interface Role {
  id: number
  type: RoleType
}

export type RoleType = 'ADMIN' | 'TEACHER' | 'STUDENT'
export type UserStatus = 'ACTIVE' | 'BLOCKED' | 'PENDING_ACTIVATION'
```

### Endpoints Auth

```typescript
// POST /auth/register
const register = (data: RegisterRequest): Promise<AuthResponse>

// POST /auth/login
const login = (data: LoginRequest): Promise<AuthResponse>

// POST /auth/refresh
const refresh = (refreshToken: string): Promise<AuthResponse>

// POST /auth/logout
const logout = (): Promise<void>

// POST /auth/logout/all
const logoutAll = (): Promise<void>
```

---

## Student Dashboard

### Endpoint Principal

```typescript
// GET /student/overview?upcomingSessionsLimit=5
// GET /student/{studentId}/overview (admin)

export interface StudentOverviewResponse {
  userId: number
  fullName: string
  email: string
  activeEnrollments: EnrollmentSummary[]
  waitingListCount: number
  upcomingSessions: UpcomingSessionSummary[]
  paymentStatus: PaymentSummary
}

export interface EnrollmentSummary {
  enrollmentId: number
  groupId: number
  subjectName: string
  subjectCode: string
  groupType: GroupType
  teacherName: string
  enrolledAt: string  // ISO datetime
}

export interface UpcomingSessionSummary {
  sessionId: number
  groupId: number
  subjectName: string
  subjectCode: string
  groupType: GroupType
  date: string         // ISO date (YYYY-MM-DD)
  startTime: string    // HH:mm
  endTime: string      // HH:mm
  classroom: string
  sessionStatus: SessionStatus
  hasReservation: boolean
}

export interface PaymentSummary {
  canAccessResources: boolean
  hasOverduePayments: boolean
  pendingPaymentsCount: number
  totalPendingAmount: number
  nextDueDate: string | null  // ISO date
}
```

### Uso en Hook

```typescript
// features/student/hooks/useStudentOverview.ts
import { useQuery } from '@tanstack/react-query'
import { studentApi } from '../services/studentApi'

export const useStudentOverview = (limit = 5) => {
  return useQuery({
    queryKey: ['student', 'overview', limit],
    queryFn: () => studentApi.getOverview(limit),
  })
}
```

---

## Inscripciones

### Tipos

```typescript
export interface Enrollment {
  id: number
  studentId: number
  groupId: number
  status: EnrollmentStatus
  waitingListPosition: number | null
  enrolledAt: string
  promotedAt: string | null
  withdrawnAt: string | null
  isActive: boolean
  isOnWaitingList: boolean
  isWithdrawn: boolean
  canBeWithdrawn: boolean
}

export type EnrollmentStatus = 'ACTIVE' | 'WAITING_LIST' | 'WITHDRAWN' | 'COMPLETED'

export interface EnrollRequest {
  studentId: number
  groupId: number
}

export interface ChangeGroupRequest {
  newGroupId: number
}
```

### Endpoints

```typescript
// GET /enrollments?studentId=&groupId=&status=&page=&size=
// GET /enrollments/{id}
// GET /enrollments/student/{studentId}  (activas)
// GET /enrollments/group/{groupId}
// POST /enrollments
// DELETE /enrollments/{id}  (retirarse)
// PUT /enrollments/{id}/change-group
```

---

## Sesiones

### Tipos

```typescript
export interface Session {
  id: number
  subjectId: number
  groupId: number | null
  scheduleId: number | null
  date: string           // YYYY-MM-DD
  startTime: string      // HH:mm
  endTime: string        // HH:mm
  status: SessionStatus
  type: SessionType
  mode: SessionMode
  classroom: Classroom
  postponedToDate: string | null
  durationMinutes: number
  isScheduled: boolean
  isInProgress: boolean
  isCompleted: boolean
  isCancelled: boolean
}

export type SessionStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED'
export type SessionType = 'REGULAR' | 'EXTRA' | 'SCHEDULING'
export type SessionMode = 'IN_PERSON' | 'ONLINE' | 'DUAL'
export type Classroom = 'AULA_PORTAL1' | 'AULA_PORTAL2' | 'AULA_VIRTUAL'
```

### Endpoints

```typescript
// GET /sessions?groupId=&status=&dateFrom=&dateTo=&page=&size=
// GET /sessions/{id}
// GET /sessions/group/{groupId}
// GET /sessions/subject/{subjectId}
```

---

## Reservas

### Tipos

```typescript
export interface Reservation {
  id: number
  studentId: number
  sessionId: number
  enrollmentId: number
  mode: ReservationMode
  status: ReservationStatus
  onlineRequestStatus: OnlineRequestStatus | null
  attendanceStatus: AttendanceStatus | null
  reservedAt: string
  isConfirmed: boolean
  isCancelled: boolean
  isInPerson: boolean
  isOnline: boolean
  hasOnlineRequest: boolean
  isOnlineRequestPending: boolean
}

export type ReservationMode = 'IN_PERSON' | 'ONLINE'
export type ReservationStatus = 'CONFIRMED' | 'CANCELLED'
export type OnlineRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'JUSTIFIED_ABSENCE' | 'NOT_RECORDED'

export interface CreateReservationRequest {
  studentId: number
  sessionId: number
  enrollmentId: number
  mode: ReservationMode
}
```

### Endpoints

```typescript
// GET /reservations?sessionId=&studentId=&status=&page=&size=
// GET /reservations/{id}
// GET /reservations/session/{sessionId}
// GET /reservations/student/{studentId}
// POST /reservations
// DELETE /reservations/{id}
// PUT /reservations/{id}/switch-session
// POST /reservations/{id}/online-request
// PUT /reservations/{id}/online-request/process (teacher)
```

---

## Pagos

### Tipos

```typescript
export interface Payment {
  id: number
  enrollmentId: number
  studentId: number
  type: PaymentType
  status: PaymentStatus
  amount: number
  totalHours: number
  pricePerHour: number
  billingMonth: number
  billingYear: number
  generatedAt: string   // ISO date
  dueDate: string       // ISO date
  paidAt: string | null
  description: string | null
  isOverdue: boolean
  daysOverdue: number
}

export type PaymentType = 'INITIAL' | 'MONTHLY' | 'INTENSIVE_FULL'
export type PaymentStatus = 'PENDING' | 'PAID' | 'CANCELLED'

export interface AccessStatus {
  canAccessResources: boolean
  hasOverduePayments: boolean
  isUpToDate: boolean
}
```

### Endpoints

```typescript
// GET /payments?studentId=&status=&isOverdue=&page=&size=
// GET /payments/{id}
// GET /payments/student/{studentId}
// GET /payments/student/{studentId}/pending
// GET /payments/student/{studentId}/overdue
// GET /payments/student/{studentId}/access  ← Verificar acceso
// POST /payments/{id}/pay
```

### Control de Acceso

```typescript
// Antes de permitir descarga de materiales:
const { data: access } = await paymentApi.checkAccess(studentId)

if (!access.canAccessResources) {
  // Mostrar mensaje de bloqueo por impago
  // Redirigir a página de pagos
}
```

---

## Materiales

### Tipos

```typescript
export interface Material {
  id: number
  subjectId: number
  uploadedById: number
  name: string
  description: string | null
  originalFilename: string
  fileExtension: string
  mimeType: string
  fileSize: number
  fileSizeFormatted: string
  isCodeFile: boolean
  isDocumentFile: boolean
  uploadedAt: string
}

export interface CanDownloadResponse {
  canDownload: boolean
}
```

### Endpoints

```typescript
// GET /materials?subjectId=&page=&size=
// GET /materials/{id}
// GET /materials/subject/{subjectId}
// GET /materials/{id}/can-download  ← Verificar permiso
// GET /materials/{id}/download      ← Descarga (blob)
```

### Descarga de Archivos

```typescript
// features/materials/services/materialApi.ts
export const downloadMaterial = async (id: number, filename: string) => {
  const response = await apiClient.get(`/materials/${id}/download`, {
    responseType: 'blob',
  })

  // Crear enlace de descarga
  const url = window.URL.createObjectURL(new Blob([response.data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}
```

---

## Asignaturas y Grupos

### Tipos

```typescript
export interface Subject {
  id: number
  code: string
  name: string
  description: string | null
  degree: Degree
  status: SubjectStatus
  weeklyHours: number
  pricePerHour: number
}

export type Degree = 'GRADO' | 'MASTER'
export type SubjectStatus = 'ACTIVE' | 'ARCHIVED'

export interface Group {
  id: number
  subjectId: number
  teacherId: number
  type: GroupType
  status: GroupStatus
  capacity: number | null
  currentEnrollmentCount: number
  maxCapacity: number
  availableSeats: number
  isOpen: boolean
  isFull: boolean
  canEnroll: boolean
}

export type GroupType = 'REGULAR_Q1' | 'REGULAR_Q2' | 'INTENSIVE_Q1' | 'INTENSIVE_Q2'
export type GroupStatus = 'OPEN' | 'CLOSED' | 'CANCELLED'
```

### Endpoints

```typescript
// Subjects
// GET /subjects?degree=&status=&searchTerm=&page=&size=
// GET /subjects/{id}
// GET /subjects/code/{code}

// Groups
// GET /groups?subjectId=&teacherId=&type=&status=&page=&size=
// GET /groups/{id}
```

---

## Paginación

### Request

```typescript
interface PaginationParams {
  page?: number        // 0-based, default: 0
  size?: number        // default: 20
  sortBy?: string      // campo a ordenar
  sortDirection?: 'ASC' | 'DESC'
}

// Ejemplo
GET /enrollments?page=0&size=10&sortBy=enrolledAt&sortDirection=DESC
```

### Response

```typescript
interface PageResponse<T> {
  content: T[]
  pageNumber: number
  totalPages: number
  totalElements: number
  size: number
  first: boolean
  last: boolean
}
```

### Hook de Paginación

```typescript
// shared/hooks/usePagination.ts
import { useState } from 'react'

export const usePagination = (initialSize = 20) => {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(initialSize)

  const nextPage = () => setPage((p) => p + 1)
  const prevPage = () => setPage((p) => Math.max(0, p - 1))
  const goToPage = (n: number) => setPage(n)
  const changeSize = (s: number) => {
    setSize(s)
    setPage(0)
  }

  return { page, size, nextPage, prevPage, goToPage, changeSize }
}
```

---

## Manejo de Errores

### Estructura de Error del Backend

```typescript
interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}
```

### Códigos de Error Comunes

| Código | Significado | Acción Frontend |
|--------|-------------|-----------------|
| 400 | Validación fallida | Mostrar errores de campo |
| 401 | No autenticado | Intentar refresh o redirigir a login |
| 403 | Sin permisos | Mostrar mensaje de acceso denegado |
| 404 | No encontrado | Mostrar página 404 |
| 409 | Conflicto (duplicado) | Mostrar mensaje específico |
| 500 | Error servidor | Mostrar error genérico |

### Manejo en Hooks

```typescript
import { useMutation } from '@tanstack/react-query'
import { AxiosError } from 'axios'
import { toast } from '@/shared/components/ui/Toast'

export const useEnroll = () => {
  return useMutation({
    mutationFn: enrollmentApi.enroll,
    onSuccess: () => {
      toast.success('Inscripción realizada correctamente')
    },
    onError: (error: AxiosError<ApiError>) => {
      const message = error.response?.data?.message || 'Error al inscribirse'
      toast.error(message)
    },
  })
}
```

---

## Fechas y Formatos

### Formatos del Backend

```typescript
// Fecha: ISO 8601 (YYYY-MM-DD)
"2025-01-15"

// Hora: HH:mm
"10:30"

// DateTime: ISO 8601
"2025-01-15T10:30:00"
```

### Formateo en Frontend

```typescript
// shared/utils/formatters.ts
import { format, parseISO } from 'date-fns'
import { es } from 'date-fns/locale'

export const formatDate = (dateStr: string) =>
  format(parseISO(dateStr), 'dd/MM/yyyy', { locale: es })

export const formatDateTime = (dateStr: string) =>
  format(parseISO(dateStr), "dd/MM/yyyy 'a las' HH:mm", { locale: es })

export const formatTime = (timeStr: string) => timeStr // Ya viene como HH:mm

export const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(amount)

// Ejemplos
formatDate('2025-01-15')           // "15/01/2025"
formatDateTime('2025-01-15T10:30') // "15/01/2025 a las 10:30"
formatCurrency(120.50)             // "120,50 €"
```

---

## Variables de Entorno

### .env

```bash
VITE_API_URL=http://localhost:8080/api
VITE_APP_NAME=AcaInfo
```

### Acceso en Código

```typescript
// shared/config/env.ts
export const config = {
  apiUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  appName: import.meta.env.VITE_APP_NAME || 'AcaInfo',
  isDev: import.meta.env.DEV,
  isProd: import.meta.env.PROD,
}
```

---

## Query Keys (TanStack Query)

Convención para cache keys:

```typescript
// features/*/hooks/*.ts

// Listas
['enrollments', 'list', { studentId, status }]
['sessions', 'list', { groupId, dateFrom }]
['payments', 'list', { studentId, status }]

// Detalles
['enrollment', id]
['session', id]
['payment', id]

// Especiales
['student', 'overview', limit]
['payment', 'access', studentId]
['material', 'canDownload', id]
```

### Invalidación

```typescript
// Al inscribirse, invalidar overview y lista de inscripciones
queryClient.invalidateQueries({ queryKey: ['enrollments'] })
queryClient.invalidateQueries({ queryKey: ['student', 'overview'] })

// Al pagar, invalidar pagos y acceso
queryClient.invalidateQueries({ queryKey: ['payments'] })
queryClient.invalidateQueries({ queryKey: ['payment', 'access'] })
```
