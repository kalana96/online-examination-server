package com.examination.online_examination_server.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Allow CORS for all endpoints
//                        .allowedOrigins("*"); // Allow all headers
                        .allowedOrigins("http://localhost:5173") // Allow only this origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "OPTIONS") // Specify allowed methods
                        .allowedHeaders(
                                "Authorization",
                                "Content-Type",
                                "X-Requested-With",
                                "Accept",
                                "Origin"
                        )
                        .allowCredentials(true) // Allow cookies if needed
                        .maxAge(3600); // Cache preflight response
            }
        };
    }
}
