package com.uber.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que registra las actualizaciones de ubicación de un conductor
 * durante un viaje activo.
 * <p>
 * Cada entrada representa un punto GPS enviado por el conductor en tiempo real
 * a través de WebSocket. Se usa para tracking en vivo y para el historial de ruta.
 * </p>
 */
@Entity
@Table(name = "location_updates")
public class LocationUpdate {

    /** Identificador único de la actualización. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del viaje al que pertenece esta actualización. */
    @Column(nullable = false)
    private Long tripId;

    /** ID del conductor que envía la ubicación. */
    @Column(nullable = false)
    private Long driverId;

    /** Latitud GPS del conductor. */
    @Column(nullable = false)
    private Double latitude;

    /** Longitud GPS del conductor. */
    @Column(nullable = false)
    private Double longitude;

    /** Marca temporal del momento en que se registró la ubicación. */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // =========================================================================
    // Constructores
    // =========================================================================

    public LocationUpdate() {
    }

    public LocationUpdate(Long tripId, Long driverId, Double latitude, Double longitude) {
        this.tripId = tripId;
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LocationUpdate{tripId=" + tripId + ", driverId=" + driverId
                + ", lat=" + latitude + ", lng=" + longitude + "}";
    }
}
