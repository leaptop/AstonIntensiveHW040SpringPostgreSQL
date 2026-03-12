package com.alekseev.userservice.repository;

import com.alekseev.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA репозиторий.
 * Автоматически предоставляет: save, findById, findAll, deleteById, existsById и др.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
