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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        request = new UserRequest("Stepan", "stepan@example.com", 30);
        user = User.builder()
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
        assertEquals(1L, response.id());
        assertEquals("Stepan", response.name());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_shouldUpdateExistingUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.update(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.id());
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
        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> responses = userService.getAll();

        assertEquals(2, responses.size());
        assertEquals("Stepan", responses.get(0).name());
        assertEquals("Anna", responses.get(1).name());
    }

    @Test
    void getById_shouldReturnResponse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
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