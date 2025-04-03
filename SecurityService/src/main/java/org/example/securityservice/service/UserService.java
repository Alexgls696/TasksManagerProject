package org.example.securityservice.service;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
public class UserService {

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

    public String registerUser(String username, String password) {
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
            credential.setValue(password);
            credential.setTemporary(false);

            // 3. Создаем представление пользователя
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setCredentials(Collections.singletonList(credential));
            user.setEnabled(true);

            // 4. Создаем пользователя в целевом realm
            Response response = keycloak.realm(targetRealm).users().create(user);

            // 5. Обрабатываем ответ
            if (response.getStatus() == 201) {
                String userId = response.getLocation().getPath()
                        .replaceAll(".*/([^/]+)$", "$1");
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