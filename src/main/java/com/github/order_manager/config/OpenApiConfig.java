package com.github.order_manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI orderManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Manager API")
                        .description("REST API for managing orders and order items. " +
                                "Demonstrates JPA one-to-many bidirectional relationships with cascade operations.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Srivatsan")
                                .url("https://github.com/srivatsan"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Local development server")));
    }
}
