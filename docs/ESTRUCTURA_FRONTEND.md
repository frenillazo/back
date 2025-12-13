# Estructura Frontend - Feature-Based Architecture

## Visión General

Arquitectura modular organizada por funcionalidades (features), alineada con los módulos del backend.

```
src/
├── app/                    # Configuración de la aplicación
├── features/               # Módulos de negocio
├── shared/                 # Código compartido
├── assets/                 # Recursos estáticos
└── main.tsx               # Punto de entrada
```

---

## Estructura Completa

```
src/
│
├── main.tsx                        # Punto de entrada React
│
├── app/
│   ├── App.tsx                     # Componente raíz
│   ├── router.tsx                  # Configuración React Router
│   └── providers/
│       ├── index.tsx               # Composición de providers
│       ├── AuthProvider.tsx        # Contexto de autenticación
│       ├── QueryProvider.tsx       # TanStack Query provider
│       └── ThemeProvider.tsx       # Tema (dark/light)
│
├── features/
│   │
│   ├── auth/                       # Autenticación
│   │   ├── components/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   ├── ForgotPasswordForm.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   ├── hooks/
│   │   │   ├── useAuth.ts          # Estado auth + métodos
│   │   │   └── useAuthGuard.ts     # Redirección si no auth
│   │   ├── services/
│   │   │   └── authApi.ts          # login, register, refresh, logout
│   │   ├── store/
│   │   │   └── authStore.ts        # Zustand store para auth
│   │   ├── types/
│   │   │   └── auth.types.ts
│   │   ├── pages/
│   │   │   ├── LoginPage.tsx
│   │   │   └── RegisterPage.tsx
│   │   └── index.ts                # Re-exports públicos
│   │
│   ├── student/                    # Dashboard estudiante
│   │   ├── components/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── OverviewCard.tsx
│   │   │   ├── EnrollmentList.tsx
│   │   │   ├── EnrollmentCard.tsx
│   │   │   ├── UpcomingSessionList.tsx
│   │   │   ├── UpcomingSessionCard.tsx
│   │   │   ├── PaymentSummary.tsx
│   │   │   └── WaitingListBadge.tsx
│   │   ├── hooks/
│   │   │   └── useStudentOverview.ts
│   │   ├── services/
│   │   │   └── studentApi.ts
│   │   ├── types/
│   │   │   └── student.types.ts
│   │   ├── pages/
│   │   │   └── StudentDashboardPage.tsx
│   │   └── index.ts
│   │
│   ├── enrollments/                # Inscripciones
│   │   ├── components/
│   │   │   ├── EnrollmentForm.tsx
│   │   │   ├── EnrollmentDetail.tsx
│   │   │   ├── GroupSelector.tsx
│   │   │   └── WaitingQueuePosition.tsx
│   │   ├── hooks/
│   │   │   ├── useEnrollments.ts
│   │   │   ├── useEnrollment.ts
│   │   │   └── useWaitingQueue.ts
│   │   ├── services/
│   │   │   └── enrollmentApi.ts
│   │   ├── types/
│   │   │   └── enrollment.types.ts
│   │   ├── pages/
│   │   │   ├── EnrollmentsPage.tsx
│   │   │   └── EnrollmentDetailPage.tsx
│   │   └── index.ts
│   │
│   ├── sessions/                   # Sesiones y reservas
│   │   ├── components/
│   │   │   ├── SessionCard.tsx
│   │   │   ├── SessionDetail.tsx
│   │   │   ├── SessionCalendar.tsx
│   │   │   ├── ReservationButton.tsx
│   │   │   ├── OnlineRequestButton.tsx
│   │   │   └── AttendanceStatus.tsx
│   │   ├── hooks/
│   │   │   ├── useSessions.ts
│   │   │   ├── useReservations.ts
│   │   │   └── useOnlineRequest.ts
│   │   ├── services/
│   │   │   ├── sessionApi.ts
│   │   │   └── reservationApi.ts
│   │   ├── types/
│   │   │   ├── session.types.ts
│   │   │   └── reservation.types.ts
│   │   ├── pages/
│   │   │   ├── SessionsPage.tsx
│   │   │   └── SessionDetailPage.tsx
│   │   └── index.ts
│   │
│   ├── payments/                   # Pagos
│   │   ├── components/
│   │   │   ├── PaymentList.tsx
│   │   │   ├── PaymentCard.tsx
│   │   │   ├── PaymentDetail.tsx
│   │   │   ├── PendingPaymentAlert.tsx
│   │   │   └── OverdueWarning.tsx
│   │   ├── hooks/
│   │   │   ├── usePayments.ts
│   │   │   └── usePaymentAccess.ts
│   │   ├── services/
│   │   │   └── paymentApi.ts
│   │   ├── types/
│   │   │   └── payment.types.ts
│   │   ├── pages/
│   │   │   └── PaymentsPage.tsx
│   │   └── index.ts
│   │
│   ├── materials/                  # Materiales
│   │   ├── components/
│   │   │   ├── MaterialList.tsx
│   │   │   ├── MaterialCard.tsx
│   │   │   ├── MaterialDownloadButton.tsx
│   │   │   └── AccessDeniedMessage.tsx
│   │   ├── hooks/
│   │   │   ├── useMaterials.ts
│   │   │   └── useDownload.ts
│   │   ├── services/
│   │   │   └── materialApi.ts
│   │   ├── types/
│   │   │   └── material.types.ts
│   │   ├── pages/
│   │   │   └── MaterialsPage.tsx
│   │   └── index.ts
│   │
│   ├── subjects/                   # Catálogo de asignaturas (público)
│   │   ├── components/
│   │   │   ├── SubjectList.tsx
│   │   │   ├── SubjectCard.tsx
│   │   │   └── SubjectDetail.tsx
│   │   ├── hooks/
│   │   │   └── useSubjects.ts
│   │   ├── services/
│   │   │   └── subjectApi.ts
│   │   ├── types/
│   │   │   └── subject.types.ts
│   │   ├── pages/
│   │   │   ├── SubjectsPage.tsx
│   │   │   └── SubjectDetailPage.tsx
│   │   └── index.ts
│   │
│   ├── profile/                    # Perfil de usuario
│   │   ├── components/
│   │   │   ├── ProfileForm.tsx
│   │   │   └── ChangePasswordForm.tsx
│   │   ├── hooks/
│   │   │   └── useProfile.ts
│   │   ├── services/
│   │   │   └── profileApi.ts
│   │   ├── pages/
│   │   │   └── ProfilePage.tsx
│   │   └── index.ts
│   │
│   └── admin/                      # Panel de administración
│       ├── components/
│       │   └── AdminLayout.tsx
│       ├── pages/
│       │   └── AdminDashboardPage.tsx
│       │
│       ├── users/                  # Gestión de usuarios
│       │   ├── components/
│       │   │   ├── UserTable.tsx
│       │   │   ├── UserForm.tsx
│       │   │   └── TeacherForm.tsx
│       │   ├── hooks/
│       │   │   └── useUsers.ts
│       │   ├── services/
│       │   │   └── userApi.ts
│       │   └── pages/
│       │       └── UsersPage.tsx
│       │
│       ├── subjects/               # Gestión de asignaturas
│       │   ├── components/
│       │   │   ├── SubjectTable.tsx
│       │   │   └── SubjectForm.tsx
│       │   ├── hooks/
│       │   │   └── useSubjectAdmin.ts
│       │   ├── services/
│       │   │   └── subjectAdminApi.ts
│       │   └── pages/
│       │       ├── SubjectsAdminPage.tsx
│       │       └── SubjectEditPage.tsx
│       │
│       ├── groups/                 # Gestión de grupos
│       │   ├── components/
│       │   │   ├── GroupTable.tsx
│       │   │   └── GroupForm.tsx
│       │   ├── hooks/
│       │   │   └── useGroupAdmin.ts
│       │   ├── services/
│       │   │   └── groupAdminApi.ts
│       │   └── pages/
│       │       └── GroupsAdminPage.tsx
│       │
│       ├── schedules/              # Gestión de horarios
│       │   ├── components/
│       │   │   ├── ScheduleTable.tsx
│       │   │   └── ScheduleForm.tsx
│       │   ├── hooks/
│       │   │   └── useScheduleAdmin.ts
│       │   └── pages/
│       │       └── SchedulesAdminPage.tsx
│       │
│       ├── sessions/               # Gestión de sesiones
│       │   ├── components/
│       │   │   ├── SessionTable.tsx
│       │   │   ├── SessionGenerator.tsx
│       │   │   └── AttendanceManager.tsx
│       │   ├── hooks/
│       │   │   └── useSessionAdmin.ts
│       │   └── pages/
│       │       └── SessionsAdminPage.tsx
│       │
│       └── payments/               # Gestión de pagos
│           ├── components/
│           │   ├── PaymentTable.tsx
│           │   ├── PaymentGenerator.tsx
│           │   └── OverdueList.tsx
│           ├── hooks/
│           │   └── usePaymentAdmin.ts
│           └── pages/
│               └── PaymentsAdminPage.tsx
│
├── shared/
│   │
│   ├── components/
│   │   ├── ui/                     # Componentes base (shadcn/ui)
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Alert.tsx
│   │   │   ├── Spinner.tsx
│   │   │   ├── Skeleton.tsx
│   │   │   └── Toast.tsx
│   │   │
│   │   ├── layout/
│   │   │   ├── MainLayout.tsx      # Layout principal
│   │   │   ├── Header.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Footer.tsx
│   │   │   └── PageContainer.tsx
│   │   │
│   │   └── common/
│   │       ├── ErrorBoundary.tsx
│   │       ├── LoadingScreen.tsx
│   │       ├── EmptyState.tsx
│   │       ├── Pagination.tsx
│   │       ├── SearchInput.tsx
│   │       ├── ConfirmDialog.tsx
│   │       └── StatusBadge.tsx
│   │
│   ├── hooks/
│   │   ├── useApi.ts               # Wrapper para llamadas API
│   │   ├── usePagination.ts
│   │   ├── useDebounce.ts
│   │   ├── useLocalStorage.ts
│   │   └── useMediaQuery.ts
│   │
│   ├── services/
│   │   └── apiClient.ts            # Axios configurado
│   │
│   ├── types/
│   │   ├── api.types.ts            # Tipos comunes de API
│   │   └── common.types.ts
│   │
│   ├── utils/
│   │   ├── formatters.ts           # Fechas, moneda, etc.
│   │   ├── validators.ts
│   │   ├── constants.ts
│   │   └── helpers.ts
│   │
│   └── config/
│       └── env.ts                  # Variables de entorno
│
└── assets/
    ├── images/
    └── styles/
        └── globals.css
```

