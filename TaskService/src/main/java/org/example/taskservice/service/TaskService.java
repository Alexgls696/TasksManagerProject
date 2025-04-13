package org.example.taskservice.service;


import org.example.taskservice.controller.payload.NewTaskPayload;
import org.example.taskservice.controller.payload.UpdateTaskPayload;
import org.example.taskservice.entity.Category;
import org.example.taskservice.entity.Project;
import org.example.taskservice.entity.Task;
import java.util.Optional;

public interface TaskService {
    Iterable<Task> findAll();
    Iterable<Task>findAllByProjectId(Integer projectId);
    Iterable<Task>findAllByProjectIdAndMemberId(Integer projectId, Integer memberId);
    Optional<Task> findById(int id);
    void update(int id, UpdateTaskPayload payload);
    Task save(NewTaskPayload payload, String username);
    void deleteById(int id);
}
