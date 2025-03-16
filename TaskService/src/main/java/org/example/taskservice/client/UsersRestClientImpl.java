package org.example.taskservice.client;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@RequiredArgsConstructor
public class UsersRestClientImpl implements UsersRestClient {
    private final RestClient restClient;

    private static final ParameterizedTypeReference<Iterable<User>>PARAMETERIZED_TYPE_REFERENCE = new ParameterizedTypeReference<>() {};
    @Override
    public Iterable<User> findAllUsers() {
        return restClient
                .get()
                .uri("/task-manager-api/users")
                .retrieve()
                .body(PARAMETERIZED_TYPE_REFERENCE);
    }

    @Override
    public Optional<User> findUserById(int id) {
        try {
            return Optional.ofNullable(restClient
                    .get()
                    .uri("task-manager-api/users/{id}", id)
                    .retrieve()
                    .body(User.class));
        }catch (HttpClientErrorException.NotFound exception){
            return Optional.empty();
        }
    }
}
