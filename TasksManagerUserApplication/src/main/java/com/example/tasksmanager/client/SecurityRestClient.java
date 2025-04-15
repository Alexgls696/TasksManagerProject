package com.example.tasksmanager.client;

import com.example.tasksmanager.controller.payload.NewUserPayload;

public interface SecurityRestClient {
     String registerUser(NewUserPayload payload);
}
