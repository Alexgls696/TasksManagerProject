package org.example.projectsservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.example.projectsservice.controller.payload.NewProjectPayload;
import org.example.projectsservice.entity.Project;
import org.example.projectsservice.entity.ProjectStatus;
import org.example.projectsservice.service.ProjectService;
import org.example.projectsservice.service.ProjectStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("task-manager-api/projects")
@RequiredArgsConstructor
public class ProjectsController {
    private final ProjectService projectService;
    private final ProjectStatusService projectStatusService;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public Iterable<Project> getAllProjects() {
        return projectService.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/by-user")
    public Iterable<Project> getAllProjectsByCurrentUser(Authentication authentication) {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        Map<String,Object> attributes = jwtAuthenticationToken.getTokenAttributes();
        String username = (String)attributes.get("preferred_username");
        return projectService.findAllByCurrentUser(username);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody NewProjectPayload payload, BindingResult bindingResult, UriComponentsBuilder uriBuilder) throws BindException {
        if(bindingResult.hasErrors()) {
            if(bindingResult instanceof BindException exception) {
                throw exception;
            }else{
                throw new BindException(bindingResult);
            }
        }else{
            var added = projectService.save(payload);
            return ResponseEntity
                    .created(uriBuilder
                            .replacePath("task-manager-api/projects/{id}")
                            .build(Map.of("id",added.getId())))
                    .body(added);
        }
    }

    @GetMapping("/statuses")
    public Iterable<ProjectStatus> getAllProjectStatuses() {
        return projectStatusService.getAllProjectStatus();
    }

    @GetMapping("/statuses/{id}")
    public ProjectStatus getProjectStatus(@PathVariable Integer id) {
        return projectStatusService.getProjectStatus(id).orElseThrow(()->new NoSuchElementException("No project status with id "+id));
    }
}
