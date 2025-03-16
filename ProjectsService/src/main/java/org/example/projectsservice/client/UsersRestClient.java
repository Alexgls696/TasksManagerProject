package org.example.projectsservice.client;


import org.example.projectsservice.entity.User;

import java.util.Optional;

public interface UsersRestClient {
    Iterable<User>findAllUsers();
    Optional<User> findUserById(int id);
}
