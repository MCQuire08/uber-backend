package com.uber.model;

/**
 * Enum que representa los posibles estados de un viaje.
 * <p>
 * Flujo normal: REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED
 * Flujo cancelado: REQUESTED → CANCELLED (rechazado por conductor)
 * </p>
 */
public enum TripStatus {
    /** Viaje solicitado por el pasajero, pendiente de conductor. */
    REQUESTED,

    /** Viaje aceptado por un conductor. */
    ACCEPTED,

    /** Viaje en curso, el conductor se dirige al destino. */
    IN_PROGRESS,

    /** Viaje completado exitosamente. */
    COMPLETED,

    /** Viaje cancelado o rechazado. */
    CANCELLED
}
