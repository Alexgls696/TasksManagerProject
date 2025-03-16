package org.example.projectsservice.service;

import lombok.RequiredArgsConstructor;

import org.example.projectsservice.controller.payload.NewProjectPayload;
import org.example.projectsservice.controller.payload.UpdateProjectPayload;
import org.example.projectsservice.entity.Project;
import org.example.projectsservice.exception.NoSuchProjectException;
import org.example.projectsservice.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;


    @Override
    public Iterable<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public Optional<Project> findById(int id) {
        return projectRepository.findById(id);
    }

    @Override
    public Project save(NewProjectPayload payload) {
        return null;
    }

    @Override
    public Project update(int projectId, UpdateProjectPayload payload) {
        return null;
    }

    @Override
    public void deleteById(int id) {
        if(projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
        }else{
            throw new NoSuchProjectException("Project with id " + id + " does not exist");
        }
    }
}
