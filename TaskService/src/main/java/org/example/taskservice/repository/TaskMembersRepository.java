package org.example.taskservice.repository;

import org.example.taskservice.entity.TaskMembers;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskMembersRepository extends CrudRepository<TaskMembers, Integer> {
    void deleteAllByTaskId(int taskId);

    @Query(nativeQuery = true, value = "select task_id from tasks_members where user_id=:userId")
    List<Integer> findAllTaskIdsByUserId(@Param("userId") int userId);

    @Query(nativeQuery = true, value = "select user_id from tasks_members where task_id=:taskId")
    List<Integer> findMembersIdsByTaskId(@Param("taskId") int taskId);
}
