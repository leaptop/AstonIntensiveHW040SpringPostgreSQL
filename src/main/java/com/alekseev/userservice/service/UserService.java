package com.alekseev.userservice.service;

import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис-слой для бизнес-логики.
 * Здесь происходит маппинг DTO <-> Entity, вызовы репозитория и обработка ошибок.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    public UserService(UserRepository userRepository, KafkaTemplate<String, Map<String, Object>> kafkaTemplate) {
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public UserResponse create(UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);

        // Send Kafka message for create
        Map<String, Object> message = new HashMap<>();
        message.put("operation", "create");
        message.put("email", saved.getEmail());
        kafkaTemplate.send("user-events", message);

        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAge());
    }

    public UserResponse update(Long id, UserRequest request) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User user = User.builder()
                .id(id)
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAge());
    }

    public List<UserResponse> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge()))
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User user = userOptional.get();
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }

    public void deleteById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        String email = userOptional.get().getEmail();
        userRepository.deleteById(id);

        // Send Kafka message for delete
        Map<String, Object> message = new HashMap<>();
        message.put("operation", "delete");
        message.put("email", email);
        kafkaTemplate.send("user-events", message);
    }
}