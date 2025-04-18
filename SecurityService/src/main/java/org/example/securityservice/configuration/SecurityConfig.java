package org.example.securityservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
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
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .refreshToken() // только обновление по refresh_token
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${frontend.redirect.uri}") String frontendRedirectUri,
            OAuth2AuthorizedClientService authorizedClientService,
            LogoutSuccessHandler oidcLogoutSuccessHandler,
            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService
    ) throws Exception {

        // Имя атрибута сессии для ID Token
        String idTokenSessionAttributeName = "oidcIdToken";

        return http
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/profile", "/user-info", "/logout").authenticated()
                        .requestMatchers("/manager").hasRole("MANAGER")
                        .requestMatchers("/api/refresh-token").authenticated()
                        .anyRequest().permitAll()
                ).oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/keycloak")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService)) // Используем внедренный бин
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

                                SecurityContext context = SecurityContextHolder.createEmptyContext();
                                context.setAuthentication(authentication);
                                SecurityContextHolder.setContext(context);

                                HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
                                repo.saveContext(context, request, response);

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
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/security/api/")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                            } else {
                                response.sendRedirect("/security/oauth2/authorization/keycloak");
                            }
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION")
                        .clearAuthentication(true)
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new UrlBasedCorsConfigurationSource();
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

}
