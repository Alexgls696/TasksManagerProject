package com.example.tasksmanager.controller.payload;

public record NewUserPayload(
        String username,
        String password,
        String email,
        String firstName,
        String lastName
) {

}
