package com.alekseev.userservice.service;

import com.alekseev.userservice.client.NotificationClient;
import com.alekseev.userservice.dto.NotificationRequest;
import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public UserResponse create(UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();

        // Пользователя сохраняем РОВНО ОДИН РАЗ.
        // Это важно: если сначала сделать save(), а потом в fallback снова save(),
        // можно получить дубль и нарушение unique constraint по email.
        User saved = userRepository.save(user);

        sendCreateEventToKafka(saved);
        sendCreateNotificationOverHttp(saved);

        return toResponse(saved);
    }

    private void sendCreateEventToKafka(User saved) {
        try {
            Map<String, Object> kafkaMessage = new HashMap<>();
            kafkaMessage.put("operation", "create");
            kafkaMessage.put("email", saved.getEmail());

            kafkaTemplate.send("user-events", kafkaMessage);
            log.info("Kafka message sent for create user: {}", saved.getEmail());
        } catch (Exception e) {
            // Создание пользователя уже удалось.
            // Ошибка Kafka не должна превращать успешный POST в 500.
            log.warn("Kafka send failed for {}: {}", saved.getEmail(), e.getMessage(), e);
        }
    }

    private void sendCreateNotificationOverHttp(User saved) {
        try {
            notificationClient.sendNotification(new NotificationRequest("create", saved.getEmail()));
            log.info("HTTP notification sent for create user: {}", saved.getEmail());
        } catch (Exception e) {
            // Уведомление — вторичная операция.
            // Даже если notification-service недоступен, POST /api/users должен остаться успешным.
            log.warn("HTTP notification failed for {}: {}", saved.getEmail(), e.getMessage(), e);
        }
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

        return toResponse(userRepository.save(user));
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }
}