package com.project.servercentrale.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Applica CORS a tutte le rotte
                        .allowedOrigins("*") // Accetta richieste da qualsiasi origine
                        .allowedMethods("*") // Permette tutti i metodi (GET, POST, PUT, DELETE, ecc.)
                        .allowedHeaders("*"); // Permette tutti gli header
            }
        };
    }
}
