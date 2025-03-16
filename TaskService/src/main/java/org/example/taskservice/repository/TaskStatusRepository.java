package org.example.taskservice.repository;

import org.example.taskservice.entity.TaskStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskStatusRepository extends CrudRepository<TaskStatus,Integer> {
}
