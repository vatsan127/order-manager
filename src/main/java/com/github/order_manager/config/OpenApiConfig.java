package com.github.order_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Manager API")
                        .description("API for managing orders and order items with JPA one-to-many relationship")
                        .version("v1"));
    }
}
