package org.example.taskservice.service;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.client.ProjectsRestClient;
import org.example.taskservice.client.UsersRestClient;
import org.example.taskservice.controller.payload.NewTaskPayload;
import org.example.taskservice.controller.payload.UpdateTaskPayload;
import org.example.taskservice.entity.*;
import org.example.taskservice.exceptions.NoSuchTaskException;
import org.example.taskservice.repository.CategoryRepository;
import org.example.taskservice.repository.TaskMembersRepository;
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
    private final TaskMembersRepository taskMembersRepository;

    private final ProjectsRestClient projectsRestClient;
    private final UsersRestClient usersRestClient;

    @Override
    public Iterable<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public Iterable<Task> findAllByProjectId(Integer projectId) {
        return taskRepository.findAllByProjectId(projectId)
                .stream()
                .peek(task->{
                    var project = projectsRestClient.findProjectById(task.getProjectId())
                            .orElseThrow(()->new NoSuchElementException("Project with id " + task.getProjectId() + " not found"));

                    var assignee = usersRestClient.findUserById(task.getAssigneeId())
                            .orElseThrow(()->new NoSuchElementException("User with id " + task.getAssigneeId() + " not found"));

                    var creator =  usersRestClient.findUserById(project.getCreatorId())
                            .orElseThrow(()->new NoSuchElementException("User with id " + task.getCreatorId() + " not found"));

                    task.setAssignee(assignee);
                    task.setCreator(creator);
                    task.setProject(project);
                })
                .toList();
    }

    @Override
    public Optional<Task> findById(int id) {
        var task = taskRepository.findById(id)
                .orElseThrow(()->new NoSuchTaskException("Task with id " + id + " not found"));
        var project = projectsRestClient.findProjectById(task.getProjectId())
                .orElseThrow(()->new NoSuchElementException("Project with id " + task.getProjectId() + " not found"));

        var assignee = usersRestClient.findUserById(task.getAssigneeId())
                .orElseThrow(()->new NoSuchElementException("User with id " + task.getAssigneeId() + " not found"));

        var creator =  usersRestClient.findUserById(project.getCreatorId())
                .orElseThrow(()->new NoSuchElementException("User with id " + task.getCreatorId() + " not found"));

        task.setAssignee(assignee);
        task.setCreator(creator);
        task.setProject(project);
        return Optional.of(task);
    }

    @Transactional
    @Override
    public void update(int id, UpdateTaskPayload payload) {
        var task = taskRepository.findById(id)
                .orElseThrow(()->new NoSuchTaskException("Task with id " + id + " not found"));
        var status = taskStatusRepository.findById(payload.statusId())
                .orElseThrow(()->new NoSuchElementException("Status with id " + payload.statusId() + " not found"));
        var assignee = usersRestClient.findUserById(payload.assigneeId())
                .orElseThrow(()->new NoSuchElementException("User with id " + payload.assigneeId() + " not found"));

        task.update(payload);
        task.setStatus(status);
        task.setAssignee(assignee);

        task.setAssigneeId(payload.assigneeId());

        taskMembersRepository.deleteByTaskId(id);
        payload.membersId()
                .forEach(userId->{
                    TaskMembers taskMembers = new TaskMembers(task,userId);
                    taskMembersRepository.save(taskMembers);
                });
        taskRepository.save(task);
    }

    @Transactional
    @Override
    public Task save(NewTaskPayload payload) {
        User creator = usersRestClient.findUserById(payload.assigneeId())
                .orElseThrow(()->new NoSuchElementException("User with id " + payload.assigneeId() + " not found"));

        User assignee =usersRestClient.findUserById(payload.assigneeId())
                .orElseThrow(()->new NoSuchElementException("User with id " + payload.assigneeId() + " not found"));

        Project project = projectsRestClient.findProjectById(payload.projectId())
                .orElseThrow(()->new NoSuchElementException("Project with id " + payload.projectId() + " not found"));

        Category category = categoryRepository.findById(payload.categoryId())
                .orElseThrow(()->new NoSuchElementException("Category with id " + payload.categoryId() + " not found"));

        var status = taskStatusRepository.findById(1)
                .orElseThrow(()->new NoSuchElementException("Status with id " + 1 + " not found"));

        Task task = new Task(payload);
        task.setAssignee(assignee);
        task.setProject(project);
        task.setCategory(category);
        task.setCreator(creator);
        task.setStatus(status);

        task.setAssigneeId(payload.assigneeId());
        task.setProjectId(payload.projectId());
        task.setCreatorId(payload.assigneeId());

        payload.membersId()
                .forEach(id->{
                    TaskMembers taskMembers = new TaskMembers(task,id);
                    taskMembersRepository.save(taskMembers);
                });

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
