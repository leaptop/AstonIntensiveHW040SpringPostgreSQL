package com.alekseev.repository;

import com.alekseev.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //Spring Data JPA предоставляет все стандартные методы для работы с
    // сущностями: save(), findById(), findAll(), deleteById().
    // Можно добавить дополнительные методы, если нужно.
}