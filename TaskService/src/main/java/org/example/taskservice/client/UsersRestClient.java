package org.example.taskservice.client;

import org.example.taskservice.entity.User;

import java.util.Optional;

public interface UsersRestClient {
    Iterable<User>findAllUsers();
    Optional<User> findUserById(int id);
    Optional<User>findUserByUsername(String username);
}
