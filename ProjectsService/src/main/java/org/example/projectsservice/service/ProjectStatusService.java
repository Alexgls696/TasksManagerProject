package org.example.projectsservice.service;


import org.example.projectsservice.entity.ProjectStatus;

import java.util.Optional;

public interface ProjectStatusService {
    Iterable<ProjectStatus> getAllProjectStatus();
    Optional<ProjectStatus> getProjectStatus(int id);
}
