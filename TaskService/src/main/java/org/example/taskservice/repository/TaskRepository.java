package org.example.taskservice.repository;

import org.example.taskservice.entity.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends CrudRepository<Task,Integer> {
    Iterable<Task>findAllByProjectId(Integer projectId);
}
