package com.uber.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para aceptar un viaje.
 * Recibe el ID del conductor enviado desde la aplicación móvil.
 */
public class TripAcceptDto {

    @NotNull(message = "El ID del conductor es obligatorio")
    private Long driverId;

    // =========================================================================
    // Constructores
    // =========================================================================

    public TripAcceptDto() {
    }

    public TripAcceptDto(Long driverId) {
        this.driverId = driverId;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }
}
