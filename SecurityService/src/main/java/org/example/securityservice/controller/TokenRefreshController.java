package org.example.securityservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class TokenRefreshController {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshController.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;


    public TokenRefreshController(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @GetMapping("/api/refresh-token") // Убедитесь, что этот путь защищен (.requestMatchers("/api/refresh-token").authenticated())
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.warn("Authentication is not an OAuth2AuthenticationToken: {}", authentication.getClass().getName());
            return ResponseEntity.status(401).body(Map.of("error", "invalid_authentication"));
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
        String principalName = oauthToken.getName();

        // Создаем запрос на авторизацию (даже если просто для обновления)
        // Передаем текущую аутентификацию и HTTP запрос/ответ (если нужны для контекста)
        // Если вызывать ВНЕ HTTP-запроса, контекст нужно будет настроить в manager'е
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal(authentication)
                .build();

        log.info("Attempting to refresh token for client '{}' and principal '{}'", clientRegistrationId, principalName);

        // Пытаемся получить/обновить авторизованный клиент
        // Менеджер сам использует Refresh Token, если Access Token истек
        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            log.error("Could not refresh token for client '{}' and principal '{}'. Authorized client or access token is null.", clientRegistrationId, principalName);
            // Возможно, Refresh Token истек или отозван
            return ResponseEntity.status(401).body(Map.of("error", "refresh_failed", "message", "Could not obtain new access token. Please log in again."));
        }

        OAuth2AccessToken newAccessToken = authorizedClient.getAccessToken();
        log.info("Successfully refreshed token for client '{}' and principal '{}'. New token expires at: {}",
                clientRegistrationId, principalName, newAccessToken.getExpiresAt());

        // Возвращаем новый токен фронтенду
        return ResponseEntity.ok(Map.of("access_token", newAccessToken.getTokenValue(),
                "expires_at", Objects.requireNonNull(newAccessToken.getExpiresAt()).toEpochMilli() // Опционально: время истечения
        ));
    }
}