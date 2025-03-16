package org.example.userservice.service;
import org.example.userservice.controller.payload.UpdateUserPayload;
import org.example.userservice.entity.User;
import org.example.userservice.exception.NoSuchUserException;

import java.util.Optional;

public interface UserService {
    Iterable<User> findAll();
    Optional<User> findById(int id);
    Optional<User> findByUsername(String username);
    User update(int id, UpdateUserPayload payload);
    //User update(User user);
    User save(User user);
    void delete(int id);
}
