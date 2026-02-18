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
 * Это unit-тесты для сервис-слоя (UserService), написанные с использованием JUnit5
 * (фреймворк для тестирования в Java) и Mockito (библиотека для создания "моков" — фейковых
 * объектов, имитирующих поведение реальных зависимостей, чтобы изолировать тестируемый код).
 * Тесты проверяют логику сервиса в изоляции: без реальной БД, без контроллера, без запуска
 * всего приложения. Под капотом они фокусируются на том, как сервис взаимодействует с
 * репозиторием (мок), маппит данные и обрабатывает ошибки.
 */
@ExtendWith(MockitoExtension.class)//Включает поддержку Mockito в JUnit5.
// Под капотом: Автоматически инициализирует моки и инжекты.
class UserServiceTest {

    @Mock
    private UserRepository userRepository;//(*)Создаёт мок репозитория. Mockito позволит
    // "запрограммировать" его поведение (например, when(userRepository.save(any())).thenReturn(user);
    // — когда вызовут save с любым User, верни заданный user).

    @InjectMocks//Автоматически инжектирует моки в сервис (в конструктор UserService).
    // Под капотом: Mockito создаёт экземпляр сервиса (**) и подставляет в него мок-репозиторий userRepository(*).
    private UserService userService;//(**)

    private UserRequest request;
    private User user;

    @BeforeEach
    void setUp() {//Выполняется перед каждым тестом. Здесь инициализируются тестовые данные (request и user).
        // Это как "setup" в Java Core, но автоматизировано JUnit.
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
        when(userRepository.save(any(User.class))).thenReturn(user);//Мокаем: при вызове save верни тестовый user.

        UserResponse response = userService.create(request);

        assertNotNull(response);//1), 2), 3) : проверяем, что вернулся правильный DTO.
        assertEquals(1L, response.id());//2)
        assertEquals("Stepan", response.name());//3)
        verify(userRepository, times(1)).save(any(User.class));//4) Проверяем, что save
        // вызван ровно 1 раз (Mockito отслеживает вызовы). Под капотом: Тест проходит, если сервис правильно
        // маппит Request в Entity, вызывает save и возвращает Response.
    }

    @Test
    void update_shouldUpdateExistingUser() {//
        when(userRepository.existsById(1L)).thenReturn(true);//Мокаем: "Если сервис спросит, существует ли
        // ID 1 — скажи да". Это имитирует реальную БД, где пользователь найден.
        // Без этого сервис кинет исключение (как в коде сервиса: if (!existsById) throw...).
        when(userRepository.save(any(User.class))).thenReturn(user);//Мокаем: "Когда сервис вызовет save с
        // любым User — верни тестовый user (с ID 1, именем "Stepan" и т.д.)". Это имитирует успешное сохранение
        // в БД. Под капотом: any(User.class) — матчер Mockito, ловит любой объект User.

        UserResponse response = userService.update(1L, request);//Вызываем реальный (он реальный, но замоканый)
        // метод update сервиса с ID 1 и
        // тестовым Request (из @BeforeEach: name="Stepan", email="stepan@example.com", age=30). Сервис внутри:
        // проверит existsById (мок вернёт true), создаст User с id=1 и данными из Request, вызовет save
        // (мок вернёт user), маппит в Response и вернёт.

        assertNotNull(response);//Проверяем, что ответ не null (сервис не сломался).
        assertEquals(1L, response.id());//Проверяем конкретные значения в DTO (ID совпадает,
        // значит маппинг прошёл). (Можно добавить больше assertEquals для name, email, age — в твоём коде их нет,
        // но можно доработать для полноты).
        verify(userRepository, times(1)).save(any(User.class));//Проверяем взаимодействие:
        // "Репозиторий должен быть вызван ровно 1 раз с save". Это подтверждает, что сервис не пропустил вызов БД.
        //В unit-тесте мы не обновляем реальную БД — это симуляция. Тест проверяет поведение сервиса: правильно ли он
        // строит Entity (с id из параметра + данные из Request), вызывает save и не ломает ничего. Если сервис имеет
        // баг (например, забыл setId или маппинг сломан) — тест упадёт. Это не проверка "обновления в БД"
        // (для этого нужны интеграционные тесты с @SpringBootTest + Testcontainers), а проверка логики сервиса
        // в изоляции.
    }

    @Test
    void update_shouldThrowNotFoundIfUserDoesNotExist() {//Этот тест проверяет ошибочный путь (error path): когда
        // пользователь не существует (existsById возвращает false), сервис должен кинуть исключение
        // (ResponseStatusException с 404), а не пытаться обновлять. Это гарантирует, что сервис не игнорирует
        // отсутствие записи и правильно обрабатывает ошибку (что вернётся клиенту как HTTP 404).
        when(userRepository.existsById(1L)).thenReturn(false);//Мокаем: "Если спросят existsById(1) — скажи false".
        // Это имитирует сценарий, когда в БД нет такого ID.

        assertThrows(ResponseStatusException.class, () -> userService.update(1L, request));//Вызываем update и
        // ожидаем, что он кинет конкретное исключение. Под капотом: JUnit выполнит
        // lambda () -> userService.update(...), поймает исключение и сравнит класс (ResponseStatusException).
        // Если не кинет или кинет другое — тест упадёт.
        //Почему "запрогаммировали на возврат исключения и получаем исключение": Да, именно! Тест проверяет контракт
        // сервиса: "Если репозиторий скажет 'не существует' — сервис должен выкинуть 404". Мы мокаем репозиторий
        // на false, чтобы симулировать этот случай, и проверяем, что сервис реагирует правильно (throw). Это не
        // "тафтология", а verification поведения: если в сервисе баг (например, if (!exists) continue без throw)
        // — тест упадёт, потому что не будет исключения. Под капотом: assertThrows — это как try-catch в Java Core,
        // но с проверкой. В целом, эти тесты — классика unit-тестирования: happy path (всё OK) + edge case (ошибка).
        // Они изолированы (моки вместо реальной БД), быстрые и фокусируются на логике update.
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        User user2 = User.builder().id(2L).name("Anna").email("anna@example.com").age(25).build();//Вызываем реальный
        //билдер энтити юзера.
        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);//Мокаем findAll.

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
        //Проверяет ошибку при пустом Optional.
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
        when(userRepository.existsById(1L)).thenReturn(false);//deleteById в себе сначала делает проверку existsById.
        //Поэтому проверяем с помощью такого возврата false.

        assertThrows(ResponseStatusException.class, () -> userService.deleteById(1L));
    }
}