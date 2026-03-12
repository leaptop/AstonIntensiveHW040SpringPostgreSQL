package com.alekseev.userservice.service;

import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты сервисного слоя.
 * Никакой зависимости от HATEOAS нет — сервис работает с чистыми DTO.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest request;
    private User        user;

    @BeforeEach
    void setUp() {
        request = new UserRequest("Stepan", "stepan@example.com", 30);
        user    = User.builder()
                .id(1L)
                .name("Stepan")
                .email("stepan@example.com")
                .age(30)
                .build();
    }

    @Test
    void create_shouldSaveAndReturnResponse() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.create(request);

        assertNotNull(response);
        assertEquals(1L,       response.getId());
        assertEquals("Stepan", response.getName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_shouldUpdateExistingUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.update(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_shouldThrowNotFoundIfUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.update(1L, request));
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        User user2 = User.builder().id(2L).name("Anna").email("anna@example.com").age(25).build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        List<UserResponse> responses = userService.getAll();

        assertEquals(2,        responses.size());
        assertEquals("Stepan", responses.get(0).getName());
        assertEquals("Anna",   responses.get(1).getName());
    }

    @Test
    void getById_shouldReturnResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getById_shouldThrowNotFoundIfUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> userService.getById(1L));
    }

    @Test
    void deleteById_shouldDeleteIfExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> userService.deleteById(1L));
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_shouldThrowNotFoundIfUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> userService.deleteById(1L));
    }
}
