package com.alekseev.userservice.controller;

import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST-контроллер для управления пользователями.
 *
 * <h3>Swagger-аннотации (Springdoc OpenAPI):</h3>
 * <ul>
 *   <li>{@code @Tag}            — группирует эндпоинты под одним заголовком в UI</li>
 *   <li>{@code @Operation}      — описание конкретного эндпоинта</li>
 *   <li>{@code @ApiResponses}   — возможные HTTP-статусы с описаниями</li>
 *   <li>{@code @Parameter}      — описание параметра пути/запроса</li>
 * </ul>
 *
 * <h3>HATEOAS (Spring HATEOAS):</h3>
 * Каждый ответ содержит ссылки ({@code _links}), которые описывают,
 * какие действия возможны над ресурсом:
 * <ul>
 *   <li>{@code self}       — ссылка на сам ресурс (GET)</li>
 *   <li>{@code update}     — обновить пользователя (PUT)</li>
 *   <li>{@code delete}     — удалить пользователя (DELETE)</li>
 *   <li>{@code collection} — список всех пользователей (GET)</li>
 * </ul>
 * {@link CollectionModel} используется для списков: он добавляет секцию
 * {@code _embedded} и общую ссылку {@code self} для коллекции.
 */
@Tag(name = "Users", description = "CRUD-операции над пользователями")
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    @Operation(
            summary     = "Создать пользователя",
            description = "Сохраняет нового пользователя в базе данных. " +
                          "Возвращает созданный ресурс с HATEOAS-ссылками."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные (нарушение валидации)",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserRequest request
    ) {
        log.info("POST /api/users — создание пользователя");

        UserResponse response = userService.create(request);
        addLinks(response);

        return ResponseEntity
                .status(HttpStatus.CREATED)   // 201 Created — семантически верно для POST
                .body(response);
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    @Operation(
            summary     = "Обновить пользователя",
            description = "Полностью заменяет данные пользователя по указанному ID (PUT-семантика)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь обновлён",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
    ) {
        log.info("PUT /api/users/{} — обновление пользователя", id);

        UserResponse response = userService.update(id, request);
        addLinks(response);

        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET ALL
    // -------------------------------------------------------------------------

    @Operation(
            summary     = "Получить всех пользователей",
            description = "Возвращает список всех пользователей. " +
                          "Каждый элемент содержит HATEOAS-ссылки. " +
                          "Формат ответа — HAL (_embedded + _links)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список успешно возвращён (может быть пустым)")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<UserResponse>> getAll() {
        log.info("GET /api/users — получение всех пользователей");

        List<UserResponse> users = userService.getAll();
        users.forEach(this::addLinks);

        // CollectionModel оборачивает список и добавляет общую ссылку self для коллекции.
        // _embedded.userResponseList — имя секции формируется автоматически из имени класса.
        CollectionModel<UserResponse> collection = CollectionModel.of(
                users,
                linkTo(methodOn(UserController.class).getAll()).withSelfRel()
        );

        return ResponseEntity.ok(collection);
    }

    // -------------------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------------------

    @Operation(
            summary     = "Получить пользователя по ID",
            description = "Возвращает одного пользователя с HATEOAS-ссылками."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id
    ) {
        log.info("GET /api/users/{} — получение пользователя", id);

        UserResponse response = userService.getById(id);
        addLinks(response);

        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Operation(
            summary     = "Удалить пользователя",
            description = "Удаляет пользователя по ID. Возвращает 204 No Content."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Пользователь удалён"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id
    ) {
        log.info("DELETE /api/users/{} — удаление пользователя", id);
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Вспомогательный метод — добавляет HATEOAS-ссылки к объекту UserResponse
    // =========================================================================

    /**
     * Добавляет четыре ссылки к ресурсу пользователя.
     *
     * <p>Что такое {@code linkTo(methodOn(...))}?
     * <ul>
     *   <li>{@code methodOn(UserController.class)} создаёт прокси контроллера.</li>
     *   <li>Вызов метода прокси <b>не выполняет реальную логику</b> — Spring HATEOAS
     *       лишь перехватывает вызов, чтобы извлечь @RequestMapping и построить URL.</li>
     *   <li>{@code linkTo(...)} оборачивает URL в объект {@link Link}.</li>
     * </ul>
     *
     * <p>Итоговый JSON для пользователя с id=1:
     * <pre>{@code
     * "_links": {
     *   "self":       { "href": "http://localhost:8080/api/users/1" },
     *   "update":     { "href": "http://localhost:8080/api/users/1" },
     *   "delete":     { "href": "http://localhost:8080/api/users/1" },
     *   "collection": { "href": "http://localhost:8080/api/users"   }
     * }
     * }</pre>
     */
    private void addLinks(UserResponse response) {
        Long id = response.getId();

        response.add(
                // GET /api/users/{id}  → ссылка на себя
                linkTo(methodOn(UserController.class).getById(id))
                        .withSelfRel(),

                // PUT /api/users/{id}  → обновление
                linkTo(methodOn(UserController.class).update(id, null))
                        .withRel("update"),

                // DELETE /api/users/{id} → удаление
                linkTo(methodOn(UserController.class).deleteById(id))
                        .withRel("delete"),

                // GET /api/users        → вся коллекция
                linkTo(methodOn(UserController.class).getAll())
                        .withRel("collection")
        );
    }
}
