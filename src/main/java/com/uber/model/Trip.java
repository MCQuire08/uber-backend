package com.uber.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un viaje dentro de la plataforma.
 * <p>
 * Un viaje es creado por un pasajero y puede ser aceptado por un conductor.
 * El ciclo de vida sigue: REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED.
 * </p>
 *
 * @see TripStatus
 */
@Entity
@Table(name = "trips")
public class Trip {

    /** Identificador único del viaje. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID del pasajero que solicitó el viaje. */
    @Column(nullable = false)
    private Long passengerId;

    /** ID del conductor asignado (null hasta que alguien acepte). */
    private Long driverId;

    /** Estado actual del viaje. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    // ----- Coordenadas de origen -----
    @Column(nullable = false)
    private Double originLat;

    @Column(nullable = false)
    private Double originLng;

    // ----- Coordenadas de destino -----
    @Column(nullable = false)
    private Double destinationLat;

    @Column(nullable = false)
    private Double destinationLng;

    // ----- Timestamps del ciclo de vida -----

    /** Momento en que el pasajero solicitó el viaje. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    /** Momento en que el conductor aceptó el viaje. */
    private LocalDateTime acceptedAt;

    /** Momento en que el viaje fue completado. */
    private LocalDateTime completedAt;

    // =========================================================================
    // Constructores
    // =========================================================================

    public Trip() {
    }

    /**
     * Crea un nuevo viaje con estado REQUESTED.
     *
     * @param passengerId    ID del pasajero
     * @param originLat      Latitud de origen
     * @param originLng      Longitud de origen
     * @param destinationLat Latitud de destino
     * @param destinationLng Longitud de destino
     */
    public Trip(Long passengerId, Double originLat, Double originLng,
                Double destinationLat, Double destinationLng) {
        this.passengerId = passengerId;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.status = TripStatus.REQUESTED;
    }

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = TripStatus.REQUESTED;
        }
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

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
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

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "Trip{id=" + id + ", passengerId=" + passengerId
                + ", driverId=" + driverId + ", status=" + status + "}";
    }
}
