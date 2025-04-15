package com.example.tasksmanager.client;

import com.example.tasksmanager.controller.payload.NewUserPayload;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

public class SecurityRestClientImpl implements SecurityRestClient {
    private final RestClient restClient;

    public SecurityRestClientImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public String registerUser(NewUserPayload payload) {
        return restClient
                .post()
                .uri("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
    }
}
