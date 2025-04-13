package org.example.projectsservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.projectsservice.controller.payload.UpdateProjectPayload;
import org.example.projectsservice.entity.Project;
import org.example.projectsservice.exception.NoSuchProjectException;
import org.example.projectsservice.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("task-manager-api/projects/{id:\\d+}")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public Project getProject(@PathVariable int id) {
        return projectService.findById(id).orElseThrow(()->new NoSuchProjectException("Project with id " + id + " not found"));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PatchMapping
    public ResponseEntity<Void> updateProject(@PathVariable int id, @Valid @RequestBody UpdateProjectPayload payload, BindingResult bindingResult)  throws BindException {
        if(bindingResult.hasErrors()) {
            if(bindingResult instanceof BindException exception){
                throw exception;
            }else{
                throw new BindException(bindingResult);
            }
        }else{
            projectService.update(id,payload);
            return ResponseEntity
                    .noContent()
                    .build();
        }
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping
    public ResponseEntity<Void> deleteProject(@PathVariable int id) {
        projectService.deleteById(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
