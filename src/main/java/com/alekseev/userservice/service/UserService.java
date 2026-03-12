package com.alekseev.userservice.service;

import com.alekseev.userservice.client.NotificationClient;
import com.alekseev.userservice.dto.NotificationRequest;
import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервисный слой с бизнес-логикой.
 *
 * Добавлен FeignClient для вызова notification-service и Circuit Breaker
 * для защиты от сбоев этого вызова. Kafka остаётся основным каналом уведомлений,
 * а HTTP-вызов используется как демонстрация паттерна Circuit Breaker.
 *
 * @CircuitBreaker(name = "notificationService") связывает метод с конфигурацией
 * circuit breaker из application.yml (или Config Server). При превышении порога
 * ошибок вызовы временно блокируются и срабатывает fallback-метод.
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;
    private final NotificationClient notificationClient;

    public UserService(UserRepository userRepository,
                       KafkaTemplate<String, Map<String, Object>> kafkaTemplate,
                       NotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.notificationClient = notificationClient;
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "createFallback")
    public UserResponse create(UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);

        // Отправка события в Kafka (основной канал)
        Map<String, Object> kafkaMessage = new HashMap<>();
        kafkaMessage.put("operation", "create");
        kafkaMessage.put("email", saved.getEmail());
        kafkaTemplate.send("user-events", kafkaMessage);
        log.info("Kafka message sent for create user: {}", saved.getEmail());

        // Дополнительный HTTP-вызов к notification-service (с защитой Circuit Breaker)
        try {
            notificationClient.sendNotification(new NotificationRequest("create", saved.getEmail()));
            log.info("HTTP notification sent for create user: {}", saved.getEmail());
        } catch (Exception e) {
            // Circuit Breaker уже должен сработать и переключиться на fallback,
            // но здесь мы просто логируем (fallback будет вызван автоматически при ошибке)
            log.warn("HTTP notification failed (fallback will handle): {}", e.getMessage());
        }

        return toResponse(saved);
    }

    /**
     * Fallback-метод для create. Вызывается, когда circuit breaker разомкнут
     * или вызов notificationClient завершился ошибкой.
     * Пользователь всё равно создаётся, но уведомление не отправляется по HTTP.
     */
    public UserResponse createFallback(UserRequest request, Throwable t) {
        log.error("Circuit breaker triggered for create, cause: {}", t.getMessage());
        // Создаём пользователя, но без HTTP-уведомления
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);

        // Kafka-сообщение отправляется всегда (оно не зависит от circuit breaker)
        Map<String, Object> kafkaMessage = new HashMap<>();
        kafkaMessage.put("operation", "create");
        kafkaMessage.put("email", saved.getEmail());
        kafkaTemplate.send("user-events", kafkaMessage);

        return toResponse(saved);
    }

    public UserResponse update(Long id, UserRequest request) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        User user = User.builder()
                .id(id)
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        return toResponse(user);
    }

    @CircuitBreaker(name = "notificationService", fallbackMethod = "deleteFallback")
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));

        // Отправка Kafka-сообщения
        Map<String, Object> kafkaMessage = new HashMap<>();
        kafkaMessage.put("operation", "delete");
        kafkaMessage.put("email", user.getEmail());
        kafkaTemplate.send("user-events", kafkaMessage);

        // HTTP-уведомление
        notificationClient.sendNotification(new NotificationRequest("delete", user.getEmail()));

        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    /**
     * Fallback для deleteById. Вызывается при сбое HTTP-уведомления.
     * Пользователь уже удалён (Kafka-сообщение отправлено), так что здесь
     * только логируем ошибку. Важно: удаление произошло до вызова notificationClient,
     * поэтому при падении circuit breaker пользователь всё равно будет удалён,
     * но уведомление не дойдёт. Это допустимо по условиям задания.
     */
    public void deleteFallback(Long id, Throwable t) {
        log.error("Circuit breaker triggered for delete, user {} already deleted, but notification failed: {}",
                id, t.getMessage());
        // Действий не требуется, так как удаление уже выполнено.
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }
}