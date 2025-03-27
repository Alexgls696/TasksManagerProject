package org.example.taskservice.controller;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.taskservice.controller.payload.NewTaskPayload;
import org.example.taskservice.entity.Category;
import org.example.taskservice.entity.Task;
import org.example.taskservice.service.CategoryService;
import org.example.taskservice.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("task-manager-api/tasks")
public class TasksController {

    private final TaskService taskService;

    @GetMapping
    public Iterable<Task> getTasks() {
        return taskService.findAll();
    }

    @GetMapping("/by-project-id/{id:\\d+}")
    public Iterable<Task> getTasksByProjectId(@PathVariable("id") int id) {
        return taskService.findAllByProjectId(id);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody NewTaskPayload taskPayload, BindingResult bindingResult, UriComponentsBuilder builder) throws BindException {
        if (bindingResult.hasErrors()) {
            if (bindingResult instanceof BindException exception) {
                throw exception;
            } else {
                throw new BindException(bindingResult);
            }
        } else {
            Task created = taskService.save(taskPayload);
            return ResponseEntity
                    .created(builder
                            .replacePath("task-manager-api/tasks/{id}")
                            .build(Map.of("id", created.getId())))
                    .body(created);
        }
    }

}
