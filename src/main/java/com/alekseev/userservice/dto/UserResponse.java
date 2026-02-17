package com.alekseev.userservice.dto;

/**
 * DTO для ответов (только чтение).
 * Record → автоматически: constructor, getters (name(), email() и т.д.), equals/hashCode/toString.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        Integer age
) {
}