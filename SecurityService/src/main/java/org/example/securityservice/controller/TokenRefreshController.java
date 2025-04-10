package org.example.securityservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class TokenRefreshController {

    private static final Logger log = LoggerFactory.getLogger(TokenRefreshController.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    // OAuth2AuthorizedClientService нужен для явной проверки, если менеджер не справился сам
    private final OAuth2AuthorizedClientService authorizedClientService;

    public TokenRefreshController(OAuth2AuthorizedClientManager authorizedClientManager,
                                  OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientManager = authorizedClientManager;
        this.authorizedClientService = authorizedClientService;
    }

    @PostMapping("/api/refresh-token") // Или другой путь по вашему выбору
    public ResponseEntity<?> refreshToken(Authentication authentication) {
        log.info("Получен запрос на обновление токена для пользователя: {}", authentication.getName());

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            log.warn("Пользователь не аутентифицирован через OAuth2, обновление невозможно.");
            // Можно вернуть 401 или 403, в зависимости от логики
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated via OAuth2"));
        }

        String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();

        // Попытка получить/обновить токен с помощью менеджера
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal(authentication) // Передаем текущую аутентификацию
                .build();

        OAuth2AuthorizedClient authorizedClient = this.authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            // Это может случиться, если Refresh Token истек или отозван
            log.warn("Не удалось обновить токен для пользователя {}. Возможно, Refresh Token истек.", authentication.getName());

            // Дополнительная проверка: пытаемся загрузить старый клиент, чтобы понять, был ли там refresh token
            OAuth2AuthorizedClient oldClient = authorizedClientService.loadAuthorizedClient(clientRegistrationId, authentication.getName());
            if (oldClient == null || oldClient.getRefreshToken() == null) {
                log.warn("Refresh Token изначально отсутствовал для пользователя {}.", authentication.getName());
            } else {
                log.warn("Refresh Token присутствовал, но обновление не удалось. Истек или отозван?");
            }

            // Отправляем 401, чтобы фронтенд понял, что нужна полная переаутентификация
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "refresh_failed", "error_description", "Unable to refresh access token. Please log in again."));
        }

        OAuth2AccessToken newAccessToken = authorizedClient.getAccessToken();
        log.info("Токен успешно обновлен для пользователя {}", authentication.getName());

        // Возвращаем НОВЫЙ Access Token фронтенду
        return ResponseEntity.ok(Map.of(
                "access_token", newAccessToken.getTokenValue(),
                "expires_at", Objects.requireNonNull(newAccessToken.getExpiresAt()).toEpochMilli() // Время истечения в мс
                // Можно добавить и другие данные, если нужно
        ));
    }
}