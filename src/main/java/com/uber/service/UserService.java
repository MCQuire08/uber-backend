package com.uber.service;

import com.uber.model.User;
import com.uber.model.UserRole;
import com.uber.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para la gestión de usuarios.
 * <p>
 * Responsabilidades:
 * <ul>
 *   <li>CRUD de usuarios</li>
 *   <li>Gestión del estado online/offline de conductores</li>
 *   <li>Búsqueda de conductores disponibles</li>
 *   <li>Inicialización de perfiles mock para desarrollo</li>
 * </ul>
 * </p>
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================================================================
    // Inicialización de datos mock
    // =========================================================================

    /**
     * Crea perfiles mock al iniciar la aplicación si no existen.
     * - Cliente A (PASSENGER): id dinámico, perfil de prueba para solicitar viajes.
     * - Conductor B (DRIVER): id dinámico, perfil de prueba disponible online.
     */
    @PostConstruct
    public void initMockProfiles() {
        // Solo crear si la tabla está vacía (evitar duplicados en reinicios)
        if (userRepository.count() == 0) {
            log.info("Creando perfiles mock para desarrollo...");

            User clientA = new User("Cliente A", "clientea@uber.com", UserRole.PASSENGER);
            clientA.setCurrentLatitude(9.9281);    // San José, Costa Rica (ejemplo)
            clientA.setCurrentLongitude(-84.0907);
            userRepository.save(clientA);
            log.info("✅ Perfil mock creado: {}", clientA);

            User driverB = new User("Conductor B", "conductorb@uber.com", UserRole.DRIVER);
            driverB.setIsOnline(true);
            driverB.setCurrentLatitude(9.9350);
            driverB.setCurrentLongitude(-84.0850);
            userRepository.save(driverB);
            log.info("✅ Perfil mock creado: {}", driverB);
        } else {
            log.info("Perfiles mock ya existen, omitiendo creación.");
        }
    }

    // =========================================================================
    // Operaciones de consulta
    // =========================================================================

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario encontrado
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de usuarios
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Encuentra todos los conductores que están actualmente online.
     * Se usa para buscar conductores disponibles cuando un pasajero solicita un viaje.
     *
     * @return lista de conductores disponibles
     */
    @Transactional(readOnly = true)
    public List<User> findAvailableDrivers() {
        return userRepository.findByRoleAndIsOnline(UserRole.DRIVER, true);
    }

    // =========================================================================
    // Operaciones de actualización
    // =========================================================================

    /**
     * Actualiza el estado online/offline de un conductor.
     * <p>
     * Solo los usuarios con rol DRIVER pueden cambiar su estado.
     * Cuando un conductor se pone online, queda disponible para recibir solicitudes de viaje.
     * </p>
     *
     * @param userId   ID del conductor
     * @param isOnline true para poner online, false para offline
     * @return el usuario actualizado
     * @throws RuntimeException si el usuario no existe o no es conductor
     */
    public User updateDriverStatus(Long userId, Boolean isOnline) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        if (user.getRole() != UserRole.DRIVER) {
            throw new RuntimeException("Solo los conductores pueden cambiar su estado online/offline");
        }

        user.setIsOnline(isOnline);
        User updated = userRepository.save(user);
        log.info("Conductor {} ahora está {}", updated.getName(), isOnline ? "ONLINE" : "OFFLINE");
        return updated;
    }

    /**
     * Actualiza la ubicación actual de un usuario.
     *
     * @param userId    ID del usuario
     * @param latitude  nueva latitud
     * @param longitude nueva longitud
     * @return el usuario actualizado
     */
    public User updateLocation(Long userId, Double latitude, Double longitude) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        user.setCurrentLatitude(latitude);
        user.setCurrentLongitude(longitude);
        return userRepository.save(user);
    }

    // =========================================================================
    // Métodos de perfiles mock (acceso directo para testing)
    // =========================================================================

    /**
     * Obtiene el perfil mock del Cliente A (pasajero).
     *
     * @return Optional con el usuario si existe
     */
    @Transactional(readOnly = true)
    public Optional<User> getClientA() {
        return userRepository.findByEmail("clientea@uber.com");
    }

    /**
     * Obtiene el perfil mock del Conductor B (conductor).
     *
     * @return Optional con el usuario si existe
     */
    @Transactional(readOnly = true)
    public Optional<User> getDriverB() {
        return userRepository.findByEmail("conductorb@uber.com");
    }
}
