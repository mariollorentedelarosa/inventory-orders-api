package com.mario.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

/**
 * Configuración general.
 * - Paginación serializada como DTO estable (evita exponer PageImpl en el JSON).
 * - @Async habilitado para los listeners de eventos de dominio.
 */
@Configuration
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class AppConfig {
}
