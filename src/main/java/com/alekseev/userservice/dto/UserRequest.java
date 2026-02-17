package com.alekseev.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRequest {
    @NotBlank(message = "Name cannot be blank")  // Проверяет не пустой после trim()
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")  // Встроенная проверка на формат email
    @Size(max = 150, message = "Email too long")
    private String email;

    @Min(value = 0, message = "Age must be non-negative")  // Age >= 0
    private Integer age;
}