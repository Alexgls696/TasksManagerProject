package org.example.taskservice.service;

import org.example.taskservice.entity.TaskStatus;

public interface TaskStatusService {
    Iterable<TaskStatus> findAll();
}
