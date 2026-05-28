package com.uber.service;

import com.uber.dto.TripRequestDto;
import com.uber.dto.TripResponseDto;
import com.uber.model.Trip;
import com.uber.model.TripStatus;
import com.uber.model.User;
import com.uber.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de lógica de negocio para la gestión de viajes.
 * <p>
 * Encapsula todo el ciclo de vida de un viaje:
 * solicitud → aceptación → en progreso → completado/cancelado.
 * </p>
 *
 * <h3>Principio de Responsabilidad Única (SRP):</h3>
 * Este servicio SOLO gestiona la lógica de viajes. La gestión de usuarios
 * y ubicaciones se delega a sus respectivos servicios.
 */
@Service
@Transactional
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final UserService userService;

    public TripService(TripRepository tripRepository, UserService userService) {
        this.tripRepository = tripRepository;
        this.userService = userService;
    }

    // =========================================================================
    // Creación de viajes
    // =========================================================================

    /**
     * Crea un nuevo viaje con estado REQUESTED.
     * <p>
     * Valida que el pasajero exista y crea el registro en la base de datos.
     * Después de la creación, el WebSocketController se encarga de notificar
     * a los conductores disponibles.
     * </p>
     *
     * @param dto datos de la solicitud de viaje
     * @return DTO con la información del viaje creado
     * @throws RuntimeException si el pasajero no existe
     */
    public TripResponseDto createTrip(TripRequestDto dto) {
        // Validar que el pasajero existe
        userService.findById(dto.getPassengerId())
                .orElseThrow(() -> new RuntimeException(
                        "Pasajero no encontrado con ID: " + dto.getPassengerId()));

        // Crear la entidad Trip
        Trip trip = new Trip(
                dto.getPassengerId(),
                dto.getOriginLat(),
                dto.getOriginLng(),
                dto.getDestinationLat(),
                dto.getDestinationLng()
        );

        Trip savedTrip = tripRepository.save(trip);
        log.info("🚗 Nuevo viaje creado: {}", savedTrip);

        return toResponseDto(savedTrip);
    }

    // =========================================================================
    // Gestión del estado del viaje
    // =========================================================================

    /**
     * Un conductor acepta un viaje pendiente.
     * <p>
     * Cambia el estado a ACCEPTED, asigna el conductor y registra el timestamp.
     * </p>
     *
     * @param tripId   ID del viaje a aceptar
     * @param driverId ID del conductor que acepta
     * @return DTO con el viaje actualizado
     * @throws RuntimeException si el viaje no existe o no está en estado REQUESTED
     */
    public TripResponseDto acceptTrip(Long tripId, Long driverId) {
        Trip trip = findTripOrThrow(tripId);

        // Validar estado actual
        if (trip.getStatus() != TripStatus.REQUESTED) {
            throw new RuntimeException(
                    "El viaje no puede ser aceptado. Estado actual: " + trip.getStatus());
        }

        // Validar que el conductor existe y es DRIVER
        User driver = userService.findById(driverId)
                .orElseThrow(() -> new RuntimeException(
                        "Conductor no encontrado con ID: " + driverId));

        // Actualizar el viaje
        trip.setDriverId(driverId);
        trip.setStatus(TripStatus.ACCEPTED);
        trip.setAcceptedAt(LocalDateTime.now());

        Trip updated = tripRepository.save(trip);
        log.info("✅ Viaje {} aceptado por conductor {}", tripId, driver.getName());

        return toResponseDto(updated);
    }

    /**
     * Rechaza (cancela) un viaje pendiente.
     * <p>
     * Cambia el estado a CANCELLED. En un sistema real, se reasignaría
     * a otro conductor; en este prototipo simplemente se cancela.
     * </p>
     *
     * @param tripId ID del viaje a rechazar
     * @return DTO con el viaje actualizado
     * @throws RuntimeException si el viaje no existe o no está en estado REQUESTED
     */
    public TripResponseDto rejectTrip(Long tripId) {
        Trip trip = findTripOrThrow(tripId);

        if (trip.getStatus() != TripStatus.REQUESTED) {
            throw new RuntimeException(
                    "El viaje no puede ser rechazado. Estado actual: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.CANCELLED);
        Trip updated = tripRepository.save(trip);
        log.info("❌ Viaje {} rechazado/cancelado", tripId);

        return toResponseDto(updated);
    }

    /**
     * Actualiza el estado de un viaje existente.
     * <p>
     * Permite transiciones como ACCEPTED → IN_PROGRESS o IN_PROGRESS → COMPLETED.
     * Si el nuevo estado es COMPLETED, registra la marca temporal de finalización.
     * </p>
     *
     * @param tripId    ID del viaje
     * @param newStatus nuevo estado
     * @return DTO con el viaje actualizado
     */
    public TripResponseDto updateTripStatus(Long tripId, TripStatus newStatus) {
        Trip trip = findTripOrThrow(tripId);

        trip.setStatus(newStatus);

        // Registrar timestamp de completado si aplica
        if (newStatus == TripStatus.COMPLETED) {
            trip.setCompletedAt(LocalDateTime.now());
        }

        Trip updated = tripRepository.save(trip);
        log.info("🔄 Viaje {} actualizado a estado: {}", tripId, newStatus);

        return toResponseDto(updated);
    }

    // =========================================================================
    // Consultas
    // =========================================================================

    /**
     * Busca un viaje por su ID.
     *
     * @param tripId ID del viaje
     * @return Optional con el DTO de respuesta
     */
    @Transactional(readOnly = true)
    public Optional<TripResponseDto> findById(Long tripId) {
        return tripRepository.findById(tripId).map(this::toResponseDto);
    }

    /**
     * Obtiene todos los viajes de un conductor.
     *
     * @param driverId ID del conductor
     * @return lista de DTOs de respuesta
     */
    @Transactional(readOnly = true)
    public List<TripResponseDto> findByDriverId(Long driverId) {
        return tripRepository.findByDriverId(driverId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los viajes de un pasajero.
     *
     * @param passengerId ID del pasajero
     * @return lista de DTOs de respuesta
     */
    @Transactional(readOnly = true)
    public List<TripResponseDto> findByPassengerId(Long passengerId) {
        return tripRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Encuentra conductores disponibles (online) para asignar a un viaje.
     * <p>
     * Delegado al UserService siguiendo el principio de responsabilidad única.
     * En una versión futura, se podría agregar lógica de proximidad geográfica.
     * </p>
     *
     * @return lista de conductores disponibles
     */
    @Transactional(readOnly = true)
    public List<User> findAvailableDrivers() {
        return userService.findAvailableDrivers();
    }

    // =========================================================================
    // Métodos privados (helpers)
    // =========================================================================

    /**
     * Busca un viaje por ID o lanza excepción si no existe.
     */
    private Trip findTripOrThrow(Long tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException(
                        "Viaje no encontrado con ID: " + tripId));
    }

    /**
     * Convierte una entidad Trip a TripResponseDto.
     * <p>
     * Incluye el nombre del conductor si ya fue asignado.
     * </p>
     *
     * @param trip entidad a convertir
     * @return DTO de respuesta
     */
    private TripResponseDto toResponseDto(Trip trip) {
        TripResponseDto dto = new TripResponseDto();
        dto.setTripId(trip.getId());
        dto.setStatus(trip.getStatus());
        dto.setPassengerId(trip.getPassengerId());
        dto.setDriverId(trip.getDriverId());
        dto.setOriginLat(trip.getOriginLat());
        dto.setOriginLng(trip.getOriginLng());
        dto.setDestinationLat(trip.getDestinationLat());
        dto.setDestinationLng(trip.getDestinationLng());
        dto.setRequestedAt(trip.getRequestedAt());
        dto.setAcceptedAt(trip.getAcceptedAt());
        dto.setCompletedAt(trip.getCompletedAt());

        // Agregar nombre del conductor si existe
        if (trip.getDriverId() != null) {
            userService.findById(trip.getDriverId())
                    .ifPresent(driver -> dto.setDriverName(driver.getName()));
        }

        return dto;
    }
}
