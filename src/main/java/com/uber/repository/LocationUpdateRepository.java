package com.uber.repository;

import com.uber.model.LocationUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link LocationUpdate}.
 * <p>
 * Gestiona el almacenamiento y consulta de actualizaciones GPS
 * enviadas por los conductores durante viajes activos.
 * </p>
 */
@Repository
public interface LocationUpdateRepository extends JpaRepository<LocationUpdate, Long> {

    /**
     * Obtiene todas las actualizaciones de ubicación de un viaje,
     * ordenadas por timestamp descendente (más reciente primero).
     *
     * @param tripId ID del viaje
     * @return lista de ubicaciones ordenadas
     */
    List<LocationUpdate> findByTripIdOrderByTimestampDesc(Long tripId);

    /**
     * Obtiene la última ubicación registrada para un viaje específico.
     *
     * @param tripId ID del viaje
     * @return Optional con la ubicación más reciente
     */
    Optional<LocationUpdate> findFirstByTripIdOrderByTimestampDesc(Long tripId);

    /**
     * Obtiene todas las actualizaciones de un conductor.
     *
     * @param driverId ID del conductor
     * @return lista de actualizaciones
     */
    List<LocationUpdate> findByDriverId(Long driverId);
}