---

## Convenciones de Nombrado

### Archivos

| Tipo | Convención | Ejemplo |
|------|------------|---------|
| Componentes | PascalCase.tsx | `LoginForm.tsx` |
| Hooks | camelCase con "use" | `useAuth.ts` |
| Services | camelCase con sufijo | `authApi.ts` |
| Types | kebab-case.types.ts | `auth.types.ts` |
| Stores | camelCase con sufijo | `authStore.ts` |
| Pages | PascalCase con sufijo | `LoginPage.tsx` |
| Utils | camelCase | `formatters.ts` |

### Código

```typescript
// Componentes: PascalCase
export const LoginForm = () => { ... }

// Hooks: camelCase con "use"
export const useAuth = () => { ... }

// Funciones: camelCase
export const formatDate = () => { ... }

// Constantes: SCREAMING_SNAKE_CASE
export const API_BASE_URL = '...'

// Types/Interfaces: PascalCase
export interface User { ... }
export type AuthState = { ... }

// Enums: PascalCase
export enum UserStatus { ... }
```

---

## Exports e Imports

### Archivo index.ts por Feature

Cada feature exporta solo lo que es público:

```typescript
// features/auth/index.ts
export { LoginForm } from './components/LoginForm'
export { RegisterForm } from './components/RegisterForm'
export { ProtectedRoute } from './components/ProtectedRoute'
export { useAuth } from './hooks/useAuth'
export { LoginPage } from './pages/LoginPage'
export { RegisterPage } from './pages/RegisterPage'
export type { LoginRequest, AuthResponse } from './types/auth.types'
```

