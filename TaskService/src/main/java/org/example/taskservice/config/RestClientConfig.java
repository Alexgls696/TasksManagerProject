package org.example.taskservice.config;

import org.example.taskservice.client.ProjectsRestClient;
import org.example.taskservice.client.ProjectsRestClientImpl;
import org.example.taskservice.client.UsersRestClient;
import org.example.taskservice.client.UsersRestClientImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    @Bean
    public UsersRestClient usersRestClient(@Value("${services.user-service.url}") String url) {
        return new UsersRestClientImpl(RestClient
                .builder()
                .requestInterceptor(new JwtTokenInterceptor())
                .baseUrl(url)
                .build());
    }

    @Bean
    public ProjectsRestClient projectsRestClient(@Value("${services.project-service.url}") String url) {
        return new ProjectsRestClientImpl(RestClient
                .builder()
                .baseUrl(url)
                .build());
    }

    public static class JwtTokenInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            String jwt = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
            request.getHeaders().add("Authorization", "Bearer " + jwt);
            return execution.execute(request, body);
        }
    }
}
