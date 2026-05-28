package com.uber.controller;

import com.uber.dto.TripRequestDto;
import com.uber.dto.TripResponseDto;
import com.uber.service.TripService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de viajes.
 * <p>
 * Expone endpoints para crear viajes, consultar su estado y
 * listar viajes por conductor o pasajero. Las operaciones en tiempo real
 * (aceptar, rechazar, tracking) se manejan vía WebSocket.
 * </p>
 *
 * <h3>Endpoints disponibles:</h3>
 * <ul>
 *   <li>POST /api/trips                     — Crear solicitud de viaje</li>
 *   <li>GET  /api/trips/{id}                — Obtener viaje por ID</li>
 *   <li>GET  /api/trips/driver/{driverId}   — Viajes de un conductor</li>
 *   <li>GET  /api/trips/passenger/{passId}  — Viajes de un pasajero</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/trips")
public class TripController {

    private static final Logger log = LoggerFactory.getLogger(TripController.class);

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    /**
     * Crea una nueva solicitud de viaje.
     * <p>
     * El pasajero envía las coordenadas de origen y destino.
     * El viaje se crea con estado REQUESTED y la notificación a conductores
     * se maneja por separado mediante WebSocket.
     * </p>
     *
     * @param dto datos de la solicitud (pasajero, origen, destino)
     * @return 201 CREATED con el viaje creado o 400 si hay error de validación
     */
    @PostMapping
    public ResponseEntity<?> createTrip(@Valid @RequestBody TripRequestDto dto) {
        try {
            TripResponseDto response = tripService.createTrip(dto);
            log.info("Viaje creado exitosamente con ID: {}", response.getTripId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error al crear viaje: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtiene la información de un viaje por su ID.
     *
     * @param id identificador del viaje
     * @return 200 con el viaje o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDto> getTripById(@PathVariable Long id) {
        return tripService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todos los viajes asignados a un conductor.
     *
     * @param driverId ID del conductor
     * @return lista de viajes del conductor
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<TripResponseDto>> getTripsByDriver(
            @PathVariable Long driverId) {
        return ResponseEntity.ok(tripService.findByDriverId(driverId));
    }

    /**
     * Lista todos los viajes solicitados por un pasajero.
     *
     * @param passengerId ID del pasajero
     * @return lista de viajes del pasajero
     */
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<TripResponseDto>> getTripsByPassenger(
            @PathVariable Long passengerId) {
        return ResponseEntity.ok(tripService.findByPassengerId(passengerId));
    }
}
