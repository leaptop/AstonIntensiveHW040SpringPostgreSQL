package com.alekseev.userservice.service;

import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный слой: маппинг DTO ↔ Entity, вызов репозитория, обработка ошибок.
 * HATEOAS-ссылки НЕ добавляются здесь — это ответственность контроллера.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse create(UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);
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
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + id));
        return toResponse(user);
    }

    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------

    /** Маппинг Entity → DTO. */
    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }
}
