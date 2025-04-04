package org.example.securityservice.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Configuration
public class SecurityConfig {

    @Bean
    protected LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            try {
                String idToken = null;

                // Проверяем, что authentication не null и principal является OidcUser
                if (authentication != null && authentication.getPrincipal() instanceof OidcUser) {
                    idToken = ((OidcUser) authentication.getPrincipal()).getIdToken().getTokenValue();
                }

                // Если idToken не получен, попробуем получить его из сессии
                if (idToken == null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        idToken = (String) session.getAttribute("id_token");
                    }
                }

                String logoutUrl = "http://localhost:8090/realms/task-manager-realm/protocol/openid-connect/logout" +
                        (idToken != null ? "?id_token_hint=" + idToken : "") +
                        "&post_logout_redirect_uri=" + URLEncoder.encode("http://localhost:8080/", StandardCharsets.UTF_8);

                response.sendRedirect(logoutUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to redirect to logout page", e);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${frontend.redirect.uri}") String frontendRedirectUri,
            // ----- Внедряем OAuth2AuthorizedClientService как параметр -----
            OAuth2AuthorizedClientService authorizedClientService
    ) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/profile", "/user-info", "/logout").authenticated()
                        .requestMatchers("/manager").hasRole("MANAGER")
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/keycloak")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                        )
                        .successHandler((request, response, authentication) -> {
                            String tokenValue = null;
                            // Проверяем, что аутентификация - это OAuth2AuthenticationToken
                            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                                // ---- Используем внедренный authorizedClientService ----
                                OAuth2AuthorizedClient authorizedClient =
                                        authorizedClientService.loadAuthorizedClient(
                                                oauthToken.getAuthorizedClientRegistrationId(), // ID регистрации клиента (например, "keycloak")
                                                authentication.getName()); // Имя принципала (обычно sub из токена)

                                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                                    tokenValue = authorizedClient.getAccessToken().getTokenValue();
                                    System.out.println("Access Token получен!"); // Логирование для отладки
                                } else {
                                    System.out.println("AuthorizedClient или Access Token не найдены."); // Логирование
                                }
                            } else {
                                System.out.println("Тип Authentication не OAuth2AuthenticationToken: " + authentication.getClass().getName());
                            }

                            // Запасной вариант: ID Token (если Access Token не нужен или не получен)
                            if (tokenValue == null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
                                tokenValue = oidcUser.getIdToken().getTokenValue();
                                request.getSession().setAttribute("id_token", tokenValue);
                                System.out.println("Используется ID Token как запасной вариант."); // Логирование
                            }

                            if (tokenValue != null) {
                                System.out.println("Перенаправление на фронтенд с токеном..."); // Логирование
                                response.sendRedirect(frontendRedirectUri + "#access_token=" + tokenValue);
                            } else {
                                System.err.println("Токен не был получен, перенаправление с ошибкой."); // Логирование ошибки
                                response.sendRedirect(frontendRedirectUri + "#error=token_missing");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        // Убедитесь, что ваш logoutSuccessHandler тоже внедрен или создан правильно
                        // .logoutSuccessHandler(logoutSuccessHandler())
                        .logoutSuccessHandler((request, response, authentication) -> {
                            System.out.println("Выход выполнен, перенаправление на фронтенд..."); // Логирование
                            response.sendRedirect(frontendRedirectUri);
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService userService = new OidcUserService();

        return userRequest -> {
            OidcUser oidcUser = userService.loadUser(userRequest);
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes()).getRequest();
            request.getSession().setAttribute("id_token", oidcUser.getIdToken().getTokenValue());
            List<SimpleGrantedAuthority> authorities = Optional.ofNullable(oidcUser.getClaimAsStringList("groups"))
                    .orElseGet(List::of)
                    .stream()
                    .filter(role -> role.startsWith("ROLE_"))
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(),"preferred_username");
        };
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Разрешаем запросы от вашего API Gateway
        config.addAllowedOrigin("http://localhost:8080"); // или адрес вашего Gateway

        // Разрешаем необходимые HTTP-методы
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Разрешаем необходимые заголовки
        config.addAllowedHeader("Authorization");
        config.addAllowedHeader("Content-Type");
        config.addAllowedHeader("X-Requested-With");
        config.addAllowedHeader("Accept");

        // Разрешаем передачу куки и авторизационных заголовков
        config.setAllowCredentials(true);

        // Применяем настройки ко всем путям
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

}
