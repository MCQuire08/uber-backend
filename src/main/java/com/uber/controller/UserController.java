package com.uber.controller;

import com.uber.dto.DriverStatusDto;
import com.uber.model.User;
import com.uber.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de usuarios.
 * <p>
 * Expone endpoints para consultar perfiles de usuario y
 * gestionar el estado online/offline de los conductores.
 * </p>
 *
 * <h3>Endpoints disponibles:</h3>
 * <ul>
 *   <li>GET  /api/users         — Listar todos los usuarios</li>
 *   <li>GET  /api/users/{id}    — Obtener usuario por ID</li>
 *   <li>PUT  /api/users/{id}/status — Cambiar estado online/offline del conductor</li>
 *   <li>GET  /api/users/drivers/available — Listar conductores disponibles</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de usuarios
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return 200 con el usuario o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza el estado online/offline de un conductor.
     * <p>
     * Solo aplica para usuarios con rol DRIVER.
     * Cuando un conductor se pone online, queda disponible para recibir viajes.
     * </p>
     *
     * @param id  ID del conductor
     * @param dto objeto con el nuevo estado
     * @return 200 con el usuario actualizado o 400 si hay error
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateDriverStatus(@PathVariable Long id,
                                                @RequestBody DriverStatusDto dto) {
        try {
            User updated = userService.updateDriverStatus(id, dto.getIsOnline());
            log.info("Estado del conductor {} actualizado a: {}",
                    id, dto.getIsOnline() ? "ONLINE" : "OFFLINE");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error al actualizar estado del conductor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Lista los conductores que están actualmente online y disponibles.
     *
     * @return lista de conductores disponibles
     */
    @GetMapping("/drivers/available")
    public ResponseEntity<List<User>> getAvailableDrivers() {
        return ResponseEntity.ok(userService.findAvailableDrivers());
    }
}
