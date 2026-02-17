package com.alekseev.userservice.controller;

import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST-контроллер.
 * @RestController = @Controller + @ResponseBody (все методы возвращают JSON).
 * @RequestMapping — базовый путь для всех эндпоинтов.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);
        return ResponseEntity.ok(new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAge()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        if (!userRepository.existsById(id)) return ResponseEntity.notFound().build();
        User user = User.builder()
                .id(id)
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
        User saved = userRepository.save(user);
        return ResponseEntity.ok(new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAge()));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = users.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOptional.get();
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}