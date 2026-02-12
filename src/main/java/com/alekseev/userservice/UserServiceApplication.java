package com.alekseev.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения.
 * @SpringBootApplication — это комбинация трёх аннотаций:
 *   1. @Configuration — говорит Spring, что этот класс содержит конфигурацию бинов.
 *   2. @EnableAutoConfiguration — включает автоконфигурацию (DataSource, JPA, Web и т.д.).
 *   3. @ComponentScan — сканирует все пакеты внутри com.alekseev.userservice и ниже,
 *      поэтому все @Service, @Repository, @Controller будут найдены автоматически.
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}