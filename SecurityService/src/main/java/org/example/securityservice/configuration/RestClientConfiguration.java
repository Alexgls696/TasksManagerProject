package org.example.securityservice.configuration;

import org.example.securityservice.client.UserRestClient;
import org.example.securityservice.client.UserRestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {
    @Bean
    public UserRestClient userRestClient(@Value("${services.users.uri}") String userServiceUrl) {
        return new UserRestClientImpl(RestClient
                .builder()
                .baseUrl(userServiceUrl)
                .build());
    }
}
