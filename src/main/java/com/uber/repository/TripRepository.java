package com.uber.repository;

import com.uber.model.Trip;
import com.uber.model.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link Trip}.
 * <p>
 * Provee consultas personalizadas para filtrar viajes por estado,
 * conductor y pasajero. Spring Data genera las implementaciones
 * automáticamente a partir de los nombres de los métodos.
 * </p>
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Encuentra viajes por su estado actual.
     *
     * @param status estado del viaje (REQUESTED, ACCEPTED, etc.)
     * @return lista de viajes con ese estado
     */
    List<Trip> findByStatus(TripStatus status);

    /**
     * Encuentra todos los viajes asignados a un conductor.
     *
     * @param driverId ID del conductor
     * @return lista de viajes del conductor
     */
    List<Trip> findByDriverId(Long driverId);

    /**
     * Encuentra todos los viajes solicitados por un pasajero.
     *
     * @param passengerId ID del pasajero
     * @return lista de viajes del pasajero
     */
    List<Trip> findByPassengerId(Long passengerId);

    /**
     * Encuentra viajes por conductor y estado.
     * Útil para verificar si un conductor ya tiene un viaje activo.
     *
     * @param driverId ID del conductor
     * @param status   estado del viaje
     * @return lista de viajes que coinciden
     */
    List<Trip> findByDriverIdAndStatus(Long driverId, TripStatus status);
}
