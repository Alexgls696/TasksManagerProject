package com.example.tasksmanager.configuration;

import com.example.tasksmanager.client.SecurityRestClient;
import com.example.tasksmanager.client.SecurityRestClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    public SecurityRestClient securityRestClient() {
        return new SecurityRestClientImpl(RestClient
                .builder()
                .baseUrl("http://localhost:8080/security/")
                .build());
    }
}
