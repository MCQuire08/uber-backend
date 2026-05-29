package com.uber.controller;

import com.uber.dto.LocationUpdateDto;
import com.uber.dto.TripRequestDto;
import com.uber.dto.TripResponseDto;
import com.uber.dto.TripAcceptDto;
import com.uber.model.LocationUpdate;
import com.uber.model.User;
import com.uber.model.TripStatus;
import com.uber.service.LocationService;
import com.uber.service.TripService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controlador WebSocket para la comunicación en tiempo real.
 * <p>
 * Maneja los mensajes STOMP entrantes y reenvía las actualizaciones
 * a los clientes suscritos a través del broker de mensajes.
 * </p>
 *
 * <h3>Flujo de mensajes:</h3>
 * <pre>
 * 1. Solicitar viaje:
 *    IN:  /app/trip.request           → Pasajero solicita viaje
 *    OUT: /topic/driver/notifications → Notifica a conductores disponibles
 *    OUT: /topic/trips/{tripId}       → Confirma creación al pasajero
 *
 * 2. Aceptar viaje:
 *    IN:  /app/trip.accept/{tripId}   → Conductor acepta viaje
 *    OUT: /topic/trips/{tripId}       → Notifica al pasajero
 *
 * 3. Rechazar viaje:
 *    IN:  /app/trip.reject/{tripId}   → Conductor rechaza viaje
 *    OUT: /topic/trips/{tripId}       → Notifica al pasajero
 *
 * 4. Actualizar ubicación:
 *    IN:  /app/location.update        → Conductor envía GPS
 *    OUT: /topic/trips/{tripId}/location → Notifica posición al pasajero
 * </pre>
 *
 * <h3>Nota sobre SimpMessagingTemplate:</h3>
 * Se usa convertAndSend() en lugar de @SendTo para tener control
 * dinámico sobre los destinos de los mensajes (por ejemplo, enviar
 * a /topic/trips/{tripId} donde tripId es variable).
 */
