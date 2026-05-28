package com.uber.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 * <p>
 * Permite solicitudes desde diferentes orígenes durante el desarrollo.
 * En producción, estos valores deben restringirse a los dominios autorizados.
 * </p>
 *
 * <h3>Orígenes permitidos:</h3>
 * <ul>
 *   <li>localhost:3000 — Frontend web (React, Angular, etc.)</li>
 *   <li>localhost:8081 — Emulador Android</li>
 *   <li>10.0.2.2      — Emulador Android (acceso al host)</li>
 *   <li>*             — Todos los orígenes (solo desarrollo)</li>
 * </ul>
 *
 * <h3>⚠️ Nota de seguridad:</h3>
 * Esta configuración es permisiva y pensada para desarrollo.
 * En producción, limitar los orígenes a dominios específicos.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permitir orígenes para desarrollo local y móvil
        config.setAllowedOriginPatterns(Arrays.asList("*"));

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList("*"));

        // Permitir envío de credenciales (cookies, auth headers)
        config.setAllowCredentials(true);

        // Tiempo de caché para preflight requests (1 hora)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
