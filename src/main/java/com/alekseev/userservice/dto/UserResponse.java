package com.alekseev.userservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.RepresentationModel;

/**
 * DTO для ответа сервера.
 *
 * Наследует {@link RepresentationModel} — это позволяет прикреплять HATEOAS-ссылки
 * (_links) прямо к объекту. Jackson сериализует их автоматически в HAL-формат:
 *
 * <pre>{@code
 * {
 *   "id": 1,
 *   "name": "Stepan",
 *   "email": "stepan@example.com",
 *   "age": 30,
 *   "_links": {
 *     "self":       { "href": "http://localhost:8080/api/users/1" },
 *     "update":     { "href": "http://localhost:8080/api/users/1" },
 *     "delete":     { "href": "http://localhost:8080/api/users/1" },
 *     "collection": { "href": "http://localhost:8080/api/users"   }
 *   }
 * }
 * }</pre>
 *
 * Почему класс, а не record?
 * — Java record не может наследовать от обычных классов.
 * @JsonCreator + @JsonProperty нужны, чтобы Jackson мог десериализовать
 * объект (используется в тестах MockMvc).
 */
@Schema(description = "Данные пользователя в ответе (включает HATEOAS-ссылки)")
public class UserResponse extends RepresentationModel<UserResponse> {

    @Schema(description = "Идентификатор пользователя", example = "1")
    private final Long    id;

    @Schema(description = "Имя пользователя", example = "Stepan Alekseev")
    private final String  name;

    @Schema(description = "Email пользователя", example = "stepan@example.com")
    private final String  email;

    @Schema(description = "Возраст пользователя", example = "30")
    private final Integer age;

    @JsonCreator
    public UserResponse(
            @JsonProperty("id")    Long    id,
            @JsonProperty("name")  String  name,
            @JsonProperty("email") String  email,
            @JsonProperty("age")   Integer age
    ) {
        this.id    = id;
        this.name  = name;
        this.email = email;
        this.age   = age;
    }

    public Long    getId()    { return id;    }
    public String  getName()  { return name;  }
    public String  getEmail() { return email; }
    public Integer getAge()   { return age;   }
}
