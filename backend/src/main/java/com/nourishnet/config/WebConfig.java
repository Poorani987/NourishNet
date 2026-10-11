package com.nourishnet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC Configuration:
 * - Enables cross-origin requests (CORS) for local frontend clients
 * - Directly serves the frontend website files from the frontend directory
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serves HTML/CSS/JS frontend files directly on http://localhost:8080/
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "file:../frontend/",
                        "file:./frontend/",
                        "classpath:/static/"
                );
    }
}
