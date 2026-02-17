package com.alekseev.userservice.controller;

import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тест только веб-слоя.
 * @WebMvcTest(UserController.class) — загружает ТОЛЬКО контроллер + MockMvc.
 * Всё остальное (репозиторий, сервис и т.д.) мокается.
 * Это делает тест очень быстрым и изолированным.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        User savedUser = User.builder().id(1L).name("Stepan").email("stepan@example.com").age(30).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        String json = """
                {
                    "name": "Stepan",
                    "email": "stepan@example.com",
                    "age": 30
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Stepan"))
                .andExpect(jsonPath("$.email").value("stepan@example.com"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        Long id = 1L;
        User updatedUser = User.builder().id(id).name("Updated Stepan").email("updated@example.com").age(31).build();
        when(userRepository.existsById(id)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        String json = """
                {
                    "name": "Updated Stepan",
                    "email": "updated@example.com",
                    "age": 31
                }
                """;

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Stepan"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.age").value(31));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        User user1 = User.builder().id(1L).name("Stepan").email("stepan@example.com").age(30).build();
        User user2 = User.builder().id(2L).name("Anna").email("anna@example.com").age(25).build();
        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Stepan"))
                .andExpect(jsonPath("$[1].name").value("Anna"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        Long id = 1L;
        User user = User.builder().id(id).name("Stepan").email("stepan@example.com").age(30).build();
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Stepan"))
                .andExpect(jsonPath("$.email").value("stepan@example.com"));
    }

    @Test
    void getUserById_shouldReturnNotFound() throws Exception {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);
        doNothing().when(userRepository).deleteById(eq(id));

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnNotFound() throws Exception {
        Long id = 1L;
        when(userRepository.existsById(id)).thenReturn(false);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}