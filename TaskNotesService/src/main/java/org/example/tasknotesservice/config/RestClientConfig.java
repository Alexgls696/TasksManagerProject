package org.example.tasknotesservice.config;


import org.example.tasknotesservice.client.UserRestClient;
import org.example.tasknotesservice.client.UserRestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    @Bean
    public UserRestClient userRestClient(@Value("${services.users.uri}") String userServiceUri) {
        return new UserRestClientImpl(WebClient
                .builder()
                .filter(jwtTokenFilter().addJwtToken())
                .baseUrl(userServiceUri)
                .build());
    }

    @Bean
    public JwtTokenExchangeFilter jwtTokenFilter() {
        return new JwtTokenExchangeFilter();
    }

}
