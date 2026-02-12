package com.alekseev.userservice.repository;

import com.alekseev.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA репозиторий.
 * JpaRepository<User, Long> автоматически даёт методы: save(), findById(), findAll(), deleteById() и т.д.
 * @Repository — необязательно (Spring Data сам регистрирует прокси), но оставляем для ясности.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}