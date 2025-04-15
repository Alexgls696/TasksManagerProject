package org.example.securityservice.client;

import lombok.RequiredArgsConstructor;
import org.example.securityservice.controller.payload.CreatedUserPayload;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class UserRestClientImpl implements UserRestClient {
    private final RestClient restClient;

    @Override
    public void createUser(CreatedUserPayload payload) {
        try {
            restClient.
                    post()
                    .uri("task-manager-api/users")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
