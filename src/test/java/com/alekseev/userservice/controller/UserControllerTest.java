package com.alekseev.userservice.controller;

import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}