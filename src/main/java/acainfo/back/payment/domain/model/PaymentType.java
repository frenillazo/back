package acainfo.back.payment.domain.model;

import java.math.BigDecimal;

/**
 * Tipo de pago según el programa académico del estudiante.
 * Define los diferentes conceptos y montos de pago.
 */
public enum PaymentType {
    /**
     * Cuota mensual para grupos regulares.
     * Pago recurrente mensual.
     */
    MENSUAL(new BigDecimal("100.00")),

    /**
     * Cuota para curso intensivo.
     * Pago único o por sesiones intensivas (verano, etc.).
     */
    INTENSIVO(new BigDecimal("200.00")),

    /**
     * Pago por sesión individual o recuperación.
     */
    SESION_UNICA(new BigDecimal("25.00")),

    /**
     * Matrícula inicial o inscripción.
     */
    MATRICULA(new BigDecimal("150.00")),

    /**
     * Otros conceptos especiales.
     */
    OTRO(BigDecimal.ZERO);

    private final BigDecimal defaultAmount;

    PaymentType(BigDecimal defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    /**
     * Obtiene el monto por defecto para este tipo de pago.
     */
    public BigDecimal getDefaultAmount() {
        return defaultAmount;
    }

    /**
     * Verifica si es un pago recurrente.
     */
    public boolean isRecurring() {
        return this == MENSUAL;
    }

    /**
     * Verifica si es un pago único.
     */
    public boolean isOneTime() {
        return this == INTENSIVO || this == SESION_UNICA || this == MATRICULA || this == OTRO;
    }
}
