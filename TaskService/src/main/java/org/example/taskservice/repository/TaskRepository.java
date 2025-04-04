package org.example.taskservice.repository;

import org.example.taskservice.entity.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task,Integer> {
    @Query(nativeQuery = true,value = "select  * from getTasksAndCheckThemDeadline(:projectId)")
    List<Task> findAllByProjectId(@Param("projectId") Integer projectId);
}
