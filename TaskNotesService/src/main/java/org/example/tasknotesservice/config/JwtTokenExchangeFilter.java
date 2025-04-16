package org.example.tasknotesservice.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

public class JwtTokenExchangeFilter {

    public  ExchangeFilterFunction addJwtToken() {
        return (clientRequest, next) ->
                ReactiveSecurityContextHolder.getContext()
                        .map(securityContext -> {
                            Authentication authentication = securityContext.getAuthentication();
                            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                                System.out.println(HttpHeaders.AUTHORIZATION);
                                return ClientRequest.from(clientRequest)
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                                        .build();
                            }
                            return clientRequest;
                        })
                        .defaultIfEmpty(clientRequest)
                        .flatMap(next::exchange);
    }
}