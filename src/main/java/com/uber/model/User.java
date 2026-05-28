package com.uber.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un usuario de la plataforma (pasajero o conductor).
 * <p>
 * Almacena información del perfil, rol, estado de conexión y ubicación actual.
 * La ubicación se actualiza en tiempo real para conductores activos.
 * </p>
 *
 * @see UserRole
 */
@Entity
@Table(name = "users")
public class User {

    /** Identificador único del usuario. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo del usuario. */
    @Column(nullable = false)
    private String name;

    /** Correo electrónico (único por usuario). */
    @Column(nullable = false, unique = true)
    private String email;

    /** Rol del usuario: DRIVER o PASSENGER. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /** Indica si el usuario (conductor) está disponible para recibir viajes. */
    @Column(nullable = false)
    private Boolean isOnline = false;

    /** Latitud actual del usuario. Se actualiza en tiempo real para conductores. */
    private Double currentLatitude;

    /** Longitud actual del usuario. Se actualiza en tiempo real para conductores. */
    private Double currentLongitude;

    /** Fecha y hora de creación del registro. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // =========================================================================
    // Constructores
    // =========================================================================

    public User() {
    }

    public User(String name, String email, UserRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.isOnline = false;
    }

    // =========================================================================
    // Callback JPA: asignar fecha de creación automáticamente
    // =========================================================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', role=" + role + ", isOnline=" + isOnline + "}";
    }
}
