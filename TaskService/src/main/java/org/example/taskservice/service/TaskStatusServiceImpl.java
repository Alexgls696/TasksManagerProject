package org.example.taskservice.service;

import lombok.RequiredArgsConstructor;
import org.example.taskservice.entity.TaskStatus;
import org.example.taskservice.repository.TaskStatusRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {
    private final  TaskStatusRepository taskStatusRepository;

    @Override
    public Iterable<TaskStatus> findAll() {
        return taskStatusRepository.findAll();
    }
}