### Imports

```typescript
// ✅ Correcto - desde el índice del feature
import { LoginForm, useAuth } from '@/features/auth'

// ✅ Correcto - shared components
import { Button, Input } from '@/shared/components/ui'
import { MainLayout } from '@/shared/components/layout'

// ❌ Evitar - imports directos a archivos internos
import { LoginForm } from '@/features/auth/components/LoginForm'
```

### Configurar Alias en vite.config.ts

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
})
```

### tsconfig.json paths

```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

---

## Patrones por Capa

### Services (API)

```typescript
// features/auth/services/authApi.ts
import { apiClient } from '@/shared/services/apiClient'
import type { LoginRequest, LoginResponse } from '../types/auth.types'

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<LoginResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    apiClient.post<AuthResponse>('/auth/register', data),

  refresh: (refreshToken: string) =>
    apiClient.post<LoginResponse>('/auth/refresh', { refreshToken }),

  logout: () =>
    apiClient.post('/auth/logout'),
}
```

### Hooks (TanStack Query)

```typescript
// features/auth/hooks/useAuth.ts
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { authApi } from '../services/authApi'
import { useAuthStore } from '../store/authStore'

export const useAuth = () => {
  const queryClient = useQueryClient()
  const { setAuth, clearAuth } = useAuthStore()

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      setAuth(data)
      queryClient.invalidateQueries({ queryKey: ['user'] })
    },
  })

  const logout = () => {
    authApi.logout()
    clearAuth()
    queryClient.clear()
  }

  return {
    login: loginMutation.mutate,
    isLoading: loginMutation.isPending,
    error: loginMutation.error,
    logout,
  }
}
```

