package com.alekseev.userservice.controller;

import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты веб-слоя контроллера.
 *
 * @WebMvcTest(UserController.class) — загружает только контроллер + MockMvc.
 * Сервис мокируется через @MockBean.
 *
 * <h3>Изменения по сравнению с ДЗ 4:</h3>
 * <p>После добавления HATEOAS контроллер возвращает объекты с секцией {@code _links}.
 * Тесты теперь проверяют не только поля ответа, но и наличие HATEOAS-ссылок.
 *
 * <h3>Структура ответа для одиночного объекта:</h3>
 * <pre>{@code
 * {
 *   "id": 1, "name": "Stepan", "email": "...", "age": 30,
 *   "_links": {
 *     "self":       { "href": "http://localhost/api/users/1" },
 *     "update":     { "href": "http://localhost/api/users/1" },
 *     "delete":     { "href": "http://localhost/api/users/1" },
 *     "collection": { "href": "http://localhost/api/users"   }
 *   }
 * }
 * }</pre>
 *
 * <h3>Структура ответа для коллекции (HAL):</h3>
 * <pre>{@code
 * {
 *   "_embedded": {
 *     "userResponseList": [
 *       { "id": 1, ..., "_links": { ... } },
 *       { "id": 2, ..., "_links": { ... } }
 *     ]
 *   },
 *   "_links": {
 *     "self": { "href": "http://localhost/api/users" }
 *   }
 * }
 * }</pre>
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/users
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void createUser_shouldReturn201WithLinksAndBody() throws Exception {
        UserResponse response = new UserResponse(1L, "Stepan", "stepan@example.com", 30);
        when(userService.create(any())).thenReturn(response);

        String json = """
                {
                    "name":  "Stepan",
                    "email": "stepan@example.com",
                    "age":   30
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                // После HATEOAS контроллер возвращает 201 Created
                .andExpect(status().isCreated())
                // Поля тела ответа
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Stepan"))
                .andExpect(jsonPath("$.email").value("stepan@example.com"))
                .andExpect(jsonPath("$.age").value(30))
                // HATEOAS-ссылки обязательно присутствуют
                .andExpect(jsonPath("$._links.self.href",       containsString("/api/users/1")))
                .andExpect(jsonPath("$._links.update.href",     containsString("/api/users/1")))
                .andExpect(jsonPath("$._links.delete.href",     containsString("/api/users/1")))
                .andExpect(jsonPath("$._links.collection.href", containsString("/api/users")));
    }

    @Test
    void createUser_shouldReturn400WhenInvalidInput() throws Exception {
        String invalidJson = """
                {
                    "name":  "",
                    "email": "not-an-email",
                    "age":   -1
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /api/users/{id}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void updateUser_shouldReturn200WithLinks() throws Exception {
        Long id = 1L;
        UserResponse response = new UserResponse(id, "Updated Stepan", "updated@example.com", 31);
        when(userService.update(eq(id), any())).thenReturn(response);

        String json = """
                {
                    "name":  "Updated Stepan",
                    "email": "updated@example.com",
                    "age":   31
                }
                """;

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Stepan"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.age").value(31))
                .andExpect(jsonPath("$._links.self.href",       containsString("/api/users/1")))
                .andExpect(jsonPath("$._links.collection.href", containsString("/api/users")));
    }

    @Test
    void updateUser_shouldReturn404WhenNotFound() throws Exception {
        Long id = 999L;
        when(userService.update(eq(id), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        String json = """
                {
                    "name":  "Someone",
                    "email": "someone@example.com",
                    "age":   20
                }
                """;

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/users
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getAllUsers_shouldReturnHalCollectionWithEmbedded() throws Exception {
        UserResponse user1 = new UserResponse(1L, "Stepan", "stepan@example.com", 30);
        UserResponse user2 = new UserResponse(2L, "Anna",   "anna@example.com",   25);
        when(userService.getAll()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                /*
                 * CollectionModel помещает элементы в _embedded.userResponseList
                 * (имя формируется из UserResponse + "List" в camelCase).
                 */
                .andExpect(jsonPath("$._embedded.userResponseList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.userResponseList[0].name").value("Stepan"))
                .andExpect(jsonPath("$._embedded.userResponseList[1].name").value("Anna"))
                // У каждого элемента свои ссылки
                .andExpect(jsonPath("$._embedded.userResponseList[0]._links.self.href",
                        containsString("/api/users/1")))
                .andExpect(jsonPath("$._embedded.userResponseList[1]._links.self.href",
                        containsString("/api/users/2")))
                // Ссылка self для всей коллекции
                .andExpect(jsonPath("$._links.self.href", containsString("/api/users")));
    }

    @Test
    void getAllUsers_shouldReturnEmptyEmbedded() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        /*
         * Когда список пуст, Spring HATEOAS не генерирует секцию _embedded.
         * Ответ содержит только _links.self.
         */
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", containsString("/api/users")));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/users/{id}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getUserById_shouldReturn200WithLinks() throws Exception {
        Long id = 1L;
        UserResponse response = new UserResponse(id, "Stepan", "stepan@example.com", 30);
        when(userService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Stepan"))
                .andExpect(jsonPath("$.email").value("stepan@example.com"))
                .andExpect(jsonPath("$._links.self.href",       containsString("/api/users/1")))
                .andExpect(jsonPath("$._links.update.href",     containsString("/api/users/1")))
                .andExpect(jsonPath("$._links.delete.href",     containsString("/api/users/1")))
                .andExpect(jsonPath("$._links.collection.href", containsString("/api/users")));
    }

    @Test
    void getUserById_shouldReturn404WhenNotFound() throws Exception {
        Long id = 99L;
        when(userService.getById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/users/{id}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        Long id = 1L;
        doNothing().when(userService).deleteById(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteById(id);
    }

    @Test
    void deleteUser_shouldReturn404WhenNotFound() throws Exception {
        Long id = 99L;
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(userService).deleteById(id);

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
