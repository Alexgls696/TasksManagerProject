package org.example.projectsservice.config;

import org.example.projectsservice.client.UsersRestClient;
import org.example.projectsservice.client.UsersRestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public UsersRestClient usersRestClient(@Value("${services.user-service.url}") String url) {
        return new UsersRestClientImpl(RestClient
                .builder()
                .baseUrl(url)
                .build());
    }
}
