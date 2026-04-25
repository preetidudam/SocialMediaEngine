package com.socialmedia;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig.java
 * ══════════════════════════════════════════════════
 * Configures CORS (Cross-Origin Resource Sharing).
 *
 * WHY THIS IS NEEDED:
 * When the HTML frontend (opened as a file:// URL or from a different
 * port) calls our Spring Boot API at localhost:8080, the browser blocks
 * the request by default as a "cross-origin" request.
 *
 * This config tells Spring Boot: "Allow requests from any origin so
 * that our index.html can freely call all /api/** endpoints."
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")           // Apply CORS to all API endpoints
                .allowedOriginPatterns("*")       // Allow any origin (file://, http://localhost:*)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}