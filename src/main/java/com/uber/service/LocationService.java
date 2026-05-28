package com.uber.service;

import com.uber.dto.LocationUpdateDto;
import com.uber.model.LocationUpdate;
import com.uber.repository.LocationUpdateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para la gestión de ubicaciones GPS.
 * <p>
 * Gestiona el almacenamiento y consulta de las actualizaciones de ubicación
 * enviadas por los conductores durante viajes activos. Cada actualización
 * se persiste en PostgreSQL y se retransmite a los clientes vía WebSocket.
 * </p>
 */
@Service
@Transactional
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationUpdateRepository locationUpdateRepository;
    private final UserService userService;

    public LocationService(LocationUpdateRepository locationUpdateRepository,
                           UserService userService) {
        this.locationUpdateRepository = locationUpdateRepository;
        this.userService = userService;
    }

    // =========================================================================
    // Operaciones de escritura
    // =========================================================================

    /**
     * Guarda una nueva actualización de ubicación del conductor.
     * <p>
     * Además de persistir en la base de datos, actualiza la ubicación actual
     * del conductor en la tabla de usuarios para consultas rápidas.
     * </p>
     *
     * @param dto datos de la actualización de ubicación
     * @return la entidad LocationUpdate persistida
     */
    public LocationUpdate saveLocationUpdate(LocationUpdateDto dto) {
        // Crear y persistir la actualización
        LocationUpdate locationUpdate = new LocationUpdate(
                dto.getTripId(),
                dto.getDriverId(),
                dto.getLatitude(),
                dto.getLongitude()
        );

        LocationUpdate saved = locationUpdateRepository.save(locationUpdate);

        // Actualizar también la ubicación actual del conductor en la tabla users
        userService.updateLocation(dto.getDriverId(), dto.getLatitude(), dto.getLongitude());

        log.debug("📍 Ubicación guardada - Viaje: {}, Conductor: {}, Lat: {}, Lng: {}",
                dto.getTripId(), dto.getDriverId(), dto.getLatitude(), dto.getLongitude());

        return saved;
    }

    // =========================================================================
    // Operaciones de consulta
    // =========================================================================

    /**
     * Obtiene la ubicación más reciente de un viaje.
     *
     * @param tripId ID del viaje
     * @return Optional con la última ubicación registrada
     */
    @Transactional(readOnly = true)
    public Optional<LocationUpdate> getLatestLocation(Long tripId) {
        return locationUpdateRepository.findFirstByTripIdOrderByTimestampDesc(tripId);
    }

    /**
     * Obtiene todo el historial de ubicaciones de un viaje.
     * Útil para reconstruir la ruta seguida por el conductor.
     *
     * @param tripId ID del viaje
     * @return lista de ubicaciones ordenadas por timestamp descendente
     */
    @Transactional(readOnly = true)
    public List<LocationUpdate> getLocationHistory(Long tripId) {
        return locationUpdateRepository.findByTripIdOrderByTimestampDesc(tripId);
    }
}
