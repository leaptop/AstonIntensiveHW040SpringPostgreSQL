package com.alekseev.userservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Главный класс user-service.
 *
 * @EnableDiscoveryClient включает регистрацию в Eureka.
 * @EnableFeignClients включает сканирование интерфейсов Feign-клиентов.
 * <p>
 * Конфигурация будет загружена из Config Server благодаря
 * spring.config.import в application.yml.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "User Service API",
                version = "v1",
                description = "REST API для управления пользователями. " +
                        "Поддерживает CRUD-операции, HATEOAS-ссылки и отправку событий в Kafka.",
                contact = @Contact(name = "Alekseev", email = "alekseev@example.com")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Gateway URL (через API Gateway)")
)
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}