@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    private final TripService tripService;
    private final LocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(TripService tripService,
                               LocationService locationService,
                               SimpMessagingTemplate messagingTemplate) {
        this.tripService = tripService;
        this.locationService = locationService;
        this.messagingTemplate = messagingTemplate;
    }

    // =========================================================================
    // Solicitud de viaje
    // =========================================================================

    /**
     * Procesa una solicitud de viaje del pasajero.
     * <p>
     * Flujo:
     * 1. Crea el viaje en la base de datos con estado REQUESTED
     * 2. Notifica a todos los conductores disponibles vía /topic/driver/notifications
     * 3. Envía confirmación al pasajero vía /topic/trips/{tripId}
     * </p>
     *
     * @param dto datos de la solicitud de viaje
     */
    @MessageMapping("/trip.request")
    public void requestTrip(@Payload TripRequestDto dto) {
        log.info("📨 Solicitud de viaje recibida del pasajero: {}", dto.getPassengerId());

        try {
            // Crear el viaje
            TripResponseDto tripResponse = tripService.createTrip(dto);

            // Notificar a todos los conductores disponibles
            List<User> availableDrivers = tripService.findAvailableDrivers();
            log.info("🔔 Notificando a {} conductores disponibles", availableDrivers.size());

            messagingTemplate.convertAndSend(
                    "/topic/driver/notifications",
                    tripResponse
            );

            // Confirmar creación al pasajero
            messagingTemplate.convertAndSend(
                    "/topic/trips/" + tripResponse.getTripId(),
                    tripResponse
            );

            log.info("✅ Viaje {} creado y notificaciones enviadas", tripResponse.getTripId());

        } catch (Exception e) {
            log.error("❌ Error al procesar solicitud de viaje: {}", e.getMessage());
            // En producción, se enviaría un mensaje de error al pasajero
        }
    }

    // =========================================================================
    // Aceptación de viaje
    // =========================================================================

    /**
     * Procesa la aceptación de un viaje por parte de un conductor.
     * <p>
     * Flujo:
     * 1. Actualiza el viaje a estado ACCEPTED con el conductor asignado
     * 2. Notifica al pasajero vía /topic/trips/{tripId}
     * </p>
     *
     * @param tripId   ID del viaje (extraído de la ruta)
     * @param driverId ID del conductor que acepta (enviado como payload)
     */
    @MessageMapping("/trip.accept/{tripId}")
    public void acceptTrip(@DestinationVariable Long tripId,
                           @Payload TripAcceptDto payload) {
        Long driverId = payload != null ? payload.getDriverId() : null;
        log.info("✋ Conductor {} intenta aceptar viaje {}", driverId, tripId);

        try {
            TripResponseDto response = tripService.acceptTrip(tripId, driverId);

            // Notificar al pasajero y a cualquier suscriptor del viaje
            messagingTemplate.convertAndSend(
                    "/topic/trips/" + tripId,
                    response
            );

            log.info("✅ Viaje {} aceptado por conductor {}", tripId, driverId);

        } catch (Exception e) {
            log.error("❌ Error al aceptar viaje {}: {}", tripId, e.getMessage());
        }
    }

    // =========================================================================
    // Rechazo de viaje
    // =========================================================================

    /**
     * Procesa el rechazo de un viaje por parte de un conductor.
     * <p>
     * En este prototipo, un rechazo cancela el viaje. En producción,
     * se reasignaría a otro conductor disponible.
     * </p>
     *
     * @param tripId ID del viaje a rechazar
     */
    @MessageMapping("/trip.reject/{tripId}")
    public void rejectTrip(@DestinationVariable Long tripId) {
        log.info("👎 Solicitud de rechazo para viaje {}", tripId);

        try {
            TripResponseDto response = tripService.rejectTrip(tripId);

            // Notificar a los suscriptores del viaje
            messagingTemplate.convertAndSend(
                    "/topic/trips/" + tripId,
                    response
            );

            log.info("❌ Viaje {} rechazado y cancelado", tripId);

        } catch (Exception e) {
            log.error("❌ Error al rechazar viaje {}: {}", tripId, e.getMessage());
        }
    }

    // =========================================================================
    // Iniciar viaje (En curso)
    // =========================================================================

    /**
     * Cambia el estado del viaje a IN_PROGRESS cuando el conductor recoge al pasajero.
     * Notifica a todos los suscriptores del viaje.
     */
    @MessageMapping("/trip.start/{tripId}")
    public void startTrip(@DestinationVariable Long tripId) {
        log.info("🚀 Iniciando viaje: {}", tripId);

        try {
            TripResponseDto response = tripService.updateTripStatus(tripId, TripStatus.IN_PROGRESS);

            // Notificar a los suscriptores del viaje
            messagingTemplate.convertAndSend(
                    "/topic/trips/" + tripId,
                    response
            );

            log.info("✅ Viaje {} en curso (IN_PROGRESS)", tripId);

        } catch (Exception e) {
            log.error("❌ Error al iniciar viaje {}: {}", tripId, e.getMessage());
        }
    }

    // =========================================================================
    // Completar viaje (Finalizado)
    // =========================================================================

    /**
     * Cambia el estado del viaje a COMPLETED cuando llegan al destino.
     * Notifica a todos los suscriptores del viaje.
     */
    @MessageMapping("/trip.complete/{tripId}")
    public void completeTrip(@DestinationVariable Long tripId) {
        log.info("🏁 Completando viaje: {}", tripId);

        try {
            TripResponseDto response = tripService.updateTripStatus(tripId, TripStatus.COMPLETED);

            // Notificar a los suscriptores del viaje
            messagingTemplate.convertAndSend(
                    "/topic/trips/" + tripId,
                    response
            );

            log.info("✅ Viaje {} completado con éxito (COMPLETED)", tripId);

        } catch (Exception e) {
            log.error("❌ Error al completar viaje {}: {}", tripId, e.getMessage());
        }
    }

    // =========================================================================
    // Actualización de ubicación
    // =========================================================================

    /**
     * Recibe y procesa actualizaciones de ubicación GPS del conductor.
     * <p>
     * Flujo:
     * 1. Persiste la ubicación en la base de datos
     * 2. Actualiza la ubicación actual del conductor en la tabla users
     * 3. Reenvía la ubicación al pasajero vía /topic/trips/{tripId}/location
     * </p>
     *
     * @param dto datos de la actualización (tripId, driverId, lat, lng)
     */
    @MessageMapping("/location.update")
    public void updateLocation(@Payload LocationUpdateDto dto) {
        log.debug("📍 Ubicación recibida - Viaje: {}, Lat: {}, Lng: {}",
                dto.getTripId(), dto.getLatitude(), dto.getLongitude());

        try {
            // Persistir en base de datos
            LocationUpdate saved = locationService.saveLocationUpdate(dto);

            // Reenviar la ubicación al pasajero suscrito al viaje
            messagingTemplate.convertAndSend(
                    "/topic/trips/" + dto.getTripId() + "/location",
                    dto
            );

        } catch (Exception e) {
            log.error("❌ Error al procesar actualización de ubicación: {}", e.getMessage());
        }
    }
}
