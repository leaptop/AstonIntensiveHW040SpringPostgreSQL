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
import static org.mockito.Mockito.;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**

 Тест только веб-слоя.
 @WebMvcTest(UserController.class) — загружает ТОЛЬКО контроллер + MockMvc.
 Всё остальное (сервис и т.д.) мокается.
 Это делает тест очень быстрым и изолированным.
 */
// @WebMvcTest(UserController.class) — аннотация Spring Boot Test для веб-слоя.
// Под капотом: Загружает минимальный контекст: только контроллер, фильтры, валидацию.
// Не загружает весь app (без БД, сервисов).
// MockMvc — для симуляции HTTP-запросов.
// JUnit5 запускает тесты.
// Это slice-тест: изолирует MVC.
@WebMvcTest(UserController.class)
class UserControllerTest {// @Autowired — инжектирует бин из тестового контекста.
    // MockMvc — инструмент для тестов: симулирует запросы как браузер.
// Под капотом: perform() строит запрос, andExpect() проверяет ответ.
    @Autowired
    private MockMvc mockMvc;// @MockBean — мокает бин UserService в контексте.
    // Под капотом: Mockito создаёт мок, Spring подставляет в контроллер.
// when(...).thenReturn(...) — программирует поведение.
    @MockBean
    private UserService userService;// @Test — маркирует метод как тест JUnit5.
    // createUser_shouldReturnCreatedUser — имя описывает сценарий.
// Тест: симулирует POST, проверяет статус и JSON.
    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
// new UserResponse — тестовый DTO для мока.
        UserResponse response = new UserResponse(1L, "Stepan", "stepan@example.com", 30);
// when(userService.create(any())).thenReturn(response); — мок: при create верни response.
// any() — матчер для любого UserRequest.
        when(userService.create(any())).thenReturn(response);// Многострочный строковый литерал (Java 15+) для JSON.
        String json = """
{
"name": "Stepan",
"email": "stepan@example.com",
"age": 30
}
""";// mockMvc.perform — строит и выполняет запрос.
// post("/api/users") — метод и путь.
// contentType(JSON) — заголовок.
// content(json) — тело.
// andExpect(status.isOk()) — проверка 200.
// jsonPath("$.name").value("Stepan") — проверка JSON (JsonPath как XPath).
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Stepan"))
                .andExpect(jsonPath("$.email").value("stepan@example.com"));
    }// Аналогичный тест для update.
    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        Long id = 1L;
        UserResponse response = new UserResponse(id, "Updated Stepan", "updated@example.com", 31);
        when(userService.update(eq(id), any())).thenReturn(response);String json = """
{
"name": "Updated Stepan",
"email": "updated@example.com",
"age": 31
}
""";mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Stepan"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.age").value(31));
    }// Тест для getAll.
    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        UserResponse user1 = new UserResponse(1L, "Stepan", "stepan@example.com", 30);
        UserResponse user2 = new UserResponse(2L, "Anna", "anna@example.com", 25);
        List<UserResponse> users = Arrays.asList(user1, user2);
        when(userService.getAll()).thenReturn(users);mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Stepan"))
                .andExpect(jsonPath("$[1].name").value("Anna"));
    }// Тест для getById success.
    @Test
    void getUserById_shouldReturnUser() throws Exception {
        Long id = 1L;
        UserResponse response = new UserResponse(id, "Stepan", "stepan@example.com", 30);
        when(userService.getById(id)).thenReturn(response);mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Stepan"))
                .andExpect(jsonPath("$.email").value("stepan@example.com"));
    }// Тест для getById error.
    @Test
    void getUserById_shouldReturnNotFound() throws Exception {
        Long id = 1L;
// thenThrow — мок кидает исключение.
        when(userService.getById(id)).thenThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }// Тест для delete success.
    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        Long id = 1L;
// doNothing — мок ничего не делает.
        doNothing().when(userService).deleteById(eq(id));mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());
    }// Тест для delete error.
    @Test
    void deleteUser_shouldReturnNotFound() throws Exception {
        Long id = 1L;
// doThrow — мок кидает исключение.
        doThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND)).when(userService).deleteById(eq(id));mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}