package org.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
                .csrf(CsrfConfigurer::disable)
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET,"/task-manager-api/users/**").hasAnyRole("MANAGER","USER")
                        .requestMatchers(HttpMethod.POST,"/task-manager-api/users/**").anonymous()
                        .requestMatchers(HttpMethod.PATCH,"/task-manager-api/users/**").hasAnyRole("MANAGER","USER")
                        .requestMatchers(HttpMethod.DELETE,"/task-manager-api/users/**").hasRole("MANAGER")
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new UrlBasedCorsConfigurationSource();
    }

}
