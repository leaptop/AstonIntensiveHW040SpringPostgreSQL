package com.alekseev.userservice.dto;

/**
 * DTO для отправки запроса в notification-service.
 * Содержит тип операции и email пользователя.
 * Используется при вызове Feign-клиента.
 */
public record NotificationRequest(String operation, String email) { }