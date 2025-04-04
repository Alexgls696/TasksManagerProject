package org.example.securityservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Configuration
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    // Внедряем репозиторий регистраций клиентов OAuth2/OIDC
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

   /* @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));

        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class) // Или более специфичный базовый тип
                .allowIfSubType("org.springframework.security")
                .allowIfSubType("java.util")
                .build();
        objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }*/

    @Bean
    public LogoutSuccessHandler oidcLogoutSuccessHandler(
            // Внедряем URI через @Value как параметр
            @Value("${frontend.logout.redirect.uri}") String postLogoutRedirectUri) {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);
        // Устанавливаем URI
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(postLogoutRedirectUri);
        log.info("OIDC Logout Success Handler configured with postLogoutRedirectUri: {}", postLogoutRedirectUri);
        return oidcLogoutSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${frontend.redirect.uri}") String frontendRedirectUri,
            OAuth2AuthorizedClientService authorizedClientService,
            LogoutSuccessHandler oidcLogoutSuccessHandler,
            // Внедряем бины для других конфигураций, если они не определены здесь же
            CorsConfigurationSource corsConfigurationSource,
            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService
    ) throws Exception {

        // Имя атрибута сессии для ID Token
        String idTokenSessionAttributeName = "oidcIdToken";

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Используем внедренный бин
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/profile", "/user-info", "/logout").authenticated()
                        .requestMatchers("/manager").hasRole("MANAGER")
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/keycloak")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService)) // Используем внедренный бин
                        // ----- Добавляем try/catch/finally в successHandler -----
                        .successHandler((request, response, authentication) -> {
                            String tokenValue = null;
                            String finalRedirectUri = frontendRedirectUri + "#error=handler_failed";
                            try {
                                if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                                    String idToken = oidcUser.getIdToken().getTokenValue();
                                    request.getSession().setAttribute(idTokenSessionAttributeName, idToken);
                                    log.debug("ID Token сохранен в сессии для logout.");
                                }

                                if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                                    OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                                            oauthToken.getAuthorizedClientRegistrationId(), authentication.getName());
                                    if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                                        tokenValue = authorizedClient.getAccessToken().getTokenValue();
                                        log.info("Access Token получен успешно.");
                                    } else {
                                        log.warn("AuthorizedClient или Access Token не найдены.");
                                    }
                                } else {
                                    log.warn("Тип Authentication не OAuth2AuthenticationToken: {}", authentication.getClass().getName());
                                }

                                if (tokenValue == null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
                                    tokenValue = oidcUser.getIdToken().getTokenValue();
                                }

                                if (tokenValue != null) {
                                    log.info("Перенаправление на фронтенд с токеном...");
                                    finalRedirectUri = frontendRedirectUri + "#access_token=" + tokenValue;
                                } else {
                                    log.error("Токен не был получен для передачи фронтенду.");
                                    finalRedirectUri = frontendRedirectUri + "#error=token_missing";
                                }
                            } catch (Exception e) {
                                log.error("Исключение в successHandler: {}", e.getMessage(), e);
                            } finally {
                                try {
                                    log.info("Выполняется редирект после логина на: {}", finalRedirectUri);
                                    response.sendRedirect(finalRedirectUri);
                                } catch (IOException ioException) {
                                    log.error("Не удалось выполнить sendRedirect после логина: {}", ioException.getMessage(), ioException);
                                }
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        // ----- ИСПОЛЬЗУЕМ ВНЕДРЕННЫЙ БИН -----
                        .logoutSuccessHandler(oidcLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION")
                        .clearAuthentication(true)
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
