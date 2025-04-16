package org.example.tasknotesservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserRestClientImpl implements UserRestClient {
    private final WebClient webClient;

    @Override
    public Mono<Integer> findUserIdByUsername(String username) {
        return webClient
                .get()
                .uri("/task-manager-api/users/id-by-username/{username}",username)
                .retrieve()
                .bodyToMono(Integer.class);
    }
}
