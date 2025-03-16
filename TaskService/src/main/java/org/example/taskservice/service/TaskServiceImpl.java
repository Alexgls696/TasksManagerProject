package org.example.taskservice.service;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.controller.ProjectsController;
import org.example.taskservice.controller.payload.NewTaskPayload;
import org.example.taskservice.controller.payload.UpdateTaskPayload;
import org.example.taskservice.entity.*;
import org.example.taskservice.exceptions.NoSuchTaskException;
import org.example.taskservice.repository.CategoryRepository;
import org.example.taskservice.repository.ProjectRepository;
import org.example.taskservice.repository.TaskRepository;
import org.example.taskservice.repository.TaskStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;


    @Override
    public Iterable<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public Iterable<Task> findAllByProjectId(Integer projectId) {
        return taskRepository.findAllByProjectId(projectId);
    }

    @Override
    public Optional<Task> findById(int id) {
        return taskRepository.findById(id);
    }

    @Transactional
    @Override
    public void update(int id, UpdateTaskPayload payload) {
        var task = taskRepository.findById(id).orElseThrow(()->new NoSuchTaskException("Task with id " + id + " not found"));
        var status = taskStatusRepository.findById(payload.statusId())
                .orElseThrow(()->new NoSuchElementException("Status with id " + payload.statusId() + " not found"));
        var assignee = taskRepository.findUserById(payload.assigneeId());
        task.update(payload);
        task.setStatus(status);
        task.setAssignee(assignee);
        taskRepository.save(task);
    }

    @Transactional
    @Override
    public Task save(NewTaskPayload payload) {
        User creator = taskRepository.findUserById(1);
        User assignee = taskRepository.findUserById(payload.assigneeId());
        Project project = projectRepository.findById(payload.projectId())
                .orElseThrow(()->new NoSuchElementException("Project with id " + payload.projectId() + " not found"));
        Category category = categoryRepository.findById(payload.categoryId())
                .orElseThrow(()->new NoSuchElementException("Category with id " + payload.categoryId() + " not found"));

        TaskStatus status = taskStatusRepository.findById(1)
                .orElseThrow(()->new NoSuchElementException("Task status with id " + 1 + " not found"));
        Task task = new Task(payload);
        task.setAssignee(assignee);
        task.setProject(project);
        task.setCategory(category);
        task.setCreator(creator);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    @Transactional
    @Override
    public void deleteById(int id) throws NoSuchTaskException {
        if(taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
        }
        else {
            throw new NoSuchTaskException("No such task with id " + id);
        }
    }
}
