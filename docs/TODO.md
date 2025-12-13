# TODO - Pendientes y Mejoras Futuras

## Estado Actual: MVP Completo

El backend está funcional con todos los módulos implementados.

---

## Pendientes Prioritarios

### Testing
- [ ] Tests unitarios para servicios de dominio
- [ ] Tests de integración para repositorios (@DataJpaTest)
- [ ] Tests E2E para flujos críticos (inscripción, pagos)
- [ ] Cobertura objetivo: >80%

### Seguridad
- [ ] Auditoría OWASP básica
- [ ] Rate limiting en endpoints públicos
- [ ] Validación de ownership (estudiante solo ve sus datos)

### Documentación API
- [ ] Completar anotaciones OpenAPI en todos los controllers
- [ ] Ejemplos de request/response en Swagger
- [ ] Documentar códigos de error específicos por endpoint

---

## Mejoras Funcionales

### Notificaciones
- [ ] Sistema de notificaciones (email/push)
- [ ] Notificar promoción desde cola de espera
- [ ] Recordatorio de pagos próximos a vencer
- [ ] Aviso de sesiones próximas

### Pagos
- [ ] Integración real con Stripe
- [ ] Webhooks para confirmación de pago
- [ ] Facturas/recibos PDF
- [ ] Histórico de transacciones

### Reportes
- [ ] Dashboard admin con estadísticas
- [ ] Exportación a Excel/CSV
- [ ] Reportes de asistencia por período
- [ ] Análisis de ocupación de aulas

### Calendario
- [ ] Vista calendario de sesiones
- [ ] Exportación iCal
- [ ] Sincronización con Google Calendar

---

## Mejoras Técnicas

### Performance
- [ ] Caché para consultas frecuentes (Redis)
- [ ] Índices de base de datos optimizados
- [ ] Paginación cursor-based para grandes datasets

### Infraestructura
- [ ] CI/CD con GitHub Actions
- [ ] Docker multi-stage builds
- [ ] Kubernetes manifests
- [ ] Monitorización (Prometheus + Grafana)
- [ ] Logging centralizado (ELK)

### Código
- [ ] Migrar RefreshToken de infraestructura a módulo propio
- [ ] Eventos de dominio para desacoplar módulos
- [ ] Saga pattern para operaciones distribuidas

---

## Deuda Técnica

### Conocida
- [ ] Algunos endpoints no validan ownership del recurso
- [ ] `findByIds` en repositorios podría usar batch queries
- [ ] Falta soft delete en algunas entidades

### Refactoring
- [ ] Extraer constantes mágicas a configuración
- [ ] Unificar manejo de excepciones entre módulos
- [ ] Revisar transaccionalidad en servicios complejos

---

## Ideas Futuras

### Funcionalidades
- [ ] Sistema de calificaciones/notas
- [ ] Foros/comentarios por asignatura
- [ ] Chat profesor-estudiante
- [ ] Videollamadas integradas
- [ ] Gamificación (logros, rankings)

### Integraciones
- [ ] SSO con Google/Microsoft
- [ ] LMS externos (Moodle)
- [ ] Plataformas de videoconferencia
- [ ] Pasarelas de pago adicionales

---

## Notas

### Convenciones de Commits
```
(feat) descripción    - Nueva funcionalidad
(fix) descripción     - Corrección de bug
(docs) descripción    - Documentación
(refactor) descripción - Refactoring
(test) descripción    - Tests
```

### Prioridades
1. **P0**: Bloquea producción
2. **P1**: Importante para MVP
3. **P2**: Nice to have
4. **P3**: Futuro
