package org.example.securityservice.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/profile", "/user-info", "/logout").authenticated()
                        .requestMatchers("/manager").hasRole("MANAGER")
                        .anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/security/oauth2/authorization/keycloak")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                        )
                        .successHandler((request, response, authentication) -> {
                            if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
                                request.getSession().setAttribute("id_token",
                                        oidcUser.getIdToken().getTokenValue());
                            }
                            response.sendRedirect("/security/profile");
                        })
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/security/login/oauth2/code/keycloak")
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "KEYCLOAK_SESSION")
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
