package org.example.securityservice.controller.payload;

public record NewUserPayload(
        String firstName,
        String lastName,
        String username,
        String password,
        String email
) {

}
