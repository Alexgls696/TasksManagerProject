package org.example.projectsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Так как это сервер ресурсов, отключаем CSRF (токены защищают от этого)
                .csrf(CsrfConfigurer::disable)
                // Настройте CORS, если нужен прямой доступ во время разработки,
                // но шлюз обрабатывает внешние запросы
                // .cors(Customizer.withDefaults())

                // ВАЖНО: Серверы ресурсов должны быть STATELESS (без состояния)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize
                        // Пример: Определите конкретные правила для эндпоинтов вашего сервиса
                        // Помните, что путь здесь относителен к context-path сервиса
                        .requestMatchers(HttpMethod.GET,"/task-manager-api/projects/**").hasAnyRole("USER","MANAGER") // Разрешить публичный доступ
                        .anyRequest().denyAll() // Запретить любые другие несовпадающие запросы
                )
                // Настраиваем как OAuth2 Resource Server, проверяющий JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    // --- Конфигурация конвертера JWT ---
    // Этот бин извлекает права (роли) из JWT.
    // Настройте имена полей (claims) и логику в соответствии с вашей конфигурацией Keycloak.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        // Указываем Spring Security использовать наш обновленный конвертер
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter()); // <--- Используем наш класс
        return jwtConverter;
    }

    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        @SuppressWarnings("unchecked") // Подавляем предупреждение о непроверенном приведении типов
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().getOrDefault("realm_access", Collections.emptyMap());
            final List<String> roles = (List<String>) realmAccess.getOrDefault("roles", Collections.emptyList());
            return roles.stream()

                    .filter(roleName -> roleName != null && roleName.startsWith("ROLE_"))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
    }
}

