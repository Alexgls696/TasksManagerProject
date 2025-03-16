package org.example.taskservice.repository;

import org.example.taskservice.entity.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task,Integer> {
    List<Task> findAllByProjectId(Integer projectId);
}
