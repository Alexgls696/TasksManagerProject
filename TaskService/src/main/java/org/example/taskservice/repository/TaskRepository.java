package org.example.taskservice.repository;

import org.example.taskservice.entity.Task;
import org.example.taskservice.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends CrudRepository<Task,Integer> {
    @Query(value = "from User where id = :assigneeId")
    User findUserById(@Param("assigneeId") Integer integer);

    Iterable<Task>findAllByProjectId(Integer projectId);
}
