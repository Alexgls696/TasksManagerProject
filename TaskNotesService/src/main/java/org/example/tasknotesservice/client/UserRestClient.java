package org.example.tasknotesservice.client;

import reactor.core.publisher.Mono;

public interface UserRestClient {
    Mono<Integer> findUserIdByUsername(String username);
}
