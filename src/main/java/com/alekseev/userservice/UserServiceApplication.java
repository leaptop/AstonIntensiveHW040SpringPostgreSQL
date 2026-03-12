package com.alekseev.userservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа в приложение.
 *
 * @OpenAPIDefinition — глобальная метаинформация для Swagger UI.
 * Отображается вверху страницы http://localhost:8080/swagger-ui/index.html
 *
 * @SpringBootApplication объединяет три аннотации:
 *   - @Configuration       — источник бинов Spring
 *   - @EnableAutoConfiguration — автоматическая конфигурация (DataSource, JPA, Web…)
 *   - @ComponentScan       — сканирование всех компонентов пакета
 */
@OpenAPIDefinition(
        info = @Info(
                title       = "User Service API",
                version     = "v1",
                description = "REST API для управления пользователями. " +
                              "Поддерживает CRUD-операции и возвращает HATEOAS-ссылки " +
                              "для навигации по ресурсам (HAL-формат).",
                contact     = @Contact(name = "Alekseev", email = "alekseev@example.com")
        ),
        servers = @Server(url = "http://localhost:8080", description = "Локальный сервер разработки")
)
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
