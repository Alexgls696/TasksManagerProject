package org.example.taskservice.config;

import org.example.taskservice.client.UsersRestClient;
import org.example.taskservice.client.UsersRestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UsersRestClientConfig {

    @Bean
    public UsersRestClient usersRestClient(@Value("${services.user-service.url}") String url) {
        return new UsersRestClientImpl(RestClient
                .builder()
                .baseUrl(url)
                .build());
    }
}
