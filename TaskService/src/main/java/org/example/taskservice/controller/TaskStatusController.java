package org.example.taskservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.TaskStatus;
import org.example.taskservice.service.TaskStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task-manager-api/tasks/statuses")
@RequiredArgsConstructor
public class TaskStatusController {
    private final TaskStatusService taskStatusService;

    @GetMapping
    public Iterable<TaskStatus> getTaskStatuses() {
        return taskStatusService.findAll();
    }

}
