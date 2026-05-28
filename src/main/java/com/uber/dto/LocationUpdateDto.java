package com.uber.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para recibir actualizaciones de ubicación del conductor vía WebSocket.
 * <p>
 * El conductor envía periódicamente su posición GPS mientras un viaje está activo.
 * </p>
 */
public class LocationUpdateDto {

    @NotNull(message = "El ID del viaje es obligatorio")
    private Long tripId;

    @NotNull(message = "El ID del conductor es obligatorio")
    private Long driverId;

    @NotNull(message = "La latitud es obligatoria")
    private Double latitude;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitude;

    // =========================================================================
    // Constructores
    // =========================================================================

    public LocationUpdateDto() {
    }

    public LocationUpdateDto(Long tripId, Long driverId, Double latitude, Double longitude) {
        this.tripId = tripId;
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
