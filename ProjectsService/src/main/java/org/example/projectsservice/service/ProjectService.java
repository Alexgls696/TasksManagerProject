package org.example.projectsservice.service;


import org.example.projectsservice.controller.payload.NewProjectPayload;
import org.example.projectsservice.controller.payload.UpdateProjectPayload;
import org.example.projectsservice.entity.Project;
import org.springframework.security.core.parameters.P;

import java.util.Optional;

public interface ProjectService {
    Iterable<Project> findAll();
    Iterable<Project>findAllByMemberId(Integer memberId);
    Optional<Project> findById(int id);
    Project save(NewProjectPayload payload);
    Project update(int projectId, UpdateProjectPayload payload);
    void deleteById(int id);
}
