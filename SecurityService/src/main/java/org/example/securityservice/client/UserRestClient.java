package org.example.securityservice.client;

import org.example.securityservice.controller.payload.CreatedUserPayload;

public interface UserRestClient {
    void createUser(CreatedUserPayload payload);
}
