package com.uber.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitar un nuevo viaje.
 * <p>
 * El pasajero envía este objeto con su ID y las coordenadas de origen y destino.
 * Se valida que todos los campos obligatorios estén presentes.
 * </p>
 */
public class TripRequestDto {

    @NotNull(message = "El ID del pasajero es obligatorio")
    private Long passengerId;

    @NotNull(message = "La latitud de origen es obligatoria")
    private Double originLat;

    @NotNull(message = "La longitud de origen es obligatoria")
    private Double originLng;

    @NotNull(message = "La latitud de destino es obligatoria")
    private Double destinationLat;

    @NotNull(message = "La longitud de destino es obligatoria")
    private Double destinationLng;

    // =========================================================================
    // Constructores
    // =========================================================================

    public TripRequestDto() {
    }

    public TripRequestDto(Long passengerId, Double originLat, Double originLng,
                          Double destinationLat, Double destinationLng) {
        this.passengerId = passengerId;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public Double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(Double originLat) {
        this.originLat = originLat;
    }

    public Double getOriginLng() {
        return originLng;
    }

    public void setOriginLng(Double originLng) {
        this.originLng = originLng;
    }

    public Double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(Double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public Double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(Double destinationLng) {
        this.destinationLng = destinationLng;
    }
}
