package com.uber.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket con STOMP sobre SockJS.
 * <p>
 * Define los endpoints de conexión, prefijos de mensajes y el broker
 * de mensajes en memoria para la comunicación en tiempo real.
 * </p>
 *
 * <h3>Arquitectura de mensajes:</h3>
 * <pre>
 * Cliente → /app/*           (mensajes de aplicación, procesados por @MessageMapping)
 * Servidor → /topic/*        (mensajes broadcast a suscriptores)
 * Conexión WebSocket: /ws-uber (endpoint SockJS)
 * </pre>
 *
 * <h3>Flujo de comunicación:</h3>
 * <ol>
 *   <li>Cliente se conecta a /ws-uber vía SockJS</li>
 *   <li>Cliente se suscribe a /topic/trips/{tripId} para recibir actualizaciones</li>
 *   <li>Cliente envía mensajes a /app/trip.request, /app/trip.accept, etc.</li>
 *   <li>Servidor procesa y reenvía a los topics correspondientes</li>
 * </ol>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura el broker de mensajes.
     * <ul>
     *   <li><b>/topic</b>: Prefijo para mensajes de suscripción (broadcast).</li>
     *   <li><b>/app</b>: Prefijo para mensajes enviados por la aplicación (entrada).</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar broker simple en memoria para topics de suscripción
        config.enableSimpleBroker("/topic");

        // Prefijo para mensajes destinados a métodos @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra los endpoints STOMP sobre SockJS.
     * <p>
     * El endpoint /ws-uber es el punto de conexión WebSocket.
     * SockJS proporciona fallback para navegadores que no soportan WebSocket nativo.
     * Se permite CORS desde cualquier origen para facilitar el desarrollo.
     * </p>
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-uber")
                .setAllowedOriginPatterns("*")  // CORS permisivo para desarrollo
                .withSockJS();                   // Habilitar fallback SockJS
    }
}
