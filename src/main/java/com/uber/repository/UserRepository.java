package com.uber.repository;

import com.uber.model.User;
import com.uber.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link User}.
 * <p>
 * Proporciona operaciones CRUD estándar y consultas personalizadas
 * para gestionar usuarios de la plataforma.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param email correo electrónico del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Encuentra todos los usuarios con un rol específico.
     *
     * @param role rol a filtrar (DRIVER o PASSENGER)
     * @return lista de usuarios con el rol indicado
     */
    List<User> findByRole(UserRole role);

    /**
     * Encuentra conductores que están actualmente online.
     * Útil para buscar conductores disponibles cuando un pasajero solicita un viaje.
     *
     * @param role     debe ser DRIVER
     * @param isOnline true para conductores activos
     * @return lista de conductores disponibles
     */
    List<User> findByRoleAndIsOnline(UserRole role, Boolean isOnline);
}
