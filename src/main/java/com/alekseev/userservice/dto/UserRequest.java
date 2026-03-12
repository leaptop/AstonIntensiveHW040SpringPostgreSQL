package com.alekseev.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для входящих данных при создании / обновлении пользователя.
 *
 * Record — неизменяемый (immutable) носитель данных.
 * Compact constructor выполняет trim() до присваивания полей.
 *
 * @Schema — добавляет описания полей в Swagger UI и генерирует пример запроса.
 */
@Schema(description = "Данные пользователя для создания или обновления")
public record UserRequest(

        @Schema(description = "Имя пользователя", example = "Stepan Alekseev",
                minLength = 2, maxLength = 100)
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @Schema(description = "Email пользователя (уникальный)", example = "stepan@example.com",
                maxLength = 150)
        @NotBlank(message = "Email cannot be blank")
        @Email(message   = "Invalid email format")
        @Size(max = 150, message = "Email too long")
        String email,

        @Schema(description = "Возраст пользователя (>= 0)", example = "30", minimum = "0")
        @Min(value = 0, message = "Age must be non-negative")
        Integer age

) {
    /**
     * Compact constructor — выполняется перед присваиванием полей.
     * Обрезает пробельные символы по краям строк.
     */
    public UserRequest {
        name  = name  == null ? null : name.trim();
        email = email == null ? null : email.trim();
    }
}
