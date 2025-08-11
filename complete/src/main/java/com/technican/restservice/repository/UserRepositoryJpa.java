package com.technican.restservice.repository;

import com.technican.restservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepositoryJpa extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
