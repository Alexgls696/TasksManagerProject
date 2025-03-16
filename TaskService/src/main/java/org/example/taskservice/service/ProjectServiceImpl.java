package org.example.taskservice.service;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.Project;
import org.example.taskservice.repository.ProjectRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    @Override
    public Iterable<Project> findAll() {
        return projectRepository.findAll();
    }
}
