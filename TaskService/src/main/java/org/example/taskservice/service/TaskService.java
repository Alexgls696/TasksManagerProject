package org.example.taskservice.service;


import org.example.taskservice.controller.payload.NewTaskPayload;
import org.example.taskservice.controller.payload.UpdateTaskPayload;
import org.example.taskservice.entity.Category;
import org.example.taskservice.entity.Project;
import org.example.taskservice.entity.Task;
import java.util.Optional;

public interface TaskService {
    Iterable<Task> findAll();
    Optional<Task> findById(int id);
    void update(int id, UpdateTaskPayload payload);
    Task save(NewTaskPayload payload);
    void deleteById(int id);
}
