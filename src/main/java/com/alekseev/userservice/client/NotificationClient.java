package com.alekseev.userservice.client;

import com.alekseev.userservice.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign-клиент для вызова notification-service.
 *
 * @FeignClient(name = "notification-service") указывает имя целевого сервиса,
 * которое зарегистрировано в Eureka. При вызове методов этого интерфейса
 * Feign автоматически выполняет HTTP-запрос к соответствующему экземпляру
 * notification-service с балансировкой нагрузки (Ribbon).
 *
 * Метод sendNotification будет вызывать POST /api/notifications/send
 * целевого сервиса.
 */
@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/send")
    void sendNotification(@RequestBody NotificationRequest request);
}