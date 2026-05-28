package com.uber.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para actualizar el estado online/offline de un conductor.
 */
public class DriverStatusDto {

    @NotNull(message = "El ID del conductor es obligatorio")
    private Long driverId;

    @NotNull(message = "El estado online es obligatorio")
    private Boolean isOnline;

    // =========================================================================
    // Constructores
    // =========================================================================

    public DriverStatusDto() {
    }

    public DriverStatusDto(Long driverId, Boolean isOnline) {
        this.driverId = driverId;
        this.isOnline = isOnline;
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

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }
}
