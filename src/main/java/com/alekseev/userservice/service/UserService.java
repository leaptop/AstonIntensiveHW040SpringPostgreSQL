package com.alekseev.userservice.service;

import com.alekseev.userservice.dto.UserRequest;
import com.alekseev.userservice.dto.UserResponse;
import com.alekseev.userservice.entity.User;
import com.alekseev.userservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис-слой для бизнес-логики.
 * Здесь происходит маппинг DTO <-> Entity, вызовы репозитория и обработка ошибок.
 */
// @Service — это аннотация Spring, которая маркирует класс как сервис-компонент.
// В Spring сервис — это слой, где размещается бизнес-логика приложения.
// Под капотом: Spring сканирует пакеты (благодаря @ComponentScan в UserServiceApplication),
// находит @Service и регистрирует этот класс как бин (bean) в контексте приложения.
// Бин — это управляемый Spring объект: Spring создаёт его экземпляр, инжектирует зависимости
// (как UserRepository в конструкторе) и управляет жизненным циклом.
// Без @Service пришлось бы вручную регистрировать в конфигурации (XML или @Bean).
// Это следует принципу IoC (Inversion of Control): не ты создаёшь объекты, а Spring.
// Сервис можно инжектировать в другие бины, как в контроллер.
// @Service добавляет транзакционность по умолчанию для методов (если включено @Transactional,
// но здесь не используется).
// В твоём проекте сервис отделяет контроллер (HTTP) от репозитория (БД), делая код модульным.
@Service
public class UserService {
    private final UserRepository userRepository;// Конструктор с инъекцией зависимости.
    // Spring использует Dependency Injection (DI): автоматически передаёт UserRepository.
// Под капотом: При создании бина UserService Spring смотрит на конструктор,
// видит параметр UserRepository (который тоже бин, благодаря @Repository),
// и инжектирует его экземпляр.
// final — делает поле неизменяемым после инициализации, хорошая практика.
// Без DI пришлось бы вручную создавать UserRepository userRepository = new UserRepositoryImpl();
// Но в Spring Data JPA UserRepository — интерфейс, реализация генерируется на лету.
// Это упрощает тестирование: в тестах можно мокать репозиторий.
// Если конструкторов несколько, используй @Autowired на нужном.
// Здесь один конструктор, так что @Autowired не обязателен.
// DI — часть IoC: контроль за зависимостями отдаётся фреймворку.
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }// Метод create: создаёт нового пользователя.

    // Принимает UserRequest (DTO от клиента), возвращает UserResponse (DTO для клиента).
// Логика: маппинг Request -> Entity, save в БД, маппинг Entity -> Response.
// Под капотом: Нет исключений здесь, так как создание не проверяет existence.
// Если email уникальный (по @UniqueConstraint в Entity), БД кинет ошибку,
// Spring перехватит и вернёт 500, но можно добавить обработку.
// Метод публичный, вызывается из контроллера.
// В реальном проекте добавь @Transactional для атомарности, если операций несколько.
// Здесь простая операция, так что транзакция по умолчанию от JPA.
// Возврат UserResponse скрывает Entity от клиента (хорошая практика безопасности).
// Builder от Lombok упрощает создание User.
    public UserResponse create(UserRequest request) {
// User.builder() — паттерн Builder от Lombok: цепочка методов для создания объекта.
// Под капотом: Lombok генерирует статический builder() и методы like .name(), .build().
// Это лучше, чем конструктор с многими параметрами или setters (иммутабельность).
// request.name() — аксессор record (без get, как в классах).
// Trim уже сделан в compact constructor UserRequest.
// Без builder: new User(null, request.name(), request.email(), request.age(), null).
// Builder делает код читаемым.
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
// userRepository.save(user) — метод из JpaRepository: вставляет в БД (INSERT SQL).
// Под капотом: Spring Data JPA генерирует SQL через Hibernate, выполняет,
// возвращает сохранённый Entity с сгенерированным ID.
// Если ID уже есть — обновит (UPDATE), но здесь новый (ID null).
// Транзакция автоматическая.
// saved имеет ID от БД.
        User saved = userRepository.save(user);
// new UserResponse — создаёт DTO для ответа.
// Под капотом: Record генерирует конструктор автоматически.
// Используем getters от Lombok в User (saved.getId() и т.д.).
// Это маппинг: копируем данные, чтобы не отдавать полный Entity.
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAge());
    }// Метод update: обновляет существующего пользователя.

    // Проверяет existence, маппит, save, возвращает Response.
// Под капотом: save с ID — UPDATE SQL.
// Если не существует — throw исключение, которое Spring превратит в HTTP 404.
// Логика простая, но в реале добавь проверки (например, email уникальный).
// Метод выбрасывает исключение, контроллер его не ловит — Spring обработает глобально.
    public UserResponse update(Long id, UserRequest request) {
// userRepository.existsById(id) — проверяет наличие по ID (SELECT COUNT).
// Под капотом: Эффективный запрос, не загружает весь объект.
// if (!...) throw — стандарт для ошибок.
// ResponseStatusException — Spring-класс для HTTP-ошибок.
// Под капотом: Кинет исключение, Spring перехватит, вернёт 404 с сообщением.
// Лучше, чем return null или Optional — для REST API.
// HttpStatus.NOT_FOUND — enum для 404.
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
// Builder с id — для обновления (ID не null).
// Копируем данные из Request, но сохраняем ID.
// createdAt не трогаем (updatable=false в Entity).
        User user = User.builder()
                .id(id)
                .name(request.name())
                .email(request.email())
                .age(request.age())
                .build();
// save как в create, но UPDATE.
        User saved = userRepository.save(user);
// Маппинг в Response.
        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getAge());
    }// Метод getAll: возвращает всех пользователей.

    // Нет параметров, возвращает List DTO.
// Под капотом: findAll — SELECT * FROM users.
// Stream для маппинга списка.
    public List<UserResponse> getAll() {
// userRepository.findAll() — возвращает List<User> из БД.
        List<User> users = userRepository.findAll();
// users.stream() — Java Stream API (с Java 8) для функциональной обработки.
// .map(...) — применяет функцию к каждому элементу (маппинг User -> UserResponse).
// Лямбда user -> new UserResponse(...) — анонимная функция.
// .collect(Collectors.toList()) — собирает в List.
// Под капотом: Эффективно, лениво (не сразу выполняется).
// Альтернатива: for-loop с ArrayList.
// Stream упрощает код.
        return users.stream()
                .map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge()))
                .collect(Collectors.toList());
    }// Метод getById: возвращает одного по ID.

    // Использует Optional для обработки отсутствия.
// Throw если empty.
    public UserResponse getById(Long id) {
// userRepository.findById(id) — SELECT по ID, возвращает Optional<User>.
// Optional — контейнер (Java 8+): может быть значение или empty, избегает null.
// Под капотом: Если нет — empty, без NullPointerException.
        Optional<User> userOptional = userRepository.findById(id);
// if (isEmpty()) throw — стандарт.
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
// userOptional.get() — извлекает значение (безопасно после проверки).
        User user = userOptional.get();
// Маппинг в Response.
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge());
    }// Метод deleteById: удаляет по ID.

    // Проверяет existence, delete.
// Void — ничего не возвращает.
// Throw если не существует.
// Под капотом: deleteById — DELETE SQL.
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}