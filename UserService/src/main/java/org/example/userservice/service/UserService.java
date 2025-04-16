package org.example.userservice.service;
import org.example.userservice.controller.payload.UpdateUserPayload;
import org.example.userservice.entity.User;

import java.util.Optional;

public interface UserService {
    Iterable<User> findAll();
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    User update(int id, UpdateUserPayload payload);
    Integer findUserIdByUsername(String username);
    User save(User user);
    void delete(int id);
}
