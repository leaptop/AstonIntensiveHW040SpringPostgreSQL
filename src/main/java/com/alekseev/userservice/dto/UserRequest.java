package com.alekseev.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для входящих данных (создание/обновление пользователя).
 * Record вместо класса + Lombok → меньше boilerplate, immutable по умолчанию.
 * Валидация Bean Validation работает прямо на компонентах record.
 * Compact constructor позволяет выполнить trim() автоматически.
 */
public record UserRequest(

        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email too long")
        String email,

        @Min(value = 0, message = "Age must be non-negative")
        Integer age
) {

    /**
     * Compact constructor — выполняется перед присваиванием полей.
     * Здесь делаем trim() для name и email (как было раньше в контроллере).
     */
    public UserRequest {
        name = name == null ? null : name.trim();
        email = email == null ? null : email.trim();
        // age не нуждается в trim
    }
}