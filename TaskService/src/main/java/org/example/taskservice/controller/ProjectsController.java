package org.example.taskservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.Project;
import org.example.taskservice.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task-manager-api/projects")
@RequiredArgsConstructor
public class ProjectsController {
    private final ProjectService projectService;

    @GetMapping
    public Iterable<Project> getAllProjects() {
        return projectService.findAll();
    }
}
