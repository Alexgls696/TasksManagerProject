package org.example.taskservice.client;

import org.example.taskservice.entity.Project;

import java.util.Optional;

public interface ProjectsRestClient {
    Iterable<Project>findAllProjects();
    Optional<Project>findProjectById(int id);
}
