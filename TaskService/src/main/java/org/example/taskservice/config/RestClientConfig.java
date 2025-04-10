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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Configuration
public class RestClientConfig {

    @Bean
    public UsersRestClient usersRestClient(@Value("${services.user-service.url}") String url) {
        return new UsersRestClientImpl(RestClient
                .builder()
                .baseUrl(url)
                .build());
    }

    @Bean
    public ProjectsRestClient projectsRestClient(@Value("${services.project-service.url}") String url) {
        return new ProjectsRestClientImpl(RestClient
                .builder()
                .baseUrl(url)
                .requestInterceptor(new JwtTokenInterceptor())
                .build());
    }

    @Bean
    public JwtTokenInterceptor jwtTokenInterceptor() {
        return new JwtTokenInterceptor();
    }

    public static class JwtTokenInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                            ClientHttpRequestExecution execution) throws IOException {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
                String token = jwt.getTokenValue();  // <-- Получаем реальный токен
                request.getHeaders().add("Authorization", "Bearer " + token);
            } else {
                System.out.println("No authentication found, not adding Authorization header.");
            }

            return execution.execute(request, body);
        }

    }
}
