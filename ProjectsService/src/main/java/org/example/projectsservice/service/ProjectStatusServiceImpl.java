package org.example.projectsservice.service;

import lombok.RequiredArgsConstructor;
import org.example.projectsservice.entity.ProjectStatus;
import org.example.projectsservice.repository.ProjectStatusRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectStatusServiceImpl implements ProjectStatusService {
    private final ProjectStatusRepository projectStatusRepository;

    @Override
    public Iterable<ProjectStatus> getAllProjectStatus() {
        return projectStatusRepository.findAll();
    }

    @Override
    public Optional<ProjectStatus> getProjectStatus(int id) {
        return projectStatusRepository.findById(id);
    }
}
