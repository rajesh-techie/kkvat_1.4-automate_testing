package com.kkvat.automation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow frontend dev server and Swagger UI to call the API
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // Also allow OpenAPI endpoints and Swagger UI resources
        registry.addMapping("/v3/api-docs/**")
                .allowedOrigins("http://localhost:4200", "http://localhost:8080")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);

        registry.addMapping("/swagger-ui/**")
                .allowedOrigins("http://localhost:4200", "http://localhost:8080")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
