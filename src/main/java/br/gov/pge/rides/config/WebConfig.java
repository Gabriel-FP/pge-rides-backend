package br.gov.pge.rides.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global CORS configuration.
 *
 * <p>The Angular dev server runs on http://localhost:4200 and calls this API on
 * http://localhost:8080. Browsers block cross-origin requests unless the server
 * explicitly allows the origin, so this opens the API to the frontend.
 *
 * <p>Origins are externalized via {@code app.cors.allowed-origins} so other
 * environments can override them through the {@code CORS_ALLOWED_ORIGINS}
 * environment variable (comma-separated for multiple origins).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
