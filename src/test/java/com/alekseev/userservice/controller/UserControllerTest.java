package com.alekseev.userservice.controller;

import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тест только веб-слоя.
 * @WebMvcTest(UserController.class) — загружает ТОЛЬКО контроллер + MockMvc.
 * Всё остальное (сервис и т.д.) мокается.
 * Это делает тест очень быстрым и изолированным.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserResponse response = new UserResponse(1L, "Stepan", "stepan@example.com", 30);
        when(userService.create(any())).thenReturn(response);

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
        UserResponse response = new UserResponse(id, "Updated Stepan", "updated@example.com", 31);
        when(userService.update(eq(id), any())).thenReturn(response);

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
        UserResponse user1 = new UserResponse(1L, "Stepan", "stepan@example.com", 30);
        UserResponse user2 = new UserResponse(2L, "Anna", "anna@example.com", 25);
        List<UserResponse> users = Arrays.asList(user1, user2);
        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Stepan"))
                .andExpect(jsonPath("$[1].name").value("Anna"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        Long id = 1L;
        UserResponse response = new UserResponse(id, "Stepan", "stepan@example.com", 30);
        when(userService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Stepan"))
                .andExpect(jsonPath("$.email").value("stepan@example.com"));
    }

    @Test
    void getUserById_shouldReturnNotFound() throws Exception {
        Long id = 1L;
        when(userService.getById(id)).thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        Long id = 1L;
        doNothing().when(userService).deleteById(eq(id));

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnNotFound() throws Exception {
        Long id = 1L;
        doThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND)).when(userService).deleteById(eq(id));

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}