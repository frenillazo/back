package acainfo.back.payment.domain.model;

/**
 * Estado de un pago en el sistema.
 * Controla el ciclo de vida de los pagos desde su creación hasta su resolución.
 */
public enum PaymentStatus {
    /**
     * Pago pendiente de realizarse.
     * Estado inicial al crear un nuevo pago.
     */
    PENDIENTE,

    /**
     * Pago completado exitosamente.
     * Se registra la fecha de pago y el ID de transacción de Stripe.
     */
    PAGADO,

    /**
     * Pago vencido (más de 5 días de retraso).
     * Bloquea el acceso a materiales y nuevas inscripciones.
     */
    ATRASADO,

    /**
     * Pago cancelado.
     * Puede ser por retiro del estudiante o decisión administrativa.
     */
    CANCELADO,

    /**
     * Pago reembolsado.
     * Se ha devuelto el monto al estudiante (retiros, errores, etc.).
     */
    REEMBOLSADO;

    /**
     * Verifica si el estado es un estado activo que requiere acción.
     */
    public boolean isActive() {
        return this == PENDIENTE || this == ATRASADO;
    }

    /**
     * Verifica si el pago está resuelto (ya no requiere acción).
     */
    public boolean isResolved() {
        return this == PAGADO || this == CANCELADO || this == REEMBOLSADO;
    }

    /**
     * Verifica si el estado bloquea el acceso del estudiante.
     */
    public boolean blocksAccess() {
        return this == ATRASADO;
    }
}