### Store (Zustand)

```typescript
// features/auth/store/authStore.ts
import { create } from 'zustand'
import { persist } from 'zustand/middleware'

interface AuthState {
  accessToken: string | null
  user: User | null
  setAuth: (data: LoginResponse) => void
  clearAuth: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      setAuth: (data) => set({
        accessToken: data.accessToken,
        user: data.user
      }),
      clearAuth: () => set({ accessToken: null, user: null }),
      isAuthenticated: () => !!get().accessToken,
    }),
    { name: 'auth-storage' }
  )
)
```

### Components

```typescript
// features/student/components/EnrollmentCard.tsx
import { Card, Badge } from '@/shared/components/ui'
import type { EnrollmentSummary } from '../types/student.types'

interface EnrollmentCardProps {
  enrollment: EnrollmentSummary
  onClick?: () => void
}

export const EnrollmentCard = ({ enrollment, onClick }: EnrollmentCardProps) => {
  return (
    <Card onClick={onClick} className="cursor-pointer hover:shadow-md">
      <h3>{enrollment.subjectName}</h3>
      <p>{enrollment.groupType}</p>
      <Badge>{enrollment.teacherName}</Badge>
    </Card>
  )
}
```

---

## Rutas Sugeridas

```typescript
// app/router.tsx
import { createBrowserRouter } from 'react-router-dom'

export const router = createBrowserRouter([
  // Públicas
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  { path: '/subjects', element: <SubjectsPage /> },

  // Protegidas (estudiante)
  {
    path: '/',
    element: <ProtectedRoute><MainLayout /></ProtectedRoute>,
    children: [
      { index: true, element: <StudentDashboardPage /> },
      { path: 'enrollments', element: <EnrollmentsPage /> },
      { path: 'enrollments/:id', element: <EnrollmentDetailPage /> },
      { path: 'sessions', element: <SessionsPage /> },
      { path: 'sessions/:id', element: <SessionDetailPage /> },
      { path: 'payments', element: <PaymentsPage /> },
      { path: 'materials', element: <MaterialsPage /> },
      { path: 'profile', element: <ProfilePage /> },
    ],
  },

  // Admin
  {
    path: '/admin',
    element: <ProtectedRoute roles={['ADMIN']}><AdminLayout /></ProtectedRoute>,
    children: [
      { index: true, element: <AdminDashboardPage /> },
      { path: 'users', element: <UsersPage /> },
      { path: 'subjects', element: <SubjectsAdminPage /> },
      { path: 'groups', element: <GroupsAdminPage /> },
      { path: 'schedules', element: <SchedulesAdminPage /> },
      { path: 'sessions', element: <SessionsAdminPage /> },
      { path: 'payments', element: <PaymentsAdminPage /> },
    ],
  },
])
```

---

## Dependencias Recomendadas

```bash
# Routing
npm install react-router-dom

# State & Data Fetching
npm install @tanstack/react-query zustand axios

# UI
npm install tailwindcss postcss autoprefixer
npm install @radix-ui/react-* # (componentes individuales)
# O usar shadcn/ui: npx shadcn-ui@latest init

# Forms
npm install react-hook-form zod @hookform/resolvers

# Utils
npm install date-fns clsx tailwind-merge

# Dev
npm install -D @types/node
```
