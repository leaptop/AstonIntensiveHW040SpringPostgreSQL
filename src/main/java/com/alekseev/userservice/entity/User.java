package com.alekseev.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Сущность JPA (таблица в БД).
 * @Entity — говорит Hibernate: «это класс, который нужно сохранять в БД».
 * @Table — имя таблицы и ограничения (unique email).
 * Lombok-аннотации генерируют boilerplate-код на этапе компиляции.
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}