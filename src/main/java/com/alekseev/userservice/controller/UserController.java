package com.alekseev.userservice.controller;
import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**

 REST-контроллер.
 @RestController = @Controller + @ResponseBody (все методы возвращают JSON).
 @RequestMapping — базовый путь для всех эндпоинтов.
 */
// @Slf4j — аннотация Lombok для логирования.
// Генерирует private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);
// SLF4J — фасад для логгеров (здесь Logback по умолчанию в Spring Boot).
// Под капотом: Позволяет log.info(...), log.error(...) с уровнями (INFO, DEBUG и т.д.).
// Логи пишутся в консоль/файл, настраиваются в application.yml.
// Полезно для отладки: "Received request..." — трассировка запросов.
// Без Lombok пришлось бы писать логгер вручную.
// SLF4J абстрагирует: можно сменить на Log4j без изменений кода.
@Slf4j
// @RestController — комбинация @Controller и @ResponseBody.
// @Controller маркирует класс как MVC-контроллер (обработчик запросов).
// @ResponseBody — все методы возвращают данные напрямую (JSON), не view.
// Под капотом: Spring регистрирует как бин, DispatcherServlet маршрутизирует запросы.
// Для REST API (без HTML views).
// Альтернатива: @Controller + @ResponseBody на методах.
// Spring Web (starter-web) включает Tomcat как сервер.
@RestController
// @RequestMapping("/api/users") — базовый путь для всех методов.
// Под капотом: Все эндпоинты начинаются с /api/users.
// Можно добавить method=RequestMethod.GET, но здесь глобальный.
// Помогает организовать API (версионирование: /api/v1/users).
// Spring сопоставляет URL запроса с методами.
@RequestMapping("/api/users")
public class UserController {private final UserService userService;// Конструктор с DI для UserService.
    // Аналогично в сервисе: Spring инжектирует бин UserService.
// final для иммутабельности.
    public UserController(UserService userService) {
        this.userService = userService;
    }// @PostMapping — обрабатывает POST-запросы на /api/users.
    // Под капотом: Spring регистрирует handler для HTTP POST.
// @Valid — активирует валидацию Request (Bean Validation).
// @RequestBody — парсит тело запроса (JSON) в UserRequest (Jackson).
// ResponseEntity.ok(...) — возвращает 200 OK с телом.
// log.info — логирует запрос.
// Метод делегирует сервису, без логики.
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
// log.info — пишет в лог "Received request to create user".
// {} — плейсхолдеры для параметров (как String.format).
// Уровень INFO — для важных событий.
        log.info("Received request to create user");
// userService.create(request) — делегирует сервису.
// ResponseEntity.ok(...) — оборачивает ответ в HTTP 200.
// Под капотом: Spring сериализует UserResponse в JSON.
        return ResponseEntity.ok(userService.create(request));
    }// @PutMapping("/{id}") — PUT на /api/users/{id}, {id} — переменная пути.
    // @PathVariable Long id — извлекает id из URL.
// Аналогично create, но с id.
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
// log.info с параметром id.
        log.info("Received request to update user with id: {}", id);
        return ResponseEntity.ok(userService.update(id, request));
    }// @GetMapping — GET на /api/users.
    // Для списка.
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        log.info("Received request to get all users");
        return ResponseEntity.ok(userService.getAll());
    }// @GetMapping("/{id}") — GET на /api/users/{id}.
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        log.info("Received request to get user by id: {}", id);
        return ResponseEntity.ok(userService.getById(id));
    }// @DeleteMapping("/{id}") — DELETE на /api/users/{id}.
    // ResponseEntity.status(NO_CONTENT) — 204 No Content, без тела.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        log.info("Received request to delete user by id: {}", id);
        userService.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}