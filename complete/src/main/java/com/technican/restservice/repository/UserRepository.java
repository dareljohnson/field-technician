package com.technican.restservice.repository;

import com.technican.restservice.model.User;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGen.getAndIncrement());
        }
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return users.values().stream().filter(u -> u.getUsername().equals(username)).findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void deleteById(Long id) {
        users.remove(id);
    }

    public void deleteAll() {
        users.clear();
    }
}
