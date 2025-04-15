package org.example.securityservice.service;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.example.securityservice.client.UserRestClient;
import org.example.securityservice.controller.payload.CreatedUserPayload;
import org.example.securityservice.controller.payload.NewUserPayload;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRestClient userRestClient;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}") // Это должен быть целевой realm, куда добавляем пользователя
    private String targetRealm;

    @Value("${keycloak.admin-realm}") // Добавьте это свойство для realm админа (обычно "master")
    private String adminRealm;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Value("${keycloak.admin-client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;


    public String registerUser(NewUserPayload payload) {
        try {
            // 1. Создаем подключение к Keycloak
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(authServerUrl)
                    .realm(adminRealm) // Realm где находится админ (обычно "master")
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            // 2. Настраиваем учетные данные пользователя
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(payload.password());
            credential.setTemporary(false);

            // 3. Создаем представление пользователя
            UserRepresentation user = new UserRepresentation();
            user.setUsername(payload.username());
            user.setCredentials(Collections.singletonList(credential));
            user.setEnabled(true);
            user.setEmail(payload.email());
            user.setFirstName(payload.firstName());
            user.setLastName(payload.lastName());

            // 4. Создаем пользователя в целевом realm
            Response response = keycloak.realm(targetRealm).users().create(user);


            if (response.getStatus() == 201) {
                 String userId = response.getLocation().getPath()
                        .replaceAll(".*/([^/]+)$", "$1");
                 try {
                     userRestClient.createUser(new CreatedUserPayload(payload, userId));
                 }catch (HttpClientErrorException exception){
                     throw exception;
                 }
                return "User created successfully with ID: " + userId;
            } else {
                String error = "Failed to create user. Status: " + response.getStatus();
                if (response.hasEntity()) {
                    error += ", Error: " + response.readEntity(String.class);
                }
                throw new RuntimeException(error);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error creating user in Keycloak: " + e.getMessage(), e);
        }
    }
}