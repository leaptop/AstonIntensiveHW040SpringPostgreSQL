package com.alekseev.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * JPA-сущность — представляет строку таблицы {@code users}.
 *
 * @Entity  — помечает класс как персистентный объект Hibernate.
 * @Table   — явно задаёт имя таблицы и unique-ограничение на email.
 * Lombok-аннотации генерируют boilerplate на этапе компиляции.
 */
@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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

    /** Заполняется автоматически при первом сохранении. */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
