package org.example.taskservice.client;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.Project;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@RequiredArgsConstructor
public class ProjectsRestClientImpl implements ProjectsRestClient {
    private final RestClient restClient;

    private static final ParameterizedTypeReference<Iterable<Project>>PARAMETERIZED_TYPE_REFERENCE
            = new ParameterizedTypeReference<>() {};

    @Override
    public Iterable<Project> findAllProjects() {
        return restClient
                .get()
                .uri("/task-manager-api/projects")
                .retrieve()
                .body(PARAMETERIZED_TYPE_REFERENCE);
    }

    @Override
    public Optional<Project> findProjectById(int id) {
        try{
            return Optional.ofNullable(restClient
                    .get()
                    .uri("/task-manager-api/projects/{id}",id)
                    .retrieve()
                    .body(Project.class));
        }catch (HttpClientErrorException.NotFound exception){
            return Optional.empty();
        }
    }
}
