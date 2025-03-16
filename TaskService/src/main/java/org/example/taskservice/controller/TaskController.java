package org.example.taskservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.taskservice.controller.payload.UpdateTaskPayload;
import org.example.taskservice.entity.Task;
import org.example.taskservice.exceptions.NoSuchTaskException;
import org.example.taskservice.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("task-manager-api/tasks/{id:\\d+}")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public Task getTaskById(@PathVariable("id") int id) {
        return taskService.findById(id).
                orElseThrow(() -> new NoSuchTaskException("Task with id " + id + " not found"));
    }

    @PatchMapping
    public ResponseEntity<Void> updateTask(@PathVariable("id") int id, @Valid @RequestBody UpdateTaskPayload payload, BindingResult bindingResult) throws BindException {
        if(bindingResult.hasErrors()) {
            if(bindingResult instanceof BindException exception){
                throw new BindException(exception);
            }else{
                throw new BindException(bindingResult);
            }
        }else{
            taskService.update(id, payload);
           return ResponseEntity
                   .noContent()
                   .build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void>deleteTaskById(@PathVariable("id") int id) {
        taskService.deleteById(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}

