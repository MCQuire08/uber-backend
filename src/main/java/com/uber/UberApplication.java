package com.uber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Uber Backend.
 * <p>
 * Punto de entrada del servidor Spring Boot que gestiona:
 * - API REST para operaciones CRUD de usuarios y viajes
 * - WebSockets (STOMP/SockJS) para comunicación en tiempo real
 * - Integración con PostgreSQL mediante Spring Data JPA
 * </p>
 *
 * @author Uber Backend Team
 * @version 1.0.0
 */
@SpringBootApplication
public class UberApplication {

    public static void main(String[] args) {
        SpringApplication.run(UberApplication.class, args);
    }
}
